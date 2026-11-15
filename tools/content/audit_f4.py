"""Audita el contenido B2 de F4 en los cuatro cursos."""

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
MAX_TOKENS = 30


def strip_ids(value):
    if isinstance(value, dict):
        return {key: strip_ids(item) for key, item in value.items() if key != "id"}
    if isinstance(value, list):
        return [strip_ids(item) for item in value]
    return value


def signature(lesson: dict) -> str:
    payload = json.dumps(strip_ids(lesson["exercises"]), ensure_ascii=False, sort_keys=True)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def sentence_tokens(text: str) -> int:
    return max(
        len(part.split())
        for part in re.split(r"[.!?。！？]+", text)
        if part.strip()
    )


def main() -> None:
    report = {}
    for course_id, path in COURSES.items():
        course = json.loads(path.read_text(encoding="utf-8"))
        units = [
            unit
            for level in course["levels"]
            for unit in level["units"]
            if re.fullmatch(re.escape(course_id) + r"-b2-u\d{2}", unit["id"])
        ]
        lessons = [lesson for unit in units for lesson in unit["lessons"]]
        exercises = [exercise for lesson in lessons for exercise in lesson["exercises"]]
        signatures = [signature(lesson) for lesson in lessons]
        duplicate_groups = {
            digest: count for digest, count in Counter(signatures).items() if count > 1
        }
        titles = [lesson["title"] for lesson in lessons]
        max_tokens = 0
        max_exercise = None
        for exercise in exercises:
            texts = []
            for key in ("audioText", "transcription", "targetPhrase", "sourcePhrase", "modelAnswer"):
                value = exercise.get(key)
                if value:
                    texts.append(value)
            if exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            for text in texts:
                longest = sentence_tokens(text)
                if longest > max_tokens:
                    max_tokens = longest
                    max_exercise = exercise["id"]
        report[course_id] = {
            "units": len(units),
            "lessons": len(lessons),
            "exercises": len(exercises),
            "b2_total": sum(
                len(unit["lessons"])
                for level in course["levels"]
                if level["id"] == ("es-en-lv-3" if course_id == "es-en" else f"{course_id}-lv-b2")
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
            "cultural_notes": sum(1 for lesson in lessons if lesson.get("culturalNotes")),
            "free_speaking": sum(
                1 for exercise in exercises if exercise["type"] == "FreeSpeakingPrompt"
            ),
            "max_tokens": max_tokens,
            "max_tokens_exercise": max_exercise,
            "max_tokens_ok": max_tokens <= MAX_TOKENS,
            "unique_exercise_ids": len(exercises)
            == len({exercise["id"] for exercise in exercises}),
        }
        if course_id == "es-en":
            idioms = [
                note["idiom"].lower()
                for lesson in lessons
                for note in lesson.get("culturalNotes", [])
            ]
            report[course_id]["new_idioms"] = len(idioms)
            report[course_id]["unique_new_idioms"] = len(set(idioms))
        if course_id == "es-ja":
            report[course_id]["max_japanese_characters"] = max(
                len((exercise.get("audioText") or "").replace(" ", ""))
                for exercise in exercises
                if exercise["type"] in {"ListenType", "SpeakRepeat", "AudioOnlyPrompt"}
            )
        print(course_id, report[course_id])
    out = ROOT / "build" / "reports" / "f4-audit.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Informe JSON: {out}")


if __name__ == "__main__":
    main()
