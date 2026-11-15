"""Actualiza versionCode/versionName de la app sin tocar el resto del archivo."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GRADLE = ROOT / "app" / "build.gradle.kts"


def main() -> None:
    if len(sys.argv) != 3:
        print("Uso: python tools/release/bump_version.py <versionName> <versionCode>")
        sys.exit(2)
    version_name = sys.argv[1]
    version_code = sys.argv[2]
    if not re.fullmatch(r"\d+\.\d+\.\d+(-[A-Za-z0-9.-]+)?", version_name):
        print("versionName inválido")
        sys.exit(2)
    if not version_code.isdigit():
        print("versionCode debe ser numérico")
        sys.exit(2)
    text = GRADLE.read_text(encoding="utf-8")
    updated = re.sub(r"versionCode = \d+", f"versionCode = {version_code}", text, count=1)
    updated = re.sub(r'versionName = "[^"]+"', f'versionName = "{version_name}"', updated, count=1)
    if updated == text:
        print("No se encontró versionCode/versionName")
        sys.exit(1)
    GRADLE.write_text(updated, encoding="utf-8", newline="\n")
    print(f"Versión actualizada a {version_name} ({version_code})")


if __name__ == "__main__":
    main()
