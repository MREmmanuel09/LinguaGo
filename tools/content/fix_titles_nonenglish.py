"""Traduce los títulos de gramática inglesa de los cursos PT/FR/JA."""

from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
TITLES = {
    "Going to": "Futuro próximo",
    "Should / shouldn't": "Consejos y recomendaciones",
    "Have to / don't have to": "Obligación y ausencia de obligación",
    "Present perfect": "Experiencias y pasado reciente",
    "Must / mustn't": "Obligación y prohibición",
    "Have to / allowed to": "Obligación y permiso",
    "Countable / uncountable": "Contables e incontables",
    "First conditional": "Condicional real",
    "Will / won't": "Futuro y negación",
    "Present perfect vs past simple": "Pasado reciente y pasado simple",
    "Used to": "Hábitos pasados",
}


def main() -> None:
    for course, path in COURSES.items():
        text, root = load(path)
        edits = []
        changed = 0
        for node in walk(root):
            if node.kind != "object" or "title" not in node.members or "exercises" not in node.members:
                continue
            title_node = node.members["title"]
            new_title = TITLES.get(title_node.value)
            if new_title is None:
                continue
            edits.append((title_node.start, title_node.end, json.dumps(new_title, ensure_ascii=False)))
            micro = node.members.get("microLesson")
            if micro is not None and micro.kind == "object":
                micro_title = micro.members.get("title")
                if micro_title is not None and micro_title.value == title_node.value:
                    edits.append(
                        (
                            micro_title.start,
                            micro_title.end,
                            json.dumps(new_title, ensure_ascii=False),
                        )
                    )
            changed += 1
        if edits:
            save(path, apply_edits(text, edits))
        print(f"{course}: {changed} títulos de gramática traducidos")


if __name__ == "__main__":
    main()
