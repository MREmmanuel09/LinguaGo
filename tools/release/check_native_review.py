"""Bloquea el release si la revisión nativa de F0/F1 no está completa."""

from __future__ import annotations

import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PLAN = ROOT / "build" / "reports" / "native-review-plan.csv"
IDIOMS = ROOT / "build" / "reports" / "native-review-b2-idioms.csv"


def load(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        print(f"Falta {path}. Ejecuta tools/content/prepare_native_review.py.")
        sys.exit(1)
    with path.open(encoding="utf-8", newline="") as handle:
        return list(csv.DictReader(handle))


def main() -> None:
    lessons = load(PLAN)
    idioms = load(IDIOMS)
    pending = [row for row in lessons if row["review_status"].strip().lower() != "approved"]
    pending_idioms = [row for row in idioms if row["review_status"].strip().lower() != "approved"]
    blockers = [
        row
        for row in lessons + idioms
        if "blocker" in row.get("notes", "").lower() or "major" in row.get("notes", "").lower()
    ]
    print(f"Lecciones revisadas: {len(lessons) - len(pending)}/{len(lessons)}")
    print(f"Modismos revisados: {len(idioms) - len(pending_idioms)}/{len(idioms)}")
    if pending:
        print(f"Pendientes de aprobar: {len(pending)} lecciones")
        for row in pending[:10]:
            print(f"  - {row['course']} {row['lesson_id']} ({row['review_status'] or 'sin estado'})")
    if pending_idioms:
        print(f"Pendientes de aprobar: {len(pending_idioms)} modismos")
        for row in pending_idioms[:10]:
            print(f"  - {row['course']} {row['lesson_id']} {row['idiom']}")
    if blockers:
        print(f"Incidencias blocker/major sin resolver: {len(blockers)}")
        for row in blockers[:10]:
            print(f"  - {row.get('lesson_id', '?')}: {row['notes']}")
    if pending or pending_idioms or blockers:
        print("\nRelease bloqueado. Completa la revisión nativa y vuelve a ejecutar.")
        sys.exit(1)
    print("\nRevisión nativa completa: release desbloqueado.")


if __name__ == "__main__":
    main()
