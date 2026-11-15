"""Comprueba que la respuesta modelo cubre los conceptClusters."""

from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
CJK = re.compile(r"[\u3040-\u30FF\u3400-\u4DBF\u4E00-\u9FFF]")


def fold(text: str) -> str:
    text = unicodedata.normalize("NFD", text.lower())
    text = "".join(char for char in text if not unicodedata.combining(char))
    return re.sub(r"[^\w\s]", " ", text)


def contains(text: str, term: str) -> bool:
    if CJK.search(term):
        return term.replace(" ", "") in text.replace(" ", "")
    return fold(term).strip() in fold(text)


for course, path in COURSES.items():
    data = json.loads(path.read_text(encoding="utf-8"))
    total = 0
    missing = []
    for level in data["levels"]:
        for unit in level["units"]:
            for lesson in unit["lessons"]:
                for exercise in lesson["exercises"]:
                    if exercise["type"] != "FreeSpeakingPrompt":
                        continue
                    total += 1
                    model = exercise.get("modelAnswer", "")
                    for cluster in exercise.get("conceptClusters") or []:
                        if not any(contains(model, term) for term in cluster):
                            missing.append((lesson["id"], exercise["id"], cluster, model[:160]))
                            break
    print("====", course, "total", total, "sin cubrir", len(missing))
    for item in missing[:10]:
        print("  ", item)
