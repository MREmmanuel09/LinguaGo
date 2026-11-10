"""Genera el bloque piloto A2 de ES→PT (F0, ADR-052)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json"
UNIT_ID = "es-pt-a2-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-pt-lv-b1"'

LESSONS = [
    {
        "slug": "supermercado",
        "title": "No supermercado",
        "topic": "o supermercado",
        "pairs": [("carrito", "carrinho"), ("caja", "caixa"), ("fruta", "fruta")],
        "translate": ("Necesito comprar fruta", ["Eu", "preciso", "comprar", "fruta"], ["verdura", "padaria"]),
        "order": ("O supermercado fica perto de casa", ["O", "supermercado", "fica", "perto", "de", "casa"]),
        "listen_choose": (
            "Eu preciso de um carrinho",
            ["Eu preciso de um carrinho", "Eu preciso de uma caixa", "Eu preciso de fruta"],
        ),
        "listen_type": "O supermercado está aberto",
        "speak": "Eu preciso comprar fruta",
        "can_do": "Puedo hacer una compra sencilla en un supermercado portugués.",
    },
    {
        "slug": "banco",
        "title": "No banco",
        "topic": "o banco",
        "pairs": [("cuenta", "conta"), ("tarjeta", "cartão"), ("dinero", "dinheiro")],
        "translate": ("Quiero abrir una cuenta", ["Eu", "quero", "abrir", "uma", "conta"], ["fechar", "cartão"]),
        "order": ("O banco abre às nove horas", ["O", "banco", "abre", "às", "nove", "horas"]),
        "listen_choose": (
            "Eu quero abrir uma conta",
            ["Eu quero abrir uma conta", "Eu quero fechar a conta", "Eu preciso de um cartão"],
        ),
        "listen_type": "O banco abre às nove horas",
        "speak": "Eu quero abrir uma conta",
        "can_do": "Puedo pedir información básica en un banco portugués.",
    },
    {
        "slug": "mudanca",
        "title": "Uma mudança",
        "topic": "a mudança de casa",
        "pairs": [("mudanza", "mudança"), ("caja", "caixa"), ("llave", "chave")],
        "translate": ("La mudanza es el viernes", ["A", "mudança", "é", "na", "sexta-feira"], ["segunda", "casa"]),
        "order": ("As caixas estão na sala", ["As", "caixas", "estão", "na", "sala"]),
        "listen_choose": (
            "A mudança é na sexta-feira",
            ["A mudança é na sexta-feira", "A mudança é na segunda-feira", "As caixas estão na sala"],
        ),
        "listen_type": "As chaves estão na mesa",
        "speak": "A mudança é na sexta-feira",
        "can_do": "Puedo hablar de una mudanza y de objetos de la casa.",
    },
    {
        "slug": "academia",
        "title": "A academia",
        "topic": "a academia",
        "pairs": [("entrenamiento", "treino"), ("peso", "peso"), ("rutina", "rotina")],
        "translate": ("Entreno tres veces por semana", ["Eu", "treino", "três", "vezes", "por", "semana"], ["todos", "dia"]),
        "order": ("A academia abre às seis", ["A", "academia", "abre", "às", "seis"]),
        "listen_choose": (
            "Eu treino três vezes por semana",
            ["Eu treino três vezes por semana", "Eu treino todos os dias", "A academia abre às seis"],
        ),
        "listen_type": "Eu levanto peso na academia",
        "speak": "Eu treino três vezes por semana",
        "can_do": "Puedo hablar de mi rutina de ejercicio en portugués.",
    },
    {
        "slug": "turismo",
        "title": "Uma visita turística",
        "topic": "o turismo",
        "pairs": [("museo", "museu"), ("entrada", "ingresso"), ("guía", "guia")],
        "translate": ("Quiero comprar una entrada", ["Eu", "quero", "comprar", "um", "ingresso"], ["museu", "guia"]),
        "order": ("O museu abre às dez horas", ["O", "museu", "abre", "às", "dez", "horas"]),
        "listen_choose": (
            "O ingresso custa vinte reais",
            ["O ingresso custa vinte reais", "O museu abre às dez horas", "O guia está esperando"],
        ),
        "listen_type": "O guia está esperando",
        "speak": "Eu quero comprar um ingresso",
        "can_do": "Puedo comprar una entrada y pedir información turística.",
    },
    {
        "slug": "aniversario",
        "title": "Um aniversário",
        "topic": "os aniversários",
        "pairs": [("cumpleaños", "aniversário"), ("pastel", "bolo"), ("regalo", "presente")],
        "translate": ("El cumpleaños es mañana", ["O", "aniversário", "é", "amanhã"], ["ontem", "bolo"]),
        "order": ("Eu vou levar um bolo", ["Eu", "vou", "levar", "um", "bolo"]),
        "listen_choose": (
            "O aniversário é amanhã",
            ["O aniversário é amanhã", "O aniversário foi ontem", "Eu vou levar um presente"],
        ),
        "listen_type": "Eu vou levar um presente",
        "speak": "O aniversário é amanhã",
        "can_do": "Puedo hablar de un cumpleaños y de lo que voy a llevar.",
    },
    {
        "slug": "carro",
        "title": "O carro",
        "topic": "o carro",
        "pairs": [("gasolina", "gasolina"), ("taller", "oficina"), ("rueda", "roda")],
        "translate": ("El coche necesita gasolina", ["O", "carro", "precisa", "de", "gasolina"], ["oficina", "roda"]),
        "order": ("O carro está na oficina", ["O", "carro", "está", "na", "oficina"]),
        "listen_choose": (
            "O carro precisa de gasolina",
            ["O carro precisa de gasolina", "O carro está na oficina", "O carro é novo"],
        ),
        "listen_type": "O carro está na oficina",
        "speak": "O carro precisa de gasolina",
        "can_do": "Puedo hablar de mi coche y de una reparación.",
    },
    {
        "slug": "escritorio",
        "title": "O escritório",
        "topic": "o escritório",
        "pairs": [("oficina", "escritório"), ("reunión", "reunião"), ("plazo", "prazo")],
        "translate": ("La reunión es a las diez", ["A", "reunião", "é", "às", "dez", "horas"], ["amanhã", "prazo"]),
        "order": ("O prazo é na sexta-feira", ["O", "prazo", "é", "na", "sexta-feira"]),
        "listen_choose": (
            "A reunião é às dez horas",
            ["A reunião é às dez horas", "A reunião é amanhã", "O prazo é na sexta-feira"],
        ),
        "listen_type": "O prazo é na sexta-feira",
        "speak": "A reunião é às dez horas",
        "can_do": "Puedo hablar de reuniones y plazos en la oficina.",
    },
    {
        "slug": "tempo-livre",
        "title": "Tempo livre",
        "topic": "o tempo livre",
        "pairs": [("cine", "cinema"), ("serie", "série"), ("paseo", "caminhada")],
        "translate": ("Me gusta pasear por el parque", ["Eu", "gosto", "de", "caminhar", "no", "parque"], ["cinema", "série"]),
        "order": ("Nós vamos ao cinema hoje", ["Nós", "vamos", "ao", "cinema", "hoje"]),
        "listen_choose": (
            "Eu gosto de caminhar no parque",
            ["Eu gosto de caminhar no parque", "Nós vamos ao cinema hoje", "Eu assisto a uma série"],
        ),
        "listen_type": "Nós vamos ao cinema hoje",
        "speak": "Eu gosto de caminhar no parque",
        "can_do": "Puedo hablar de mis planes de tiempo libre en portugués.",
    },
    {
        "slug": "tramite",
        "title": "Um trâmite",
        "topic": "os trâmites",
        "pairs": [("documento", "documento"), ("formulario", "formulário"), ("firma", "assinatura")],
        "translate": ("Necesito un documento", ["Eu", "preciso", "de", "um", "documento"], ["formulário", "assinatura"]),
        "order": ("O formulário precisa de uma assinatura", ["O", "formulário", "precisa", "de", "uma", "assinatura"]),
        "listen_choose": (
            "O formulário precisa de uma assinatura",
            ["O formulário precisa de uma assinatura", "Eu preciso de um documento", "O documento está pronto"],
        ),
        "listen_type": "Eu preciso de um documento",
        "speak": "O formulário precisa de uma assinatura",
        "can_do": "Puedo resolver un trámite sencillo con documentos en portugués.",
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-pt-a2-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    answer = spec["order"][1]
    rotated = answer[1:] + answer[:1] if len(answer) > 1 else answer
    return {
        "id": f"es-pt-a2-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "A2",
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
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las preposiciones de lugar.",
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
    assert lesson.get("grammarPoints") is None, f"{lesson_id}: A2 no declara gramática"
    for exercise in lesson["exercises"]:
        texts = []
        if exercise["type"] == "ListenType":
            texts.append(exercise["transcription"])
        elif exercise["type"] == "SpeakRepeat":
            texts.append(exercise["targetPhrase"])
        elif exercise["type"] == "ListenChoose":
            texts.append(exercise["audioText"])
        for text in texts:
            assert len(text.split()) <= 12, f"{lesson_id}/{exercise['id']}: frase demasiado larga"
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
        "title": "Piloto A2 · Autonomia",
        "lessons": lessons,
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel A2 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(lessons)} lecciones y {len(lessons) * 7} ejercicios.")


if __name__ == "__main__":
    main()
