"""Audita las réplicas piloto de F0 en ES→PT, ES→FR y ES→JA."""

from __future__ import annotations

import copy
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "es-ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
LEVEL_ORDER = ["A1", "A2", "B1", "B2", "C1", "C2"]


def strip_ids(value):
    if isinstance(value, dict):
        return {key: strip_ids(item) for key, item in value.items() if key != "id"}
    if isinstance(value, list):
        return [strip_ids(item) for item in value]
    return value


def signature(lesson: dict) -> str:
    payload = json.dumps(strip_ids(lesson["exercises"]), ensure_ascii=False, sort_keys=True)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def sentence_lengths(text: str) -> list[int]:
    return [len(part.split()) for part in re.split(r"[.!?。！？]+", text) if part.strip()]


def longest_sentence(lesson: dict) -> int:
    lengths = []
    for exercise in lesson["exercises"]:
        if exercise["type"] == "Translate":
            lengths.append(len(exercise["correctTokens"]))
        elif exercise["type"] == "OrderWords":
            lengths.append(len(exercise["answerTokens"]))
        elif exercise["type"] == "ClozeDragDrop":
            passage = exercise["passage"]
            for blank in exercise["blanks"]:
                passage = passage.replace("{{" + blank["id"] + "}}", "x")
            lengths.extend(sentence_lengths(passage))
        elif exercise["type"] in {"ListenType", "SpeakRepeat"}:
            text = exercise.get("transcription") or exercise.get("targetPhrase") or ""
            lengths.extend(sentence_lengths(text))
        elif exercise["type"] in {"AudioOnlyPrompt"}:
            lengths.extend(sentence_lengths(exercise["audioText"]))
        elif exercise["type"] == "FreeSpeakingPrompt":
            lengths.extend(sentence_lengths(exercise["modelAnswer"]))
        elif exercise["type"] == "SemanticMatch":
            for pair in exercise["pairs"]:
                lengths.extend(sentence_lengths(pair["left"]))
    return max(lengths) if lengths else 0


def audit_course(course_id: str, path: Path) -> dict:
    course = json.loads(path.read_text(encoding="utf-8"))
    report = {
        "course": course_id,
        "levels": [],
        "pilot_units": [],
        "historical_signatures": {},
        "issues": [],
    }
    historical_by_level = {}
    for level in course["levels"]:
        lessons = [lesson for unit in level["units"] for lesson in unit["lessons"]]
        cefr = Counter(lesson["cefrLevel"] for lesson in lessons)
        historical_by_level[lessons[0]["cefrLevel"] if lessons else "?"] = {
            signature(lesson): lesson["id"] for lesson in lessons if "-p0-" not in lesson["id"]
        }
        report["levels"].append(
            {
                "id": level["id"],
                "lessons": len(lessons),
                "cefr": dict(cefr),
                "last_unit": level["units"][-1]["id"],
            }
        )

    for level in course["levels"]:
        for unit in level["units"]:
            if "-p0-" not in unit["id"]:
                continue
            lessons = unit["lessons"]
            cefr = lessons[0]["cefrLevel"] if lessons else "?"
            historical = historical_by_level.get(cefr, {})
            signatures = [signature(lesson) for lesson in lessons]
            duplicates = [lesson["id"] for lesson, sig in zip(lessons, signatures) if sig in historical]
            title_counts = Counter(lesson["title"] for lesson in lessons)
            repeated_titles = {title: count for title, count in title_counts.items() if count > 1}
            idioms = [
                lesson["culturalNotes"][0]["idiom"]
                for lesson in lessons
                if lesson.get("culturalNotes")
            ]
            report["pilot_units"].append(
                {
                    "unit": unit["id"],
                    "title": unit["title"],
                    "cefr": cefr,
                    "lessons": len(lessons),
                    "is_last_unit": level["units"][-1]["id"] == unit["id"],
                    "exact_duplicates": len(duplicates),
                    "duplicate_lessons": duplicates,
                    "repeated_titles": repeated_titles,
                    "exercise_counts": dict(Counter(len(lesson["exercises"]) for lesson in lessons)),
                    "grammar_counts": dict(Counter(len(lesson.get("grammarPoints", [])) for lesson in lessons)),
                    "max_sentence": max(longest_sentence(lesson) for lesson in lessons),
                    "idioms": idioms,
                    "idiom_duplicates": [idiom for idiom, count in Counter(idioms).items() if count > 1],
                    "missing_feedback": sum(
                        1
                        for lesson in lessons
                        for exercise in lesson["exercises"]
                        if exercise.get("feedbackOnError") is None
                    ),
                    "bad_lesson_ids": [
                        lesson["id"]
                        for lesson in lessons
                        if not re.fullmatch(rf"{re.escape(course_id)}-[a-z0-9]+-p0-u1-l\d{{2}}", lesson["id"])
                    ],
                    "bad_exercise_ids": [
                        exercise["id"]
                        for lesson in lessons
                        for exercise in lesson["exercises"]
                        if not re.fullmatch(rf"{re.escape(lesson['id'])}-e\d{{2}}", exercise["id"])
                    ],
                }
            )

            caps = {"A1": 8, "A2": 12, "B1": 24, "B2": 30, "C1": 38, "C2": 48}
            if report["pilot_units"][-1]["max_sentence"] > caps[cefr]:
                report["issues"].append(
                    f"{unit['id']}: frase de {report['pilot_units'][-1]['max_sentence']} tokens "
                    f"supera el tope {cefr} de {caps[cefr]}"
                )

            if not report["pilot_units"][-1]["is_last_unit"]:
                report["issues"].append(f"{unit['id']} no es la última unidad de su nivel")
            if duplicates:
                report["issues"].append(
                    f"{unit['id']}: {len(duplicates)} lecciones replican ejercicios históricos"
                )
            if repeated_titles:
                report["issues"].append(f"{unit['id']}: títulos repetidos {repeated_titles}")
            if report["pilot_units"][-1]["idiom_duplicates"]:
                report["issues"].append(
                    f"{unit['id']}: modismos repetidos {report['pilot_units'][-1]['idiom_duplicates']}"
                )
            if report["pilot_units"][-1]["missing_feedback"]:
                report["issues"].append(f"{unit['id']}: ejercicios sin feedbackOnError")
            if report["pilot_units"][-1]["bad_lesson_ids"] or report["pilot_units"][-1]["bad_exercise_ids"]:
                report["issues"].append(f"{unit['id']}: ids fuera de patrón")
    return report


def main() -> None:
    full = {}
    for course_id, path in COURSES.items():
        report = audit_course(course_id, path)
        full[course_id] = report
        print(f"== {course_id} ==")
        for level in report["levels"]:
            print(f"  {level['id']}: {level['lessons']} lecciones, última unidad {level['last_unit']}")
        for unit in report["pilot_units"]:
            print(
                f"  {unit['unit']} [{unit['cefr']}] lecciones={unit['lessons']} "
                f"duplicadas={unit['exact_duplicates']} tipos={unit['exercise_counts']} "
                f"max_frase={unit['max_sentence']} modismos={len(unit['idioms'])}"
            )
        print("  incidencias:", report["issues"] or "ninguna")
    out = ROOT / "build" / "reports" / "f0-replicas-audit.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(full, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"\nInforme JSON: {out}")


if __name__ == "__main__":
    main()
