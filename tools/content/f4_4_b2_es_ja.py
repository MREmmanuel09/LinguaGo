"""F4.4 — B2 completo de ES→JA (138 lecciones nuevas)."""

from __future__ import annotations

import argparse

from a2_factory import course_path, insert_unit
from b2_factory import build_unit
from b2_specs import build_specs

COURSE = course_path("es-ja")
COURSE_ID = "es-ja"
NEXT_LEVEL = "es-ja-lv-c1"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--unit", type=int)
    parser.add_argument("--all", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    selected = list(enumerate(build_specs("ja"), start=1))
    if args.unit is not None:
        selected = [item for item in selected if item[0] == args.unit]
        if not selected:
            raise SystemExit(f"No existe el bloque {args.unit}")
    elif not args.all and not args.dry_run:
        parser.error("Indica --unit N, --all o --dry-run")
    built = []
    for number, (title, specs) in selected:
        unit = build_unit(COURSE_ID, number, title, specs, "ja")
        built.append(unit)
        print(f"Bloque {number:02d}: {unit['title']} · {len(unit['lessons'])} lecciones")
    if args.dry_run:
        print(f"Total validado: {sum(len(unit['lessons']) for unit in built)} lecciones")
        return
    inserted = 0
    for unit in built:
        if insert_unit(COURSE, NEXT_LEVEL, unit):
            inserted += 1
        else:
            print(f"Ya existe {unit['id']}; no se modifica.")
    print(f"Bloques insertados: {inserted}")


if __name__ == "__main__":
    main()
