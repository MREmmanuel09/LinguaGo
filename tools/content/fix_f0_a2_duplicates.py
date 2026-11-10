"""Sustituye las lecciones A2 duplicadas de las réplicas FR/JA por contenido nuevo."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

COURSES = [
    {
        "course": "es-fr",
        "path": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
        "unit": "es-fr-a2-p0-un-1",
        "language": "francés",
        "lessons": [
            {
                "index": 9,
                "title": "Au marché",
                "topic": "le marché",
                "pairs": [("mercado", "marché"), ("fruta", "fruits"), ("precio", "prix")],
                "translate": ("Compro fruta en el mercado", ["J'achète", "des", "fruits", "au", "marché"], ["légumes", "magasin"]),
                "order": ("El mercado abre a las ocho", ["Le", "marché", "ouvre", "à", "huit", "heures"]),
                "listen_choose": (
                    "J'achète des fruits au marché",
                    ["J'achète des fruits au marché", "J'achète des légumes au marché", "Le marché ouvre à huit heures"],
                ),
                "listen_type": "Le marché ouvre à huit heures",
                "speak": "J'achète des fruits au marché",
                "can_do": "Puedo comprar en un mercado y preguntar el precio.",
            },
            {
                "index": 10,
                "title": "À la gare",
                "topic": "la gare",
                "pairs": [("estación", "gare"), ("billete", "billet"), ("andén", "quai")],
                "translate": ("Quiero un billete para París", ["Je", "veux", "un", "billet", "pour", "Paris"], ["gare", "quai"]),
                "order": ("El tren sale del andén número tres", ["Le", "train", "part", "du", "quai", "numéro", "trois"]),
                "listen_choose": (
                    "Je veux un billet pour Paris",
                    ["Je veux un billet pour Paris", "Le train part du quai numéro trois", "Je cherche la gare"],
                ),
                "listen_type": "Le train part du quai numéro trois",
                "speak": "Je veux un billet pour Paris",
                "can_do": "Puedo comprar un billete y preguntar por el andén.",
            },
        ],
    },
    {
        "course": "es-ja",
        "path": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
        "unit": "es-ja-a2-p0-un-1",
        "language": "japonés",
        "lessons": [
            {
                "index": 9,
                "title": "レストランで",
                "topic": "el restaurante",
                "pairs": [("menú", "メニュー"), ("agua", "水"), ("cuenta", "お会計")],
                "translate": ("Quiero el menú, por favor", ["メニュー", "を", "お願い", "します"], ["水", "会計"]),
                "order": ("El agua es gratis", ["水", "は", "無料", "です"]),
                "listen_choose": (
                    "メニュー を お願い します",
                    ["メニュー を お願い します", "水 を お願い します", "お会計 を お願い します"],
                ),
                "listen_type": "お会計 は どこ です か",
                "speak": "メニュー を お願い します",
                "can_do": "Puedo pedir el menú, agua y la cuenta en un restaurante.",
            },
            {
                "index": 10,
                "title": "天気と季節",
                "topic": "el tiempo y las estaciones",
                "pairs": [("primavera", "春"), ("verano", "夏"), ("lluvia", "雨")],
                "translate": ("En primavera hace calor", ["春", "は", "暖かい", "です"], ["夏", "雨"]),
                "order": ("En verano llueve mucho", ["夏", "は", "雨", "が", "多い", "です"]),
                "listen_choose": (
                    "春 は 暖かい です",
                    ["春 は 暖かい です", "夏 は 雨 が 多い です", "冬 は 寒い です"],
                ),
                "listen_type": "夏 は 雨 が 多い です",
                "speak": "春 は 暖かい です",
                "can_do": "Puedo hablar del tiempo y de las estaciones.",
            },
        ],
    },
]


def exercise_id(lesson_id: str, suffix: int) -> str:
    return f"{lesson_id}-e{suffix:02d}"


def build_lesson(course: str, language: str, spec: dict) -> dict:
    lesson_id = f"{course}-a2-p0-u1-l{spec['index']:02d}"
    first_source, first_target = spec["pairs"][0]
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    answer = spec["order"][1]
    rotated = answer[1:] + answer[:1] if len(answer) > 1 else answer
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A2",
        "vocabFrequencyBand": "1000-2000",
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en {language}.",
            ],
            "sections": [
                {
                    "heading": "Palabras y frases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las preposiciones.",
                    "examples": [
                        {"source": spec["translate"][0], "target": sentence},
                        {"source": spec["order"][0], "target": order_sentence},
                    ],
                }
            ],
        },
        "glossary": [
            {"term": target, "translations": [source]}
            for source, target in spec["pairs"]
        ],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": exercise_id(lesson_id, 1),
                "question": f"¿Cómo se dice «{first_source}» en {language}?",
                "options": [target for _, target in spec["pairs"]],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_source}» se dice «{first_target}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": exercise_id(lesson_id, 2),
                "sourcePhrase": spec["translate"][0],
                "correctTokens": spec["translate"][1],
                "distractorTokens": spec["translate"][2],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"El orden correcto es «{sentence}».",
                },
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": exercise_id(lesson_id, 3),
                "tokens": rotated,
                "answerTokens": answer,
                "sourcePhrase": spec["order"][0],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{order_sentence}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": exercise_id(lesson_id, 4),
                "pairs": [{"left": source, "right": target} for source, target in spec["pairs"]],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": exercise_id(lesson_id, 5),
                "audioText": spec["listen_choose"][0],
                "options": spec["listen_choose"][1],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "comprehension",
                    "explanation": f"El audio dice «{spec['listen_choose'][0]}».",
                },
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": exercise_id(lesson_id, 6),
                "audioText": spec["listen_type"],
                "transcription": spec["listen_type"],
                "feedbackOnError": {
                    "type": "spelling",
                    "explanation": f"Escribe exactamente «{spec['listen_type']}».",
                },
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": exercise_id(lesson_id, 7),
                "targetPhrase": spec["speak"],
                "audioText": spec["speak"],
                "feedbackOnError": {
                    "type": "pronunciation",
                    "explanation": f"Repite «{spec['speak']}» con claridad.",
                },
                "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
            },
        ],
    }


def find_object_bounds(text: str, id_value: str) -> tuple[int, int]:
    token = f'"id": "{id_value}"'
    position = text.index(token)
    start = text.rfind("{", 0, position)
    depth = 0
    in_string = False
    escaped = False
    for index in range(start, len(text)):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return start, index + 1
    raise SystemExit(f"No se encontró el cierre del objeto {id_value}")


def replace_lesson(text: str, lesson_id: str, lesson: dict) -> str:
    start, end = find_object_bounds(text, lesson_id)
    line_start = text.rfind("\n", 0, start) + 1
    indent = len(text[line_start:start])
    formatted = json.dumps(lesson, ensure_ascii=False, indent=2)
    indented = "\n".join((" " * indent) + line for line in formatted.splitlines())
    return text[:start] + indented + text[end:]


def main() -> None:
    for config in COURSES:
        path: Path = config["path"]
        text = path.read_text(encoding="utf-8")
        replaced = 0
        for spec in config["lessons"]:
            lesson_id = f"{config['course']}-a2-p0-u1-l{spec['index']:02d}"
            lesson = build_lesson(config["course"], config["language"], spec)
            lesson["id"] = lesson_id
            for number, exercise in enumerate(lesson["exercises"], start=1):
                exercise["id"] = exercise_id(lesson_id, number)
            text = replace_lesson(text, lesson_id, lesson)
            replaced += 1
        if replaced != 2:
            raise SystemExit(f"{config['course']}: se esperaban 2 reemplazos, hay {replaced}")
        path.write_text(text, encoding="utf-8", newline="\n")
        json.loads(path.read_text(encoding="utf-8"))
        print(f"{config['course']}: {replaced} lecciones A2 sustituidas.")


if __name__ == "__main__":
    main()
