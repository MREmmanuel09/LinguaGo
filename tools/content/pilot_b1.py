"""Genera el bloque piloto B1 de ES→EN (F0, ADR-052)."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-b1-p0-un-1"
MARKER = '\n      ]\n    },\n    {\n      "id": "es-en-lv-3"'

LESSONS = [
    {
        "slug": "piso",
        "title": "Buscar piso",
        "scenario": "Llamas a una inmobiliaria para preguntar por un piso en alquiler.",
        "can_do": "Puedo preguntar por un alquiler y comparar dos viviendas.",
        "topic": "la vivienda",
        "grammar": [
            ("gp-rent-perfect", "Present perfect", "Experiencias recientes con have/has + participio."),
            ("gp-rent-comparative", "Comparatives", "Comparar precio y ubicación con -er / more."),
        ],
        "pairs": [("alquiler", "rent"), ("contrato", "contract"), ("barrio", "neighbourhood")],
        "translate": (
            "He visto tres pisos esta semana",
            ["I", "have", "looked", "at", "three", "flats", "this", "week"],
            ["flat", "month"],
        ),
        "order": ("El alquiler es más barato que el centro", ["The", "rent", "is", "cheaper", "than", "the", "centre"]),
        "listen_choose": (
            "The flat is near the station",
            ["The flat is near the station", "The flat is in the centre", "The rent is too high"],
        ),
        "listen_type": "The flat is near the station",
        "cloze": (
            "I {{b1}} at three flats, but they are {{b2}} than my old flat.",
            [
                ("b1", "have looked", ["have looked", "looked", "look"]),
                ("b2", "cheaper", ["cheaper", "cheap", "cheapest"]),
            ],
        ),
        "free": (
            "Describe el piso que buscas y por qué.",
            "I am looking for a flat near the station.",
            ["flat", "station"],
        ),
        "speak": "I have looked at three flats this week",
        "cultural": ("flat", "En inglés británico se dice flat; en inglés americano, apartment.", "piso"),
    },
    {
        "slug": "entrevista",
        "title": "Entrevista de trabajo",
        "scenario": "Una entrevista para un puesto de atención al cliente.",
        "can_do": "Puedo hablar de mi experiencia y de lo que busco en un trabajo.",
        "topic": "la búsqueda de empleo",
        "grammar": [
            ("gp-job-perfect", "Present perfect", "Experiencia acumulada con have/has + participio."),
            ("gp-job-passive", "Passive voice", "Describir procesos con be + participio."),
        ],
        "pairs": [("entrevista", "interview"), ("experiencia", "experience"), ("sueldo", "salary")],
        "translate": (
            "He trabajado en atención al cliente",
            ["I", "have", "worked", "in", "customer", "service"],
            ["office", "job"],
        ),
        "order": ("El sueldo es importante para mí", ["The", "salary", "is", "important", "to", "me"]),
        "listen_choose": (
            "I have two years of experience",
            ["I have two years of experience", "I have an interview today", "The salary is important to me"],
        ),
        "listen_type": "I have two years of experience",
        "cloze": (
            "I {{b1}} in a shop, and I {{b2}} for a new challenge.",
            [
                ("b1", "have worked", ["have worked", "worked", "work"]),
                ("b2", "am looking", ["am looking", "look", "looked"]),
            ],
        ),
        "free": (
            "Habla de tu experiencia y del trabajo que buscas.",
            "I have two years of experience in customer service.",
            ["experience", "service"],
        ),
        "speak": "I have worked in customer service",
        "cultural": ("CV", "CV es el currículum en británico; en americano se usa résumé.", "currículum"),
    },
    {
        "slug": "reclamacion",
        "title": "Reclamar un pedido",
        "scenario": "Tu pedido online ha llegado tarde y llamas para reclamar.",
        "can_do": "Puedo explicar un problema con un pedido y pedir una solución.",
        "topic": "las reclamaciones",
        "grammar": [
            ("gp-order-perfect", "Present perfect", "Acciones recientes con yet/already."),
            ("gp-order-conditional", "First conditional", "Consecuencias reales con if + present, will."),
        ],
        "pairs": [("pedido", "order"), ("reembolso", "refund"), ("retraso", "delay")],
        "translate": (
            "Todavía no he recibido mi pedido",
            ["I", "have", "not", "received", "my", "order", "yet"],
            ["already", "refund"],
        ),
        "order": ("Si llega tarde, quiero un reembolso", ["If", "it", "is", "late", "I", "want", "a", "refund"]),
        "listen_choose": (
            "The order is two days late",
            ["The order is two days late", "I have received my order", "I want a refund"],
        ),
        "listen_type": "The order is two days late",
        "cloze": (
            "If the order {{b1}}, I {{b2}} a refund.",
            [
                ("b1", "is late", ["is late", "late", "will be late"]),
                ("b2", "will ask for", ["will ask for", "ask for", "asked for"]),
            ],
        ),
        "free": (
            "Explica un problema con un pedido y pide una solución.",
            "I have not received my order yet.",
            ["order", "received"],
        ),
        "speak": "I have not received my order yet",
        "cultural": ("refund", "Refund es devolver el dinero; return es devolver el producto.", "reembolso"),
    },
    {
        "slug": "medio-ambiente",
        "title": "Medio ambiente",
        "scenario": "Un grupo de vecinos propone medidas para reducir residuos en el barrio.",
        "can_do": "Puedo proponer acciones medioambientales y explicar sus consecuencias.",
        "topic": "el medio ambiente",
        "grammar": [
            ("gp-eco-conditional", "First conditional", "Condiciones reales sobre el futuro."),
            ("gp-eco-comparative", "Comparatives", "Comparar soluciones y su impacto."),
        ],
        "pairs": [("reciclar", "recycle"), ("residuos", "waste"), ("energía", "energy")],
        "translate": (
            "Si reciclamos, reducimos los residuos",
            ["If", "we", "recycle", "we", "reduce", "waste"],
            ["energy", "produce"],
        ),
        "order": ("La energía solar es más limpia", ["Solar", "energy", "is", "cleaner"]),
        "listen_choose": (
            "We should recycle more at home",
            ["We should recycle more at home", "Solar energy is cleaner", "We produce less waste"],
        ),
        "listen_type": "We should recycle more at home",
        "cloze": (
            "If we {{b1}} more, we {{b2}} less waste.",
            [
                ("b1", "recycle", ["recycle", "recycled", "will recycle"]),
                ("b2", "will produce", ["will produce", "produce", "produced"]),
            ],
        ),
        "free": (
            "Propón una medida para cuidar el medio ambiente en tu barrio.",
            "We should recycle more at home.",
            ["recycle", "home"],
        ),
        "speak": "If we recycle, we reduce waste",
        "cultural": ("eco-friendly", "Eco-friendly describe productos o hábitos que no dañan el medio ambiente.", "respetuoso con el medio ambiente"),
    },
    {
        "slug": "redes",
        "title": "Redes sociales",
        "scenario": "Hablas con una amiga sobre el tiempo que pasas en el móvil.",
        "can_do": "Puedo hablar de hábitos digitales y de privacidad.",
        "topic": "la vida digital",
        "grammar": [
            ("gp-social-perfect", "Present perfect", "Cambios recientes en hábitos."),
            ("gp-social-gerund", "Gerunds", "Hablar de actividades con -ing."),
        ],
        "pairs": [("pantalla", "screen"), ("notificación", "notification"), ("privacidad", "privacy")],
        "translate": (
            "He desactivado las notificaciones",
            ["I", "have", "turned", "off", "notifications"],
            ["screen", "privacy"],
        ),
        "order": ("Uso el móvil menos por la noche", ["I", "use", "my", "phone", "less", "at", "night"]),
        "listen_choose": (
            "I spend too much time on social media",
            ["I spend too much time on social media", "I have turned off notifications", "I use my phone less at night"],
        ),
        "listen_type": "I spend too much time on social media",
        "cloze": (
            "I {{b1}} social media less, but I still check my {{b2}}.",
            [
                ("b1", "use", ["use", "used", "using"]),
                ("b2", "notifications", ["notifications", "privacy", "screens"]),
            ],
        ),
        "free": (
            "Cuenta qué haces para usar menos el móvil.",
            "I use social media less at night.",
            ["social media", "night"],
        ),
        "speak": "I have turned off notifications",
        "cultural": ("doomscrolling", "Doomscrolling es pasar mucho tiempo leyendo noticias negativas en el móvil.", "consumo compulsivo de noticias"),
    },
    {
        "slug": "cultura",
        "title": "Cultura local",
        "scenario": "Describes a un visitante una fiesta tradicional de tu ciudad.",
        "can_do": "Puedo describir una tradición y una escena del pasado.",
        "topic": "las tradiciones",
        "grammar": [
            ("gp-culture-relative", "Relative clauses", "Unir información con who/that/which."),
            ("gp-culture-past-continuous", "Past continuous", "Describir una escena en desarrollo."),
        ],
        "pairs": [("festival", "festival"), ("tradición", "tradition"), ("artesanía", "craft")],
        "translate": (
            "El festival que visitamos era muy colorido",
            ["The", "festival", "we", "visited", "was", "very", "colourful"],
            ["tradition", "craft"],
        ),
        "order": ("Mientras caminábamos, vimos un desfile", ["While", "we", "were", "walking", "we", "saw", "a", "parade"]),
        "listen_choose": (
            "The local festival starts on Friday",
            ["The local festival starts on Friday", "The festival was very colourful", "We saw a parade"],
        ),
        "listen_type": "The local festival starts on Friday",
        "cloze": (
            "The festival {{b1}} we visited {{b2}} very colourful.",
            [
                ("b1", "that", ["that", "who", "where"]),
                ("b2", "was", ["was", "were", "is"]),
            ],
        ),
        "free": (
            "Describe una fiesta o tradición de tu ciudad.",
            "The festival we visited was very colourful.",
            ["festival", "colourful"],
        ),
        "speak": "The festival we visited was very colourful",
        "cultural": ("bank holiday", "Bank holiday es un día festivo nacional en el Reino Unido.", "día festivo"),
    },
    {
        "slug": "viaje",
        "title": "Viaje con retraso",
        "scenario": "Tu vuelo se ha retrasado y hablas con el mostrador de la aerolínea.",
        "can_do": "Puedo explicar un retraso y pedir una solución.",
        "topic": "los viajes",
        "grammar": [
            ("gp-travel-past-perfect", "Past perfect", "Una acción anterior a otra en el pasado."),
            ("gp-travel-modals", "Modals", "Dar consejos con should."),
        ],
        "pairs": [("retraso", "delay"), ("enlace", "connection"), ("equipaje", "luggage")],
        "translate": (
            "El vuelo había salido cuando llegamos",
            ["The", "flight", "had", "left", "when", "we", "arrived"],
            ["delay", "luggage"],
        ),
        "order": ("Deberíamos llamar a la aerolínea", ["We", "should", "call", "the", "airline"]),
        "listen_choose": (
            "My flight was delayed for two hours",
            ["My flight was delayed for two hours", "I missed my connection", "We should call the airline"],
        ),
        "listen_type": "My flight was delayed for two hours",
        "cloze": (
            "The flight {{b1}} before we {{b2}} at the airport.",
            [
                ("b1", "had left", ["had left", "left", "has left"]),
                ("b2", "arrived", ["arrived", "arrive", "had arrived"]),
            ],
        ),
        "free": (
            "Explica un retraso de viaje y lo que deberías hacer.",
            "My flight was delayed for two hours.",
            ["flight", "delayed"],
        ),
        "speak": "We should call the airline",
        "cultural": ("layover", "Layover es una escala entre dos vuelos.", "escala"),
    },
    {
        "slug": "estres",
        "title": "Salud y estrés",
        "scenario": "Hablas con un compañero que está muy cansado por el trabajo.",
        "can_do": "Puedo dar consejos sobre descanso y hábitos saludables.",
        "topic": "el bienestar",
        "grammar": [
            ("gp-health-modal", "Should / ought to", "Dar recomendaciones."),
            ("gp-health-perfect-continuous", "Present perfect continuous", "Acciones que continúan hasta ahora."),
        ],
        "pairs": [("estrés", "stress"), ("descanso", "rest"), ("hábito", "habit")],
        "translate": (
            "Deberías descansar más",
            ["You", "should", "rest", "more"],
            ["stress", "habit"],
        ),
        "order": ("He estado durmiendo mal esta semana", ["I", "have", "been", "sleeping", "badly", "this", "week"]),
        "listen_choose": (
            "You should take a break",
            ["You should take a break", "I have been sleeping badly", "You need more rest"],
        ),
        "listen_type": "You should take a break",
        "cloze": (
            "You {{b1}} take a break, and I {{b2}} better this week.",
            [
                ("b1", "should", ["should", "must", "can"]),
                ("b2", "have felt", ["have felt", "felt", "feel"]),
            ],
        ),
        "free": (
            "Da un consejo a alguien que está estresado.",
            "You should take a break.",
            ["break", "should"],
        ),
        "speak": "You should rest more",
        "cultural": ("burnout", "Burnout es el agotamiento prolongado por el trabajo.", "agotamiento"),
    },
    {
        "slug": "voluntariado",
        "title": "Voluntariado",
        "scenario": "Te informas sobre un proyecto de voluntariado en tu barrio.",
        "can_do": "Puedo hablar de acciones solidarias y de sus condiciones.",
        "topic": "la solidaridad",
        "grammar": [
            ("gp-volunteer-gerund", "Gerunds and infinitives", "Verbos seguidos de -ing o infinitivo."),
            ("gp-volunteer-conditional", "First conditional", "Condiciones para participar."),
        ],
        "pairs": [("voluntario", "volunteer"), ("comunidad", "community"), ("donar", "donate")],
        "translate": (
            "Me gusta ayudar en mi comunidad",
            ["I", "like", "helping", "in", "my", "community"],
            ["volunteer", "donate"],
        ),
        "order": ("Si tengo tiempo, seré voluntario", ["If", "I", "have", "time", "I", "will", "volunteer"]),
        "listen_choose": (
            "I volunteer at the local shelter",
            ["I volunteer at the local shelter", "I like helping in my community", "I will donate some clothes"],
        ),
        "listen_type": "I volunteer at the local shelter",
        "cloze": (
            "I enjoy {{b1}} at the shelter, and I {{b2}} if I have time.",
            [
                ("b1", "helping", ["helping", "help", "to help"]),
                ("b2", "will volunteer", ["will volunteer", "volunteer", "volunteered"]),
            ],
        ),
        "free": (
            "Habla de una acción solidaria que te gustaría hacer.",
            "I like helping in my community.",
            ["helping", "community"],
        ),
        "speak": "I like helping in my community",
        "cultural": ("charity shop", "Charity shop es una tienda benéfica con artículos de segunda mano.", "tienda benéfica"),
    },
    {
        "slug": "futuro",
        "title": "Futuro laboral",
        "scenario": "Hablas con un orientador sobre tus planes de formación y trabajo.",
        "can_do": "Puedo hablar de planes futuros y de sus consecuencias.",
        "topic": "el futuro profesional",
        "grammar": [
            ("gp-future-forms", "Future forms", "going to, will y presente continuo."),
            ("gp-future-conditional", "First conditional", "Planes condicionados al futuro."),
        ],
        "pairs": [("carrera", "career"), ("habilidad", "skill"), ("formación", "training")],
        "translate": (
            "Voy a estudiar una nueva habilidad",
            ["I", "am", "going", "to", "learn", "a", "new", "skill"],
            ["career", "training"],
        ),
        "order": ("Si cambio de trabajo, aprenderé más", ["If", "I", "change", "jobs", "I", "will", "learn", "more"]),
        "listen_choose": (
            "I am going to apply for a new job",
            ["I am going to apply for a new job", "I will learn a new skill", "I have a training course"],
        ),
        "listen_type": "I am going to apply for a new job",
        "cloze": (
            "If I {{b1}} jobs, I {{b2}} more.",
            [
                ("b1", "change", ["change", "changed", "will change"]),
                ("b2", "will learn", ["will learn", "learn", "learned"]),
            ],
        ),
        "free": (
            "Cuenta un plan de formación o trabajo para el próximo año.",
            "I am going to learn a new skill.",
            ["learn", "skill"],
        ),
        "speak": "I am going to learn a new skill",
        "cultural": ("upskilling", "Upskilling es aprender nuevas competencias para mejorar en el trabajo.", "actualización de competencias"),
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-b1-p0-u1-l{index:02d}-e{suffix:02d}"


def build_lesson(index: int, spec: dict) -> dict:
    grammar_one, grammar_two = spec["grammar"]
    sentence = " ".join(spec["translate"][1])
    order_sentence = " ".join(spec["order"][1])
    first_source, first_target = spec["pairs"][0]
    features = [
        {
            "type": "MultipleChoice",
            "id": exercise_id(index, 1),
            "question": f"¿Cuál es la opción correcta para «{first_source}»?",
            "options": [target for _, target in spec["pairs"]],
            "correctIndex": 0,
            "feedbackOnError": {
                "type": "vocabulary",
                "explanation": f"«{first_source}» se dice «{first_target}».",
                "ruleRef": grammar_one[0],
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
                "type": "grammar",
                "explanation": f"El orden correcto es «{sentence}».",
                "ruleRef": grammar_one[0],
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
                "ruleRef": grammar_two[0],
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
                "ruleRef": grammar_one[0],
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
                "ruleRef": grammar_one[0],
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
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
        },
        {
            "type": "ClozeDragDrop",
            "id": exercise_id(index, 7),
            "passage": spec["cloze"][0],
            "blanks": [
                {"id": blank_id, "answer": answer, "options": options}
                for blank_id, answer, options in spec["cloze"][1]
            ],
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "Las dos formas verbales deben encajar con el contexto.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Los dos huecos encajan con la situación."},
        },
        {
            "type": "FreeSpeakingPrompt",
            "id": exercise_id(index, 8),
            "prompt": spec["free"][0],
            "minTokens": 4,
            "conceptClusters": [spec["free"][2]],
            "modelAnswer": spec["free"][1],
            "selfAssessmentCriteria": ["¿La respuesta es clara y responde a la tarea?"],
            "grammarRuleRefs": [],
            "timeLimitSeconds": 60,
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "La respuesta modelo usa una frase completa y responde a la tarea.",
                "ruleRef": grammar_one[0],
            },
        },
        {
            "type": "SpeakRepeat",
            "id": exercise_id(index, 9),
            "targetPhrase": spec["speak"],
            "audioText": spec["speak"],
            "feedbackOnError": {
                "type": "pronunciation",
                "explanation": f"Repite «{spec['speak']}» con claridad.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
        },
    ]
    return {
        "id": f"es-en-b1-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "B1",
        "scenario": spec["scenario"],
        "canDo": spec["can_do"],
        "grammarPoints": [
            {"id": grammar_one[0], "label": grammar_one[1], "description": grammar_one[2]},
            {"id": grammar_two[0], "label": grammar_two[1], "description": grammar_two[2]},
        ],
        "vocabFrequencyBand": "2000-3000",
        "culturalNotes": [
            {
                "idiom": spec["cultural"][0],
                "note": spec["cultural"][1],
                "translation": spec["cultural"][2],
            }
        ],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Usar vocabulario de {spec['topic']}.",
                f"Resolver una tarea comunicativa sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": "Lengua en contexto",
                    "body": f"Fíjate en las formas verbales y en el orden de la información para hablar de {spec['topic']}.",
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
        "exercises": features,
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
        "title": "Piloto B1 · Autonomía real",
        "lessons": [build_lesson(index, spec) for index, spec in enumerate(LESSONS, start=1)],
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel B1 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(LESSONS)} lecciones.")


if __name__ == "__main__":
    main()
