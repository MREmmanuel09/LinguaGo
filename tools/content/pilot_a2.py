"""Genera el bloque piloto A2 de ES→EN (F0, ADR-052).

Inserta una unidad de 10 lecciones al final del nivel A2 de es-en sin
reformatear el resto del course.json. Es idempotente.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-a2-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-en-lv-2"'

LESSONS = [
    {
        "slug": "supermercado",
        "title": "En el supermercado",
        "topic": "la compra diaria",
        "pairs": [("carrito", "shopping cart"), ("caja", "checkout"), ("oferta", "offer")],
        "translate": ("Necesito un carrito", ["I", "need", "a", "shopping", "cart"], ["checkout", "offer"]),
        "order": ("La caja está al final", ["The", "checkout", "is", "at", "the", "end"]),
        "listen_choose": (
            "The milk is on offer",
            ["The milk is on offer", "I need a shopping cart", "The checkout is at the end"],
        ),
        "listen_type": "The milk is on offer",
        "speak": "I need a shopping cart",
        "can_do": "Puedo resolver una compra sencilla en el supermercado.",
    },
    {
        "slug": "banco",
        "title": "En el banco",
        "topic": "los trámites bancarios",
        "pairs": [("cuenta", "account"), ("tarjeta", "card"), ("efectivo", "cash")],
        "translate": ("Quiero abrir una cuenta", ["I", "want", "to", "open", "an", "account"], ["card", "cash"]),
        "order": ("Pago con tarjeta", ["I", "pay", "by", "card"]),
        "listen_choose": (
            "I need some cash",
            ["I need some cash", "I pay by card", "I want to open an account"],
        ),
        "listen_type": "I need some cash",
        "speak": "I want to open an account",
        "can_do": "Puedo pedir información básica en un banco.",
    },
    {
        "slug": "mudanza",
        "title": "Una mudanza",
        "topic": "una mudanza",
        "pairs": [("piso", "flat"), ("caja", "box"), ("mudanza", "move")],
        "translate": ("La mudanza es el sábado", ["The", "move", "is", "on", "Saturday"], ["flat", "box"]),
        "order": ("Las cajas están en el piso", ["The", "boxes", "are", "in", "the", "flat"]),
        "listen_choose": (
            "I have many boxes",
            ["I have many boxes", "The move is on Saturday", "The boxes are in the flat"],
        ),
        "listen_type": "I have many boxes",
        "speak": "The move is on Saturday",
        "can_do": "Puedo hablar de una mudanza y de mis cajas.",
    },
    {
        "slug": "gimnasio",
        "title": "El gimnasio",
        "topic": "el ejercicio",
        "pairs": [("gimnasio", "gym"), ("entrenador", "trainer"), ("cansado", "tired")],
        "translate": ("Estoy cansado después de entrenar", ["I", "am", "tired", "after", "training"], ["gym", "trainer"]),
        "order": ("El gimnasio abre temprano", ["The", "gym", "opens", "early"]),
        "listen_choose": (
            "I go to the gym",
            ["I go to the gym", "I am tired after training", "The gym opens early"],
        ),
        "listen_type": "I go to the gym",
        "speak": "I am tired after training",
        "can_do": "Puedo hablar de mi gimnasio y de cómo me siento.",
    },
    {
        "slug": "turismo",
        "title": "Una visita turística",
        "topic": "el turismo",
        "pairs": [("mapa", "map"), ("monumento", "monument"), ("turista", "tourist")],
        "translate": ("El monumento está cerca", ["The", "monument", "is", "near"], ["map", "tourist"]),
        "order": ("Necesito un mapa", ["I", "need", "a", "map"]),
        "listen_choose": (
            "The tourist has a map",
            ["The tourist has a map", "The monument is near", "I need a map"],
        ),
        "listen_type": "The tourist has a map",
        "speak": "I need a map",
        "can_do": "Puedo orientarme en una visita turística sencilla.",
    },
    {
        "slug": "cumpleanos",
        "title": "Un cumpleaños",
        "topic": "una celebración",
        "pairs": [("cumpleaños", "birthday"), ("regalo", "gift"), ("invitado", "guest")],
        "translate": ("Mi cumpleaños es mañana", ["My", "birthday", "is", "tomorrow"], ["gift", "guest"]),
        "order": ("Tengo un regalo para ti", ["I", "have", "a", "gift", "for", "you"]),
        "listen_choose": (
            "The guests arrive at eight",
            ["The guests arrive at eight", "My birthday is tomorrow", "I have a gift for you"],
        ),
        "listen_type": "The guests arrive at eight",
        "speak": "My birthday is tomorrow",
        "can_do": "Puedo invitar y hablar de una celebración.",
    },
    {
        "slug": "coche",
        "title": "El coche",
        "topic": "el coche y la gasolina",
        "pairs": [("coche", "car"), ("gasolina", "petrol"), ("aparcar", "park")],
        "translate": ("Necesito gasolina", ["I", "need", "petrol"], ["car", "park"]),
        "order": ("Puedo aparcar aquí", ["I", "can", "park", "here"]),
        "listen_choose": (
            "The car is in the garage",
            ["The car is in the garage", "I need petrol", "I can park here"],
        ),
        "listen_type": "The car is in the garage",
        "speak": "I need petrol",
        "can_do": "Puedo hablar de mi coche y de dónde aparcar.",
    },
    {
        "slug": "oficina",
        "title": "La oficina",
        "topic": "el trabajo de oficina",
        "pairs": [("correo", "email"), ("proyecto", "project"), ("plazo", "deadline")],
        "translate": ("El proyecto termina el viernes", ["The", "project", "ends", "on", "Friday"], ["email", "deadline"]),
        "order": ("Envío el correo hoy", ["I", "send", "the", "email", "today"]),
        "listen_choose": (
            "The deadline is on Friday",
            ["The deadline is on Friday", "I send the email today", "The project ends on Friday"],
        ),
        "listen_type": "The deadline is on Friday",
        "speak": "I send the email today",
        "can_do": "Puedo hablar de tareas y plazos de oficina.",
    },
    {
        "slug": "tiempo-libre",
        "title": "Tiempo libre",
        "topic": "los planes de ocio",
        "pairs": [("quedar", "meet up"), ("cine", "cinema"), ("entrada", "ticket")],
        "translate": ("¿Quedamos el sábado?", ["Shall", "we", "meet", "on", "Saturday"], ["cinema", "ticket"]),
        "order": ("Compro dos entradas", ["I", "buy", "two", "tickets"]),
        "listen_choose": (
            "We meet at the cinema",
            ["We meet at the cinema", "Shall we meet on Saturday", "I buy two tickets"],
        ),
        "listen_type": "We meet at the cinema",
        "speak": "Shall we meet on Saturday?",
        "can_do": "Puedo proponer un plan de ocio y comprar entradas.",
    },
    {
        "slug": "tramites",
        "title": "Un trámite",
        "topic": "los trámites y las citas",
        "pairs": [("trámite", "procedure"), ("firma", "signature"), ("cita", "appointment")],
        "translate": ("Necesito una cita", ["I", "need", "an", "appointment"], ["procedure", "signature"]),
        "order": ("Firmo el documento hoy", ["I", "sign", "the", "document", "today"]),
        "listen_choose": (
            "The appointment is at nine",
            ["The appointment is at nine", "I need an appointment", "I sign the document today"],
        ),
        "listen_type": "The appointment is at nine",
        "speak": "I need an appointment",
        "can_do": "Puedo pedir una cita y hablar de un trámite.",
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-a2-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    mcq_options = [target for _, target in spec["pairs"]]
    examples = [
        {"source": spec["translate"][0], "target": sentence},
        {"source": spec["order"][0], "target": order_sentence},
    ]
    lesson_id = f"es-en-a2-p0-u1-l{index:02d}"
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A2",
        "vocabFrequencyBand": "2000-3000",
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Usar vocabulario de {spec['topic']}.",
                f"Resolver una situación sencilla sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": "Palabras y frases",
                    "body": f"Vocabulario y frases para {spec['topic']}. Fíjate en el verbo y en el orden de la información.",
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
        "title": "Piloto A2 · Autonomía",
        "lessons": [build_lesson(index, spec) for index, spec in enumerate(LESSONS, start=1)],
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel A2 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(LESSONS)} lecciones.")


if __name__ == "__main__":
    main()
