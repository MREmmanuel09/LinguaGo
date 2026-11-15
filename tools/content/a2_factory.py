"""Fábrica de contenido A2 (F2) para ES→EN y ES→FR.

Genera lecciones A2 con el contrato v2: 7 ejercicios, microficha, glosario,
feedback de error y hasta dos focos gramaticales. La unidad de producción es
el bloque de 10 lecciones; los ids nuevos siguen el patrón
`es-<idioma>-a2-u<bloque>-l<lección>`.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TARGET_INDEX = {"en": 1, "fr": 2}
VOCAB_BAND = "2000-3000"
MAX_TOKENS = 12

GRAMMAR = {
    "present-simple": {
        "id": "gp-present-simple",
        "label": "Presente simple",
        "description": "Usa el presente simple para rutinas y hechos habituales.",
    },
    "adverbs-frequency": {
        "id": "gp-adverbs-frequency",
        "label": "Adverbios de frecuencia",
        "description": "Always, usually y sometimes indican con qué frecuencia ocurre algo.",
    },
    "time-expressions": {
        "id": "gp-time-expressions",
        "label": "Expresiones de tiempo",
        "description": "In the morning, on Monday y at seven sitúan la acción en el tiempo.",
    },
    "past-simple": {
        "id": "gp-past-simple",
        "label": "Pasado simple",
        "description": "El pasado simple describe acciones terminadas en un momento concreto.",
    },
    "past-irregular": {
        "id": "gp-past-irregular",
        "label": "Pasado simple irregular",
        "description": "Muchos verbos frecuentes forman el pasado sin -ed: go-went, see-saw.",
    },
    "past-continuous": {
        "id": "gp-past-continuous",
        "label": "Pasado continuo",
        "description": "Was/were + -ing describe una acción en desarrollo en el pasado.",
    },
    "past-simple-vs-continuous": {
        "id": "gp-past-simple-vs-continuous",
        "label": "Pasado simple y continuo",
        "description": "El continuo pinta el fondo; el simple marca el hecho que interrumpe.",
    },
    "going-to": {
        "id": "gp-going-to",
        "label": "Going to",
        "description": "Going to expresa planes e intenciones ya decididos.",
    },
    "present-continuous-future": {
        "id": "gp-present-continuous-future",
        "label": "Presente continuo para el futuro",
        "description": "El presente continuo con una expresión de tiempo describe citas y planes cerrados.",
    },
    "comparatives": {
        "id": "gp-comparatives",
        "label": "Comparativos",
        "description": "Compara con -er/more ... than o tan ... como.",
    },
    "superlatives": {
        "id": "gp-superlatives",
        "label": "Superlativos",
        "description": "El superlativo expresa el grado máximo: the best, the most interesting.",
    },
    "should": {
        "id": "gp-should",
        "label": "Should para consejos",
        "description": "Should y shouldn't dan consejos y recomendaciones.",
    },
    "have-to": {
        "id": "gp-have-to",
        "label": "Have to",
        "description": "Have to expresa obligación externa; don't have to, ausencia de obligación.",
    },
    "must": {
        "id": "gp-must",
        "label": "Must y mustn't",
        "description": "Must expresa obligación fuerte; mustn't, prohibición.",
    },
    "present-perfect": {
        "id": "gp-present-perfect",
        "label": "Presente perfecto",
        "description": "Have/has + participio conecta una experiencia pasada con el presente.",
    },
    "for-since": {
        "id": "gp-for-since",
        "label": "For y since",
        "description": "For indica duración; since, el punto de inicio.",
    },
    "first-conditional": {
        "id": "gp-first-conditional",
        "label": "Primera condicional",
        "description": "If + presente, will + infinitivo expresa una condición real y su consecuencia.",
    },
    "will": {
        "id": "gp-will",
        "label": "Will",
        "description": "Will expresa predicciones, promesas y decisiones espontáneas.",
    },
    "used-to": {
        "id": "gp-used-to",
        "label": "Used to",
        "description": "Used to describe hábitos pasados que ya no ocurren.",
    },
    "too-much": {
        "id": "gp-too-much",
        "label": "Too much / too many",
        "description": "Too much acompaña incontables; too many, contables.",
    },
    "enough": {
        "id": "gp-enough",
        "label": "Enough",
        "description": "Enough indica cantidad suficiente; not enough, insuficiente.",
    },
    "polite-requests": {
        "id": "gp-polite-requests",
        "label": "Peticiones corteses",
        "description": "Could you...? y Would you...? suavizan una petición.",
    },
    "agree-disagree": {
        "id": "gp-agree-disagree",
        "label": "Acuerdo y desacuerdo",
        "description": "I agree, I don't agree y I think... expresan una postura.",
    },
    "relative-clauses": {
        "id": "gp-relative-clauses",
        "label": "Oraciones de relativo",
        "description": "Who, which y where añaden información sobre una persona, cosa o lugar.",
    },
    "may-might": {
        "id": "gp-may-might",
        "label": "May y might",
        "description": "May y might expresan posibilidad en el presente o el futuro.",
    },
    "verb-patterns": {
        "id": "gp-verb-patterns",
        "label": "Verbo + infinitivo o gerundio",
        "description": "Algunos verbos van seguidos de infinitivo y otros de gerundio.",
    },
    "countable-uncountable": {
        "id": "gp-countable-uncountable",
        "label": "Contables e incontables",
        "description": "Los contables tienen plural; los incontables no se cuentan con unidades.",
    },
    "some-any": {
        "id": "gp-some-any",
        "label": "Some y any",
        "description": "Some aparece en afirmativas; any, en negativas y preguntas.",
    },
    "questions": {
        "id": "gp-questions",
        "label": "Preguntas",
        "description": "El orden auxiliar + sujeto + verbo forma preguntas claras.",
    },
    "prepositions": {
        "id": "gp-prepositions",
        "label": "Preposiciones de lugar",
        "description": "In, on y at sitúan personas y objetos en el espacio.",
    },
    "adjectives": {
        "id": "gp-adjectives",
        "label": "Adjetivos",
        "description": "El adjetivo describe el sustantivo y no cambia de forma en inglés.",
    },
    "opinions": {
        "id": "gp-opinions",
        "label": "Expresar opiniones",
        "description": "I think, in my opinion y it seems to me introducen una opinión.",
    },
    "quantity": {
        "id": "gp-quantity",
        "label": "Cantidad",
        "description": "Some, much, many, a lot of y enough expresan cantidad.",
    },
    "imperatives": {
        "id": "gp-imperatives",
        "label": "Imperativos",
        "description": "El imperativo da instrucciones y consejos directos.",
    },
}


def tokens(text: str) -> list[str]:
    return re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text)


def fr_de(phrase: str) -> str:
    """Contrae la preposición francesa de con el artículo del sintagma."""
    if phrase.startswith("un "):
        return "d'un " + phrase[3:]
    if phrase.startswith("une "):
        return "d'une " + phrase[4:]
    if phrase.startswith("le "):
        return "du " + phrase[3:]
    if phrase.startswith("la "):
        return "de la " + phrase[4:]
    if phrase.startswith("l'"):
        return "de " + phrase
    if phrase.startswith("les "):
        return "des " + phrase[4:]
    if phrase[:1].lower() in "aeiouéèêàâîïôûù":
        return "d'" + phrase
    return "de " + phrase


def sentences_for(pattern: str, items: list[tuple[str, str, str]], lang: str) -> list[tuple[str, str]]:
    es = [item[0] for item in items]
    target_index = TARGET_INDEX[lang]
    target = [item[target_index] for item in items]

    def pair(es_text: str, target_text: str) -> tuple[str, str]:
        return es_text, target_text

    if pattern == "phrase":
        return [pair(es[i], target[i]) for i in range(3)]
    if pattern == "routine":
        return [
            pair(
                f"Normalmente {es[i]}",
                f"I usually {target[i]}" if lang == "en" else f"D'habitude, {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "past":
        return [
            pair(
                f"Ayer {es[i]}",
                f"Yesterday I {target[i]}" if lang == "en" else f"Hier, {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "weekend":
        return [
            pair(
                f"El sábado {es[i]}",
                f"On Saturday I {target[i]}" if lang == "en" else f"Samedi, {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "trip":
        return [
            pair(
                f"El viaje {es[i]}",
                f"The trip {target[i]}" if lang == "en" else f"Le voyage {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "postcard":
        return [
            pair(
                f"Estoy {es[i]}",
                f"I am {target[i]}" if lang == "en" else f"Je suis {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "going_to":
        return [
            pair(
                f"Este fin de semana voy a {es[i]}",
                f"This weekend I'm going to {target[i]}"
                if lang == "en"
                else f"Ce week-end, je vais {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "future":
        return [
            pair(
                f"Mañana {es[i]}",
                f"Tomorrow I'm {target[i]}" if lang == "en" else f"Demain, {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "should":
        return [
            pair(
                f"Deberías {es[i]}",
                f"You should {target[i]}" if lang == "en" else f"Tu devrais {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "have_to":
        return [
            pair(
                f"Tengo que {es[i]}",
                f"I have to {target[i]}" if lang == "en" else f"Je dois {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "must":
        return [
            pair(
                f"Debes {es[i]}",
                f"You must {target[i]}" if lang == "en" else f"Tu dois {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "present_perfect":
        return [
            pair(
                f"He {es[i]}",
                f"I have {target[i]}" if lang == "en" else f"J'ai {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "will":
        return [
            pair(
                f"Mañana {es[i]}",
                f"Tomorrow I will {target[i]}" if lang == "en" else f"Demain, je {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "used_to":
        return [
            pair(
                f"Antes {es[i]}",
                f"I used to {target[i]}" if lang == "en" else f"Avant, je {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "too_much":
        return [
            pair(
                f"Hay demasiado {es[i]}",
                f"There is too much {target[i]}"
                if lang == "en"
                else f"Il y a trop de {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "enough":
        return [
            pair(
                f"No hay suficiente {es[i]}",
                f"There isn't enough {target[i]}"
                if lang == "en"
                else f"Il n'y a pas assez de {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "polite":
        return [
            pair(
                f"¿Podrías {es[i]}?",
                f"Could you {target[i]}?" if lang == "en" else f"Pourrais-tu {target[i]} ?",
            )
            for i in range(3)
        ]
    if pattern == "agree":
        return [
            pair(
                f"Estoy de acuerdo con {es[i]}",
                f"I agree with {target[i]}" if lang == "en" else f"Je suis d'accord avec {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "relative":
        return [
            pair(
                f"La persona que {es[i]}",
                f"The person who {target[i]}" if lang == "en" else f"La personne qui {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "maybe":
        return [
            pair(
                f"Quizá {es[i]}",
                f"Maybe I {target[i]}" if lang == "en" else f"Peut-être que je {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "question":
        return [
            pair(
                f"¿Dónde está {es[i]}?",
                f"Where is {target[i]}?" if lang == "en" else f"Où est {target[i]} ?",
            )
            for i in range(3)
        ]
    if pattern == "want":
        return [
            pair(
                f"Quiero {es[i]}",
                f"I want to {target[i]}" if lang == "en" else f"Je veux {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "opinion":
        return [
            pair(
                f"Me parece {es[i]}",
                f"It seems {target[i]}" if lang == "en" else f"Ça me semble {target[i]}",
            )
            for i in range(3)
        ]
    if pattern == "need":
        return [
            pair(
                f"Necesito {es[i]}",
                f"I need {target[i]}" if lang == "en" else f"J'ai besoin {fr_de(target[i])}",
            )
            for i in range(3)
        ]
    raise ValueError(f"Patrón desconocido: {pattern}")


def safe_distractors(correct: list[str], candidates: list[list[str]], lang: str) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    fallbacks = (
        ["very", "today", "here", "now", "also", "too"]
        if lang == "en"
        else ["très", "aujourd'hui", "ici", "maintenant", "aussi", "toujours"]
    )
    for fallback in fallbacks:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def resolve_grammar(keys: list[str]) -> list[dict]:
    missing = [key for key in keys if key not in GRAMMAR]
    if missing:
        raise ValueError(f"Focos gramaticales desconocidos: {missing}")
    return [GRAMMAR[key] for key in keys]


def grammar_sentence(grammar: list[dict]) -> str:
    if not grammar:
        return ""
    return " ".join(f"{point['label']}: {point['description']}" for point in grammar)


def build_lesson(
    course_id: str,
    unit_number: int,
    lesson_number: int,
    spec: dict,
    lang: str,
) -> dict:
    lesson_id = f"{course_id}-a2-u{unit_number:02d}-l{lesson_number:02d}"
    items = spec["items"]
    if len(items) != 3:
        raise ValueError(f"{lesson_id}: se esperaban 3 pares de vocabulario")
    target_index = TARGET_INDEX[lang]
    target_items = [item[target_index] for item in items]
    if len(set(target_items)) != 3:
        raise ValueError(f"{lesson_id}: vocabulario duplicado en la lección")
    sentences = spec.get("sentences")
    if sentences is None:
        sentences = sentences_for(spec["pattern"], items, lang)
    if len(sentences) != 3:
        raise ValueError(f"{lesson_id}: se esperaban 3 frases")
    target_sentences = [sentence[1] for sentence in sentences]
    if len(set(target_sentences)) != 3:
        raise ValueError(f"{lesson_id}: frases duplicadas en la lección")
    grammar = resolve_grammar(spec.get("grammar", []))
    if len(grammar) > 2:
        raise ValueError(f"{lesson_id}: A2 admite como máximo 2 focos gramaticales")

    first_es, first_target = items[0][0], target_items[0]
    s0_tokens = tokens(target_sentences[0])
    s1_tokens = tokens(target_sentences[1])
    s2_tokens = tokens(target_sentences[2])
    rule_ref = grammar[0]["id"] if grammar else None

    def feedback(
        kind: str,
        explanation: str,
        grammar_rule: bool = False,
    ) -> dict:
        payload = {"type": kind, "explanation": explanation}
        if grammar_rule and rule_ref is not None:
            payload["ruleRef"] = rule_ref
        return payload

    micro_sections = [
        {
            "heading": "Foco gramatical" if grammar else "Palabras y frases",
            "body": grammar_sentence(grammar)
            or f"Vocabulario y expresiones de {spec['topic']}.",
            "examples": [
                {"source": sentences[0][0], "target": sentences[0][1]},
                {"source": sentences[1][0], "target": sentences[1][1]},
            ],
        }
    ]
    if grammar:
        micro_sections.append(
            {
                "heading": "En contexto",
                "body": f"Observa cómo se usa {grammar[0]['label'].lower()} en una situación de {spec['topic']}.",
                "examples": [
                    {"source": sentences[2][0], "target": sentences[2][1]},
                ],
            }
        )

    lesson = {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A2",
        "vocabFrequencyBand": VOCAB_BAND,
        "canDo": spec["can_do"],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']}.",
                f"Producir un mensaje breve sobre {spec['topic']}.",
            ],
            "sections": micro_sections,
        },
        "glossary": [
            {"term": target, "translations": [source]}
            for source, target in zip([item[0] for item in items], target_items, strict=True)
        ],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}»?",
                "options": target_items,
                "correctIndex": 0,
                "feedbackOnError": feedback(
                    "vocabulary",
                    f"«{first_es}» se dice «{first_target}».",
                ),
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": sentences[0][0],
                "correctTokens": s0_tokens,
                "distractorTokens": safe_distractors(
                    s0_tokens,
                    [s1_tokens, s2_tokens],
                    lang,
                ),
                "feedbackOnError": feedback(
                    "grammar" if grammar else "word_order",
                    f"El orden correcto es «{' '.join(s0_tokens)}»."
                    + (f" {grammar[0]['label']}: {grammar[0]['description']}" if grammar else ""),
                    grammar_rule=True,
                ),
                "feedbackOnSuccess": {
                    "explanation": "La frase conserva el orden y el significado."
                },
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (s1_tokens[1:] + s1_tokens[:1]) if len(s1_tokens) > 1 else s1_tokens,
                "answerTokens": s1_tokens,
                "sourcePhrase": sentences[1][0],
                "feedbackOnError": feedback(
                    "grammar" if grammar else "word_order",
                    f"La frase correcta es «{' '.join(s1_tokens)}»."
                    + (f" {grammar[0]['label']}: {grammar[0]['description']}" if grammar else ""),
                    grammar_rule=True,
                ),
                "feedbackOnSuccess": {
                    "explanation": "El orden produce una frase natural."
                },
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [
                    {"left": source, "right": target}
                    for source, target in zip(
                        [item[0] for item in items],
                        target_items,
                        strict=True,
                    )
                ],
                "feedbackOnError": feedback(
                    "vocabulary",
                    "Cada expresión tiene una traducción distinta.",
                ),
                "feedbackOnSuccess": {
                    "explanation": "Has asociado cada expresión con su traducción."
                },
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": target_sentences[2],
                "options": target_sentences,
                "correctIndex": 0,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[2]}».",
                ),
                "feedbackOnSuccess": {
                    "explanation": "Has reconocido la frase del audio."
                },
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": target_sentences[2],
                "transcription": target_sentences[2],
                "feedbackOnError": feedback(
                    "spelling",
                    f"Escribe exactamente «{target_sentences[2]}».",
                ),
                "feedbackOnSuccess": {
                    "explanation": "La transcripción coincide con el audio."
                },
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": target_sentences[2],
                "audioText": target_sentences[2],
                "feedbackOnError": feedback(
                    "pronunciation",
                    f"Repite «{target_sentences[2]}» con claridad.",
                ),
                "feedbackOnSuccess": {
                    "explanation": "La frase se entiende y mantiene el ritmo."
                },
            },
        ],
    }
    if grammar:
        lesson["grammarPoints"] = grammar
    return lesson


def build_unit(
    course_id: str,
    unit_number: int,
    block_title: str,
    specs: list[dict],
    lang: str,
) -> dict:
    lessons = [
        build_lesson(course_id, unit_number, index, spec, lang)
        for index, spec in enumerate(specs, start=1)
    ]
    seen_titles: set[str] = set()
    for lesson in lessons:
        if lesson["title"] in seen_titles:
            raise ValueError(f"Título duplicado en la unidad {unit_number}: {lesson['title']}")
        seen_titles.add(lesson["title"])
        if len(lesson["exercises"]) != 7:
            raise ValueError(f"{lesson['id']}: se esperaban 7 ejercicios")
        if lesson["grammarPoints"] if "grammarPoints" in lesson else []:
            if len(lesson["grammarPoints"]) > 2:
                raise ValueError(f"{lesson['id']}: demasiados focos gramaticales")
        for exercise in lesson["exercises"]:
            if exercise.get("feedbackOnError") is None:
                raise ValueError(f"{exercise['id']}: falta feedbackOnError")
            if exercise["type"] in {"MultipleChoice", "ListenChoose"}:
                if len(set(exercise["options"])) != len(exercise["options"]):
                    raise ValueError(f"{exercise['id']}: opciones duplicadas")
            if exercise["type"] == "MatchPairs":
                lefts = [pair["left"] for pair in exercise["pairs"]]
                rights = [pair["right"] for pair in exercise["pairs"]]
                if len(set(lefts)) != len(lefts) or len(set(rights)) != len(rights):
                    raise ValueError(f"{exercise['id']}: pares duplicados")
            if exercise["type"] == "Translate":
                overlap = set(exercise["correctTokens"]).intersection(
                    exercise["distractorTokens"]
                )
                if overlap:
                    raise ValueError(
                        f"{exercise['id']}: distractor repetido {sorted(overlap)}"
                    )
            texts: list[str] = []
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            elif exercise["type"] in {"ListenType", "SpeakRepeat", "ListenChoose"}:
                texts.append(exercise["audioText"])
            for text in texts:
                longest = max(len(part.split()) for part in re.split(r"[.!?]+", text) if part.strip())
                if longest > MAX_TOKENS:
                    raise ValueError(
                        f"{exercise['id']}: frase de {longest} tokens (máximo {MAX_TOKENS})"
                    )
    return {
        "id": f"{course_id}-a2-u{unit_number:02d}",
        "title": f"A2 · {block_title}",
        "lessons": lessons,
    }


def find_insert_bracket(text: str, next_level: str) -> int:
    level_match = re.search(
        r'\n[ \t]*\{\n[ \t]*"id": "' + re.escape(next_level) + '"',
        text,
    )
    if level_match is None:
        raise SystemExit(f"No se encontró el nivel {next_level}")
    bracket = None
    for match in re.finditer(r"\n([ \t]*)\]", text[: level_match.start()]):
        bracket = match
    if bracket is None:
        raise SystemExit("No se encontró el cierre de units del nivel A2")
    return bracket.start(), len(bracket.group(1)) + 2


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def insert_unit(course_path: Path, next_level: str, unit: dict) -> bool:
    text = course_path.read_text(encoding="utf-8")
    if f'"id": "{unit["id"]}"' in text:
        return False
    bracket_start, indent = find_insert_bracket(text, next_level)
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), indent)
    text = text[:bracket_start] + ",\n" + unit_text + text[bracket_start:]
    course_path.write_text(text, encoding="utf-8", newline="\n")
    json.loads(course_path.read_text(encoding="utf-8"))
    return True


def course_path(course_id: str) -> Path:
    return ROOT / f"content/content-{course_id}/src/main/assets/courses/{course_id}/course.json"
