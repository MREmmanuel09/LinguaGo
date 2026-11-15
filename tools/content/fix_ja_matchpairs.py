"""Normaliza MatchPairs de JA A1/A2: español a la izquierda, japonés a la derecha.

Los pares kana→romaji y kana→número se conservan. En B1+ no se toca porque
algunos pares emparejan una frase japonesa con una función discursiva en
español (no son traducciones literales).
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save, walk  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json"
CJK = re.compile(r"[\u3040-\u30FF\u3400-\u4DBF\u4E00-\u9FFF]")
ROMAJI = {
    "a", "i", "u", "e", "o", "ka", "ki", "ku", "ke", "ko", "sa", "shi", "su",
    "se", "so", "ta", "chi", "tsu", "te", "to", "na", "ni", "nu", "ne", "no",
    "ha", "hi", "fu", "he", "ho", "ma", "mi", "mu", "me", "mo", "ya", "yu",
    "yo", "ra", "ri", "ru", "re", "ro", "wa", "wo", "n", "ga", "gi", "gu",
    "ge", "go", "za", "ji", "zu", "ze", "zo", "da", "di", "du", "de", "do",
    "ba", "bi", "bu", "be", "bo", "pa", "pi", "pu", "pe", "po", "kya", "kyu",
    "kyo", "sha", "shu", "sho", "cha", "chu", "cho", "nya", "nyu", "nyo",
    "hya", "hyu", "hyo", "mya", "myu", "myo", "rya", "ryu", "ryo", "gya",
    "gyu", "gyo", "ja", "ju", "jo", "bya", "byu", "byo", "pya", "pyu", "pyo",
}


def main() -> None:
    text, root = load(COURSE)
    data = json.loads(text)
    level_of = {}
    for level in data["levels"]:
        for unit in level["units"]:
            for lesson in unit["lessons"]:
                for exercise in lesson["exercises"]:
                    level_of[exercise["id"]] = lesson["cefrLevel"]

    edits = []
    swapped = 0
    for node in walk(root):
        if node.kind != "object" or "type" not in node.members:
            continue
        if node.members["type"].value != "MatchPairs":
            continue
        exercise_id = node.members.get("id")
        if exercise_id is None or level_of.get(exercise_id.value) not in {"A1", "A2"}:
            continue
        pairs = node.members.get("pairs")
        if pairs is None or pairs.kind != "array":
            continue
        for pair in pairs.items:
            left = pair.members.get("left")
            right = pair.members.get("right")
            if left is None or right is None:
                continue
            if not isinstance(left.value, str) or not isinstance(right.value, str):
                continue
            if not CJK.search(left.value) or CJK.search(right.value):
                continue
            candidate = right.value.strip()
            if candidate.lower() in ROMAJI or candidate.isdigit():
                continue
            edits.append((left.start, left.end, json.dumps(right.value, ensure_ascii=False)))
            edits.append((right.start, right.end, json.dumps(left.value, ensure_ascii=False)))
            swapped += 1
    if edits:
        save(COURSE, apply_edits(text, edits))
    print("pares invertidos corregidos:", swapped)


if __name__ == "__main__":
    main()
