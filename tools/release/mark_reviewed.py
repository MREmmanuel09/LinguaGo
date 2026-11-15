"""Registra el resultado de la revisión nativa en los CSV."""

from __future__ import annotations

import argparse
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PLAN = ROOT / "build" / "reports" / "native-review-plan.csv"
IDIOMS = ROOT / "build" / "reports" / "native-review-b2-idioms.csv"


def load(path: Path) -> tuple[list[dict[str, str]], list[str]]:
    if not path.exists():
        raise SystemExit(f"Falta {path}. Ejecuta tools/content/prepare_native_review.py.")
    with path.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle)
        return list(reader), list(reader.fieldnames or [])


def save(path: Path, rows: list[dict[str, str]], fields: list[str]) -> None:
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def matches(row: dict[str, str], args: argparse.Namespace) -> bool:
    if args.lesson_id and row["lesson_id"] != args.lesson_id:
        return False
    if args.course and row["course"] != args.course:
        return False
    if args.level and row.get("level") != args.level:
        return False
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--course", choices=["es-en", "es-pt", "es-fr", "es-ja"])
    parser.add_argument("--level", choices=["A1", "A2", "B1", "B2", "C1", "C2"])
    parser.add_argument("--lesson-id")
    parser.add_argument("--status", choices=["approved", "needs_change", "pending"], default="approved")
    parser.add_argument("--reviewer", default="")
    parser.add_argument("--notes", default="")
    parser.add_argument("--include-idioms", action="store_true")
    args = parser.parse_args()
    if args.status in {"approved", "needs_change"} and not args.reviewer.strip():
        raise SystemExit("Indica --reviewer para approved/needs_change.")
    lessons, lesson_fields = load(PLAN)
    selected = [row for row in lessons if matches(row, args)]
    if not selected:
        raise SystemExit("No hay filas que coincidan.")
    for row in selected:
        row["review_status"] = args.status
        row["reviewer"] = args.reviewer
        row["notes"] = args.notes
    save(PLAN, lessons, lesson_fields)
    print(f"Lecciones actualizadas: {len(selected)}")
    if args.include_idioms:
        idioms, idiom_fields = load(IDIOMS)
        selected_idioms = [row for row in idioms if matches(row, args)]
        for row in selected_idioms:
            row["review_status"] = args.status
            row["reviewer"] = args.reviewer
            row["notes"] = args.notes
        save(IDIOMS, idioms, idiom_fields)
        print(f"Modismos actualizados: {len(selected_idioms)}")
    print("\nProgreso por curso/nivel:")
    progress: dict[tuple[str, str], list[int]] = {}
    for row in lessons:
        key = (row["course"], row["level"])
        if key not in progress:
            progress[key] = [0, 0]
        progress[key][1] += 1
        if row["review_status"] == "approved":
            progress[key][0] += 1
    for (course, level), (approved, total) in sorted(progress.items()):
        print(f"  {course} {level}: {approved}/{total} aprobadas")


if __name__ == "__main__":
    main()
