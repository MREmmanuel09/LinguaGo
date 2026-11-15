"""Genera el plan de revisión nativa de F0/F1."""

from __future__ import annotations

import csv
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "es-pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "es-ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
OUT = ROOT / "build" / "reports"


def audio_count(lesson: dict) -> int:
    return sum(
        1
        for exercise in lesson["exercises"]
        if exercise["type"] in {"ListenChoose", "ListenType", "SpeakRepeat", "AudioOnlyPrompt"}
    )


def free_speaking_count(lesson: dict) -> int:
    return sum(1 for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    rows = []
    idioms = []
    for course_id, path in COURSES.items():
        data = json.loads(path.read_text(encoding="utf-8"))
        for level in data["levels"]:
            lessons = [lesson for unit in level["units"] for lesson in unit["lessons"]]
            review_lessons = [
                lesson
                for lesson in lessons
                if "-p0-" in lesson["id"] or re.search(r"-a1-u\d{2}-l\d{2}$", lesson["id"])
            ]
            for index, lesson in enumerate(review_lessons):
                stratified = index % 5 == 0 or index == len(review_lessons) - 1
                rows.append(
                    {
                        "course": course_id,
                        "level": lesson["cefrLevel"],
                        "lesson_id": lesson["id"],
                        "title": lesson["title"],
                        "micro_lesson_required": "yes",
                        "audio_required": "yes" if audio_count(lesson) > 0 else "no",
                        "audio_exercises": audio_count(lesson),
                        "free_speaking_required": "yes" if free_speaking_count(lesson) > 0 else "no",
                        "stratified_sample": "yes" if stratified else "no",
                        "review_status": "",
                        "reviewer": "",
                        "notes": "",
                    }
                )
                if lesson["cefrLevel"] == "B2" and "-p0-" in lesson["id"]:
                    for note in lesson.get("culturalNotes", []):
                        idioms.append(
                            {
                                "course": course_id,
                                "lesson_id": lesson["id"],
                                "idiom": note["idiom"],
                                "translation": note.get("translation", ""),
                                "note": note["note"],
                                "review_status": "",
                                "reviewer": "",
                                "notes": "",
                            }
                        )
    with (OUT / "native-review-plan.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)
    with (OUT / "native-review-b2-idioms.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(idioms[0].keys()))
        writer.writeheader()
        writer.writerows(idioms)
    print(f"Plan de revisión: {OUT / 'native-review-plan.csv'} ({len(rows)} lecciones)")
    print(f"Modismos B2: {OUT / 'native-review-b2-idioms.csv'} ({len(idioms)} modismos)")


if __name__ == "__main__":
    main()
