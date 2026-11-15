"""Reasigna gramática y notas culturales de los repasos desde su lección origen.

El primer pase emparejó solo por título y podía tomar una lección B1 homónima
de una B2. Ahora se exige el mismo CEFR y la misma distribución de tipos de
ejercicio. Las lecciones A2 sin gramática/notas omiten esas claves (el schema
las permite opcionales).
"""

from __future__ import annotations

import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}


def to_python(node):
    if node.kind == "object":
        return {key: to_python(value) for key, value in node.members.items()}
    if node.kind == "array":
        return [to_python(item) for item in node.items]
    return node.value


def type_signature(lesson: dict) -> tuple:
    return tuple(sorted(Counter(exercise["type"] for exercise in lesson["exercises"]).items()))


def replace_array(text: str, node, values: list) -> tuple[int, int, str]:
    line_start = text.rfind("\n", 0, node.start) + 1
    indent = " " * (node.start - line_start)
    serialized = json.dumps(values, ensure_ascii=False, indent=2)
    serialized = serialized.replace("\n", "\n" + indent)
    return node.start, node.end, serialized


def remove_line(text: str, key_position: int, value_end: int) -> tuple[int, int, str]:
    line_start = text.rfind("\n", 0, key_position) + 1
    line_end = text.find("\n", value_end)
    if line_end < 0:
        line_end = len(text) - 1
    return line_start, line_end + 1, ""


def main() -> None:
    for course, path in COURSES.items():
        text, root = load(path)
        reviews = []
        sources = []
        for node in walk(root):
            if node.kind != "object" or "id" not in node.members or "exercises" not in node.members:
                continue
            lesson = to_python(node)
            if not isinstance(lesson.get("id"), str):
                continue
            if "-p0-" in lesson["id"] and str(lesson.get("title", "")).startswith("Repaso:"):
                reviews.append((node, lesson))
            elif "-p0-" not in lesson["id"]:
                sources.append(lesson)

        by_key = defaultdict(list)
        for lesson in sources:
            title = lesson.get("title")
            level = lesson.get("cefrLevel")
            by_key[(title, level, type_signature(lesson))].append(lesson)

        edits = []
        for node, review in reviews:
            source_title = review["title"][len("Repaso:") :].strip()
            key = (source_title, review.get("cefrLevel"), type_signature(review))
            candidates = by_key.get(key, [])
            if not candidates:
                candidates = [
                    lesson
                    for lesson in sources
                    if lesson.get("title") == source_title
                    and lesson.get("cefrLevel") == review.get("cefrLevel")
                ]
            source = candidates[0] if candidates else None
            for field in ("grammarPoints", "culturalNotes"):
                value_node = node.members.get(field)
                if value_node is None or value_node.kind != "array":
                    continue
                current = [to_python(item) for item in value_node.items]
                replacement = (source or {}).get(field) or []
                if not replacement:
                    key_position = text.rfind(f'"{field}"', 0, value_node.start)
                    if key_position >= 0:
                        edits.append(remove_line(text, key_position, value_node.end))
                elif replacement != current:
                    edits.append(replace_array(text, value_node, replacement))
        if edits:
            save(path, apply_edits(text, edits))
        print(f"{course}: {len(edits)} metadatos corregidos")


if __name__ == "__main__":
    main()
