"""Audita el contenido A1 de F1 en los cuatro cursos."""

from __future__ import annotations

import hashlib
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "es-pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "es-ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}


def strip_ids(value):
    if isinstance(value, dict):
        return {key: strip_ids(item) for key, item in value.items() if key != "id"}
    if isinstance(value, list):
        return [strip_ids(item) for item in value]
    return value


def signature(lesson: dict) -> str:
    payload = json.dumps(strip_ids(lesson["exercises"]), ensure_ascii=False, sort_keys=True)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def main() -> None:
    full = {}
    for course_id, path in COURSES.items():
        course = json.loads(path.read_text(encoding="utf-8"))
        units = [
            unit
            for level in course["levels"]
            for unit in level["units"]
            if re.fullmatch(re.escape(course_id) + r"-a1-u\d{2}", unit["id"])
        ]
        lessons = [lesson for unit in units for lesson in unit["lessons"]]
        signatures = [signature(lesson) for lesson in lessons]
        duplicate_groups = {
            sig: count for sig, count in Counter(signatures).items() if count > 1
        }
        titles = [lesson["title"] for lesson in lessons]
        exercises = [exercise for lesson in lessons for exercise in lesson["exercises"]]
        report = {
            "course": course_id,
            "units": len(units),
            "lessons": len(lessons),
            "exercises": len(exercises),
            "duplicate_lesson_groups": len(duplicate_groups),
            "duplicate_lessons": sum(duplicate_groups.values()),
            "unique_titles": len(set(titles)),
            "exercise_types": dict(Counter(exercise["type"] for exercise in exercises)),
            "max_whitespace_tokens": max(
                len((exercise.get("audioText") or "").split())
                for exercise in exercises
                if exercise["type"] in {"ListenType", "SpeakRepeat", "ListenChoose"}
            ),
            "missing_feedback": sum(
                1 for exercise in exercises if exercise.get("feedbackOnError") is None
            ),
        }
        if course_id == "es-ja":
            report["max_japanese_characters"] = max(
                len((exercise.get("audioText") or "").replace(" ", ""))
                for exercise in exercises
                if exercise["type"] in {"ListenType", "SpeakRepeat", "ListenChoose"}
            )
        full[course_id] = report
        print(course_id, report)
    out = ROOT / "build" / "reports" / "f1-audit.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(full, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Informe JSON: {out}")


if __name__ == "__main__":
    main()
