#!/usr/bin/env python3
"""Prepara las voces Piper empaquetadas por LinguaGo (ADR-057).

El APK no puede incluir espeak-ng (GPL). Este script usa espeak-ng 1.52.0
solo en tiempo de build para precalcular los fonemas IPA de cada frase del
contenido y un lexicón de respaldo; el APK resultante solo lleva los datos
fonéticos y los modelos Piper, nunca la biblioteca ni los diccionarios de
espeak-ng.

Requisitos:
    python -m pip install -r tools/tts/requirements.txt

Uso:
    python tools/tts/prepare_voices.py --download
    python tools/tts/prepare_voices.py --check
    python tools/tts/prepare_voices.py --voices en --download

El modo `--check` regenera en un directorio temporal y compara byte a byte
con los archivos publicados; es el guardián de que el contenido y las voces
no se hayan desincronizado.
"""

from __future__ import annotations

import argparse
import array
import ctypes
import ctypes.util
import hashlib
import json
import os
import re
import shutil
import struct
import sys
import tempfile
import unicodedata
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Sequence

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MODELS = ROOT / "models"
DEFAULT_CONTENT = ROOT / "content"
LOCK_NAME = "voices.lock.json"
MAGIC = b"LGP2"
VERSION = 2
CLUSTER_ALIAS_BASE = 0xE001
REQUIRED_METADATA = {
    "model_type": "vits",
    "comment": "piper",
    "n_speakers": "1",
    "sample_rate": "22050",
}


@dataclass(frozen=True)
class VoiceSpec:
    id: str
    language: str
    espeak_voice: str
    language_name: str
    model_url: str
    config_url: str
    model_card_url: str
    raw_sha256: str
    dataset_license: str
    dataset_url: str
    attribution: str
    license_text: str

    @property
    def path(self) -> str:
        return f"tts/{self.id}"


@dataclass
class VoiceConfig:
    spec: VoiceSpec
    id_map: dict[str, int]
    clusters: list[tuple[str, ...]]
    cluster_aliases: dict[tuple[str, ...], int]
    token2id: dict[int, int]


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


def normalize_text(text: str) -> str:
    text = unicodedata.normalize("NFC", text)
    text = text.replace("\u200b", "").replace("\ufeff", "")
    return re.sub(r"\s+", " ", text).strip()


def normalize_key(text: str) -> str:
    return unicodedata.normalize("NFC", normalize_text(text).lower())


def is_word_char(char: str) -> bool:
    category = unicodedata.category(char)
    return category[0] in "LN" or category in ("Mn", "Mc", "Me")


def strip_word(token: str) -> str:
    start = 0
    end = len(token)
    while start < end and not is_word_char(token[start]):
        start += 1
    while end > start and not is_word_char(token[end - 1]):
        end -= 1
    return token[start:end]


def word_tokens(text: str) -> set[str]:
    result = set()
    for token in re.split(r"[\s,;:.!?]+", normalize_text(text)):
        stripped = strip_word(token)
        if stripped:
            result.add(normalize_key(stripped))
    return result


def load_lock(models_dir: Path) -> list[VoiceSpec]:
    lock_path = models_dir / LOCK_NAME
    data = json.loads(lock_path.read_text(encoding="utf-8"))
    voices = []
    for raw in data["voices"]:
        spec = VoiceSpec(
            id=raw["id"],
            language=raw["language"],
            espeak_voice=raw["espeakVoice"],
            language_name=raw["languageName"],
            model_url=raw["modelUrl"],
            config_url=raw["configUrl"],
            model_card_url=raw["modelCardUrl"],
            raw_sha256=raw["rawSha256"],
            dataset_license=raw["datasetLicense"],
            dataset_url=raw["datasetUrl"],
            attribution=raw["attribution"],
            license_text=raw["licenseText"],
        )
        voices.append(spec)
    return voices


# ---------------------------------------------------------------------------
# espeak-ng (solo build time)
# ---------------------------------------------------------------------------


class Espeak:
    """Envoltorio mínimo de la API C de espeak-ng 1.52.0."""

    def __init__(
        self,
        library: str | None = None,
        data_dir: str | None = None,
        allow_version: bool = False,
    ) -> None:
        if library is None or data_dir is None:
            try:
                import espeakng_loader

                library = library or espeakng_loader.get_library_path()
                data_dir = data_dir or espeakng_loader.get_data_path()
            except ImportError:
                library = library or ctypes.util.find_library("espeak-ng")
                data_dir = data_dir or self._find_data_dir()
        if not library:
            raise RuntimeError(
                "No se encontró libespeak-ng. Instala tools/tts/requirements.txt "
                "o pasa --espeak-lib/--espeak-data."
            )
        if not data_dir:
            raise RuntimeError("No se encontró espeak-ng-data; pasa --espeak-data.")

        self.library_path = library
        self.data_dir = str(Path(data_dir).resolve())
        self.dll = ctypes.WinDLL(library) if os.name == "nt" else ctypes.CDLL(library)
        self._bind()

        self.dll.espeak_Info.restype = ctypes.c_char_p
        self.dll.espeak_Info.argtypes = [ctypes.POINTER(ctypes.c_char_p)]
        version_ptr = ctypes.c_char_p()
        self.version = (self.dll.espeak_Info(ctypes.byref(version_ptr)) or b"").decode(
            "utf-8", "replace"
        )
        if not allow_version and not self.version.startswith("1.52"):
            raise RuntimeError(
                f"espeak-ng {self.version} no es la versión fijada 1.52.x; "
                "usa --allow-espeak-version solo si has validado los fonemas."
            )

        base = Path(self.data_dir)
        if base.name == "espeak-ng-data":
            base = base.parent
        initialized = self.dll.espeak_Initialize(2, 0, str(base).encode(), 0)
        if initialized <= 0:
            initialized = self.dll.espeak_Initialize(
                2, 0, str(Path(self.data_dir)).encode(), 0
            )
        if initialized <= 0:
            raise RuntimeError(f"espeak_Initialize falló para {self.data_dir}")

    @staticmethod
    def _find_data_dir() -> str | None:
        env = os.environ.get("ESPEAK_DATA_PATH")
        if env:
            return env
        candidates = [
            "/usr/share/espeak-ng-data",
            "/usr/lib/x86_64-linux-gnu/espeak-ng-data",
            "/usr/local/share/espeak-ng-data",
            r"C:\Program Files\eSpeak NG\espeak-ng-data",
        ]
        for candidate in candidates:
            if Path(candidate).is_dir():
                return candidate
        return None

    def _bind(self) -> None:
        self.dll.espeak_Initialize.restype = ctypes.c_int
        self.dll.espeak_Initialize.argtypes = [
            ctypes.c_int,
            ctypes.c_int,
            ctypes.c_char_p,
            ctypes.c_int,
        ]
        self.dll.espeak_SetVoiceByName.restype = ctypes.c_int
        self.dll.espeak_SetVoiceByName.argtypes = [ctypes.c_char_p]
        self.dll.espeak_TextToPhonemes.restype = ctypes.c_char_p
        self.dll.espeak_TextToPhonemes.argtypes = [
            ctypes.POINTER(ctypes.c_void_p),
            ctypes.c_int,
            ctypes.c_int,
        ]

    def clauses(self, text: str, voice: str) -> list[tuple[str, str]]:
        """Devuelve (fonemas IPA, texto consumido) por cláusula.

        `espeak_TextToPhonemes` deja el primer carácter de la cláusula
        siguiente como lookahead; se reasigna a la cláusula siguiente para
        reconstruir el terminador real.
        """
        if self.dll.espeak_SetVoiceByName(voice.encode()) != 0:
            raise RuntimeError(f"espeak-ng no reconoce la voz {voice!r}")

        data = normalize_text(text).encode("utf-8")
        buffer = ctypes.create_string_buffer(data)
        base = ctypes.addressof(buffer)
        pointer = ctypes.c_void_p(base)
        pointer_ref = ctypes.pointer(pointer)
        result: list[tuple[str, str]] = []
        previous = 0
        pending = ""
        while True:
            value = self.dll.espeak_TextToPhonemes(pointer_ref, 1, 0x02)
            if not value:
                break
            if not pointer.value:
                last = True
                consumed = data[previous:].decode("utf-8", "replace")
                clause_text = pending + consumed
                pending = ""
            else:
                offset = pointer.value - base
                last = offset >= len(data)
                consumed = data[previous:offset].decode("utf-8", "replace")
                if last:
                    clause_text = pending + consumed
                    pending = ""
                else:
                    lookahead = consumed[-1:] if consumed else ""
                    clause_text = (
                        pending + consumed[: -len(lookahead)]
                        if lookahead
                        else pending + consumed
                    )
                    pending = lookahead
                    previous = offset
            result.append((value.decode("utf-8"), clause_text))
            if last:
                break
        return result

    def phonemes(self, text: str, voice: str) -> str:
        return "".join(phon for phon, _ in self.clauses(text, voice))


def terminator(clause_text: str) -> str:
    for char in reversed(clause_text.strip()):
        if char in ".,!?;:":
            return char
        if char.isalnum():
            break
    return ""


def merge_clusters(phonemes: list[str], clusters: Sequence[tuple[str, ...]]) -> list[str]:
    if not clusters:
        return phonemes
    by_length: dict[int, set[tuple[str, ...]]] = {}
    for cluster in clusters:
        by_length.setdefault(len(cluster), set()).add(cluster)
    max_length = max(by_length)
    result: list[str] = []
    index = 0
    while index < len(phonemes):
        match = None
        for length in range(min(max_length, len(phonemes) - index), 1, -1):
            candidate = tuple(phonemes[index : index + length])
            if candidate in by_length.get(length, set()):
                match = candidate
                break
        if match is not None:
            result.append("".join(match))
            index += len(match)
        else:
            result.append(phonemes[index])
            index += 1
    return result


def sentences_for(
    text: str,
    voice: str,
    espeak: Espeak,
    clusters: Sequence[tuple[str, ...]],
) -> list[list[str]]:
    """Reproduce el troceado de Piper 1.5 (frases y terminadores)."""
    sentences: list[list[str]] = []
    current: list[str] = []
    for phonemes, clause_text in espeak.clauses(text, voice):
        end = terminator(clause_text)
        phonemes = re.sub(r"\([^)]*\)", "", phonemes)
        phonemes += end
        if end in (",", ":", ";"):
            phonemes += " "
        current.extend(list(unicodedata.normalize("NFD", phonemes)))
        if end in (".", "?", "!"):
            sentences.append(merge_clusters(current, clusters))
            current = []
    if current:
        sentences.append(merge_clusters(current, clusters))
    return sentences


# ---------------------------------------------------------------------------
# ONNX metadata (protobuf mínimo, sin dependencia de onnx/onnxruntime)
# ---------------------------------------------------------------------------


def read_varint(data: bytes, position: int) -> tuple[int, int]:
    result = 0
    shift = 0
    while True:
        byte = data[position]
        position += 1
        result |= (byte & 0x7F) << shift
        if not byte & 0x80:
            return result, position
        shift += 7


def write_varint(value: int) -> bytes:
    result = bytearray()
    while True:
        byte = value & 0x7F
        value >>= 7
        if value:
            result.append(byte | 0x80)
        else:
            result.append(byte)
            return bytes(result)


def field_slices(data: bytes) -> Iterable[tuple[int, int, int]]:
    position = 0
    while position < len(data):
        start = position
        key, position = read_varint(data, position)
        field = key >> 3
        wire = key & 7
        if wire == 0:
            _, position = read_varint(data, position)
        elif wire == 1:
            position += 8
        elif wire == 2:
            length, position = read_varint(data, position)
            position += length
        elif wire == 5:
            position += 4
        else:
            raise ValueError(f"wire type {wire} no soportado")
        yield field, start, position


def onnx_metadata(data: bytes) -> dict[str, str]:
    result: dict[str, str] = {}
    for field, wire, value in _iter_field_values(data):
        if field != 14 or wire != 2:
            continue
        key = None
        text = None
        for inner_field, inner_wire, inner_value in _iter_field_values(value):
            if inner_field == 1 and inner_wire == 2:
                key = bytes(inner_value).decode("utf-8")
            elif inner_field == 2 and inner_wire == 2:
                text = bytes(inner_value).decode("utf-8")
        if key is not None:
            result[key] = text or ""
    return result


def _iter_field_values(data: bytes):
    position = 0
    while position < len(data):
        key, position = read_varint(data, position)
        field = key >> 3
        wire = key & 7
        if wire == 0:
            value, position = read_varint(data, position)
        elif wire == 1:
            value = data[position : position + 8]
            position += 8
        elif wire == 2:
            length, position = read_varint(data, position)
            value = data[position : position + length]
            position += length
        elif wire == 5:
            value = data[position : position + 4]
            position += 4
        else:
            raise ValueError(f"wire type {wire} no soportado")
        yield field, wire, value


def encode_metadata(key: str, value: str) -> bytes:
    key_bytes = key.encode("utf-8")
    value_bytes = value.encode("utf-8")
    payload = (
        b"\x0a"
        + write_varint(len(key_bytes))
        + key_bytes
        + b"\x12"
        + write_varint(len(value_bytes))
        + value_bytes
    )
    return b"\x72" + write_varint(len(payload)) + payload


def patch_onnx(path: Path, spec: VoiceSpec) -> None:
    data = path.read_bytes()
    metadata = onnx_metadata(data)
    desired = dict(REQUIRED_METADATA)
    desired["language"] = spec.language_name
    desired["voice"] = spec.espeak_voice
    if all(metadata.get(key) == value for key, value in desired.items()):
        return
    merged = dict(metadata)
    merged.update(desired)
    output = bytearray()
    for field, start, end in field_slices(data):
        if field == 14:
            continue
        output.extend(data[start:end])
    for key, value in merged.items():
        output.extend(encode_metadata(key, value))
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_bytes(output)
    os.replace(temporary, path)


# ---------------------------------------------------------------------------
# Voz: tokens, fonemas y lexicón
# ---------------------------------------------------------------------------


def load_voice_config(spec: VoiceSpec, voice_dir: Path) -> VoiceConfig:
    config = json.loads((voice_dir / f"{spec.id}.onnx.json").read_text(encoding="utf-8"))
    phoneme_map = config["phoneme_id_map"]
    id_map: dict[str, int] = {}
    for symbol, ids in phoneme_map.items():
        if not ids:
            raise ValueError(f"{spec.id}: símbolo {symbol!r} sin id")
        id_map[symbol] = int(ids[0])
    clusters = [tuple(item) for item in config.get("vowel_clusters", [])]
    aliases: dict[tuple[str, ...], int] = {}
    token2id: dict[int, int] = {}
    for symbol, identifier in sorted(id_map.items(), key=lambda item: (item[1], item[0])):
        if len(symbol) == 1:
            codepoint = ord(symbol)
            if codepoint in token2id and token2id[codepoint] != identifier:
                raise ValueError(f"{spec.id}: codepoint duplicado {symbol!r}")
            token2id[codepoint] = identifier
    for index, cluster in enumerate(clusters):
        joined = "".join(cluster)
        if joined not in id_map:
            raise ValueError(f"{spec.id}: cluster {joined!r} sin id")
        alias = CLUSTER_ALIAS_BASE + index
        if alias in token2id:
            raise ValueError(f"{spec.id}: alias PUA {alias:#x} ya existe")
        aliases[cluster] = alias
        token2id[alias] = id_map[joined]
    return VoiceConfig(
        spec=spec,
        id_map=id_map,
        clusters=clusters,
        cluster_aliases=aliases,
        token2id=token2id,
    )


def write_tokens(voice_dir: Path, config: VoiceConfig) -> None:
    rows = sorted(
        ((identifier, symbol) for symbol, identifier in config.id_map.items() if len(symbol) == 1),
        key=lambda item: (item[0], item[1]),
    )
    lines = []
    seen = set()
    for identifier, symbol in rows:
        if identifier in seen:
            raise ValueError(f"{config.spec.id}: id duplicado {identifier}")
        seen.add(identifier)
        lines.append(str(identifier) if symbol == " " else f"{symbol} {identifier}")
    for cluster, alias in sorted(config.cluster_aliases.items(), key=lambda item: item[1]):
        identifier = config.id_map["".join(cluster)]
        lines.append(f"{chr(alias)} {identifier}")
    (voice_dir / "tokens.txt").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def phoneme_codepoints(
    phonemes: Sequence[str],
    aliases: dict[tuple[str, ...], int],
) -> list[int]:
    result: list[int] = []
    for phoneme in phonemes:
        if len(phoneme) == 1:
            result.append(ord(phoneme))
        elif tuple(phoneme) in aliases:
            result.append(aliases[tuple(phoneme)])
        else:
            raise ValueError(f"fonema no representable: {phoneme!r}")
    return result


def piper_ids(phonemes: Sequence[str], id_map: dict[str, int]) -> list[int]:
    result = [id_map["^"], id_map["_"]]
    for phoneme in phonemes:
        if phoneme not in id_map:
            raise KeyError(phoneme)
        result.append(id_map[phoneme])
        result.append(id_map["_"])
    result.append(id_map["$"])
    return result


def sherpa_ids(codepoints: Sequence[int], token2id: dict[int, int]) -> list[int]:
    result = [token2id[ord("^")], token2id[ord("_")]]
    for codepoint in codepoints:
        result.append(token2id[codepoint])
        result.append(token2id[ord("_")])
    result.append(token2id[ord("$")])
    return result


def write_phonemes(path: Path, entries: Sequence[tuple[str, Sequence[Sequence[int]]]]) -> None:
    sentence_index: dict[tuple[int, ...], int] = {}
    sentences: list[tuple[int, ...]] = []
    encoded_entries: list[tuple[bytes, list[int]]] = []
    for key, entry_sentences in entries:
        key_bytes = key.encode("utf-8")
        if len(key_bytes) > 0xFFFF:
            raise ValueError("clave demasiado larga")
        refs = []
        for sentence in entry_sentences:
            if len(sentence) > 0xFFFF:
                raise ValueError("frase demasiado larga")
            immutable = tuple(int(value) for value in sentence)
            index = sentence_index.get(immutable)
            if index is None:
                index = len(sentences)
                if index > 0xFFFF:
                    raise ValueError("demasiadas frases para referencias u16")
                sentence_index[immutable] = index
                sentences.append(immutable)
            refs.append(index)
        if len(refs) > 0xFFFF:
            raise ValueError("demasiadas frases por entrada")
        encoded_entries.append((key_bytes, refs))
    if len(encoded_entries) > 0xFFFFFFFF:
        raise ValueError("demasiadas entradas")

    output = bytearray(MAGIC)
    output.extend(struct.pack("<HHI", VERSION, 0, len(sentences)))
    for sentence in sentences:
        output.extend(struct.pack("<H", len(sentence)))
        values = array.array("H", sentence)
        if sys.byteorder != "little":
            values.byteswap()
        output.extend(values.tobytes())
    output.extend(struct.pack("<I", len(encoded_entries)))
    for key_bytes, refs in encoded_entries:
        output.extend(struct.pack("<H", len(key_bytes)))
        output.extend(key_bytes)
        output.extend(struct.pack("<H", len(refs)))
        values = array.array("H", refs)
        if sys.byteorder != "little":
            values.byteswap()
        output.extend(values.tobytes())
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_bytes(output)
    os.replace(temporary, path)


def read_phonemes(path: Path) -> list[tuple[str, list[list[int]]]]:
    data = path.read_bytes()
    position = 0
    if data[:4] != MAGIC:
        raise ValueError(f"{path}: magic inválido")
    position = 4
    version, _, sentence_count = struct.unpack_from("<HHI", data, position)
    position += 8
    if version != VERSION:
        raise ValueError(f"{path}: versión {version} no soportada")
    sentences: list[list[int]] = []
    for _ in range(sentence_count):
        (count,) = struct.unpack_from("<H", data, position)
        position += 2
        values = array.array("H")
        values.frombytes(data[position : position + count * 2])
        if sys.byteorder != "little":
            values.byteswap()
        position += count * 2
        sentences.append(list(values))
    (entry_count,) = struct.unpack_from("<I", data, position)
    position += 4
    result = []
    for _ in range(entry_count):
        (key_length,) = struct.unpack_from("<H", data, position)
        position += 2
        key = data[position : position + key_length].decode("utf-8")
        position += key_length
        (ref_count,) = struct.unpack_from("<H", data, position)
        position += 2
        refs = array.array("H")
        refs.frombytes(data[position : position + ref_count * 2])
        if sys.byteorder != "little":
            refs.byteswap()
        position += ref_count * 2
        result.append((key, [sentences[index] for index in refs]))
    if position != len(data):
        raise ValueError(f"{path}: bytes sobrantes")
    return result


def extract_strings(content_dir: Path, spec: VoiceSpec) -> list[str]:
    course = (
        content_dir
        / f"content-es-{spec.language}"
        / "src"
        / "main"
        / "assets"
        / "courses"
        / f"es-{spec.language}"
        / "course.json"
    )
    data = json.loads(course.read_text(encoding="utf-8"))
    strings: set[str] = set()

    def walk(node) -> None:
        if isinstance(node, dict):
            for key, value in node.items():
                if key in ("audioText", "modelAnswer", "idiom") and isinstance(value, str):
                    strings.add(normalize_text(value))
                elif key == "right" and isinstance(value, str):
                    strings.add(normalize_text(value))
                else:
                    walk(value)
        elif isinstance(node, list):
            for value in node:
                walk(value)

    walk(data)
    strings.discard("")
    return sorted(strings)


def write_lexicon(
    path: Path,
    words: Iterable[str],
    espeak: Espeak,
    config: VoiceConfig,
) -> tuple[int, list[str]]:
    lines = []
    skipped = []
    for word in sorted(set(words)):
        phonemes = espeak.phonemes(word, config.spec.espeak_voice)
        phonemes = re.sub(r"\([^)]*\)", "", phonemes)
        phonemes = unicodedata.normalize("NFD", phonemes)
        try:
            codepoints = phoneme_codepoints(phonemes, config.cluster_aliases)
        except ValueError:
            skipped.append(word)
            continue
        if not codepoints:
            skipped.append(word)
            continue
        payload = " ".join(chr(codepoint) for codepoint in codepoints)
        lines.append(f"{word} || {payload}")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    return len(lines), skipped


def write_attribution(voice_dir: Path, spec: VoiceSpec) -> None:
    text = (
        f"LinguaGo — voz {spec.id}\n"
        f"\n"
        f"Idioma: {spec.language_name}\n"
        f"Fuente del modelo: {spec.model_url}\n"
        f"Model card: {spec.model_card_url}\n"
        f"Licencia del dataset: {spec.dataset_license}\n"
        f"Dataset: {spec.dataset_url}\n"
        f"Uso comercial: https://github.com/OHF-Voice/piper1-gpl/blob/main/docs/COMMERCIAL_USE.md\n"
        f"\n"
        f"Atribución:\n{spec.attribution}\n"
        f"\n"
        f"El texto completo de la licencia está en {spec.license_text} dentro de\n"
        f"los assets de la app. Este modelo se empaqueta en el APK y no se\n"
        f"descarga en tiempo de ejecución.\n"
    )
    (voice_dir / "ATTRIBUTION.txt").write_text(text, encoding="utf-8", newline="\n")


def file_entry(models_dir: Path, path: Path) -> dict[str, str]:
    return {
        "path": path.relative_to(models_dir).as_posix(),
        "sha256": sha256_file(path),
    }


def update_manifest(
    models_dir: Path,
    voices: Sequence[tuple[VoiceSpec, Path, str]],
) -> None:
    manifest_path = models_dir / "manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    tts_entries = []
    for spec, voice_dir, onnx_sha in voices:
        data_files = [
            voice_dir / "phonemes.bin",
            voice_dir / "lexicon.txt",
            voice_dir / "tokens.txt",
        ]
        tts_entries.append(
            {
                "id": spec.id,
                "engine": "piper",
                "language": spec.language,
                "path": spec.path,
                "sha256": onnx_sha,
                "files": [file_entry(models_dir, path) for path in data_files],
            }
        )
    manifest["tts"] = tts_entries
    manifest["notes"] = (
        "Los sha256 los llena tools/tts/prepare_voices.py (tts) y "
        "`fetchVoiceModels` (stt). En tts, sha256 es el .onnx ya parcheado con "
        "los metadatos de sherpa-onnx y files[] cubre phonemes.bin, lexicon.txt "
        "y tokens.txt. Campos vacíos = modelo no verificado; el engine no lo carga."
    )
    manifest_path.write_text(
        json.dumps(manifest, indent=4, ensure_ascii=True) + "\n",
        encoding="utf-8",
        newline="\n",
    )


# ---------------------------------------------------------------------------
# Descarga y preparación
# ---------------------------------------------------------------------------


def download(url: str, target: Path, expected_sha256: str | None = None) -> None:
    if target.is_file() and target.stat().st_size > 0:
        if expected_sha256 is None or sha256_file(target) == expected_sha256:
            return
    target.parent.mkdir(parents=True, exist_ok=True)
    temporary = target.with_suffix(target.suffix + ".part")
    request = urllib.request.Request(url, headers={"User-Agent": "LinguaGo-build/1.0"})
    print(f"Descargando {url}")
    with urllib.request.urlopen(request, timeout=300) as response:
        with temporary.open("wb") as output:
            shutil.copyfileobj(response, output)
    actual = sha256_file(temporary)
    if expected_sha256 is not None and actual != expected_sha256:
        temporary.unlink(missing_ok=True)
        raise RuntimeError(
            f"SHA-256 inesperado para {target.name}: {actual} != {expected_sha256}"
        )
    os.replace(temporary, target)


def ensure_voice(
    spec: VoiceSpec,
    models_dir: Path,
    downloads_dir: Path,
    allow_download: bool,
    check: bool,
) -> Path:
    voice_dir = models_dir / spec.path
    if check:
        for required in (
            voice_dir / f"{spec.id}.onnx",
            voice_dir / f"{spec.id}.onnx.json",
            voice_dir / "MODEL_CARD",
        ):
            if not required.is_file():
                raise RuntimeError(f"Falta {required}; ejecuta --download")
        return voice_dir

    voice_dir.mkdir(parents=True, exist_ok=True)
    raw_dir = downloads_dir / "tts" / spec.id
    raw_model = raw_dir / f"{spec.id}.onnx"
    raw_config = raw_dir / f"{spec.id}.onnx.json"
    raw_card = raw_dir / "MODEL_CARD"

    if allow_download:
        download(spec.model_url, raw_model, spec.raw_sha256)
        download(spec.config_url, raw_config)
        download(spec.model_card_url, raw_card)
    elif not raw_model.is_file():
        raw_model = voice_dir / f"{spec.id}.onnx"
        raw_config = voice_dir / f"{spec.id}.onnx.json"
        raw_card = voice_dir / "MODEL_CARD"

    for required in (raw_model, raw_config, raw_card):
        if not required.is_file():
            raise RuntimeError(
                f"Falta {required}; ejecuta con --download o revisa models/tts/{spec.id}."
            )

    target_model = voice_dir / f"{spec.id}.onnx"
    if allow_download or not target_model.is_file():
        shutil.copyfile(raw_model, target_model)
    target_config = voice_dir / f"{spec.id}.onnx.json"
    if allow_download or not target_config.is_file():
        shutil.copyfile(raw_config, target_config)
    target_card = voice_dir / "MODEL_CARD"
    if allow_download or not target_card.is_file():
        shutil.copyfile(raw_card, target_card)
    patch_onnx(target_model, spec)
    return voice_dir


def prepare_voice(
    spec: VoiceSpec,
    models_dir: Path,
    content_dir: Path,
    espeak: Espeak,
    allow_download: bool,
    check: bool,
    output_dir: Path | None = None,
) -> dict[str, object]:
    voice_dir = ensure_voice(
        spec,
        models_dir,
        models_dir.parent / ".voice-downloads",
        allow_download,
        check,
    )
    config = load_voice_config(spec, voice_dir)
    strings = extract_strings(content_dir, spec)
    entries: list[tuple[str, list[list[int]]]] = []
    total_sentences = 0
    total_codepoints = 0
    mismatches = 0
    for key in strings:
        sentences = sentences_for(key, spec.espeak_voice, espeak, config.clusters)
        if not sentences:
            continue
        encoded = [phoneme_codepoints(sentence, config.cluster_aliases) for sentence in sentences]
        for sentence, codepoints in zip(sentences, encoded):
            expected = piper_ids(sentence, config.id_map)
            actual = sherpa_ids(codepoints, config.token2id)
            if expected != actual:
                mismatches += 1
                raise RuntimeError(f"{spec.id}: IDs distintos para {key!r}")
            total_sentences += 1
            total_codepoints += len(codepoints)
        entries.append((key, encoded))
    if mismatches:
        raise RuntimeError(f"{spec.id}: {mismatches} frases con IDs distintos")

    words = set()
    for key in strings:
        words.update(word_tokens(key))

    target_dir = output_dir or voice_dir
    target_dir.mkdir(parents=True, exist_ok=True)
    write_tokens(target_dir, config)
    lexicon_count, skipped = write_lexicon(
        target_dir / "lexicon.txt", words, espeak, config
    )
    write_phonemes(target_dir / "phonemes.bin", entries)
    write_attribution(target_dir, spec)

    onnx_sha = sha256_file(voice_dir / f"{spec.id}.onnx")
    return {
        "spec": spec,
        "voice_dir": voice_dir,
        "entries": len(entries),
        "sentences": total_sentences,
        "codepoints": total_codepoints,
        "words": lexicon_count,
        "skipped": skipped,
        "onnx_sha": onnx_sha,
        "target_dir": target_dir,
    }


def check_voice(
    spec: VoiceSpec,
    models_dir: Path,
    content_dir: Path,
    espeak: Espeak,
) -> None:
    voice_dir = models_dir / spec.path
    if not voice_dir.is_dir():
        raise RuntimeError(f"Falta {voice_dir}; ejecuta --download")
    with tempfile.TemporaryDirectory(prefix=f"linguago-{spec.id}-") as temporary:
        output = Path(temporary)
        result = prepare_voice(
            spec,
            models_dir,
            content_dir,
            espeak,
            allow_download=False,
            check=True,
            output_dir=output,
        )
        for name in ("tokens.txt", "phonemes.bin", "lexicon.txt"):
            expected = voice_dir / name
            actual = output / name
            if not expected.is_file():
                raise RuntimeError(f"Falta {expected}")
            if expected.read_bytes() != actual.read_bytes():
                raise RuntimeError(
                    f"{expected} está desactualizado; ejecuta prepare_voices.py --download"
                )
        manifest = json.loads((models_dir / "manifest.json").read_text(encoding="utf-8"))
        entry = next((item for item in manifest.get("tts", []) if item["id"] == spec.id), None)
        if entry is None:
            raise RuntimeError(f"{spec.id} no está en models/manifest.json")
        if entry.get("sha256", "").lower() != result["onnx_sha"]:
            raise RuntimeError(f"{spec.id}: sha256 del .onnx no coincide con el manifest")
        for file in entry.get("files", []):
            path = models_dir / file["path"]
            if not path.is_file() or sha256_file(path) != file["sha256"]:
                raise RuntimeError(f"{spec.id}: hash inválido para {path}")
    print(f"OK {spec.id}")


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--models-dir", type=Path, default=DEFAULT_MODELS)
    parser.add_argument("--content-dir", type=Path, default=DEFAULT_CONTENT)
    parser.add_argument("--voices", default="all", help="all o lista separada por comas (en,fr,pt)")
    parser.add_argument("--download", action="store_true", help="descarga las voces fijadas en voices.lock.json")
    parser.add_argument("--check", action="store_true", help="regenera y compara con lo publicado")
    parser.add_argument("--espeak-lib", help="ruta a libespeak-ng (.dll/.so)")
    parser.add_argument("--espeak-data", help="ruta a espeak-ng-data")
    parser.add_argument("--allow-espeak-version", action="store_true")
    parser.add_argument("--keep-going", action="store_true", help="no abortar al primer fallo")
    return parser.parse_args()


def selected_specs(specs: Sequence[VoiceSpec], value: str) -> list[VoiceSpec]:
    if value == "all":
        return list(specs)
    wanted = {item.strip() for item in value.split(",") if item.strip()}
    result = [spec for spec in specs if spec.language in wanted or spec.id in wanted]
    if not result:
        raise SystemExit(f"No hay voces para {value!r}")
    return result


def main() -> int:
    args = parse_args()
    specs = selected_specs(load_lock(args.models_dir), args.voices)
    espeak = Espeak(
        library=args.espeak_lib,
        data_dir=args.espeak_data,
        allow_version=args.allow_espeak_version,
    )
    print(f"espeak-ng {espeak.version} ({espeak.library_path})")

    if args.check:
        failures = 0
        for spec in specs:
            try:
                check_voice(spec, args.models_dir, args.content_dir, espeak)
            except Exception as error:  # noqa: BLE001 - CLI con informe claro
                failures += 1
                print(f"FALLO {spec.id}: {error}", file=sys.stderr)
                if not args.keep_going:
                    return 1
        return 1 if failures else 0

    prepared = []
    for spec in specs:
        print(f"Preparando {spec.id}…")
        result = prepare_voice(
            spec,
            args.models_dir,
            args.content_dir,
            espeak,
            allow_download=args.download,
            check=False,
        )
        prepared.append((spec, result["voice_dir"], result["onnx_sha"]))
        print(
            f"  {result['entries']} textos, {result['sentences']} frases, "
            f"{result['codepoints']} codepoints, {result['words']} palabras en lexicón"
        )
        if result["skipped"]:
            print(f"  aviso: {len(result['skipped'])} palabras sin fonemas: {result['skipped'][:8]}")
    update_manifest(args.models_dir, prepared)
    print("OK: models/manifest.json actualizado")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
