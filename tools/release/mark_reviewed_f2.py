"""Registra la revisión nativa de F2 en el CSV de 599 lecciones."""

from __future__ import annotations

import argparse
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PLAN = ROOT / "build" / "reports" / "native-review-f2-plan.csv"


def load() -> tuple[list[dict[str, str]], list[str]]:
    if not PLAN.exists():
        raise SystemExit(
            f"Falta {PLAN}. Ejecuta tools/content/prepare_native_review_f2.py."
        )
    with PLAN.open(encoding="utf-8", newline="") as handle:
        reader = csv.DictReader(handle)
        return list(reader), list(reader.fieldnames or [])


def save(rows: list[dict[str, str]], fields: list[str]) -> None:
    with PLAN.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)


def matches(row: dict[str, str], args: argparse.Namespace) -> bool:
    if args.lesson_id and row["lesson_id"] != args.lesson_id:
        return False
    if args.course and row["course"] != args.course:
        return False
    if args.unit and not row["lesson_id"].startswith(f"{row['course']}-a2-u{args.unit:02d}-"):
        return False
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--course", choices=["es-en", "es-pt", "es-fr", "es-ja"])
    parser.add_argument("--unit", type=int, help="Bloque A2 (01-16)")
    parser.add_argument("--lesson-id")
    parser.add_argument("--status", choices=["approved", "needs_change", "pending"], default="approved")
    parser.add_argument("--reviewer", default="")
    parser.add_argument("--notes", default="")
    args = parser.parse_args()
    if args.status in {"approved", "needs_change"} and not args.reviewer.strip():
        raise SystemExit("Indica --reviewer para approved/needs_change.")
    rows, fields = load()
    selected = [row for row in rows if matches(row, args)]
    if not selected:
        raise SystemExit("No hay filas que coincidan.")
    for row in selected:
        row["review_status"] = args.status
        row["reviewer"] = args.reviewer
        row["notes"] = args.notes
    save(rows, fields)
    print(f"Lecciones actualizadas: {len(selected)}")
    approved = sum(1 for row in rows if row["review_status"] == "approved")
    print(f"Progreso: {approved}/{len(rows)} aprobadas")


if __name__ == "__main__":
    main()
