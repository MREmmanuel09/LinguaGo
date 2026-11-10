"""Genera el bloque piloto A1 de ES→EN (F0, ADR-052).

Uso:
    python tools/content/pilot_a1.py

Inserta una unidad de 10 lecciones al final del nivel A1 de es-en sin
reformatear el resto del course.json. Es idempotente: si la unidad ya existe,
no hace nada.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-a1-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-en-lv-a2"'

LESSONS = [
    {
        "slug": "mascotas",
        "title": "Mi mascota",
        "topic": "las mascotas",
        "pairs": [("perro", "dog"), ("gato", "cat"), ("pájaro", "bird")],
        "translate": ("Mi perro es pequeño", ["My", "dog", "is", "small"], ["cat", "big"]),
        "order": ("El gato duerme en casa", ["The", "cat", "sleeps", "at", "home"]),
        "listen_choose": (
            "I have a cat",
            ["I have a cat", "I have a dog", "I have a bird"],
        ),
        "listen_type": "My dog is friendly",
        "speak": "I have a pet",
        "can_do": "Puedo hablar de mi mascota con frases sencillas.",
    },
    {
        "slug": "ropa",
        "title": "La ropa",
        "topic": "la ropa",
        "pairs": [("camisa", "shirt"), ("zapatos", "shoes"), ("abrigo", "coat")],
        "translate": ("Mi camisa es azul", ["My", "shirt", "is", "blue"], ["red", "coat"]),
        "order": ("El abrigo es nuevo", ["The", "coat", "is", "new"]),
        "listen_choose": (
            "I wear a coat",
            ["I wear a coat", "I wear a shirt", "I wear shoes"],
        ),
        "listen_type": "My shoes are black",
        "speak": "I wear a blue shirt",
        "can_do": "Puedo nombrar ropa y describir sus colores.",
    },
    {
        "slug": "rutina",
        "title": "Mi rutina",
        "topic": "la rutina diaria",
        "pairs": [("despertarse", "wake up"), ("vestirse", "get dressed"), ("acostarse", "go to bed")],
        "translate": ("Me levanto a las siete", ["I", "wake", "up", "at", "seven"], ["eight", "sleep"]),
        "order": ("Desayuno a las ocho", ["I", "have", "breakfast", "at", "eight"]),
        "listen_choose": (
            "I go to bed at ten",
            ["I go to bed at ten", "I wake up at seven", "I have breakfast at eight"],
        ),
        "listen_type": "I get dressed in the morning",
        "speak": "I wake up at seven",
        "can_do": "Puedo describir mi rutina diaria con horas sencillas.",
    },
    {
        "slug": "transporte",
        "title": "El transporte",
        "topic": "el transporte",
        "pairs": [("autobús", "bus"), ("tren", "train"), ("bicicleta", "bicycle")],
        "translate": ("Voy en autobús", ["I", "go", "by", "bus"], ["train", "bicycle"]),
        "order": ("La bicicleta es rápida", ["The", "bicycle", "is", "fast"]),
        "listen_choose": (
            "I take the train",
            ["I take the train", "I take the bus", "I ride a bicycle"],
        ),
        "listen_type": "The bus is late",
        "speak": "I go by bus",
        "can_do": "Puedo decir cómo me desplazo por la ciudad.",
    },
    {
        "slug": "direcciones",
        "title": "Direcciones",
        "topic": "las direcciones",
        "pairs": [("izquierda", "left"), ("derecha", "right"), ("calle", "street")],
        "translate": ("La calle está a la derecha", ["The", "street", "is", "on", "the", "right"], ["left", "park"]),
        "order": ("Gira a la izquierda", ["Turn", "left"]),
        "listen_choose": (
            "The bank is on the left",
            ["The bank is on the left", "The bank is on the right", "The bank is near the park"],
        ),
        "listen_type": "Go straight",
        "speak": "Turn right",
        "can_do": "Puedo pedir y dar direcciones básicas.",
    },
    {
        "slug": "colegio",
        "title": "El colegio",
        "topic": "el colegio",
        "pairs": [("escuela", "school"), ("libro", "book"), ("profesor", "teacher")],
        "translate": ("Mi libro es nuevo", ["My", "book", "is", "new"], ["old", "school"]),
        "order": ("El profesor es amable", ["The", "teacher", "is", "kind"]),
        "listen_choose": (
            "I go to school",
            ["I go to school", "I read a book", "The teacher is kind"],
        ),
        "listen_type": "My school is big",
        "speak": "I have a book",
        "can_do": "Puedo hablar de objetos y personas del colegio.",
    },
    {
        "slug": "trabajo",
        "title": "El trabajo",
        "topic": "el trabajo",
        "pairs": [("oficina", "office"), ("jefe", "boss"), ("reunión", "meeting")],
        "translate": ("Mi jefe está en la oficina", ["My", "boss", "is", "in", "the", "office"], ["meeting", "home"]),
        "order": ("La reunión es a las diez", ["The", "meeting", "is", "at", "ten"]),
        "listen_choose": (
            "I work in an office",
            ["I work in an office", "The meeting is at ten", "My boss is busy"],
        ),
        "listen_type": "My boss is busy",
        "speak": "I work in an office",
        "can_do": "Puedo presentar mi trabajo y hablar de una reunión.",
    },
    {
        "slug": "salud",
        "title": "La salud",
        "topic": "la salud",
        "pairs": [("médico", "doctor"), ("farmacia", "pharmacy"), ("dolor", "pain")],
        "translate": ("Me duele la cabeza", ["My", "head", "hurts"], ["stomach", "doctor"]),
        "order": ("Necesito un médico", ["I", "need", "a", "doctor"]),
        "listen_choose": (
            "I have a headache",
            ["I have a headache", "I need a doctor", "The pharmacy is open"],
        ),
        "listen_type": "The pharmacy is open",
        "speak": "I need a doctor",
        "can_do": "Puedo explicar un problema de salud sencillo.",
    },
    {
        "slug": "pasatiempos",
        "title": "Pasatiempos",
        "topic": "los pasatiempos",
        "pairs": [("música", "music"), ("deporte", "sport"), ("película", "movie")],
        "translate": ("Me gusta la música", ["I", "like", "music"], ["sport", "movie"]),
        "order": ("El deporte es divertido", ["Sport", "is", "fun"]),
        "listen_choose": (
            "I watch a movie",
            ["I watch a movie", "I listen to music", "I play a sport"],
        ),
        "listen_type": "I listen to music",
        "speak": "I like music",
        "can_do": "Puedo hablar de lo que me gusta hacer en mi tiempo libre.",
    },
    {
        "slug": "ciudad",
        "title": "La ciudad",
        "topic": "la ciudad",
        "pairs": [("parque", "park"), ("museo", "museum"), ("restaurante", "restaurant")],
        "translate": ("El parque es grande", ["The", "park", "is", "big"], ["small", "museum"]),
        "order": ("El restaurante está cerca", ["The", "restaurant", "is", "near"]),
        "listen_choose": (
            "The museum is open",
            ["The museum is open", "The park is beautiful", "The restaurant is near"],
        ),
        "listen_type": "The park is beautiful",
        "speak": "I go to the park",
        "can_do": "Puedo hablar de lugares de mi ciudad.",
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-a1-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    mcq_options = [target for _, target in spec["pairs"]]
    examples = [
        {"source": spec["translate"][0], "target": sentence},
        {"source": spec["order"][0], "target": order_sentence},
    ]
    lesson_id = f"es-en-a1-p0-u1-l{index:02d}"
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": "Palabras y frases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase: sujeto + verbo + complemento.",
                    "examples": examples,
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
                "id": exercise_id(index, 1),
                "question": f"¿Cómo se dice «{first_source}»?",
                "options": mcq_options,
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_source}» se dice «{first_target}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": exercise_id(index, 2),
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
                "id": exercise_id(index, 3),
                "tokens": spec["order"][1],
                "answerTokens": spec["order"][1],
                "sourcePhrase": spec["order"][0],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{order_sentence}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": exercise_id(index, 4),
                "pairs": [
                    {"left": source, "right": target}
                    for source, target in spec["pairs"]
                ],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": exercise_id(index, 5),
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
                "id": exercise_id(index, 6),
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
                "id": exercise_id(index, 7),
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


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def main() -> None:
    course_text = COURSE.read_text(encoding="utf-8")
    if UNIT_ID in course_text:
        print(f"El bloque {UNIT_ID} ya existe; no se modifica.")
        return

    unit = {
        "id": UNIT_ID,
        "title": "Piloto A1 · Vida diaria",
        "lessons": [build_lesson(index, spec) for index, spec in enumerate(LESSONS, start=1)],
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel A1 en course.json")
    course_text = course_text.replace(
        MARKER,
        ",\n" + unit_text + MARKER,
        1,
    )
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(LESSONS)} lecciones.")


if __name__ == "__main__":
    main()
