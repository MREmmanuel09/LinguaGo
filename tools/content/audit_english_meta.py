"""Barrido final de términos gramaticales ingleses en cursos no ingleses."""

from __future__ import annotations

import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
ENGLISH = re.compile(
    r"\b("
    r"present simple|present perfect|past simple|past continuous|future perfect|"
    r"passive voice|passive reporting|passive reportative|reported speech|"
    r"non-defining relative clauses|relative clauses|third conditional|"
    r"going to|used to|will|would|could|should|must|have to|has to|"
    r"perfect gerund|question tags|comparatives|superlatives|"
    r"in the morning|on monday|at seven|the more|who, which|it is said|"
    r"if \+ past perfect|to, in order to|i was wondering|hedging|"
    r"cleft sentences|participle clauses|fronting|nominalisation|parallelism|"
    r"speculation|understatement|subjunctive|purpose|concession|modal perfect|"
    r"mixed conditionals|advanced comparatives|advanced quantifiers|"
    r"softening requests|diplomatic hedging"
    r")\b",
    re.IGNORECASE,
)
# Campos cuya lengua no es la meta.
FIELDS = {
    "label",
    "description",
    "goals",
    "body",
    "canDo",
    "scenario",
    "note",
    "translation",
    "explanation",
    "question",
    "prompt",
    "selfAssessmentCriteria",
    "instructions",
    "translations",
    "sourcePhrase",
    "source",
    "title",
}


def walk(value, path=()):
    if isinstance(value, dict):
        for key, item in value.items():
            yield from walk(item, path + (key,))
    elif isinstance(value, list):
        for item in value:
            yield from walk(item, path)
    elif isinstance(value, str):
        yield path, value


for course, path in COURSES.items():
    data = json.loads(path.read_text(encoding="utf-8"))
    hits = Counter()
    samples = []
    for field_path, text in walk(data):
        if not field_path or field_path[-1] not in FIELDS:
            continue
        if ENGLISH.search(text):
            hits[field_path[-1]] += 1
            if len(samples) < 15:
                samples.append((field_path, text))
    print("====", course, dict(hits))
    for sample in samples:
        print("  ", sample)
