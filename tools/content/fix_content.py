"""Correcciones de contenido demostrables (sin reescribir el curso entero).

- Recorta espacios al inicio/final de cualquier string (I5: contenido limpio).
- Deduplica las opciones de cada hueco de ClozeDragDrop usando respuestas y
  opciones hermanas como candidatos.
- Ajusta `minTokens` cuando la propia respuesta modelo no lo alcanza (el
  alumno que reproduzca el modelo no debe recibir TOO_SHORT).
"""

from __future__ import annotations

import json
import re
import sys
import unicodedata
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import Node, apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
CJK = re.compile(r"[\u3040-\u30FF\u3400-\u4DBF\u4E00-\u9FFF]")
TOKEN = re.compile(r"[^\W_]+(?:'[^\W_]+)*", re.UNICODE)


def token_count(text: str) -> int:
    if CJK.search(text):
        return sum(1 for char in text if char.isalnum())
    plain = unicodedata.normalize("NFD", text.lower())
    plain = "".join(char for char in plain if not unicodedata.combining(char))
    return len(TOKEN.findall(plain))


def trim_strings(text: str, root: Node) -> list[tuple[int, int, str]]:
    edits = []
    for node in walk(root):
        if node.kind != "string" or not isinstance(node.value, str):
            continue
        raw = text[node.start + 1 : node.end - 1]
        trimmed = raw.strip(" \t\r\n")
        trimmed = re.sub(r"\bde el\b", "del", trimmed)
        trimmed = re.sub(r"\bDe el\b", "Del", trimmed)
        if trimmed != raw:
            edits.append((node.start + 1, node.end - 1, trimmed))
    return edits


def fix_cloze_options(text: str, root: Node) -> list[tuple[int, int, str]]:
    edits = []
    for node in walk(root):
        if node.kind != "object" or node.members.get("type", Node("null", 0, 0)).value != "ClozeDragDrop":
            continue
        blanks = node.members.get("blanks")
        if blanks is None or blanks.kind != "array":
            continue
        answers = []
        for blank in blanks.items:
            answer = blank.members.get("answer")
            answers.append(answer.value if answer is not None else "")
        candidates = []
        for blank in blanks.items:
            options = blank.members.get("options")
            if options is not None:
                candidates.extend(item.value for item in options.items)
        candidates = [value for value in dict.fromkeys(candidates) if value]
        for blank in blanks.items:
            options = blank.members.get("options")
            if options is None or options.kind != "array":
                continue
            seen = set()
            for item in options.items:
                value = item.value
                if value not in seen:
                    seen.add(value)
                    continue
                replacement = next(
                    (candidate for candidate in candidates if candidate not in seen),
                    None,
                )
                if replacement is not None:
                    edits.append((item.start, item.end, json.dumps(replacement, ensure_ascii=False)))
                    seen.add(replacement)
    return edits


def fix_min_tokens(text: str, root: Node) -> list[tuple[int, int, str]]:
    edits = []
    for node in walk(root):
        if node.kind != "object" or node.members.get("type", Node("null", 0, 0)).value != "FreeSpeakingPrompt":
            continue
        model = node.members.get("modelAnswer")
        minimum = node.members.get("minTokens")
        if model is None or minimum is None or minimum.kind != "number":
            continue
        actual = token_count(model.value)
        if actual < minimum.value:
            edits.append((minimum.start, minimum.end, str(actual)))
    return edits


def main() -> None:
    dry_run = "--dry-run" in sys.argv
    for course, path in COURSES.items():
        text, root = load(path)
        edits = []
        edits.extend(trim_strings(text, root))
        edits.extend(fix_cloze_options(text, root))
        edits.extend(fix_min_tokens(text, root))
        if edits and not dry_run:
            save(path, apply_edits(text, edits))
        print(f"{course}: {len(edits)} correcciones" + (" (dry-run)" if dry_run else ""))


if __name__ == "__main__":
    main()
