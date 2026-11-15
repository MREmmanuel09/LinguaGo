"""Genera el plan de revisión nativa de F2 (A2 de los cuatro cursos)."""

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
OUT = ROOT / "build" / "reports" / "native-review-f2-plan.csv"


def audio_count(lesson: dict) -> int:
    return sum(
        1
        for exercise in lesson["exercises"]
        if exercise["type"] in {"ListenChoose", "ListenType", "SpeakRepeat", "AudioOnlyPrompt"}
    )


def free_speaking_count(lesson: dict) -> int:
    return sum(1 for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt")


def sample(lesson: dict) -> tuple[str, str]:
    for exercise in lesson["exercises"]:
        if exercise["type"] == "Translate":
            return exercise.get("sourcePhrase", ""), " ".join(exercise.get("correctTokens", []))
        if exercise["type"] == "ListenType":
            return "", exercise.get("transcription", "")
    return "", ""


def main() -> None:
    rows = []
    for course_id, path in COURSES.items():
        data = json.loads(path.read_text(encoding="utf-8"))
        for level in data["levels"]:
            if level["id"] != f"{course_id}-lv-a2":
                continue
            lessons = [
                lesson
                for unit in level["units"]
                for lesson in unit["lessons"]
                if re.search(r"-a2-u\d{2}-l\d{2}$", lesson["id"])
            ]
            for index, lesson in enumerate(lessons):
                source, target = sample(lesson)
                rows.append(
                    {
                        "course": course_id,
                        "level": lesson["cefrLevel"],
                        "lesson_id": lesson["id"],
                        "title": lesson["title"],
                        "grammar_points": "|".join(
                            point["label"] for point in lesson.get("grammarPoints", [])
                        ),
                        "audio_exercises": audio_count(lesson),
                        "free_speaking_required": "yes" if free_speaking_count(lesson) > 0 else "no",
                        "stratified_sample": "yes" if index % 5 == 0 or index == len(lessons) - 1 else "no",
                        "sample_source": source,
                        "sample_target": target,
                        "review_status": "",
                        "reviewer": "",
                        "notes": "",
                    }
                )
    if not rows:
        raise SystemExit("No se encontraron lecciones F2")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)
    print(f"Plan de revisión F2: {OUT} ({len(rows)} lecciones)")


if __name__ == "__main__":
    main()
