"""Currículo A2 de LinguaGo (F2).

Cada bloque contiene 10 lecciones y cada lección tres pares de vocabulario
(es, en, fr). Los patrones de frase viven en `a2_factory.py`; las lecciones
que necesitan una estructura propia declaran `sentences` explícitas.
"""

from __future__ import annotations


def L(
    title: str,
    topic: str,
    can_do: str,
    grammar: list[str],
    pattern: str,
    items: list[tuple[str, str, str]],
    sentences: list[tuple[str, str, str]] | None = None,
) -> dict:
    spec = {
        "title": title,
        "topic": topic,
        "can_do": can_do,
        "grammar": grammar,
        "pattern": pattern,
        "items": items,
    }
    if sentences is not None:
        spec["sentences"] = sentences
    return spec


BLOCKS: list[tuple[str, list[dict]]] = [
    (
        "Recuerdos y pasado",
        [
            L(
                "Repaso: mi rutina",
                "la rutina diaria",
                "Puedo describir mi rutina y decir a qué hora hago cada cosa.",
                ["present-simple", "adverbs-frequency"],
                "routine",
                [
                    (
                        "me levanto a las siete",
                        "get up at seven",
                        "je me lève à sept heures",
                    ),
                    (
                        "desayuno con mi familia",
                        "have breakfast with my family",
                        "je prends le petit-déjeuner avec ma famille",
                    ),
                    (
                        "trabajo desde casa",
                        "work from home",
                        "je travaille depuis la maison",
                    ),
                ],
            ),
            L(
                "Un día ocupado",
                "un día de trabajo",
                "Puedo contar cómo es un día normal y en qué momento ocurre cada cosa.",
                ["present-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "Por la mañana voy al trabajo en autobús.",
                        "In the morning I go to work by bus.",
                        "Le matin, je vais au travail en bus.",
                    ),
                    (
                        "Por la tarde tengo una reunión.",
                        "In the afternoon I have a meeting.",
                        "L'après-midi, j'ai une réunion.",
                    ),
                    (
                        "Por la noche ceno con mis amigos.",
                        "In the evening I have dinner with my friends.",
                        "Le soir, je dîne avec mes amis.",
                    ),
                ],
            ),
            L(
                "Verbos de rutina",
                "los objetos de la rutina",
                "Puedo nombrar objetos y acciones que necesito cada día.",
                ["present-simple"],
                "need",
                [
                    ("un despertador", "an alarm clock", "un réveil"),
                    ("un billete de autobús", "a bus ticket", "un ticket de bus"),
                    ("una ducha caliente", "a hot shower", "une douche chaude"),
                ],
            ),
            L(
                "Pasado simple regular",
                "las acciones de ayer",
                "Puedo contar acciones terminadas que hice ayer.",
                ["past-simple"],
                "past",
                [
                    (
                        "visité a mi familia",
                        "visited my family",
                        "j'ai visité ma famille",
                    ),
                    (
                        "caminé por el parque",
                        "walked in the park",
                        "je me suis promené dans le parc",
                    ),
                    (
                        "cené en un restaurante",
                        "had dinner in a restaurant",
                        "j'ai dîné dans un restaurant",
                    ),
                ],
            ),
            L(
                "Pasado simple irregular",
                "los recuerdos del fin de semana",
                "Puedo usar verbos irregulares para contar recuerdos.",
                ["past-irregular"],
                "past",
                [
                    ("fui al mercado", "went to the market", "je suis allé au marché"),
                    ("hice la compra", "did the shopping", "j'ai fait les courses"),
                    ("vi a mis amigos", "saw my friends", "j'ai vu mes amis"),
                ],
            ),
            L(
                "Contar el fin de semana",
                "el fin de semana",
                "Puedo contar lo que hice el fin de semana.",
                ["past-simple", "past-irregular"],
                "weekend",
                [
                    (
                        "jugué al fútbol con mis amigos",
                        "played football with my friends",
                        "j'ai joué au football avec mes amis",
                    ),
                    (
                        "leí un libro en casa",
                        "read a book at home",
                        "j'ai lu un livre à la maison",
                    ),
                    (
                        "escuché música en el parque",
                        "listened to music in the park",
                        "j'ai écouté de la musique au parc",
                    ),
                ],
            ),
            L(
                "Escuchar: un viaje corto",
                "un viaje corto",
                "Puedo entender y describir cómo fue un viaje.",
                ["past-simple", "adjectives"],
                "trip",
                [
                    ("fue tranquilo", "was quiet", "était tranquille"),
                    ("fue interesante", "was interesting", "était intéressant"),
                    ("fue un poco largo", "was a bit long", "était un peu long"),
                ],
            ),
            L(
                "Escribir una postal",
                "una postal de vacaciones",
                "Puedo escribir una postal sencilla desde un lugar de vacaciones.",
                ["present-simple", "prepositions"],
                "postcard",
                [
                    ("en la playa", "at the beach", "à la plage"),
                    ("en un hotel pequeño", "in a small hotel", "dans un petit hôtel"),
                    ("con mis amigos", "with my friends", "avec mes amis"),
                ],
            ),
            L(
                "Integración: ayer y hoy",
                "los cambios en mi vida",
                "Puedo comparar lo que hacía antes con lo que hago ahora.",
                ["used-to", "present-simple"],
                "phrase",
                [
                    (
                        "Antes iba al trabajo en autobús; ahora voy en bicicleta.",
                        "I used to go to work by bus; now I cycle.",
                        "Avant, j'allais au travail en bus ; maintenant, je vais à vélo.",
                    ),
                    (
                        "Antes comía en casa; ahora como en la oficina.",
                        "I used to eat at home; now I eat at the office.",
                        "Avant, je mangeais à la maison ; maintenant, je mange au bureau.",
                    ),
                    (
                        "Antes veía la televisión; ahora leo un libro.",
                        "I used to watch TV; now I read a book.",
                        "Avant, je regardais la télévision ; maintenant, je lis un livre.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi semana",
                "mi semana",
                "Puedo contar lo que hice durante la semana.",
                ["past-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "El lunes empecé un curso de inglés.",
                        "On Monday I started an English course.",
                        "Lundi, j'ai commencé un cours d'anglais.",
                    ),
                    (
                        "El miércoles visité a mi familia.",
                        "On Wednesday I visited my family.",
                        "Mercredi, j'ai rendu visite à ma famille.",
                    ),
                    (
                        "El viernes terminé un proyecto.",
                        "On Friday I finished a project.",
                        "Vendredi, j'ai terminé un projet.",
                    ),
                ],
            ),
        ],
    ),
]

CAPSTONE_FR: list[dict] = [
    L(
        "Diálogo final: una conversación",
        "una conversación cotidiana",
        "Puedo mantener una conversación sencilla y responder a preguntas.",
        ["questions", "present-simple"],
        "phrase",
        [
            (
                "¿Qué haces los fines de semana?",
                "What do you do at the weekend?",
                "Que fais-tu le week-end ?",
            ),
            (
                "Normalmente quedo con amigos y voy al cine.",
                "I usually meet friends and go to the cinema.",
                "D'habitude, je retrouve des amis et je vais au cinéma.",
            ),
            (
                "¿Te gustaría venir con nosotros?",
                "Would you like to come with us?",
                "Est-ce que tu voudrais venir avec nous ?",
            ),
        ],
    ),
    L(
        "Autoevaluación y metas",
        "mis metas de aprendizaje",
        "Puedo hablar de lo que he aprendido y de mis próximos objetivos.",
        ["present-perfect", "going-to"],
        "phrase",
        [
            (
                "He aprendido a contar historias en pasado.",
                "I have learned to tell stories in the past.",
                "J'ai appris à raconter des histoires au passé.",
            ),
            (
                "Todavía necesito practicar la pronunciación.",
                "I still need to practise pronunciation.",
                "J'ai encore besoin de pratiquer la prononciation.",
            ),
            (
                "El próximo mes voy a leer un libro en inglés.",
                "Next month I'm going to read a book in English.",
                "Le mois prochain, je vais lire un livre en anglais.",
            ),
        ],
    ),
]
