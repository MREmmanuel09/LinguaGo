"""Cierra F0 con réplicas estructurales de repaso (PT, FR y JA).

Cada unidad piloto reutiliza 10 lecciones ya validadas del mismo nivel, con
IDs nuevos. No reformatea el resto del course.json. Es idempotente.
"""

from __future__ import annotations

import copy
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

B2_IDIOMS = {
    "es-pt": [
        ("dar um jeito", "Encontrar una solución práctica a un problema.", "encontrar una solución"),
        ("ficar de olho", "Vigilar o prestar atención a algo.", "estar atento"),
        ("quebrar o galho", "Salir del apuro con una solución provisional.", "salir del apuro"),
        ("pôr a mão na massa", "Ponerse a trabajar directamente.", "ponerse manos a la obra"),
        ("dar com os burros n'água", "Fracasar en un intento.", "fracasar"),
        ("estar com a corda no pescoço", "Estar en una situación económica difícil.", "estar en apuros"),
        ("fazer vista grossa", "Ignorar deliberadamente una falta.", "hacer la vista gorda"),
        ("custar os olhos da cara", "Costar muchísimo dinero.", "costar un ojo de la cara"),
        ("acertar na mosca", "Dar exactamente en el clavo.", "dar en el clavo"),
        ("a cereja do bolo", "El detalle final que completa algo.", "la guinda del pastel"),
    ],
    "es-fr": [
        ("tirer les ficelles", "Manejar los hilos de una situación.", "manejar los hilos"),
        ("mettre la main à la pâte", "Ponerse a trabajar directamente.", "ponerse manos a la obra"),
        ("coûter les yeux de la tête", "Costar muchísimo dinero.", "costar un ojo de la cara"),
        ("poser un lapin", "Dejar plantado a alguien sin avisar.", "dejar plantado"),
        ("tirer son épingle du jeu", "Salir bien parado de una situación.", "salir bien parado"),
        ("avoir le bras long", "Tener contactos influyentes.", "tener enchufe"),
        ("faire la sourde oreille", "Hacerse el sordo ante una petición.", "hacerse el sordo"),
        ("casser les pieds", "Dar la lata o molestar.", "dar la lata"),
        ("mettre les points sur les i", "Decir las cosas con claridad.", "decir las cosas claras"),
        ("jeter l'éponge", "Tirar la toalla.", "tirar la toalla"),
    ],
    "es-ja": [
        ("猫の手も借りたい", "Estar tan ocupado que se necesitaría ayuda hasta de un gato.", "estar muy ocupado"),
        ("顔が広い", "Tener muchos contactos.", "tener contactos"),
        ("耳が痛い", "Escuchar una crítica que duele.", "ser una crítica dura"),
        ("口が堅い", "Saber guardar un secreto.", "ser discreto"),
        ("目を通す", "Revisar un documento por encima.", "revisar"),
        ("手を貸す", "Echar una mano.", "echar una mano"),
        ("足を運ぶ", "Ir personalmente a un lugar.", "ir en persona"),
        ("胸を張る", "Sentirse orgulloso.", "sentirse orgulloso"),
        ("水に流す", "Olvidar una disputa pasada.", "olvidar el pasado"),
        ("油を売る", "Perder el tiempo en el trabajo.", "perder el tiempo"),
    ],
}

COURSES = [
    {
        "course_id": "es-pt",
        "path": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
        "levels": [
            ("b1", "B1", "es-pt-lv-b1", "es-pt-lv-b2", "Piloto B1 · Revisão"),
            ("b2", "B2", "es-pt-lv-b2", "es-pt-lv-c1", "Piloto B2 · Revisão"),
            ("c1", "C1", "es-pt-lv-c1", "es-pt-lv-c2", "Piloto C1 · Revisão"),
            ("c2", "C2", "es-pt-lv-c2", None, "Piloto C2 · Revisão"),
        ],
    },
    {
        "course_id": "es-fr",
        "path": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
        "levels": [
            ("a2", "A2", "es-fr-lv-a2", "es-fr-lv-b1", "Piloto A2 · Révision"),
            ("b1", "B1", "es-fr-lv-b1", "es-fr-lv-b2", "Piloto B1 · Révision"),
            ("b2", "B2", "es-fr-lv-b2", "es-fr-lv-c1", "Piloto B2 · Révision"),
            ("c1", "C1", "es-fr-lv-c1", "es-fr-lv-c2", "Piloto C1 · Révision"),
            ("c2", "C2", "es-fr-lv-c2", None, "Piloto C2 · Révision"),
        ],
    },
    {
        "course_id": "es-ja",
        "path": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
        "levels": [
            ("a1", "A1", "es-ja-lv-1", "es-ja-lv-a2", "Piloto A1 · 復習"),
            ("a2", "A2", "es-ja-lv-a2", "es-ja-lv-b1", "Piloto A2 · 復習"),
            ("b1", "B1", "es-ja-lv-b1", "es-ja-lv-b2", "Piloto B1 · 復習"),
            ("b2", "B2", "es-ja-lv-b2", "es-ja-lv-c1", "Piloto B2 · 復習"),
            ("c1", "C1", "es-ja-lv-c1", "es-ja-lv-c2", "Piloto C1 · 復習"),
            ("c2", "C2", "es-ja-lv-c2", None, "Piloto C2 · 復習"),
        ],
    },
]


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def pick_lessons(lessons: list[dict], count: int = 10) -> list[tuple[dict, bool]]:
    """Selecciona lecciones repartidas por el nivel; duplica si faltan."""
    total = len(lessons)
    if total >= count:
        indexes = []
        for position in range(count):
            index = round(position * (total - 1) / (count - 1))
            if index not in indexes:
                indexes.append(index)
        for index in range(total):
            if len(indexes) == count:
                break
            if index not in indexes:
                indexes.append(index)
        return [(lessons[index], False) for index in indexes[:count]]
    selected = [(lesson, False) for lesson in lessons]
    for offset in range(count - total):
        selected.append((lessons[offset % total], True))
    return selected


def clone_lesson(
    lesson: dict,
    lesson_id: str,
    duplicate: bool,
    suffix: str,
    b2_idiom: tuple[str, str, str] | None,
) -> dict:
    clone = copy.deepcopy(lesson)
    clone["id"] = lesson_id
    if duplicate:
        clone["title"] = f"{clone['title']} · {suffix}"
    for index, exercise in enumerate(clone["exercises"], start=1):
        exercise["id"] = f"{lesson_id}-e{index:02d}"
    if b2_idiom is not None:
        idiom, note, translation = b2_idiom
        clone["culturalNotes"] = [
            {"idiom": idiom, "note": note, "translation": translation}
        ]
    return clone


def clone_unit(
    course_id: str,
    level_code: str,
    cefr: str,
    lessons: list[dict],
    suffix: str,
) -> dict:
    unit_id = f"{course_id}-{level_code}-p0-un-1"
    chosen = pick_lessons(lessons)
    cloned = []
    idioms = B2_IDIOMS.get(course_id, [])
    for index, (lesson, duplicate) in enumerate(chosen, start=1):
        lesson_id = f"{course_id}-{level_code}-p0-u1-l{index:02d}"
        idiom = idioms[index - 1] if cefr == "B2" else None
        cloned.append(clone_lesson(lesson, lesson_id, duplicate, suffix, idiom))
    return {
        "id": unit_id,
        "title": {
            "es-pt": f"Piloto {cefr} · Revisão",
            "es-fr": f"Piloto {cefr} · Révision",
            "es-ja": f"Piloto {cefr} · 復習",
        }[course_id],
        "lessons": cloned,
    }


def insert_unit(text: str, next_level_id: str | None, unit: dict) -> str:
    if next_level_id is None:
        end_match = re.search(r"\n  \]\n\}\s*$", text)
        if end_match is None:
            raise SystemExit("No se encontró el cierre del curso")
        search_limit = end_match.start()
    else:
        level_match = re.search(
            r'\n[ \t]*\{\n[ \t]*"id": "' + re.escape(next_level_id) + '"',
            text,
        )
        if level_match is None:
            raise SystemExit(f"No se encontró el nivel {next_level_id}")
        search_limit = level_match.start()
    bracket_match = None
    for match in re.finditer(r"\n([ \t]*)\]", text[:search_limit]):
        bracket_match = match
    if bracket_match is None:
        raise SystemExit(f"No se encontró el cierre de units para {next_level_id or 'el final'}")
    unit_indent = len(bracket_match.group(1)) + 2
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), unit_indent)
    return text[: bracket_match.start()] + ",\n" + unit_text + text[bracket_match.start() :]


def main() -> None:
    for config in COURSES:
        course_id = config["course_id"]
        path: Path = config["path"]
        text = path.read_text(encoding="utf-8")
        course = json.loads(text)
        for level_code, cefr, level_id, next_level_id, _title in config["levels"]:
            unit_id = f"{course_id}-{level_code}-p0-un-1"
            if unit_id in text:
                print(f"{unit_id} ya existe; se omite.")
                continue
            level = next(level for level in course["levels"] if level["id"] == level_id)
            lessons = [lesson for unit in level["units"] for lesson in unit["lessons"]]
            unit = clone_unit(course_id, level_code, cefr, lessons, "repaso")
            text = insert_unit(text, next_level_id, unit)
            print(f"Insertado {unit_id} con {len(unit['lessons'])} lecciones.")
        path.write_text(text, encoding="utf-8", newline="\n")
        json.loads(path.read_text(encoding="utf-8"))


if __name__ == "__main__":
    main()
