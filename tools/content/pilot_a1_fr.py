"""Genera el bloque piloto A1 de ES→FR (F0, ADR-052)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json"
UNIT_ID = "es-fr-a1-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-fr-lv-a2"'

LESSONS = [
    {
        "slug": "animaux",
        "title": "Mon animal de compagnie",
        "topic": "les animaux de compagnie",
        "pairs": [("perro", "chien"), ("gato", "chat"), ("pájaro", "oiseau")],
        "translate": ("Mi perro es pequeño", ["Mon", "chien", "est", "petit"], ["chat", "grand"]),
        "order": ("El gato duerme en casa", ["Le", "chat", "dort", "à", "la", "maison"]),
        "listen_choose": (
            "J'ai un chat",
            ["J'ai un chat", "J'ai un chien", "J'ai un oiseau"],
        ),
        "listen_type": "Mon chien est gentil",
        "speak": "J'ai un animal de compagnie",
        "can_do": "Puedo hablar de mi mascota con frases sencillas en francés.",
    },
    {
        "slug": "vetements",
        "title": "Les vêtements",
        "topic": "les vêtements",
        "pairs": [("camisa", "chemise"), ("zapatos", "chaussures"), ("abrigo", "manteau")],
        "translate": ("Mi camisa es azul", ["Ma", "chemise", "est", "bleue"], ["rouge", "manteau"]),
        "order": ("El abrigo es nuevo", ["Le", "manteau", "est", "nouveau"]),
        "listen_choose": (
            "Je porte un manteau",
            ["Je porte un manteau", "Je porte une chemise", "Je porte des chaussures"],
        ),
        "listen_type": "Mes chaussures sont noires",
        "speak": "Je porte une chemise bleue",
        "can_do": "Puedo nombrar ropa y describir sus colores en francés.",
    },
    {
        "slug": "routine",
        "title": "Ma routine",
        "topic": "la routine quotidienne",
        "pairs": [("despertarse", "se réveiller"), ("vestirse", "s'habiller"), ("acostarse", "se coucher")],
        "translate": ("Me levanto a las siete", ["Je", "me", "réveille", "à", "sept", "heures"], ["huit", "couche"]),
        "order": ("Desayuno a las ocho", ["Je", "prends", "le", "petit-déjeuner", "à", "huit", "heures"]),
        "listen_choose": (
            "Je me couche à dix heures",
            ["Je me couche à dix heures", "Je me réveille à sept heures", "Je prends le petit-déjeuner à huit heures"],
        ),
        "listen_type": "Je m'habille le matin",
        "speak": "Je me réveille à sept heures",
        "can_do": "Puedo describir mi rutina diaria con horas sencillas en francés.",
    },
    {
        "slug": "transport",
        "title": "Le transport",
        "topic": "le transport",
        "pairs": [("autobús", "bus"), ("tren", "train"), ("bicicleta", "vélo")],
        "translate": ("Voy en autobús", ["Je", "vais", "en", "bus"], ["train", "vélo"]),
        "order": ("La bicicleta es rápida", ["Le", "vélo", "est", "rapide"]),
        "listen_choose": (
            "Je prends le train",
            ["Je prends le train", "Je prends le bus", "Je vais à vélo"],
        ),
        "listen_type": "Le bus est en retard",
        "speak": "Je vais en bus",
        "can_do": "Puedo decir cómo me desplazo por la ciudad en francés.",
    },
    {
        "slug": "directions",
        "title": "Les directions",
        "topic": "les directions",
        "pairs": [("izquierda", "gauche"), ("derecha", "droite"), ("calle", "rue")],
        "translate": ("La calle está a la derecha", ["La", "rue", "est", "à", "droite"], ["gauche", "place"]),
        "order": ("Gira a la izquierda", ["Tournez", "à", "gauche"]),
        "listen_choose": (
            "La banque est à gauche",
            ["La banque est à gauche", "La banque est à droite", "La banque est près du parc"],
        ),
        "listen_type": "Allez tout droit",
        "speak": "Tournez à droite",
        "can_do": "Puedo pedir y dar direcciones básicas en francés.",
    },
    {
        "slug": "ecole",
        "title": "L'école",
        "topic": "l'école",
        "pairs": [("escuela", "école"), ("libro", "livre"), ("profesor", "professeur")],
        "translate": ("Mi libro es nuevo", ["Mon", "livre", "est", "neuf"], ["vieux", "école"]),
        "order": ("El profesor es amable", ["Le", "professeur", "est", "gentil"]),
        "listen_choose": (
            "Je vais à l'école",
            ["Je vais à l'école", "Je lis un livre", "Le professeur est gentil"],
        ),
        "listen_type": "Mon école est grande",
        "speak": "J'ai un livre",
        "can_do": "Puedo hablar de objetos y personas de la escuela en francés.",
    },
    {
        "slug": "travail",
        "title": "Le travail",
        "topic": "le travail",
        "pairs": [("oficina", "bureau"), ("jefe", "chef"), ("reunión", "réunion")],
        "translate": ("Mi jefe está en la oficina", ["Mon", "chef", "est", "au", "bureau"], ["réunion", "maison"]),
        "order": ("La reunión es a las diez", ["La", "réunion", "est", "à", "dix", "heures"]),
        "listen_choose": (
            "Je travaille dans un bureau",
            ["Je travaille dans un bureau", "La réunion est à dix heures", "Mon chef est occupé"],
        ),
        "listen_type": "Mon chef est occupé",
        "speak": "Je travaille dans un bureau",
        "can_do": "Puedo presentar mi trabajo y hablar de una reunión en francés.",
    },
    {
        "slug": "sante",
        "title": "La santé",
        "topic": "la santé",
        "pairs": [("médico", "médecin"), ("farmacia", "pharmacie"), ("dolor", "douleur")],
        "translate": ("Me duele la cabeza", ["J'ai", "mal", "à", "la", "tête"], ["estomac", "médecin"]),
        "order": ("Necesito un médico", ["J'ai", "besoin", "d'un", "médecin"]),
        "listen_choose": (
            "J'ai mal à la tête",
            ["J'ai mal à la tête", "J'ai besoin d'un médecin", "La pharmacie est ouverte"],
        ),
        "listen_type": "La pharmacie est ouverte",
        "speak": "J'ai besoin d'un médecin",
        "can_do": "Puedo explicar un problema de salud sencillo en francés.",
    },
    {
        "slug": "loisirs",
        "title": "Les loisirs",
        "topic": "les loisirs",
        "pairs": [("música", "musique"), ("deporte", "sport"), ("película", "film")],
        "translate": ("Me gusta la música", ["J'aime", "la", "musique"], ["sport", "film"]),
        "order": ("El deporte es divertido", ["Le", "sport", "est", "amusant"]),
        "listen_choose": (
            "Je regarde un film",
            ["Je regarde un film", "J'écoute de la musique", "Je fais du sport"],
        ),
        "listen_type": "J'écoute de la musique",
        "speak": "J'aime la musique",
        "can_do": "Puedo hablar de lo que me gusta hacer en mi tiempo libre en francés.",
    },
    {
        "slug": "ville",
        "title": "La ville",
        "topic": "la ville",
        "pairs": [("parque", "parc"), ("museo", "musée"), ("restaurante", "restaurant")],
        "translate": ("El parque es grande", ["Le", "parc", "est", "grand"], ["petit", "musée"]),
        "order": ("El restaurante está cerca", ["Le", "restaurant", "est", "près"]),
        "listen_choose": (
            "Le musée est ouvert",
            ["Le musée est ouvert", "Le parc est beau", "Le restaurant est près"],
        ),
        "listen_type": "Le parc est beau",
        "speak": "Je vais au parc",
        "can_do": "Puedo hablar de lugares de mi ciudad en francés.",
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-fr-a1-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    answer = spec["order"][1]
    rotated = answer[1:] + answer[:1] if len(answer) > 1 else answer
    return {
        "id": f"es-fr-a1-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en francés.",
            ],
            "sections": [
                {
                    "heading": "Mots et phrases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en los artículos franceses.",
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
                "question": f"¿Cómo se dice «{first_source}» en francés?",
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
        "title": "Piloto A1 · Vie quotidienne",
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
