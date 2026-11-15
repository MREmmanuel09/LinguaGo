"""Traduce el currículo A2 compartido a portugués y japonés.

Genera `a2_translations_pt.json` y `a2_translations_ja.json` a partir de las
frases españolas de `a2_specs.py`. El japonés se segmenta con Janome y se
espacia para que los ejercicios de orden de palabras tengan tokens estables.
"""

from __future__ import annotations

import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

from janome.tokenizer import Tokenizer

from a2_factory import sentences_for
from a2_specs import BLOCKS, CAPSTONE_FR

ROOT = Path(__file__).resolve().parents[2]
CACHE_DIR = ROOT / "build" / "translations"
OUT_PT = ROOT / "tools" / "content" / "a2_translations_pt.json"
OUT_JA = ROOT / "tools" / "content" / "a2_translations_ja.json"
BATCH_SIZE = 20
TOKENIZER = Tokenizer()
PUNCTUATION = "。、！？!?.,;:"


def spanish_sentences(spec: dict) -> list[str]:
    if "sentences" in spec:
        return [sentence[0] for sentence in spec["sentences"]]
    return [pair[0] for pair in sentences_for(spec["pattern"], spec["items"], "en")]


def all_sentences() -> list[str]:
    specs = [spec for _, block in BLOCKS for spec in block] + CAPSTONE_FR
    seen: set[str] = set()
    ordered: list[str] = []
    for spec in specs:
        for sentence in spanish_sentences(spec):
            if sentence not in seen:
                seen.add(sentence)
                ordered.append(sentence)
    return ordered


def google_translate(text: str, target: str) -> str:
    query = urllib.parse.quote(text)
    url = (
        "https://translate.googleapis.com/translate_a/single"
        f"?client=gtx&sl=es&tl={target}&dt=t&q={query}"
    )
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(request, timeout=60) as response:
        data = json.loads(response.read().decode("utf-8"))
    return "".join(part[0] for part in data[0]).strip()


def mymemory_translate(text: str, target: str) -> str:
    query = urllib.parse.quote(text)
    url = (
        "https://api.mymemory.translated.net/get"
        f"?q={query}&langpair=es|{target}"
    )
    with urllib.request.urlopen(url, timeout=60) as response:
        data = json.loads(response.read().decode("utf-8"))
    return data["responseData"]["translatedText"].strip()


def translate_one(text: str, target: str) -> str:
    try:
        return google_translate(text, target)
    except Exception:
        time.sleep(1)
        return mymemory_translate(text, target)


def translate_batch(texts: list[str], target: str) -> list[str]:
    joined = "\n".join(texts)
    try:
        translated = google_translate(joined, target)
        lines = [line.strip() for line in translated.splitlines() if line.strip()]
        if len(lines) == len(texts):
            return lines
    except Exception:
        pass
    return [translate_one(text, target) for text in texts]


def merge_japanese(text: str) -> str:
    merged: list[str] = []
    previous_pos = ""
    for token in TOKENIZER.tokenize(text):
        surface = token.surface
        pos = token.part_of_speech
        if pos.startswith("記号"):
            continue
        merge = False
        if merged:
            if pos.startswith("助動詞"):
                merge = True
            elif pos.startswith("動詞,非自立"):
                merge = True
            elif pos.startswith("動詞,接尾"):
                merge = True
            elif pos.startswith("助詞,接続助詞"):
                merge = True
            elif pos.startswith("助詞,並立助詞"):
                merge = True
            elif pos.startswith("動詞,自立") and previous_pos.startswith("名詞,サ変"):
                merge = True
            elif pos.startswith("名詞") and previous_pos.startswith("名詞"):
                merge = True
            elif pos.startswith("名詞") and previous_pos.startswith("接頭詞"):
                merge = True
            elif surface.isdigit() and re.fullmatch(r"[A-Za-z]", merged[-1]):
                merge = True
        if merge:
            merged[-1] += surface
        else:
            merged.append(surface)
        previous_pos = pos
    return " ".join(merged)


def normalize(text: str) -> str:
    text = re.sub(r"\s+", " ", text).strip()
    return text.strip(PUNCTUATION)


def token_count(text: str, target: str) -> int:
    if target == "ja":
        return len(text.split())
    return len(re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text))


def load_cache(path: Path) -> dict[str, str]:
    if path.exists():
        return json.loads(path.read_text(encoding="utf-8"))
    return {}


def translate_target(sentences: list[str], target: str, out_path: Path) -> None:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    cache_path = CACHE_DIR / f"a2_{target}.json"
    cache = load_cache(cache_path)
    pending = [sentence for sentence in sentences if sentence not in cache]
    print(f"{target}: {len(sentences)} frases, {len(pending)} pendientes")
    for start in range(0, len(pending), BATCH_SIZE):
        batch = pending[start : start + BATCH_SIZE]
        translated = translate_batch(batch, target)
        for source, result in zip(batch, translated, strict=True):
            if target == "ja":
                result = merge_japanese(result)
            cache[source] = normalize(result)
        cache_path.write_text(
            json.dumps(cache, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
        print(f"  {min(start + BATCH_SIZE, len(pending))}/{len(pending)}")
        time.sleep(0.2)
    missing = [sentence for sentence in sentences if sentence not in cache]
    if missing:
        raise SystemExit(f"{target}: faltan {len(missing)} traducciones")
    out_path.write_text(
        json.dumps({sentence: cache[sentence] for sentence in sentences}, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    too_long = [
        (sentence, cache[sentence], token_count(cache[sentence], target))
        for sentence in sentences
        if token_count(cache[sentence], target) > 12
    ]
    short = [
        (sentence, cache[sentence])
        for sentence in sentences
        if token_count(cache[sentence], target) < 2
    ]
    print(f"{target}: guardado en {out_path}")
    print(f"{target}: {len(too_long)} frases > 12 tokens, {len(short)} frases < 2 tokens")
    for item in too_long[:10]:
        print("  LARGA:", item)
    for item in short[:10]:
        print("  CORTA:", item)


def main() -> None:
    sentences = all_sentences()
    print(f"Frases únicas: {len(sentences)}")
    translate_target(sentences, "pt", OUT_PT)
    translate_target(sentences, "ja", OUT_JA)


if __name__ == "__main__":
    main()
