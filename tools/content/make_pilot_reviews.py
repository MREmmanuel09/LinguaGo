"""Convierte las lecciones piloto duplicadas en repasos mixtos.

Cada lección `*-p0-*` que era idéntica a una lección del camino principal se
reescribe como un repaso que combina ejercicios de esa lección con otra del
mismo nivel. Así deja de ser un duplicado exacto y el alumno repasa dos
lecciones en un solo bloque. Se conservan el número y los tipos de ejercicio
para no romper la rampa CEFR ni los tests de contenido.
"""

from __future__ import annotations

import hashlib
import json
import sys
from collections import Counter, defaultdict
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
INDENT = " " * 14


def to_python(node):
    if node.kind == "object":
        return {key: to_python(value) for key, value in node.members.items()}
    if node.kind == "array":
        return [to_python(item) for item in node.items]
    return node.value


def signature(lesson: dict) -> str:
    def strip_ids(value):
        if isinstance(value, dict):
            return {key: strip_ids(item) for key, item in value.items() if key != "id"}
        if isinstance(value, list):
            return [strip_ids(item) for item in value]
        return value

    payload = json.dumps(strip_ids(lesson["exercises"]), ensure_ascii=False, sort_keys=True)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def type_map(exercises: list[dict]) -> dict[str, list[dict]]:
    grouped = defaultdict(list)
    for exercise in exercises:
        grouped[exercise["type"]].append(exercise)
    return grouped


def choose_exercises(
    pilot_id: str,
    source: dict,
    partner: dict,
) -> list[dict]:
    source_types = type_map(source["exercises"])
    partner_types = type_map(partner["exercises"])
    chosen = []
    seed = sum(ord(char) for char in pilot_id)
    for type_index, (type_name, source_group) in enumerate(source_types.items()):
        partner_group = partner_types.get(type_name)
        use_partner = (
            partner_group is not None
            and len(partner_group) >= len(source_group)
            and (seed + type_index) % 2 == 1
        )
        group = partner_group if use_partner else source_group
        chosen.extend(group[: len(source_group)])
    # Ordena por tipo de la lección original para conservar una progresión estable.
    order = {id(exercise): index for index, exercise in enumerate(source["exercises"])}
    chosen.sort(key=lambda exercise: order.get(id(exercise), 10_000))
    return chosen


def sanitize_exercise(exercise: dict, grammar_ids: set[str], new_id: str) -> dict:
    result = json.loads(json.dumps(exercise, ensure_ascii=False))
    result["id"] = new_id
    for key in ("feedbackOnError", "feedbackOnSuccess"):
        feedback = result.get(key)
        if isinstance(feedback, dict) and feedback.get("ruleRef") not in grammar_ids:
            feedback.pop("ruleRef", None)
    refs = result.get("grammarRuleRefs")
    if isinstance(refs, list):
        result["grammarRuleRefs"] = [ref for ref in refs if ref in grammar_ids]
    return result


def build_review(pilot: dict, source: dict, partner: dict) -> dict:
    grammar = source.get("grammarPoints") or []
    grammar_ids = {point.get("id") for point in grammar}
    exercises = []
    for index, exercise in enumerate(choose_exercises(pilot["id"], source, partner), start=1):
        exercises.append(sanitize_exercise(exercise, grammar_ids, f"{pilot['id']}-e{index:02d}"))

    cultural = []
    seen_idioms = set()
    for lesson in (source, partner):
        for note in lesson.get("culturalNotes") or []:
            idiom = note.get("idiom")
            if idiom and idiom not in seen_idioms:
                seen_idioms.add(idiom)
                cultural.append(note)

    glossary = []
    seen_terms = set()
    for lesson in (source, partner):
        for entry in lesson.get("glossary") or []:
            term = entry.get("term")
            if term and term not in seen_terms:
                seen_terms.add(term)
                glossary.append(entry)

    micro = json.loads(json.dumps(source.get("microLesson") or {}, ensure_ascii=False))
    micro["title"] = f"Repaso: {source['title']}"
    micro["goals"] = [
        f"Repasar el vocabulario de «{source['title']}» y «{partner['title']}».",
        "Combinar las estructuras de las dos lecciones.",
        "Resolver una tarea comunicativa de repaso.",
    ]
    if not micro.get("sections"):
        micro["sections"] = [
            {
                "heading": "Lengua en contexto",
                "body": "Repaso integrador de las dos lecciones.",
                "examples": [],
            }
        ]

    return {
        "id": pilot["id"],
        "title": f"Repaso: {source['title']}",
        "cefrLevel": source["cefrLevel"],
        "scenario": f"Repaso integrador de «{source['title']}» y «{partner['title']}». {source.get('scenario', '')}".strip(),
        "canDo": (
            f"Puedo repasar y combinar lo aprendido en «{source['title']}» "
            f"y «{partner['title']}»."
        ),
        "grammarPoints": grammar,
        "vocabFrequencyBand": source.get("vocabFrequencyBand"),
        "culturalNotes": cultural,
        "microLesson": micro,
        "glossary": glossary,
        "exercises": exercises,
    }


def main() -> None:
    only = sys.argv[1] if len(sys.argv) > 1 else None
    for course, path in COURSES.items():
        if only and course != only:
            continue
        text, root = load(path)
        lesson_nodes = []
        for node in walk(root):
            if node.kind != "object" or "id" not in node.members or "exercises" not in node.members:
                continue
            lesson_id = node.members["id"].value
            if not isinstance(lesson_id, str) or "-p0-" not in lesson_id:
                continue
            lesson_nodes.append((node, to_python(node)))

        all_lessons = {}
        unit_of = {}
        unit_lessons = defaultdict(list)
        level_of = {}
        for node in walk(root):
            if node.kind != "object" or "id" not in node.members or "exercises" not in node.members:
                continue
            lesson = to_python(node)
            if not isinstance(lesson.get("id"), str):
                continue
            all_lessons[lesson["id"]] = lesson
        # La unidad y el nivel se leen del JSON completo una sola vez.
        data = json.loads(text)
        for level in data["levels"]:
            for unit in level["units"]:
                for lesson in unit["lessons"]:
                    unit_of[lesson["id"]] = unit["id"]
                    level_of[lesson["id"]] = lesson["cefrLevel"]
                    unit_lessons[unit["id"]].append(lesson["id"])

        groups = defaultdict(list)
        for lesson in all_lessons.values():
            groups[signature(lesson)].append(lesson["id"])

        exact_sources = {}
        for ids in groups.values():
            pilots = [lid for lid in ids if "-p0-" in lid]
            others = [lid for lid in ids if "-p0-" not in lid]
            for pilot in pilots:
                if others:
                    exact_sources[pilot] = others[0]

        edits = []
        converted = 0
        for node, pilot in lesson_nodes:
            source_id = exact_sources.get(pilot["id"])
            if source_id is None:
                continue
            source = all_lessons[source_id]
            partner = None
            for candidate in unit_lessons[unit_of[source_id]]:
                if candidate != source_id and candidate != pilot["id"]:
                    partner = all_lessons[candidate]
                    break
            if partner is None or Counter(
                exercise["type"] for exercise in source["exercises"]
            ) != Counter(exercise["type"] for exercise in partner["exercises"]):
                for candidate in all_lessons.values():
                    if (
                        candidate["id"] != source_id
                        and candidate["id"] != pilot["id"]
                        and "-p0-" not in candidate["id"]
                        and candidate["cefrLevel"] == source["cefrLevel"]
                        and Counter(exercise["type"] for exercise in candidate["exercises"])
                        == Counter(exercise["type"] for exercise in source["exercises"])
                    ):
                        partner = candidate
                        break
            if partner is None:
                print("SIN PAREJA", pilot["id"])
                continue
            review = build_review(pilot, source, partner)
            expected = Counter(exercise["type"] for exercise in source["exercises"])
            actual = Counter(exercise["type"] for exercise in review["exercises"])
            if expected != actual:
                print("TIPOS DISTINTOS", pilot["id"], expected, actual)
                continue
            serialized = json.dumps(review, ensure_ascii=False, indent=2)
            serialized = serialized.replace("\n", "\n" + INDENT)
            edits.append((node.start, node.end, serialized))
            converted += 1
        if edits:
            save(path, apply_edits(text, edits))
        print(f"{course}: {converted} repasos generados")


if __name__ == "__main__":
    main()
