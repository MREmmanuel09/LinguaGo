"""F2.2 — A2 completo de ES→PT (152 lecciones nuevas)."""

from __future__ import annotations

import argparse

from a2_factory import build_unit, course_path, insert_unit
from a2_localized import localized_blocks, localized_capstone

COURSE = course_path("es-pt")
COURSE_ID = "es-pt"
NEXT_LEVEL = "es-pt-lv-b1"


def units() -> list[tuple[int, str, list[dict]]]:
    result = [
        (number, title, specs)
        for number, (title, specs) in enumerate(localized_blocks("pt"), start=1)
    ]
    result.append((16, "Projeto final e autonomia", localized_capstone("pt")))
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--unit", type=int, help="Número de bloque que se inserta")
    parser.add_argument("--all", action="store_true", help="Inserta todos los bloques")
    parser.add_argument("--dry-run", action="store_true", help="Valida sin escribir")
    args = parser.parse_args()

    selected = units()
    if args.unit is not None:
        selected = [item for item in selected if item[0] == args.unit]
        if not selected:
            raise SystemExit(f"No existe el bloque {args.unit}")
    elif not args.all and not args.dry_run:
        parser.error("Indica --unit N, --all o --dry-run")

    built = []
    for number, title, specs in selected:
        unit = build_unit(COURSE_ID, number, title, specs, "pt")
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
