"""Fábrica de contenido B2 (F4) para los cuatro cursos.

Cada lección B2 tiene 10 ejercicios, dos focos gramaticales, escenario,
canDo, banda 3000-5000, nota cultural, microficha, glosario y feedback.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

from a2_factory import safe_distractors
from b1_factory import B1_TARGET_INDEX, content_tokens, make_cloze, tokens_for

VOCAB_BAND = "3000-5000"
MAX_TOKENS = 30

GRAMMAR_B2 = {
    "hedging": {
        "id": "gp-b2-hedging",
        "label": "Hedging",
        "description": "Matizar una opinión con I'm afraid, it seems o might.",
    },
    "future-perfect": {
        "id": "gp-b2-future-perfect",
        "label": "Future perfect",
        "description": "Will have + participio sitúa un resultado antes de una fecha.",
    },
    "passive-voice": {
        "id": "gp-b2-passive-voice",
        "label": "Passive voice",
        "description": "Be + participio desplaza el foco a la acción o al resultado.",
    },
    "reported-speech": {
        "id": "gp-b2-reported-speech",
        "label": "Reported speech",
        "description": "El estilo indirecto ajusta tiempos, pronombres y referencias.",
    },
    "inversion": {
        "id": "gp-b2-inversion",
        "label": "Inversion",
        "description": "La inversión enfática coloca el auxiliar antes del sujeto.",
    },
    "cleft-sentences": {
        "id": "gp-b2-cleft-sentences",
        "label": "Cleft sentences",
        "description": "It is... / What... destaca una parte de la información.",
    },
    "subjunctive": {
        "id": "gp-b2-subjunctive",
        "label": "Subjunctive",
        "description": "El subjuntivo expresa deseos, dudas y valoraciones.",
    },
    "mixed-conditionals": {
        "id": "gp-b2-mixed-conditionals",
        "label": "Mixed conditionals",
        "description": "Combina pasado y presente para hablar de consecuencias hipotéticas.",
    },
    "non-defining-relative": {
        "id": "gp-b2-non-defining-relative",
        "label": "Non-defining relative clauses",
        "description": "Las relativas explicativas añaden información con comas.",
    },
    "participle-clauses": {
        "id": "gp-b2-participle-clauses",
        "label": "Participle clauses",
        "description": "Las cláusulas de participio reducen y conectan ideas.",
    },
    "modal-perfect": {
        "id": "gp-b2-modal-perfect",
        "label": "Modal perfect",
        "description": "Must have, might have y should have especulan sobre el pasado.",
    },
    "perfect-gerund": {
        "id": "gp-b2-perfect-gerund",
        "label": "Perfect gerund",
        "description": "Having + participio expresa una acción anterior.",
    },
    "concession": {
        "id": "gp-b2-concession",
        "label": "Concession",
        "description": "Although, despite y however introducen concesiones.",
    },
    "purpose": {
        "id": "gp-b2-purpose",
        "label": "Purpose",
        "description": "To, in order to y so that expresan finalidad.",
    },
    "advanced-comparatives": {
        "id": "gp-b2-advanced-comparatives",
        "label": "Advanced comparatives",
        "description": "The more..., the more... y comparaciones complejas.",
    },
    "advanced-quantifiers": {
        "id": "gp-b2-advanced-quantifiers",
        "label": "Advanced quantifiers",
        "description": "Little, few, plenty of y most matizan la cantidad.",
    },
    "articles": {
        "id": "gp-b2-articles",
        "label": "Articles",
        "description": "El uso del artículo cambia con nombres abstractos y genéricos.",
    },
    "phrasal-verbs": {
        "id": "gp-b2-phrasal-verbs",
        "label": "Phrasal verbs",
        "description": "Los verbos compuestos cambian de significado con la partícula.",
    },
    "collocations": {
        "id": "gp-b2-collocations",
        "label": "Collocations",
        "description": "Las combinaciones léxicas naturales suenan más precisas.",
    },
    "register": {
        "id": "gp-b2-register",
        "label": "Register",
        "description": "El registro formal o informal condiciona las elecciones léxicas.",
    },
    "speculation": {
        "id": "gp-b2-speculation",
        "label": "Speculation",
        "description": "May, might y could expresan distintos grados de posibilidad.",
    },
    "distancing": {
        "id": "gp-b2-distancing",
        "label": "Distancing",
        "description": "It is said, apparently y according to matizan la fuente.",
    },
    "emphasis": {
        "id": "gp-b2-emphasis",
        "label": "Emphasis",
        "description": "Do, indeed y absolutely refuerzan el mensaje.",
    },
    "ellipsis": {
        "id": "gp-b2-ellipsis",
        "label": "Ellipsis",
        "description": "La elipsis evita repetir elementos ya conocidos.",
    },
    "discourse-markers": {
        "id": "gp-b2-discourse-markers",
        "label": "Discourse markers",
        "description": "Los marcadores ordenan y conectan el discurso.",
    },
    "nominalisation": {
        "id": "gp-b2-nominalisation",
        "label": "Nominalisation",
        "description": "Convertir verbos en nombres hace el texto más formal.",
    },
    "negative-inversion": {
        "id": "gp-b2-negative-inversion",
        "label": "Negative inversion",
        "description": "Never, rarely y not only invierten el orden del sujeto.",
    },
    "third-conditional": {
        "id": "gp-b2-third-conditional",
        "label": "Third conditional",
        "description": "If + past perfect, would have + participio expresa hipótesis pasadas.",
    },
}

SUFFIXES = {
    "es": [
        " Sin embargo, conviene matizarlo.",
        " Dicho esto, depende del contexto.",
        " En otras palabras, merece la pena considerarlo.",
        " Me temo que puede ser más complicado.",
        " Sinceramente, tengo sentimientos encontrados.",
        " Parece que esto está cambiando.",
        " Desde mi punto de vista, es una opción razonable.",
        " No obstante, también tiene ventajas.",
        " A fin de cuentas, hay que priorizar.",
        " En resumen, es un tema complejo.",
    ],
    "en": [
        " However, it is worth qualifying that.",
        " That said, it depends on the context.",
        " In other words, it is worth considering.",
        " I'm afraid it may be more complicated.",
        " To be honest, I have mixed feelings.",
        " It seems that this is changing.",
        " From my point of view, it is a reasonable option.",
        " Nevertheless, there are also advantages.",
        " At the end of the day, we need to prioritise.",
        " In short, it is a complex issue.",
    ],
    "fr": [
        " Cependant, il convient de nuancer.",
        " Cela dit, cela dépend du contexte.",
        " En d'autres termes, cela vaut la peine d'être considéré.",
        " Je crains que ce soit plus compliqué.",
        " Pour être honnête, j'ai des sentiments mitigés.",
        " Il semble que cela change.",
        " De mon point de vue, c'est une option raisonnable.",
        " Néanmoins, il y a aussi des avantages.",
        " En fin de compte, il faut prioriser.",
        " En bref, c'est un sujet complexe.",
    ],
    "pt": [
        " No entanto, vale a pena qualificar.",
        " Dito isto, depende do contexto.",
        " Por outras palavras, vale a pena considerar.",
        " Receio que possa ser mais complicado.",
        " Para ser honesto, tenho sentimentos mistos.",
        " Parece que isto está a mudar.",
        " Do meu ponto de vista, é uma opção razoável.",
        " Ainda assim, também há vantagens.",
        " No fim do dia, é preciso priorizar.",
        " Em resumo, é um tema complexo.",
    ],
    "ja": [
        " しかし 、それを 明確 に する 必要 が あります 。",
        " そう は いえ 、状況 に よります 。",
        " つまり 、検討 する 価値 が あります 。",
        " 残念 ながら 、もっと 複雑 かも しれません 。",
        " 正直 に 言う と 、複雑 な 気持ち です 。",
        " これ は 変わって きて いる よう です 。",
        " 私 の 観点 で は 、妥当 な 選択 です 。",
        " それでも 、利点 も あります 。",
        " 結局 、優先 する 必要 が あります 。",
        " 要するに 、複雑 な 問題 です 。",
    ],
}


def extend_sentence(sentence: str, lang: str, index: int) -> str:
    suffix = SUFFIXES[lang][index % len(SUFFIXES[lang])]
    text = sentence.rstrip()
    terminal = ("。", "！", "？") if lang == "ja" else (".", "!", "?")
    if not text.endswith(terminal):
        text += "。" if lang == "ja" else "."
    return text + suffix


def resolve_grammar(keys: list[str]) -> list[dict]:
    missing = [key for key in keys if key not in GRAMMAR_B2]
    if missing:
        raise ValueError(f"Focos gramaticales B2 desconocidos: {missing}")
    return [GRAMMAR_B2[key] for key in keys]


def semantic_pairs(sentences: list[str], labels: list[tuple[str, str]]) -> list[dict]:
    return [
        {
            "left": sentence,
            "right": label,
            "register": register,
            "nuance": "Clasifica la función comunicativa de la intervención.",
        }
        for sentence, (label, register) in zip(sentences, labels, strict=True)
    ]


def build_lesson(
    course_id: str,
    unit_number: int,
    lesson_number: int,
    spec: dict,
    lang: str,
) -> dict:
    lesson_id = f"{course_id}-b2-u{unit_number:02d}-l{lesson_number:02d}"
    items = spec["items"]
    if len(items) != 3:
        raise ValueError(f"{lesson_id}: se esperaban 3 pares de frases")
    target_index = B1_TARGET_INDEX[lang]
    target_items = [item[target_index] for item in items]
    if len(set(target_items)) != 3:
        raise ValueError(f"{lesson_id}: frases duplicadas")
    sentences = [(item[0], item[target_index]) for item in items]
    grammar = resolve_grammar(spec["grammar"])
    if len(grammar) != 2:
        raise ValueError(f"{lesson_id}: B2 exige 2 focos gramaticales")
    cultural = spec["cultural"]
    target_sentences = [sentence[1] for sentence in sentences]
    s0_tokens = tokens_for(target_sentences[0], lang)
    s2_tokens = tokens_for(target_sentences[2], lang)
    first_es, first_target = items[0][0], target_items[0]
    rule_ref = grammar[0]["id"]
    cloze = make_cloze(spec.get("cloze_sentence", target_sentences[0]), lang, grammar)
    free_keywords = content_tokens(" ".join(target_sentences), lang)[:3]
    if not free_keywords:
        free_keywords = [target_items[2]]
    model_answer = " ".join(target_sentences)

    def feedback(kind: str, explanation: str, rule: str = rule_ref) -> dict:
        return {"type": kind, "explanation": explanation, "ruleRef": rule}

    lesson = {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "B2",
        "scenario": f"Situación comunicativa avanzada sobre {spec['topic']}.",
        "canDo": f"Puedo argumentar y matizar sobre {spec['topic']}.",
        "grammarPoints": grammar,
        "vocabFrequencyBand": VOCAB_BAND,
        "culturalNotes": [
            {
                "idiom": cultural["idiom"],
                "note": cultural["note"],
                "translation": cultural["translation"],
            }
        ],
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Usar vocabulario avanzado de {spec['topic']}.",
                f"Aplicar {grammar[0]['label'].lower()} y {grammar[1]['label'].lower()}.",
                f"Argumentar y matizar sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": "Lengua en contexto",
                    "body": (
                        f"{grammar[0]['label']}: {grammar[0]['description']} "
                        f"{grammar[1]['label']}: {grammar[1]['description']}"
                    ),
                    "examples": [
                        {"source": sentences[0][0], "target": sentences[0][1]},
                        {"source": sentences[1][0], "target": sentences[1][1]},
                    ],
                }
            ],
        },
        "glossary": [
            {"term": target, "translations": [source]}
            for source, target in zip([item[0] for item in items], target_items, strict=True)
        ],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cuál es la opción correcta para «{first_es}»?",
                "options": target_items,
                "correctIndex": 0,
                "feedbackOnError": feedback(
                    "vocabulary",
                    f"«{first_es}» se dice «{first_target}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e02",
                "tokens": (s0_tokens[1:] + s0_tokens[:1]) if len(s0_tokens) > 1 else s0_tokens,
                "answerTokens": s0_tokens,
                "sourcePhrase": sentences[0][0],
                "feedbackOnError": feedback(
                    "word_order",
                    f"El orden correcto es «{' '.join(s0_tokens)}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e03",
                "passage": cloze["passage"],
                "blanks": cloze["blanks"],
                "feedbackOnError": cloze["feedbackOnError"],
                "feedbackOnSuccess": cloze["feedbackOnSuccess"],
            },
            {
                "type": "SemanticMatch",
                "id": f"{lesson_id}-e04",
                "instructions": "Clasifica la función comunicativa de cada intervención.",
                "pairs": semantic_pairs(
                    target_sentences,
                    [
                        ("Planteamiento", "formal"),
                        ("Concesión", "neutral"),
                        ("Conclusión", "informal"),
                    ],
                ),
                "feedbackOnError": feedback(
                    "vocabulary",
                    "Cada intervención cumple una función comunicativa distinta.",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has identificado la función de cada intervención."},
            },
            {
                "type": "SemanticMatch",
                "id": f"{lesson_id}-e05",
                "instructions": "Distingue el registro de cada intervención.",
                "pairs": semantic_pairs(
                    target_sentences,
                    [
                        ("Formal", "formal"),
                        ("Neutral", "neutral"),
                        ("Informal", "informal"),
                    ],
                ),
                "feedbackOnError": feedback(
                    "register",
                    "El registro cambia según el contexto y la relación con el interlocutor.",
                    grammar[1]["id"],
                ),
                "feedbackOnSuccess": {"explanation": "Has distinguido el registro de cada intervención."},
            },
            {
                "type": "AudioOnlyPrompt",
                "id": f"{lesson_id}-e06",
                "audioText": target_sentences[1],
                "question": "¿Qué ha dicho el hablante?",
                "options": [target_sentences[1], target_sentences[0], target_sentences[2]],
                "correctIndex": 0,
                "allowSlowPlayback": True,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[1]}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has comprendido el mensaje del audio."},
            },
            {
                "type": "AudioOnlyPrompt",
                "id": f"{lesson_id}-e07",
                "audioText": target_sentences[2],
                "question": "¿Cuál es la idea principal?",
                "options": [target_sentences[2], target_sentences[0], target_sentences[1]],
                "correctIndex": 0,
                "allowSlowPlayback": False,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[2]}».",
                    grammar[1]["id"],
                ),
                "feedbackOnSuccess": {"explanation": "Has identificado la idea principal."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e08",
                "audioText": target_sentences[2],
                "transcription": target_sentences[2],
                "feedbackOnError": feedback(
                    "spelling",
                    f"Escribe exactamente «{target_sentences[2]}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e09",
                "targetPhrase": target_sentences[2],
                "audioText": target_sentences[2],
                "feedbackOnError": feedback(
                    "pronunciation",
                    f"Repite «{target_sentences[2]}» con claridad.",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
            },
            {
                "type": "FreeSpeakingPrompt",
                "id": f"{lesson_id}-e10",
                "prompt": f"Argumenta y matiza una opinión sobre {spec['topic']}.",
                "minTokens": 20,
                "conceptClusters": [[keyword] for keyword in free_keywords],
                "modelAnswer": model_answer,
                "selfAssessmentCriteria": [
                    "¿La respuesta argumenta y responde a la tarea?",
                    "¿Usa las dos estructuras gramaticales de la lección?",
                    "¿Incluye una concesión o una matización?",
                ],
                "grammarRuleRefs": [grammar[0]["id"], grammar[1]["id"]],
                "timeLimitSeconds": 90,
                "minWpm": 70,
                "maxWpm": 160,
                "feedbackOnError": feedback(
                    "grammar",
                    "La respuesta modelo desarrolla una opinión con matices y una concesión.",
                    rule_ref,
                ),
            },
        ],
    }
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
        if len(lesson["exercises"]) != 10:
            raise ValueError(f"{lesson['id']}: se esperaban 10 ejercicios")
        if len(lesson["grammarPoints"]) != 2:
            raise ValueError(f"{lesson['id']}: se esperaban 2 focos gramaticales")
        if sum(1 for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt") != 1:
            raise ValueError(f"{lesson['id']}: se esperaba un FreeSpeakingPrompt")
        if not lesson["culturalNotes"]:
            raise ValueError(f"{lesson['id']}: falta nota cultural")
        for exercise in lesson["exercises"]:
            if exercise.get("feedbackOnError") is None:
                raise ValueError(f"{exercise['id']}: falta feedbackOnError")
            if exercise["type"] in {"MultipleChoice", "AudioOnlyPrompt"}:
                if len(set(exercise["options"])) != len(exercise["options"]):
                    raise ValueError(f"{exercise['id']}: opciones duplicadas")
            if exercise["type"] == "SemanticMatch" and len(exercise["pairs"]) < 2:
                raise ValueError(f"{exercise['id']}: SemanticMatch con menos de 2 pares")
            if exercise["type"] == "ClozeDragDrop" and len(exercise["blanks"]) < 2:
                raise ValueError(f"{exercise['id']}: cloze con menos de 2 huecos")
            texts: list[str] = []
            for key in ("audioText", "transcription", "targetPhrase", "sourcePhrase", "modelAnswer"):
                value = exercise.get(key)
                if value:
                    texts.append(value)
            if exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            for text in texts:
                longest = max(
                    len(part.split())
                    for part in re.split(r"[.!?。！？]+", text)
                    if part.strip()
                )
                if longest > MAX_TOKENS:
                    raise ValueError(
                        f"{exercise['id']}: frase de {longest} tokens (máximo {MAX_TOKENS})"
                    )
    return {
        "id": f"{course_id}-b2-u{unit_number:02d}",
        "title": f"B2 · {block_title}",
        "lessons": lessons,
    }
