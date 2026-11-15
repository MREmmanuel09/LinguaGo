"""Comprueba que la revisión nativa de F2 esté completa."""

from __future__ import annotations

import csv
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PLAN = ROOT / "build" / "reports" / "native-review-f2-plan.csv"


def main() -> None:
    if not PLAN.exists():
        raise SystemExit(
            f"Falta {PLAN}. Ejecuta tools/content/prepare_native_review_f2.py."
        )
    with PLAN.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle))
    approved = [row for row in rows if row["review_status"] == "approved"]
    pending = [row for row in rows if row["review_status"] not in {"approved", "needs_change"}]
    needs_change = [row for row in rows if row["review_status"] == "needs_change"]
    print(f"Lecciones revisadas: {len(approved)}/{len(rows)}")
    print(f"Pendientes de aprobar: {len(pending)}")
    if needs_change:
        print(f"Necesitan cambios: {len(needs_change)}")
    progress = Counter((row["course"], row["review_status"]) for row in rows)
    for (course, status), count in sorted(progress.items()):
        print(f"  {course} {status or 'pending'}: {count}")
    if pending or needs_change:
        for row in (pending + needs_change)[:10]:
            print(f"  - {row['course']} {row['lesson_id']} ({row['review_status'] or 'sin estado'})")
        raise SystemExit("Release bloqueado. Completa la revisión nativa de F2.")
    print("Revisión nativa de F2 completa: release 3.3.0 desbloqueado.")


if __name__ == "__main__":
    main()
    sys.exit(0)
