"""Desambigua títulos repetidos dentro de un mismo nivel.

El camino muestra dos lecciones con el mismo nombre (una histórica y otra de
ampliación). Se conserva el primer título y se marca el segundo como
«práctica»; las lecciones del piloto se marcan como «Repaso: …».
"""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}


def main() -> None:
    for course, path in COURSES.items():
        text, root = load(path)
        nodes = {}
        for node in walk(root):
            if node.kind != "object" or "id" not in node.members or "exercises" not in node.members:
                continue
            lesson_id = node.members["id"].value
            if isinstance(lesson_id, str):
                nodes[lesson_id] = node

        data = json.loads(text)
        edits = []
        renamed = 0
        for level in data["levels"]:
            used = set()
            for unit in level["units"]:
                for lesson in unit["lessons"]:
                    title = lesson["title"]
                    node = nodes.get(lesson["id"])
                    if node is None:
                        continue
                    new_title = title
                    if title in used:
                        if "-p0-" in lesson["id"] and not title.startswith("Repaso:"):
                            new_title = f"Repaso: {title}"
                        else:
                            new_title = f"{title} · práctica"
                        suffix = 2
                        while new_title in used:
                            new_title = f"{title} · práctica {suffix}"
                            suffix += 1
                        title_node = node.members.get("title")
                        if title_node is not None:
                            edits.append(
                                (
                                    title_node.start,
                                    title_node.end,
                                    json.dumps(new_title, ensure_ascii=False),
                                )
                            )
                        micro = node.members.get("microLesson")
                        if micro is not None and micro.kind == "object":
                            micro_title = micro.members.get("title")
                            if micro_title is not None:
                                edits.append(
                                    (
                                        micro_title.start,
                                        micro_title.end,
                                        json.dumps(new_title, ensure_ascii=False),
                                    )
                                )
                        renamed += 1
                    used.add(new_title)
        if edits:
            save(path, apply_edits(text, edits))
        print(f"{course}: {renamed} títulos desambiguados")


if __name__ == "__main__":
    main()
