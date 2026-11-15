"""Adapta el currículo A2 compartido a portugués y japonés."""

from __future__ import annotations

import json
from pathlib import Path

from a2_factory import sentences_for
from a2_specs import BLOCKS, CAPSTONE_FR

ROOT = Path(__file__).resolve().parents[2]
TRANSLATIONS = {
    "pt": json.loads(
        (ROOT / "tools/content/a2_translations_pt.json").read_text(encoding="utf-8")
    ),
    "ja": json.loads(
        (ROOT / "tools/content/a2_translations_ja.json").read_text(encoding="utf-8")
    ),
}


def spec_sentences(spec: dict) -> list[str]:
    if "sentences" in spec:
        return [sentence[0] for sentence in spec["sentences"]]
    return [pair[0] for pair in sentences_for(spec["pattern"], spec["items"], "en")]


def localize_spec(spec: dict, lang: str) -> dict:
    spanish = spec_sentences(spec)
    table = TRANSLATIONS[lang]
    missing = [sentence for sentence in spanish if sentence not in table]
    if missing:
        raise KeyError(f"Faltan traducciones {lang}: {missing}")
    return {
        "title": spec["title"],
        "topic": spec["topic"],
        "can_do": spec["can_do"],
        "grammar": spec["grammar"],
        "pattern": "phrase",
        "items": [(sentence, table[sentence]) for sentence in spanish],
    }


def localized_blocks(lang: str) -> list[tuple[str, list[dict]]]:
    return [
        (title, [localize_spec(spec, lang) for spec in specs])
        for title, specs in BLOCKS
    ]


def localized_capstone(lang: str) -> list[dict]:
    return [localize_spec(spec, lang) for spec in CAPSTONE_FR]
