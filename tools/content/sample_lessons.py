"""Extrae una muestra legible de lecciones para revisión cualitativa."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "ES→EN": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "ES→PT": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "ES→FR": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ES→JA": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
OUT = ROOT / "build/reports/sample-review.txt"


def compact(exercise: dict) -> str:
    kind = exercise["type"]
    parts = [kind]
    for key in (
        "question",
        "sourcePhrase",
        "audioText",
        "transcription",
        "targetPhrase",
        "prompt",
        "passage",
    ):
        value = exercise.get(key)
        if value:
            parts.append(f"{key}={value}")
    for key in ("options", "particles", "romajiOptions", "readingOptions"):
        value = exercise.get(key)
        if value:
            parts.append(f"{key}={value}")
    if exercise.get("correctIndex") is not None:
        parts.append(f"correctIndex={exercise['correctIndex']}")
    if exercise.get("pairs"):
        parts.append(f"pairs={[(p.get('left'), p.get('right')) for p in exercise['pairs']]}")
    if exercise.get("blanks"):
        parts.append(f"blanks={[(b.get('id'), b.get('answer'), b.get('options')) for b in exercise['blanks']]}")
    if exercise.get("correctTokens"):
        parts.append(f"correctTokens={exercise['correctTokens']}")
    if exercise.get("answerTokens"):
        parts.append(f"answerTokens={exercise['answerTokens']}")
    if exercise.get("modelAnswer"):
        parts.append(f"modelAnswer={exercise['modelAnswer']}")
    if exercise.get("feedbackOnError"):
        parts.append(f"feedback={exercise['feedbackOnError'].get('explanation')}")
    return " | ".join(parts)


lines = []
for course, path in COURSES.items():
    data = json.loads(path.read_text(encoding="utf-8"))
    lines.append(f"\n{'=' * 90}\nCURSO {course}\n{'=' * 90}")
    for level in data["levels"]:
        lessons = [lesson for unit in level["units"] for lesson in unit["lessons"]]
        picks = [lessons[0], lessons[len(lessons) // 2], lessons[-1]]
        for lesson in picks:
            lines.append(f"\n--- {lesson['id']} · {lesson['title']} · {lesson['cefrLevel']} ---")
            lines.append(f"canDo: {lesson.get('canDo')}")
            micro = lesson.get("microLesson") or {}
            for section in micro.get("sections") or []:
                lines.append(f"micro[{section.get('heading')}]: {section.get('body')}")
                for example in section.get("examples") or []:
                    lines.append(f"  ejemplo: {example.get('source')} → {example.get('target')}")
            lines.append(f"glosario: {[(g['term'], g['translations']) for g in (lesson.get('glossary') or [])[:8]]}")
            for exercise in lesson["exercises"]:
                lines.append(f"  {compact(exercise)}")

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text("\n".join(lines), encoding="utf-8")
print("ok", OUT, "lineas", len(lines))
