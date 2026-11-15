"""Auditoría global del contenido: 4 cursos × 6 niveles × 170 lecciones.

Revisa estructura, claves de respuesta, feedback, placeholders, duplicados,
higiene de texto, scripts de idioma, tokens y coherencia de nivel. No sustituye
la revisión nativa humana: detecta lo que una máquina puede demostrar.
"""

from __future__ import annotations

import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-en": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
    "es-pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "es-ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
LEVEL_ORDER = {"A1": 1, "A2": 2, "B1": 3, "B2": 4, "C1": 5, "C2": 6}
EXPECTED_EXERCISES = {"A1": 7, "A2": 7, "B1": 9, "B2": 10, "C1": 11, "C2": 12}
TOKEN_CAPS = {"A1": 8, "A2": 12, "B1": 24, "B2": 30, "C1": 38, "C2": 48}
ALLOWED_TYPES = {
    "A1": {
        "MultipleChoice", "Translate", "OrderWords", "MatchPairs", "ListenChoose",
        "ListenType", "SpeakRepeat", "KanaChoose", "KanjiReading", "ParticleFill",
    },
    "A2": {
        "MultipleChoice", "Translate", "OrderWords", "MatchPairs", "ListenChoose",
        "ListenType", "SpeakRepeat", "KanaChoose", "KanjiReading", "ParticleFill",
    },
    "B1": {
        "MultipleChoice", "Translate", "OrderWords", "MatchPairs", "ListenChoose",
        "ListenType", "SpeakRepeat", "KanaChoose", "KanjiReading", "ParticleFill",
        "ClozeDragDrop", "FreeSpeakingPrompt",
    },
    "B2": {
        "MultipleChoice", "OrderWords", "ListenType", "SpeakRepeat",
        "ClozeDragDrop", "SemanticMatch", "AudioOnlyPrompt", "FreeSpeakingPrompt",
    },
    "C1": {
        "MultipleChoice", "OrderWords", "Translate", "ClozeDragDrop", "SemanticMatch",
        "ListenChoose", "AudioOnlyPrompt", "ListenType", "SpeakRepeat", "MatchPairs",
        "FreeSpeakingPrompt",
    },
    "C2": {
        "MultipleChoice", "Translate", "ClozeDragDrop", "SemanticMatch", "ListenChoose",
        "AudioOnlyPrompt", "ListenType", "SpeakRepeat", "MatchPairs", "FreeSpeakingPrompt",
    },
}
REGISTERS = {"neutral", "formal", "informal", "literary", "colloquial"}
CJK = re.compile(r"[\u3040-\u30FF\u3400-\u4DBF\u4E00-\u9FFF]")
MOJIBAKE = ("Ã", "Â ", "â€", "\ufffd")
PLACEHOLDER = re.compile(r"\{\{([^}]+)\}\}")


def is_ja(course: str) -> bool:
    return course == "es-ja"


def tokens(text: str, course: str) -> list[str]:
    if not text:
        return []
    if is_ja(course):
        return [part for part in text.split() if part]
    return re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text)


def sentence_tokens(text: str, course: str) -> int:
    parts = [part for part in re.split(r"[.!?。！？]+", text or "") if part.strip()]
    return max((len(tokens(part, course)) for part in parts), default=0)


def token_count(text: str, course: str) -> int:
    """Cuenta tokens como FreeSpeakingHeuristicGrader (JA cuenta caracteres)."""
    if is_ja(course):
        return sum(1 for char in text or "" if char.isalnum())
    return len(tokens(text, course))


def iter_strings(value, path: str = ""):
    if isinstance(value, dict):
        for key, item in value.items():
            yield from iter_strings(item, f"{path}.{key}" if path else key)
    elif isinstance(value, list):
        for index, item in enumerate(value):
            yield from iter_strings(item, f"{path}[{index}]")
    elif isinstance(value, str):
        yield path, value


def signature(lesson: dict) -> str:
    def strip_ids(value):
        if isinstance(value, dict):
            return {k: strip_ids(v) for k, v in value.items() if k != "id"}
        if isinstance(value, list):
            return [strip_ids(item) for item in value]
        return value

    payload = json.dumps(strip_ids(lesson["exercises"]), ensure_ascii=False, sort_keys=True)
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


class Audit:
    def __init__(self) -> None:
        self.issues: list[dict] = []
        self.totals = Counter()
        self.duplicate_sentences: dict[str, Counter] = {}
        self.titles: dict[str, Counter] = defaultdict(Counter)
        self.level_titles: dict[tuple[str, str], Counter] = defaultdict(Counter)
        self.signatures: dict[str, dict[str, list[str]]] = defaultdict(lambda: defaultdict(list))

    def add(self, severity: str, code: str, course: str, lesson: str, detail: str, exercise: str = "") -> None:
        self.issues.append(
            {
                "severity": severity,
                "code": code,
                "course": course,
                "lesson_id": lesson,
                "exercise_id": exercise,
                "detail": detail,
            }
        )

    def audit_lesson(self, course: str, lesson: dict, level_id: str, level: str) -> None:
        lid = lesson.get("id", "?")
        cefr = lesson.get("cefrLevel")
        exercises = lesson.get("exercises", [])
        self.totals["lessons"] += 1
        self.totals["exercises"] += len(exercises)
        if cefr != level:
            self.add("error", "lesson-level-mismatch", course, lid, f"cefrLevel={cefr} en nivel {level}")
        expected = EXPECTED_EXERCISES[level]
        if len(exercises) != expected:
            self.add("error", "exercise-count", course, lid, f"{len(exercises)} ejercicios; esperados {expected}")

        grammar = lesson.get("grammarPoints") or []
        if level == "A1" and grammar:
            self.add("error", "grammar-a1", course, lid, f"A1 con {len(grammar)} focos")
        if level == "A2" and len(grammar) > 2:
            self.add("error", "grammar-a2", course, lid, f"A2 con {len(grammar)} focos")
        if LEVEL_ORDER[level] >= 3 and len(grammar) != 2:
            self.add("error", "grammar-b1plus", course, lid, f"{len(grammar)} focos; esperados 2")
        grammar_ids = [point.get("id") for point in grammar]
        if len(set(grammar_ids)) != len(grammar_ids):
            self.add("error", "grammar-duplicate", course, lid, str(grammar_ids))
        for index, point in enumerate(grammar):
            if not point.get("label") or not point.get("description"):
                self.add("error", "grammar-empty", course, lid, f"foco {index} sin label/description")

        if LEVEL_ORDER[level] >= 3:
            if not lesson.get("scenario"):
                self.add("error", "scenario-empty", course, lid, "B1+ sin scenario")
            if not lesson.get("canDo"):
                self.add("error", "cando-empty", course, lid, "B1+ sin canDo")
            if not lesson.get("vocabFrequencyBand"):
                self.add("error", "band-empty", course, lid, "B1+ sin banda léxica")
            if not lesson.get("culturalNotes"):
                self.add("error", "cultural-empty", course, lid, "B1+ sin nota cultural")
        if not lesson.get("microLesson"):
            self.add("error", "micro-empty", course, lid, "sin microLesson")
        glossary = lesson.get("glossary") or []
        if not glossary:
            self.add("error", "glossary-empty", course, lid, "sin glosario")
        for entry in glossary:
            if not entry.get("term") or not entry.get("translations"):
                self.add("error", "glossary-entry", course, lid, f"entrada inválida {entry}")
        glossary_terms = [entry.get("term") for entry in glossary]
        if len(set(glossary_terms)) != len(glossary_terms):
            self.add("warning", "glossary-duplicate", course, lid, str(glossary_terms))

        title = lesson.get("title", "")
        self.titles[course][title] += 1
        self.level_titles[(course, level_id)][title] += 1
        self.signatures[course][signature(lesson)].append(lid)

        for exercise in exercises:
            eid = exercise.get("id", "?")
            etype = exercise.get("type")
            self.totals[f"type:{etype}"] += 1
            if etype not in ALLOWED_TYPES[level]:
                self.add("error", "type-not-allowed", course, lid, f"{etype} no permitido en {level}", eid)
            if not exercise.get("feedbackOnError"):
                self.add("error", "feedback-missing", course, lid, "sin feedbackOnError", eid)
            self.audit_exercise(course, lesson, level, exercise, grammar_ids)

        for path, text in iter_strings(lesson):
            for marker in MOJIBAKE:
                if marker in text:
                    self.add("error", "mojibake", course, lid, f"{path}: {text[:80]}")
            if "\x00" in text or any(ord(ch) < 32 and ch not in "\n\t" for ch in text):
                self.add("error", "control-char", course, lid, path)
            if "  " in text:
                self.add("warning", "double-space", course, lid, f"{path}: {text[:80]}")
            if space_before_punct(text, course):
                self.add("warning", "space-before-punct", course, lid, f"{path}: {text[:80]}")
            if text != text.strip():
                self.add("warning", "outer-space", course, lid, path)
            if not is_ja(course) and etype_cjk(text):
                self.add("error", "script-cjk", course, lid, f"{path}: {text[:60]}")
            if is_ja(course) and path.split(".")[-1] in {
                "audioText", "transcription", "targetPhrase", "modelAnswer", "prompt",
            } and text and not CJK.search(text) and not re.search(r"[A-Za-z]", text):
                self.add("warning", "script-ja", course, lid, f"{path}: {text[:60]}")

    def audit_exercise(self, course: str, lesson: dict, level: str, exercise: dict, grammar_ids: list) -> None:
        lid = lesson.get("id", "?")
        eid = exercise.get("id", "?")
        etype = exercise.get("type")
        cap = TOKEN_CAPS[level]

        def check_tokens(text: str, label: str) -> None:
            longest = sentence_tokens(text, course)
            if longest > cap:
                self.add("error", "token-cap", course, lid, f"{label} {longest}>{cap}", eid)

        if etype in {"MultipleChoice", "KanaChoose", "KanjiReading", "ParticleFill", "ListenChoose", "AudioOnlyPrompt"}:
            options = exercise.get("options") or exercise.get("romajiOptions") or exercise.get("readingOptions") or exercise.get("particles") or []
            if len(options) < 2:
                self.add("error", "options-count", course, lid, f"{len(options)} opciones", eid)
            if len(set(options)) != len(options):
                self.add("error", "options-duplicate", course, lid, "opciones repetidas", eid)
            index = exercise.get("correctIndex")
            if index is None and etype == "KanaChoose":
                index = options.index(exercise.get("correctRomaji")) if exercise.get("correctRomaji") in options else -1
            if index is None and etype == "KanjiReading":
                index = options.index(exercise.get("correctReading")) if exercise.get("correctReading") in options else -1
            if index is None and etype == "ParticleFill":
                index = options.index(exercise.get("correctParticle")) if exercise.get("correctParticle") in options else -1
            if index is None or not (0 <= index < len(options)):
                self.add("error", "correct-index", course, lid, f"correctIndex={index}", eid)
            if etype == "ListenChoose":
                audio = exercise.get("audioText", "")
                if audio not in options:
                    if LEVEL_ORDER[level] <= 2:
                        self.add("error", "listen-not-in-options", course, lid, audio[:120], eid)
                    else:
                        self.add("warning", "listen-comprehension", course, lid, "opciones son paráfrasis", eid)
                elif 0 <= index < len(options) and options[index] != audio:
                    self.add(
                        "error",
                        "listen-key-mismatch",
                        course,
                        lid,
                        f"correctIndex={index} apunta a «{options[index]}»; audio=«{audio}»",
                        eid,
                    )
                check_tokens(audio, "audio")
            if etype == "AudioOnlyPrompt":
                check_tokens(exercise.get("audioText", ""), "audio")
                if not exercise.get("question"):
                    self.add("warning", "question-empty", course, lid, "sin pregunta", eid)

        if etype == "Translate":
            correct = exercise.get("correctTokens") or []
            distractors = exercise.get("distractorTokens") or []
            if not correct:
                self.add("error", "translate-empty", course, lid, "sin tokens", eid)
            if set(correct) & set(distractors):
                self.add("error", "distractor-overlap", course, lid, str(set(correct) & set(distractors)), eid)
            if any(not token.strip() for token in correct + distractors):
                self.add("error", "empty-token", course, lid, "token vacío", eid)
            check_tokens(" ".join(correct), "translate")
            if not exercise.get("sourcePhrase"):
                self.add("error", "source-empty", course, lid, "sin sourcePhrase", eid)

        if etype == "OrderWords":
            answer = exercise.get("answerTokens") or []
            provided = exercise.get("tokens") or []
            if not answer:
                self.add("error", "order-empty", course, lid, "sin answerTokens", eid)
            missing = Counter(answer) - Counter(provided)
            if missing:
                self.add("error", "order-missing-token", course, lid, f"faltan {dict(missing)}", eid)
            check_tokens(" ".join(answer), "order")

        if etype == "MatchPairs":
            pairs = exercise.get("pairs") or []
            if len(pairs) < 2:
                self.add("error", "pairs-count", course, lid, f"{len(pairs)} pares", eid)
            lefts = [pair.get("left") for pair in pairs]
            rights = [pair.get("right") for pair in pairs]
            if len(set(lefts)) != len(lefts) or len(set(rights)) != len(rights):
                self.add("error", "pairs-duplicate", course, lid, "pares repetidos", eid)
            if any(not left or not right for left, right in zip(lefts, rights)):
                self.add("error", "pairs-empty", course, lid, "par vacío", eid)
            for left, right in zip(lefts, rights):
                if left and right and left.casefold() == right.casefold():
                    self.add("warning", "pairs-identical", course, lid, f"{left} = {right}", eid)

        if etype == "ListenType":
            audio = exercise.get("audioText", "")
            transcription = exercise.get("transcription", "")
            if audio != transcription:
                self.add("error", "listen-type-mismatch", course, lid, "audio != transcription", eid)
            check_tokens(audio, "listen")

        if etype == "SpeakRepeat":
            audio = exercise.get("audioText")
            target = exercise.get("targetPhrase", "")
            if audio is not None and audio != target:
                self.add("error", "speak-mismatch", course, lid, "audio != target", eid)
            check_tokens(target, "speak")

        if etype == "ClozeDragDrop":
            passage = exercise.get("passage", "")
            blanks = exercise.get("blanks") or []
            placeholders = PLACEHOLDER.findall(passage)
            ids = [blank.get("id") for blank in blanks]
            if placeholders != ids:
                self.add("error", "cloze-placeholders", course, lid, f"{placeholders} != {ids}", eid)
            for blank in blanks:
                options = blank.get("options") or []
                if blank.get("answer") not in options:
                    self.add("error", "cloze-answer", course, lid, f"{blank.get('id')} answer ausente", eid)
                if len(set(options)) != len(options):
                    self.add("error", "cloze-options", course, lid, f"{blank.get('id')} opciones repetidas", eid)
                if not blank.get("answer"):
                    self.add("error", "cloze-empty", course, lid, f"{blank.get('id')} vacío", eid)
            check_tokens(passage.replace("{{", "").replace("}}", ""), "cloze")

        if etype == "SemanticMatch":
            pairs = exercise.get("pairs") or []
            if len(pairs) < 2:
                self.add("error", "semantic-count", course, lid, f"{len(pairs)} pares", eid)
            lefts = [pair.get("left") for pair in pairs]
            rights = [pair.get("right") for pair in pairs]
            if len(set(lefts)) != len(lefts) or len(set(rights)) != len(rights):
                self.add("error", "semantic-duplicate", course, lid, "pares repetidos", eid)
            for pair in pairs:
                if pair.get("register") not in REGISTERS:
                    self.add("error", "semantic-register", course, lid, str(pair.get("register")), eid)
                if not pair.get("left") or not pair.get("right"):
                    self.add("error", "semantic-empty", course, lid, "par vacío", eid)

        if etype == "FreeSpeakingPrompt":
            if not exercise.get("modelAnswer"):
                self.add("error", "free-model", course, lid, "sin modelAnswer", eid)
            min_tokens = exercise.get("minTokens") or 0
            model_tokens = token_count(exercise.get("modelAnswer", ""), course)
            if model_tokens < min_tokens:
                self.add(
                    "error",
                    "free-min-tokens",
                    course,
                    lid,
                    f"modelAnswer tiene {model_tokens} tokens; minTokens={min_tokens}",
                    eid,
                )
            clusters = exercise.get("conceptClusters") or []
            if not clusters or any(not cluster or any(not item for item in cluster) for cluster in clusters):
                self.add("error", "free-clusters", course, lid, "conceptClusters inválidos", eid)
            if not exercise.get("selfAssessmentCriteria"):
                self.add("error", "free-criteria", course, lid, "sin criterios", eid)
            refs = exercise.get("grammarRuleRefs") or []
            missing = [ref for ref in refs if ref not in grammar_ids]
            if missing:
                self.add("error", "free-rule-ref", course, lid, f"refs inexistentes {missing}", eid)
            check_tokens(exercise.get("modelAnswer", ""), "free")

    def finish(self, course: str, lessons: list[dict]) -> None:
        for (title_course, level_id), counter in self.level_titles.items():
            if title_course != course:
                continue
            for title, count in counter.items():
                if count > 1:
                    self.add(
                        "warning",
                        "duplicate-title-level",
                        course,
                        level_id,
                        f"«{title}» ×{count}",
                    )
        for title, count in self.titles[course].items():
            if count > 1:
                self.add("warning", "duplicate-title", course, "", f"«{title}» ×{count}")
        for digest, ids in self.signatures[course].items():
            if len(ids) > 1:
                self.add("error", "duplicate-lesson", course, ",".join(ids), "misma firma de ejercicios")
        sentences: Counter = Counter()
        for lesson in lessons:
            for exercise in lesson.get("exercises", []):
                etype = exercise.get("type")
                texts = []
                if etype in {"ListenType", "SpeakRepeat", "ListenChoose", "AudioOnlyPrompt"}:
                    texts.append(exercise.get("audioText", ""))
                elif etype == "Translate":
                    texts.append(" ".join(exercise.get("correctTokens", [])))
                elif etype == "OrderWords":
                    texts.append(" ".join(exercise.get("answerTokens", [])))
                for text in texts:
                    normalized = re.sub(r"\s+", " ", text).strip().lower()
                    if normalized:
                        sentences[normalized] += 1
        self.duplicate_sentences[course] = sentences


def etype_cjk(text: str) -> bool:
    return bool(CJK.search(text))


def space_before_punct(text: str, course: str) -> bool:
    """Detecta espacios tipográficos indebidos, sin falsos positivos.

    En francés es correcto escribir espacio antes de ! ? : ; (y antes de »).
    Los puntos suspensivos con espacios (" ... ") tampoco son un error.
    """
    for match in re.finditer(r" ([.,;:!?])", text):
        char = match.group(1)
        if char == "." and text[match.start() + 1 : match.start() + 4] == "...":
            continue
        if course == "es-fr" and char in "!?:;":
            continue
        return True
    return False


def main() -> None:
    audit = Audit()
    per_course = {}
    for course, path in COURSES.items():
        data = json.loads(path.read_text(encoding="utf-8"))
        levels = data["levels"]
        rank = -1
        lessons_all = []
        summary = {}
        for level in levels:
            level_lessons = [lesson for unit in level["units"] for lesson in unit["lessons"]]
            if not level_lessons:
                continue
            cefr = level_lessons[0].get("cefrLevel")
            if LEVEL_ORDER[cefr] < rank:
                audit.add("error", "ramp-backwards", course, level["id"], cefr)
            rank = LEVEL_ORDER[cefr]
            for lesson in level_lessons:
                audit.audit_lesson(course, lesson, level["id"], cefr)
            lessons_all.extend(level_lessons)
            summary[cefr] = len(level_lessons)
        audit.finish(course, lessons_all)
        per_course[course] = summary
        print(f"{course}: {summary} · {len(lessons_all)} lecciones")

    # Duplicados de frase por curso
    duplicate_sentences = {}
    for course, counter in audit.duplicate_sentences.items():
        repeated = {text: count for text, count in counter.items() if count > 1}
        duplicate_sentences[course] = {
            "repeated_groups": len(repeated),
            "repeated_uses": sum(repeated.values()),
            "top": sorted(repeated.items(), key=lambda item: -item[1])[:20],
        }

    by_code = Counter(issue["code"] for issue in audit.issues)
    by_severity = Counter(issue["severity"] for issue in audit.issues)
    report = {
        "totals": dict(audit.totals),
        "per_course": per_course,
        "issues_by_severity": dict(by_severity),
        "issues_by_code": dict(by_code),
        "duplicate_sentences": duplicate_sentences,
        "issues": audit.issues,
    }
    out = ROOT / "build" / "reports" / "audit-all.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print("\nSeveridad:", dict(by_severity))
    print("Códigos:", dict(by_code))
    print("\nPrimeros errores:")
    for issue in [item for item in audit.issues if item["severity"] == "error"][:25]:
        print(f"  {issue['course']} {issue['lesson_id']} {issue['code']}: {issue['detail']}")
    print(f"\nInforme: {out}")


if __name__ == "__main__":
    main()
