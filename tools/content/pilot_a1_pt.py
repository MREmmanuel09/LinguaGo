"""Genera el bloque piloto A1 de ES→PT (F0, ADR-052)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json"
UNIT_ID = "es-pt-a1-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-pt-lv-a2"'

LESSONS = [
    {
        "slug": "mascotas",
        "title": "Meu animal de estimação",
        "topic": "os animais de estimação",
        "pairs": [("perro", "cachorro"), ("gato", "gato"), ("pájaro", "pássaro")],
        "translate": ("Mi perro es pequeño", ["Meu", "cachorro", "é", "pequeno"], ["gato", "grande"]),
        "order": ("El gato duerme en casa", ["O", "gato", "dorme", "em", "casa"]),
        "listen_choose": (
            "Eu tenho um gato",
            ["Eu tenho um gato", "Eu tenho um cachorro", "Eu tenho um pássaro"],
        ),
        "listen_type": "Meu cachorro é simpático",
        "speak": "Eu tenho um animal de estimação",
        "can_do": "Puedo hablar de mi mascota con frases sencillas en portugués.",
    },
    {
        "slug": "roupa",
        "title": "A roupa",
        "topic": "a roupa",
        "pairs": [("camisa", "camisa"), ("zapatos", "sapatos"), ("abrigo", "casaco")],
        "translate": ("Mi camisa es azul", ["Minha", "camisa", "é", "azul"], ["vermelha", "casaco"]),
        "order": ("El abrigo es nuevo", ["O", "casaco", "é", "novo"]),
        "listen_choose": (
            "Eu uso um casaco",
            ["Eu uso um casaco", "Eu uso uma camisa", "Eu uso sapatos"],
        ),
        "listen_type": "Meus sapatos são pretos",
        "speak": "Eu uso uma camisa azul",
        "can_do": "Puedo nombrar ropa y describir sus colores en portugués.",
    },
    {
        "slug": "rotina",
        "title": "Minha rotina",
        "topic": "a rotina diária",
        "pairs": [("despertarse", "acordar"), ("vestirse", "vestir-se"), ("acostarse", "deitar-se")],
        "translate": ("Me levanto a las siete", ["Eu", "acordo", "às", "sete"], ["oito", "durmo"]),
        "order": ("Desayuno a las ocho", ["Eu", "tomo", "café", "às", "oito"]),
        "listen_choose": (
            "Eu me deito às dez",
            ["Eu me deito às dez", "Eu acordo às sete", "Eu tomo café às oito"],
        ),
        "listen_type": "Eu me visto pela manhã",
        "speak": "Eu acordo às sete",
        "can_do": "Puedo describir mi rutina diaria con horas sencillas en portugués.",
    },
    {
        "slug": "transporte",
        "title": "O transporte",
        "topic": "o transporte",
        "pairs": [("autobús", "ônibus"), ("tren", "trem"), ("bicicleta", "bicicleta")],
        "translate": ("Voy en autobús", ["Eu", "vou", "de", "ônibus"], ["trem", "bicicleta"]),
        "order": ("La bicicleta es rápida", ["A", "bicicleta", "é", "rápida"]),
        "listen_choose": (
            "Eu pego o trem",
            ["Eu pego o trem", "Eu pego o ônibus", "Eu ando de bicicleta"],
        ),
        "listen_type": "O ônibus está atrasado",
        "speak": "Eu vou de ônibus",
        "can_do": "Puedo decir cómo me desplazo por la ciudad en portugués.",
    },
    {
        "slug": "direcciones",
        "title": "Direções",
        "topic": "as direções",
        "pairs": [("izquierda", "esquerda"), ("derecha", "direita"), ("calle", "rua")],
        "translate": ("La calle está a la derecha", ["A", "rua", "fica", "à", "direita"], ["esquerda", "praça"]),
        "order": ("Gira a la izquierda", ["Vire", "à", "esquerda"]),
        "listen_choose": (
            "O banco fica à esquerda",
            ["O banco fica à esquerda", "O banco fica à direita", "O banco fica perto do parque"],
        ),
        "listen_type": "Siga em frente",
        "speak": "Vire à direita",
        "can_do": "Puedo pedir y dar direcciones básicas en portugués.",
    },
    {
        "slug": "escola",
        "title": "A escola",
        "topic": "a escola",
        "pairs": [("escuela", "escola"), ("libro", "livro"), ("profesor", "professor")],
        "translate": ("Mi libro es nuevo", ["Meu", "livro", "é", "novo"], ["velho", "escola"]),
        "order": ("El profesor es amable", ["O", "professor", "é", "simpático"]),
        "listen_choose": (
            "Eu vou à escola",
            ["Eu vou à escola", "Eu leio um livro", "O professor é simpático"],
        ),
        "listen_type": "Minha escola é grande",
        "speak": "Eu tenho um livro",
        "can_do": "Puedo hablar de objetos y personas de la escuela en portugués.",
    },
    {
        "slug": "trabalho",
        "title": "O trabalho",
        "topic": "o trabalho",
        "pairs": [("oficina", "escritório"), ("jefe", "chefe"), ("reunión", "reunião")],
        "translate": ("Mi jefe está en la oficina", ["Meu", "chefe", "está", "no", "escritório"], ["reunião", "casa"]),
        "order": ("La reunión es a las diez", ["A", "reunião", "é", "às", "dez"]),
        "listen_choose": (
            "Eu trabalho em um escritório",
            ["Eu trabalho em um escritório", "A reunião é às dez", "Meu chefe está ocupado"],
        ),
        "listen_type": "Meu chefe está ocupado",
        "speak": "Eu trabalho em um escritório",
        "can_do": "Puedo presentar mi trabajo y hablar de una reunión en portugués.",
    },
    {
        "slug": "saude",
        "title": "A saúde",
        "topic": "a saúde",
        "pairs": [("médico", "médico"), ("farmacia", "farmácia"), ("dolor", "dor")],
        "translate": ("Me duele la cabeza", ["Estou", "com", "dor", "de", "cabeça"], ["estômago", "médico"]),
        "order": ("Necesito un médico", ["Eu", "preciso", "de", "um", "médico"]),
        "listen_choose": (
            "Estou com dor de cabeça",
            ["Estou com dor de cabeça", "Eu preciso de um médico", "A farmácia está aberta"],
        ),
        "listen_type": "A farmácia está aberta",
        "speak": "Eu preciso de um médico",
        "can_do": "Puedo explicar un problema de salud sencillo en portugués.",
    },
    {
        "slug": "passatempos",
        "title": "Passatempos",
        "topic": "os passatempos",
        "pairs": [("música", "música"), ("deporte", "esporte"), ("película", "filme")],
        "translate": ("Me gusta la música", ["Eu", "gosto", "de", "música"], ["esporte", "filme"]),
        "order": ("El deporte es divertido", ["O", "esporte", "é", "divertido"]),
        "listen_choose": (
            "Eu vejo um filme",
            ["Eu vejo um filme", "Eu ouço música", "Eu pratico um esporte"],
        ),
        "listen_type": "Eu ouço música",
        "speak": "Eu gosto de música",
        "can_do": "Puedo hablar de lo que me gusta hacer en mi tiempo libre en portugués.",
    },
    {
        "slug": "cidade",
        "title": "A cidade",
        "topic": "a cidade",
        "pairs": [("parque", "parque"), ("museo", "museu"), ("restaurante", "restaurante")],
        "translate": ("El parque es grande", ["O", "parque", "é", "grande"], ["pequeno", "museu"]),
        "order": ("El restaurante está cerca", ["O", "restaurante", "fica", "perto"]),
        "listen_choose": (
            "O museu está aberto",
            ["O museu está aberto", "O parque é bonito", "O restaurante fica perto"],
        ),
        "listen_type": "O parque é bonito",
        "speak": "Eu vou ao parque",
        "can_do": "Puedo hablar de lugares de mi ciudad en portugués.",
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-pt-a1-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    answer = spec["order"][1]
    rotated = answer[1:] + answer[:1] if len(answer) > 1 else answer
    return {
        "id": f"es-pt-a1-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en portugués.",
            ],
            "sections": [
                {
                    "heading": "Palavras e frases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase: sujeto + verbo + complemento.",
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
                "id": exercise_id(index, 1),
                "question": f"¿Cómo se dice «{first_source}» en portugués?",
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


def self_check(lesson: dict) -> None:
    lesson_id = lesson["id"]
    assert len(lesson["exercises"]) == 7, f"{lesson_id}: se esperaban 7 ejercicios"
    assert lesson.get("grammarPoints") is None, f"{lesson_id}: A1 no declara gramática"
    for exercise in lesson["exercises"]:
        texts = []
        if exercise["type"] == "ListenType":
            texts.append(exercise["transcription"])
        elif exercise["type"] == "SpeakRepeat":
            texts.append(exercise["targetPhrase"])
        elif exercise["type"] == "ListenChoose":
            texts.append(exercise["audioText"])
        for text in texts:
            assert len(text.split()) <= 8, f"{lesson_id}/{exercise['id']}: frase demasiado larga"
        if exercise["type"] == "Translate":
            overlap = set(exercise["correctTokens"]).intersection(exercise["distractorTokens"])
            assert not overlap, f"{lesson_id}/{exercise['id']}: distractor repetido {overlap}"


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def main() -> None:
    course_text = COURSE.read_text(encoding="utf-8")
    if UNIT_ID in course_text:
        print(f"El bloque {UNIT_ID} ya existe; no se modifica.")
        return
    lessons = [build_lesson(index, spec) for index, spec in enumerate(LESSONS, start=1)]
    for lesson in lessons:
        self_check(lesson)
    unit = {
        "id": UNIT_ID,
        "title": "Piloto A1 · Vida cotidiana",
        "lessons": lessons,
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel A1 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(lessons)} lecciones y {len(lessons) * 7} ejercicios.")


if __name__ == "__main__":
    main()
