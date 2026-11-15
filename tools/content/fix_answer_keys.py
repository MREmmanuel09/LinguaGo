"""Corrige claves de ListenChoose cuya opción correcta es el audio.

La fábrica dejó `correctIndex: 0` en miles de ejercicios aunque el audio era
la segunda o tercera opción. La regla es demostrable: si `audioText` está
entre las opciones, la opción correcta es exactamente esa. Se edita solo el
número, conservando el formato del JSON.
"""

from __future__ import annotations

import sys
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


def fix_course(path: Path, dry_run: bool) -> tuple[int, int]:
    text, root = load(path)
    edits: list[tuple[int, int, str]] = []
    checked = 0
    for node in walk(root):
        if node.kind != "object":
            continue
        type_node = node.members.get("type")
        if type_node is None or type_node.value != "ListenChoose":
            continue
        options_node = node.members.get("options")
        audio_node = node.members.get("audioText")
        index_node = node.members.get("correctIndex")
        if options_node is None or audio_node is None or index_node is None:
            continue
        options = [item.value for item in options_node.items]
        audio = audio_node.value
        if audio not in options:
            continue
        expected = options.index(audio)
        checked += 1
        if index_node.value != expected:
            edits.append((index_node.start, index_node.end, str(expected)))
    if edits and not dry_run:
        save(path, apply_edits(text, edits))
    return checked, len(edits)


def main() -> None:
    dry_run = "--dry-run" in sys.argv
    total_fixed = 0
    for course, path in COURSES.items():
        checked, fixed = fix_course(path, dry_run)
        total_fixed += fixed
        print(f"{course}: {fixed} claves corregidas de {checked} ListenChoose con audio en opciones")
    print("total:", total_fixed, "(dry-run)" if dry_run else "")


if __name__ == "__main__":
    main()
