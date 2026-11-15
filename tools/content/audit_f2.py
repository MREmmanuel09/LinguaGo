"""Audita el contenido A2 de F2 en ES→EN y ES→FR."""

from __future__ import annotations

import hashlib
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
}
MAX_TOKENS = 12


def strip_ids(value):
    if isinstance(value, dict):
        return {
            key: strip_ids(item)
            for key, item in value.items()
            if key != "id"
        }
    if isinstance(value, list):
        return [strip_ids(item) for item in value]
    return value


def signature(lesson: dict) -> str:
    payload = json.dumps(
        strip_ids(lesson["exercises"]),
        ensure_ascii=False,
        sort_keys=True,
    )
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def sentence_tokens(text: str) -> int:
    return max(
        len(part.split())
        for part in re.split(r"[.!?]+", text)
        if part.strip()
    )


def main() -> None:
    report = {}
    for course_id, path in COURSES.items():
        course = json.loads(path.read_text(encoding="utf-8"))
        a2_level = next(
            level for level in course["levels"] if level["id"] == f"{course_id}-lv-a2"
        )
        units = [
            unit
            for unit in a2_level["units"]
            if re.fullmatch(re.escape(course_id) + r"-a2-u\d{2}", unit["id"])
        ]
        lessons = [lesson for unit in units for lesson in unit["lessons"]]
        exercises = [exercise for lesson in lessons for exercise in lesson["exercises"]]
        signatures = [signature(lesson) for lesson in lessons]
        duplicate_groups = {
            digest: count
            for digest, count in Counter(signatures).items()
            if count > 1
        }
        titles = [lesson["title"] for lesson in lessons]
        max_lesson_tokens = 0
        max_exercise_id = None
        for exercise in exercises:
            texts: list[str] = []
            for key in ("audioText", "transcription", "targetPhrase", "sourcePhrase"):
                value = exercise.get(key)
                if value:
                    texts.append(value)
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            for text in texts:
                longest = sentence_tokens(text)
                if longest > max_lesson_tokens:
                    max_lesson_tokens = longest
                    max_exercise_id = exercise["id"]
        report[course_id] = {
            "units": len(units),
            "lessons": len(lessons),
            "exercises": len(exercises),
            "a2_total": sum(
                len(unit["lessons"])
                for level in course["levels"]
                if level["id"] == f"{course_id}-lv-a2"
                for unit in level["units"]
            ),
            "duplicate_lesson_groups": len(duplicate_groups),
            "duplicate_lessons": sum(duplicate_groups.values()),
            "unique_titles": len(set(titles)),
            "missing_feedback": sum(
                1 for exercise in exercises if exercise.get("feedbackOnError") is None
            ),
            "exercise_types": dict(Counter(exercise["type"] for exercise in exercises)),
            "grammar_points": dict(Counter(len(lesson.get("grammarPoints", [])) for lesson in lessons)),
            "max_tokens": max_lesson_tokens,
            "max_tokens_exercise": max_exercise_id,
            "max_tokens_ok": max_lesson_tokens <= MAX_TOKENS,
            "unique_exercise_ids": len(exercises)
            == len({exercise["id"] for exercise in exercises}),
        }
        print(course_id, report[course_id])

    out = ROOT / "build" / "reports" / "f2-audit.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Informe JSON: {out}")


if __name__ == "__main__":
    main()
