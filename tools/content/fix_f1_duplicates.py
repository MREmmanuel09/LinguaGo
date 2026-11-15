"""Sustituye las 5 lecciones duplicadas de F1 en cada curso."""

from __future__ import annotations

import importlib.util
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC_F1_1 = importlib.util.spec_from_file_location("f1_1", ROOT / "tools/content/f1_1_a1_es_en.py")
SPEC_F1_2 = importlib.util.spec_from_file_location("f1_2", ROOT / "tools/content/f1_2_a1_es_pt.py")
SPEC_F1_3 = importlib.util.spec_from_file_location("f1_3", ROOT / "tools/content/f1_3_a1_es_fr.py")
SPEC_F1_4 = importlib.util.spec_from_file_location("f1_4", ROOT / "tools/content/f1_4_a1_es_ja.py")
F1_1 = importlib.util.module_from_spec(SPEC_F1_1)
F1_1.__spec__.loader.exec_module(F1_1)
F1_2 = importlib.util.module_from_spec(SPEC_F1_2)
F1_2.__spec__.loader.exec_module(F1_2)
F1_3 = importlib.util.module_from_spec(SPEC_F1_3)
F1_3.__spec__.loader.exec_module(F1_3)
F1_4 = importlib.util.module_from_spec(SPEC_F1_4)
F1_4.__spec__.loader.exec_module(F1_4)
FIX_A2 = importlib.util.spec_from_file_location("fix_a2", ROOT / "tools/content/fix_f0_a2_duplicates.py")
FIX = importlib.util.module_from_spec(FIX_A2)
FIX_A2.loader.exec_module(FIX)

COURSES = {
    "es-en": {
        "path": ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json",
        "replacements": [
            ("es-en-a1-u09-l05", "En el aeropuerto", "el aeropuerto", "place", [("el aeropuerto", "the airport"), ("la terminal", "the terminal"), ("la sala de espera", "the waiting room")]),
            ("es-en-a1-u10-l04", "Más deporte", "el deporte", "sport", [("al voleibol", "volleyball"), ("al bádminton", "badminton"), ("al rugby", "rugby")]),
            ("es-en-a1-u14-l07", "Más invitaciones", "las invitaciones", "invitation", [("ir al cine", "go to the cinema"), ("tomar un café", "have a coffee"), ("hacer una excursión", "go on a trip")]),
            ("es-en-a1-u14-l09", "Dudas y preguntas", "las dudas", "problem", [("una duda", "a doubt"), ("una pregunta", "a question"), ("una idea", "an idea")]),
            ("es-en-a1-u14-l10", "Resultados", "los resultados", "problem", [("un examen", "an exam"), ("una nota", "a grade"), ("un resultado", "a result")]),
        ],
    },
    "es-pt": {
        "path": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
        "replacements": [
            ("es-pt-a1-u09-l05", "No aeroporto", "o aeroporto", "place", [("el aeropuerto", "o aeroporto"), ("la terminal", "o terminal"), ("la sala de espera", "a sala de espera")]),
            ("es-pt-a1-u10-l04", "Mais esporte", "o esporte", "sport", [("al voleibol", "vôlei"), ("al bádminton", "badminton"), ("al rugby", "rúgbi")]),
            ("es-pt-a1-u14-l07", "Mais convites", "os convites", "invitation", [("ir al cine", "ir ao cinema"), ("tomar un café", "tomar um café"), ("hacer una excursión", "fazer uma excursão")]),
            ("es-pt-a1-u14-l09", "Dúvidas e perguntas", "as dúvidas", "problem", [("una duda", "uma dúvida"), ("una pregunta", "uma pergunta"), ("una idea", "uma ideia")]),
            ("es-pt-a1-u14-l10", "Resultados", "os resultados", "problem", [("un examen", "um exame"), ("una nota", "uma nota"), ("un resultado", "um resultado")]),
        ],
    },
    "es-fr": {
        "path": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
        "replacements": [
            ("es-fr-a1-u09-l05", "À l'aéroport", "l'aéroport", "place", [("el aeropuerto", "l'aéroport"), ("la terminal", "le terminal"), ("la sala de espera", "la salle d'attente")]),
            ("es-fr-a1-u10-l04", "Plus de sport", "le sport", "sport", [("al voleibol", "au volley"), ("al bádminton", "au badminton"), ("al rugby", "au rugby")]),
            ("es-fr-a1-u14-l07", "Plus d'invitations", "les invitations", "invitation", [("ir al cine", "aller au cinéma"), ("tomar un café", "prendre un café"), ("hacer una excursión", "faire une excursion")]),
            ("es-fr-a1-u14-l09", "Doutes et questions", "les doutes", "problem", [("una duda", "un doute"), ("una pregunta", "une question"), ("una idea", "une idée")]),
            ("es-fr-a1-u14-l10", "Résultats", "les résultats", "problem", [("un examen", "un examen"), ("una nota", "une note"), ("un resultado", "un résultat")]),
        ],
    },
    "es-ja": {
        "path": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
        "replacements": [
            ("es-ja-a1-u09-l05", "空港で", "空港", "place", [("el aeropuerto", "空港"), ("la terminal", "ターミナル"), ("la sala de espera", "待合室")]),
            ("es-ja-a1-u10-l04", "もっとスポーツ", "スポーツ", "sport", [("al voleibol", "バレーボール"), ("al bádminton", "バドミントン"), ("al rugby", "ラグビー")]),
            ("es-ja-a1-u14-l07", "もっと誘い", "誘い", "invitation", [("ir al cine", "映画 に 行き"), ("tomar un café", "コーヒー を 飲み"), ("hacer una excursión", "遠足 に 行き")]),
            ("es-ja-a1-u14-l09", "疑問と質問", "疑問", "problem", [("una duda", "疑問"), ("una pregunta", "質問"), ("una idea", "考え")]),
            ("es-ja-a1-u14-l10", "結果", "結果", "problem", [("un examen", "試験"), ("una nota", "成績"), ("un resultado", "結果")]),
        ],
    },
}


def tokenize(course_id: str, text: str) -> list[str]:
    if course_id == "es-ja":
        return [token for token in text.split() if token]
    return F1_1.tokens(text)


def target_sentences(course_id: str, pattern: str, items: list[tuple[str, str]]) -> list[str]:
    if course_id == "es-en":
        return [pair[1] for pair in F1_1.sentences_for(pattern, items)]
    if course_id == "es-pt":
        return F1_2.pt_sentences(pattern, items)
    if course_id == "es-fr":
        return F1_3.fr_sentences(pattern, items)
    return F1_4.ja_sentences(pattern, items)


def safe_distractors(correct: list[str], candidates: list[list[str]], course_id: str) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    fallbacks = ["muito", "hoje", "aqui", "agora", "também", "sempre"] if course_id == "es-pt" else ["very", "today", "here", "now", "also", "too"]
    if course_id == "es-fr":
        fallbacks = ["très", "aujourd'hui", "ici", "maintenant", "aussi", "toujours"]
    if course_id == "es-ja":
        fallbacks = ["とても", "今日", "ここ", "今", "も", "いつも"]
    for fallback in fallbacks:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def build_lesson(course_id: str, lesson_id: str, title: str, topic: str, pattern: str, items: list[tuple[str, str]]) -> dict:
    es_sentences = [pair[0] for pair in F1_1.sentences_for(pattern, [(es, es) for es, _ in items])]
    targets = target_sentences(course_id, pattern, items)
    target_tokens = [tokenize(course_id, target) for target in targets]
    first_es, first_target = items[0]
    return {
        "id": lesson_id,
        "title": title,
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": f"Puedo usar frases sencillas sobre {topic}.",
        "microLesson": {
            "title": title,
            "goals": [f"Reconocer vocabulario de {topic}.", f"Usar frases sencillas sobre {topic}."],
            "sections": [
                {
                    "heading": "Palabras y frases",
                    "body": f"Vocabulario básico de {topic}. Fíjate en el orden de la frase y en las palabras nuevas.",
                    "examples": [
                        {"source": es_sentences[0], "target": targets[0]},
                        {"source": es_sentences[1], "target": targets[1]},
                    ],
                }
            ],
        },
        "glossary": [{"term": target, "translations": [es]} for es, target in items],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}»?",
                "options": [target for _, target in items],
                "correctIndex": 0,
                "feedbackOnError": {"type": "vocabulary", "explanation": f"«{first_es}» se dice «{first_target}»."},
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": es_sentences[0],
                "correctTokens": target_tokens[0],
                "distractorTokens": safe_distractors(target_tokens[0], [target_tokens[1], target_tokens[2]], course_id),
                "feedbackOnError": {"type": "word_order", "explanation": f"El orden correcto es «{' '.join(target_tokens[0])}»."},
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (target_tokens[1][1:] + target_tokens[1][:1]) if len(target_tokens[1]) > 1 else target_tokens[1],
                "answerTokens": target_tokens[1],
                "sourcePhrase": es_sentences[1],
                "feedbackOnError": {"type": "word_order", "explanation": f"La frase correcta es «{' '.join(target_tokens[1])}»."},
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [{"left": es, "right": target} for es, target in items],
                "feedbackOnError": {"type": "vocabulary", "explanation": "Cada palabra tiene una traducción distinta."},
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": targets[2],
                "options": targets,
                "correctIndex": 0,
                "feedbackOnError": {"type": "comprehension", "explanation": f"El audio dice «{targets[2]}»."},
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": targets[2],
                "transcription": targets[2],
                "feedbackOnError": {"type": "spelling", "explanation": f"Escribe exactamente «{targets[2]}»."},
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": targets[2],
                "audioText": targets[2],
                "feedbackOnError": {"type": "pronunciation", "explanation": f"Repite «{targets[2]}» con claridad."},
                "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
            },
        ],
    }


def main() -> None:
    for course_id, config in COURSES.items():
        path: Path = config["path"]
        text = path.read_text(encoding="utf-8")
        for lesson_id, title, topic, pattern, items in config["replacements"]:
            lesson = build_lesson(course_id, lesson_id, title, topic, pattern, items)
            text = FIX.replace_lesson(text, lesson_id, lesson)
        path.write_text(text, encoding="utf-8", newline="\n")
        json.loads(path.read_text(encoding="utf-8"))
        print(f"{course_id}: {len(config['replacements'])} lecciones sustituidas.")


if __name__ == "__main__":
    main()
