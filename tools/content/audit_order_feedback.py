"""Comprueba que el feedback de OrderWords coincida con answerTokens."""

from __future__ import annotations

import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
QUOTED = re.compile(r"«([^»]+)»")


def normalize(text: str) -> str:
    return re.sub(r"[\s.,;:!?¡¿«»\"']+", "", text).casefold()


for course, path in COURSES.items():
    data = json.loads(path.read_text(encoding="utf-8"))
    total = 0
    mismatch = []
    for level in data["levels"]:
        for unit in level["units"]:
            for lesson in unit["lessons"]:
                for exercise in lesson["exercises"]:
                    if exercise["type"] != "OrderWords":
                        continue
                    total += 1
                    answer = " ".join(exercise.get("answerTokens") or [])
                    feedback = exercise.get("feedbackOnError", {}).get("explanation", "")
                    quoted = QUOTED.findall(feedback)
                    if not quoted:
                        continue
                    if not any(normalize(answer) == normalize(item) for item in quoted):
                        mismatch.append((lesson["id"], exercise["id"], answer, quoted, feedback[:200]))
    print("====", course, "total", total, "mismatch", len(mismatch))
    for item in mismatch[:9]:
        print("  ", item)
