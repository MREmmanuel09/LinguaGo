"""Añade la micro-clase que falta en las 42 lecciones históricas B1/B2 de es-en.

El resto de cursos y niveles ya la tienen. Se construye a partir de datos que
la propia lección declara (focos gramaticales, nota cultural, glosario y
ejercicios) para no inventar gramática nueva.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"


def build_micro(lesson: dict) -> dict:
    grammar = lesson.get("grammarPoints") or []
    labels = [point.get("label", "") for point in grammar if point.get("label")]
    descriptions = [
        f"{point.get('label')}: {point.get('description')}"
        for point in grammar
        if point.get("label") and point.get("description")
    ]
    goals = [f"Usar vocabulario de «{lesson['title']}»."]
    if labels:
        goals.append(f"Aplicar {' y '.join(labels[:2])}.")
    else:
        goals.append("Aplicar las estructuras de la lección.")
    goals.append(f"Resolver una tarea comunicativa sobre «{lesson['title']}».")

    examples: list[dict[str, str]] = []
    for note in lesson.get("culturalNotes") or []:
        idiom = note.get("idiom")
        translation = note.get("translation")
        if idiom and translation:
            examples.append({"source": translation, "target": idiom})
            break
    for exercise in lesson.get("exercises") or []:
        if len(examples) >= 2:
            break
        if exercise.get("type") == "Translate" and exercise.get("sourcePhrase"):
            examples.append(
                {
                    "source": exercise["sourcePhrase"],
                    "target": " ".join(exercise.get("correctTokens") or []),
                }
            )
        elif exercise.get("type") == "OrderWords" and exercise.get("sourcePhrase"):
            examples.append(
                {
                    "source": exercise["sourcePhrase"],
                    "target": " ".join(exercise.get("answerTokens") or []),
                }
            )
    for entry in lesson.get("glossary") or []:
        if len(examples) >= 2:
            break
        translations = entry.get("translations") or []
        if translations:
            examples.append({"source": translations[0], "target": entry.get("term", "")})

    seen = set()
    unique = []
    for example in examples:
        key = (example["source"], example["target"])
        if key in seen:
            continue
        seen.add(key)
        unique.append(example)

    return {
        "title": lesson["title"],
        "goals": goals,
        "sections": [
            {
                "heading": "Lengua en contexto",
                "body": " ".join(descriptions),
                "examples": unique[:2],
            }
        ],
    }


def main() -> None:
    text, root = load(COURSE)
    edits = []
    added = 0
    for node in walk(root):
        if node.kind != "object":
            continue
        lesson_id = node.members.get("id")
        title = node.members.get("title")
        if lesson_id is None or title is None:
            continue
        if not isinstance(lesson_id.value, str) or not lesson_id.value.startswith("es-en-lv-"):
            continue
        level = node.members.get("cefrLevel")
        if level is None or level.value not in {"B1", "B2"}:
            continue
        if "microLesson" in node.members or "grammarPoints" not in node.members:
            continue
        micro = build_micro(
            {
                "title": title.value,
                "grammarPoints": [member_value(item) for item in node.members.get("grammarPoints", empty_array()).items],
                "culturalNotes": [member_value(item) for item in node.members.get("culturalNotes", empty_array()).items],
                "glossary": [member_value(item) for item in node.members.get("glossary", empty_array()).items],
                "exercises": [member_value(item) for item in node.members.get("exercises", empty_array()).items],
            }
        )
        grammar_node = node.members["grammarPoints"]
        key_position = text.rfind('"grammarPoints"', 0, grammar_node.start)
        if key_position < 0:
            continue
        payload = '"microLesson": ' + json.dumps(micro, ensure_ascii=False, indent=2)
        payload = payload.replace("\n", "\n" + " " * 14) + ",\n" + " " * 14
        edits.append((key_position, key_position, payload))
        added += 1
    if edits:
        save(COURSE, apply_edits(text, edits))
    print("micro-clases añadidas:", added)


def empty_array():
    from json_spans import Node

    return Node("array", 0, 0, items=[])


def member_value(node):
    """Convierte un nodo JSON de vuelta a Python (solo los tipos usados)."""
    if node.kind == "object":
        return {key: member_value(value) for key, value in node.members.items()}
    if node.kind == "array":
        return [member_value(item) for item in node.items]
    return node.value


if __name__ == "__main__":
    main()
