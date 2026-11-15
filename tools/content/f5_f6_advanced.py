"""F5/F6 — inserta C1 o C2 completo en un curso."""

from __future__ import annotations

import argparse

from a2_factory import course_path, insert_unit
from advanced_factory import build_unit, insert_unit_last_level
from advanced_specs import build_specs

COURSES = {
    "es-en": ("en", "es-en-lv-5"),
    "es-pt": ("pt", "es-pt-lv-c2"),
    "es-fr": ("fr", "es-fr-lv-c2"),
    "es-ja": ("ja", "es-ja-lv-c2"),
}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--level", choices=["C1", "C2"], required=True)
    parser.add_argument("--course", choices=list(COURSES), required=True)
    parser.add_argument("--unit", type=int)
    parser.add_argument("--all", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    lang, next_level = COURSES[args.course]
    path = course_path(args.course)
    selected = list(enumerate(build_specs(args.level, lang), start=1))
    if args.unit is not None:
        selected = [item for item in selected if item[0] == args.unit]
        if not selected:
            raise SystemExit(f"No existe el bloque {args.unit}")
    elif not args.all and not args.dry_run:
        parser.error("Indica --unit N, --all o --dry-run")

    built = []
    for number, (title, specs) in selected:
        unit = build_unit(args.course, args.level, number, title, specs, lang)
        built.append(unit)
        print(
            f"{args.level} {args.course} bloque {number:02d}: "
            f"{unit['title']} · {len(unit['lessons'])} lecciones"
        )
    if args.dry_run:
        print(f"Total validado: {sum(len(unit['lessons']) for unit in built)} lecciones")
        return

    inserted = 0
    for unit in built:
        if args.level == "C2":
            was_inserted = insert_unit_last_level(path, unit)
        else:
            was_inserted = insert_unit(path, next_level, unit)
        if was_inserted:
            inserted += 1
        else:
            print(f"Ya existe {unit['id']}; no se modifica.")
    print(f"Bloques insertados: {inserted}")


if __name__ == "__main__":
    main()
