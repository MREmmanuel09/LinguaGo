"""Fábrica de contenido B1 (F3) para los cuatro cursos.

Cada lección B1 tiene 9 ejercicios, dos focos gramaticales, escenario,
canDo, banda léxica, nota cultural, microficha, glosario y feedback completo.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

from a2_factory import (
    course_path,
    find_insert_bracket,
    indent_block,
    insert_unit,
    safe_distractors,
    tokens,
    tokens_for,
)

ROOT = Path(__file__).resolve().parents[2]
B1_TARGET_INDEX = {"en": 1, "fr": 2, "pt": 3, "ja": 4}
VOCAB_BAND = "2000-3000"
MAX_TOKENS = 24

GRAMMAR_B1 = {
    "present-perfect": {
        "id": "gp-b1-present-perfect",
        "label": "Presente perfecto",
        "description": "Have/has + participio conecta experiencias pasadas con el presente.",
    },
    "comparatives": {
        "id": "gp-b1-comparatives",
        "label": "Comparativos",
        "description": "Compara con -er/more ... than y tan ... como.",
    },
    "passive-voice": {
        "id": "gp-b1-passive-voice",
        "label": "Voz pasiva",
        "description": "Be + participio desplaza el foco a la acción o al resultado.",
    },
    "past-continuous": {
        "id": "gp-b1-past-continuous",
        "label": "Pasado continuo",
        "description": "Was/were + -ing describe el fondo de una acción pasada.",
    },
    "first-conditional": {
        "id": "gp-b1-first-conditional",
        "label": "Primera condicional",
        "description": "If + presente, will + infinitivo expresa condiciones reales.",
    },
    "modals-obligation": {
        "id": "gp-b1-modals-obligation",
        "label": "Modales de obligación",
        "description": "Must, have to y should expresan obligación y consejo.",
    },
    "modals-advice": {
        "id": "gp-b1-modals-advice",
        "label": "Modales de consejo",
        "description": "Should, ought to y could ayudan a recomendar sin imponer.",
    },
    "future-forms": {
        "id": "gp-b1-future-forms",
        "label": "Formas de futuro",
        "description": "Will, going to y el presente continuo expresan planes y predicciones.",
    },
    "reported-speech": {
        "id": "gp-b1-reported-speech",
        "label": "Estilo indirecto",
        "description": "Reported speech ajusta tiempos, pronombres y expresiones de tiempo.",
    },
    "relative-clauses": {
        "id": "gp-b1-relative-clauses",
        "label": "Oraciones de relativo",
        "description": "Who, which, that y where añaden información sobre personas, cosas y lugares.",
    },
    "connectors-contrast": {
        "id": "gp-b1-connectors-contrast",
        "label": "Conectores de contraste",
        "description": "However, although y whereas contraponen ideas.",
    },
    "used-to": {
        "id": "gp-b1-used-to",
        "label": "Used to",
        "description": "Used to describe hábitos pasados que ya no ocurren.",
    },
    "countable-uncountable": {
        "id": "gp-b1-countable-uncountable",
        "label": "Contables e incontables",
        "description": "Los contables tienen plural; los incontables no se cuentan con unidades.",
    },
    "gerund-infinitive": {
        "id": "gp-b1-gerund-infinitive",
        "label": "Gerundio e infinitivo",
        "description": "Algunos verbos van seguidos de -ing y otros de to + infinitivo.",
    },
    "second-conditional": {
        "id": "gp-b1-second-conditional",
        "label": "Segunda condicional",
        "description": "If + pasado, would + infinitivo expresa situaciones hipotéticas.",
    },
    "indirect-questions": {
        "id": "gp-b1-indirect-questions",
        "label": "Preguntas indirectas",
        "description": "Could you tell me...? usa el orden de una afirmación.",
    },
    "modals-deduction": {
        "id": "gp-b1-modals-deduction",
        "label": "Modales de deducción",
        "description": "Must, might y can't expresan grados de certeza.",
    },
    "connectors-purpose": {
        "id": "gp-b1-connectors-purpose",
        "label": "Conectores de finalidad",
        "description": "To, in order to y so that expresan propósito.",
    },
    "present-simple": {
        "id": "gp-b1-present-simple",
        "label": "Presente simple",
        "description": "El presente simple describe rutinas y hechos habituales.",
    },
    "past-simple": {
        "id": "gp-b1-past-simple",
        "label": "Pasado simple",
        "description": "El pasado simple describe acciones terminadas.",
    },
    "modals-possibility": {
        "id": "gp-b1-modals-possibility",
        "label": "Modales de posibilidad",
        "description": "May, might y could expresan posibilidad.",
    },
}

SUFFIXES = {
    "es": [
        " Esto es importante para mí.",
        " En general, es así.",
        " Además, me ayuda a aprender.",
        " Por eso lo recomiendo.",
        " Aunque no siempre es fácil.",
        " Es una buena pregunta.",
        " Siempre intento mejorarlo.",
        " En mi caso, funciona.",
        " También tiene ventajas.",
        " Y quiero seguir practicando.",
    ],
    "en": [
        " This is important to me.",
        " In general, that is how it is.",
        " It also helps me learn.",
        " That is why I recommend it.",
        " Although it is not always easy.",
        " That is a good question.",
        " I always try to improve it.",
        " In my case, it works.",
        " It also has advantages.",
        " And I want to keep practising.",
    ],
    "fr": [
        " C'est important pour moi.",
        " En général, c'est ainsi.",
        " Cela m'aide aussi à apprendre.",
        " C'est pourquoi je le recommande.",
        " Même si ce n'est pas toujours facile.",
        " C'est une bonne question.",
        " J'essaie toujours de l'améliorer.",
        " Dans mon cas, ça marche.",
        " Cela a aussi des avantages.",
        " Et je veux continuer à pratiquer.",
    ],
    "pt": [
        " Isso é importante para mim.",
        " Em geral, é assim.",
        " Também me ajuda a aprender.",
        " É por isso que recomendo.",
        " Embora nem sempre seja fácil.",
        " É uma boa pergunta.",
        " Sempre tento melhorar.",
        " No meu caso, funciona.",
        " Também tem vantagens.",
        " E quero continuar praticando.",
    ],
    "ja": [
        " これ は 私 に とって 重要 です 。",
        " 一般的 に 、そう です 。",
        " それ は 学ぶ の に も 役立ちます 。",
        " だから おすすめ です 。",
        " いつも 簡単 で は ありません が 。",
        " いい 質問 です 。",
        " いつも 改善 しよう と 思っています 。",
        " 私 の 場合 は うまく いきます 。",
        " 利点 も あります 。",
        " これから も 練習 したい です 。",
    ],
}

STOPWORDS = {
    "en": {
        "the", "a", "an", "and", "or", "but", "to", "of", "in", "on", "at", "for",
        "with", "from", "by", "is", "are", "was", "were", "be", "been", "it", "this",
        "that", "these", "those", "i", "you", "he", "she", "we", "they", "my", "your",
        "his", "her", "our", "their", "as", "if", "so", "not", "no", "do", "does",
        "did", "have", "has", "had", "will", "would", "can", "could", "should", "may",
        "might", "must", "there", "here", "very", "also", "just", "about", "into",
    },
    "fr": {
        "le", "la", "les", "un", "une", "des", "et", "ou", "mais", "à", "de", "en",
        "sur", "dans", "pour", "avec", "par", "est", "sont", "était", "étaient", "être",
        "il", "elle", "nous", "vous", "ils", "elles", "je", "tu", "mon", "ma", "mes",
        "ton", "ta", "tes", "son", "sa", "ses", "notre", "votre", "leur", "ce", "cette",
        "ces", "qui", "que", "dont", "où", "si", "ne", "pas", "plus", "très", "aussi",
    },
    "pt": {
        "o", "a", "os", "as", "um", "uma", "uns", "umas", "e", "ou", "mas", "para",
        "de", "em", "no", "na", "nos", "nas", "por", "com", "é", "são", "era", "eram",
        "ser", "ele", "ela", "nós", "você", "eles", "elas", "eu", "tu", "meu", "minha",
        "meus", "minhas", "seu", "sua", "seus", "suas", "este", "esta", "estes", "estas",
        "que", "se", "não", "muito", "também", "já", "como", "mais", "menos",
    },
}

JA_PARTICLES = {
    "は", "が", "を", "に", "で", "と", "も", "の", "から", "まで", "へ", "より",
    "や", "か", "ね", "よ", "です", "ます", "ました", "ません", "た", "て", "し",
}


def extend_sentence(sentence: str, lang: str, index: int) -> str:
    suffix = SUFFIXES[lang][index % len(SUFFIXES[lang])]
    text = sentence.rstrip()
    terminal = ("。", "！", "？") if lang == "ja" else (".", "!", "?")
    if not text.endswith(terminal):
        text += "。" if lang == "ja" else "."
    return text + suffix


def resolve_grammar(keys: list[str]) -> list[dict]:
    missing = [key for key in keys if key not in GRAMMAR_B1]
    if missing:
        raise ValueError(f"Focos gramaticales B1 desconocidos: {missing}")
    return [GRAMMAR_B1[key] for key in keys]


def content_tokens(text: str, lang: str) -> list[str]:
    values = tokens_for(text, lang)
    if lang == "ja":
        return [value for value in values if value not in JA_PARTICLES and len(value) > 1]
    stop = STOPWORDS.get(lang, set())
    return [value for value in values if value.lower() not in stop and len(value) > 2]


def make_cloze(sentence: str, lang: str, grammar: list[dict]) -> dict:
    values = content_tokens(sentence, lang)
    if len(values) < 2:
        values = [value for value in tokens_for(sentence, lang) if value not in {".", "?", "!"}]
    if len(values) < 2:
        values = tokens_for(sentence, lang)
    if len(values) < 2:
        raise ValueError(f"No hay suficientes tokens para el cloze: {sentence}")
    first, second = values[0], values[1]
    if first == second:
        second = values[2] if len(values) > 2 else second + "s"
    passage = sentence
    for placeholder, token in (("{{b1}}", first), ("{{b2}}", second)):
        if lang == "ja":
            passage = passage.replace(token, placeholder, 1)
        else:
            passage = re.sub(rf"\b{re.escape(token)}\b", placeholder, passage, count=1)
    if "{{b1}}" not in passage or "{{b2}}" not in passage:
        raise ValueError(f"No se pudieron crear los huecos: {sentence}")
    pool = [value for value in values if value not in {first, second}]
    fallback = (
        ["very", "today", "here", "now"]
        if lang == "en"
        else ["très", "aujourd'hui", "ici", "maintenant"]
        if lang == "fr"
        else ["muito", "hoje", "aqui", "agora"]
        if lang == "pt"
        else ["とても", "今日", "ここ", "今"]
    )
    pool.extend(fallback)
    options_one = [first] + [value for value in pool if value != first][:2]
    options_two = [second] + [value for value in pool if value != second][:2]
    return {
        "passage": passage,
        "blanks": [
            {"id": "b1", "answer": first, "options": options_one},
            {"id": "b2", "answer": second, "options": options_two},
        ],
        "feedbackOnError": {
            "type": "grammar",
            "explanation": "Las dos formas deben encajar con el contexto de la frase.",
            "ruleRef": grammar[1]["id"],
        },
        "feedbackOnSuccess": {
            "explanation": "Los dos huecos encajan con la situación.",
        },
    }


def build_lesson(
    course_id: str,
    unit_number: int,
    lesson_number: int,
    spec: dict,
    lang: str,
) -> dict:
    lesson_id = f"{course_id}-b1-u{unit_number:02d}-l{lesson_number:02d}"
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
        raise ValueError(f"{lesson_id}: B1 exige 2 focos gramaticales")
    cultural = spec["cultural"]
    target_sentences = [sentence[1] for sentence in sentences]
    s0_tokens = tokens_for(target_sentences[0], lang)
    s1_tokens = tokens_for(target_sentences[1], lang)
    s2_tokens = tokens_for(target_sentences[2], lang)
    first_es, first_target = items[0][0], target_items[0]
    rule_ref = grammar[0]["id"]
    cloze = make_cloze(spec.get("cloze_sentence", target_sentences[0]), lang, grammar)
    free_keywords = content_tokens(target_sentences[2], lang)[:3]
    if not free_keywords:
        free_keywords = [target_items[2]]

    def feedback(kind: str, explanation: str, rule: str = rule_ref) -> dict:
        return {"type": kind, "explanation": explanation, "ruleRef": rule}

    lesson = {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "B1",
        "scenario": f"Situación comunicativa sobre {spec['topic']}.",
        "canDo": f"Puedo comunicarme con soltura sobre {spec['topic']}.",
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
                f"Usar vocabulario de {spec['topic']}.",
                f"Aplicar {grammar[0]['label'].lower()} y {grammar[1]['label'].lower()}.",
                f"Resolver una tarea comunicativa sobre {spec['topic']}.",
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
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": sentences[0][0],
                "correctTokens": s0_tokens,
                "distractorTokens": safe_distractors(s0_tokens, [s1_tokens, s2_tokens], lang),
                "feedbackOnError": feedback(
                    "grammar",
                    f"El orden correcto es «{' '.join(s0_tokens)}». {grammar[0]['label']}: {grammar[0]['description']}",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (s1_tokens[1:] + s1_tokens[:1]) if len(s1_tokens) > 1 else s1_tokens,
                "answerTokens": s1_tokens,
                "sourcePhrase": sentences[1][0],
                "feedbackOnError": feedback(
                    "grammar",
                    f"La frase correcta es «{' '.join(s1_tokens)}». {grammar[1]['label']}: {grammar[1]['description']}",
                    grammar[1]["id"],
                ),
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
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
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has asociado cada expresión con su traducción."},
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
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
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
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e07",
                "passage": cloze["passage"],
                "blanks": cloze["blanks"],
                "feedbackOnError": cloze["feedbackOnError"],
                "feedbackOnSuccess": cloze["feedbackOnSuccess"],
            },
            {
                "type": "FreeSpeakingPrompt",
                "id": f"{lesson_id}-e08",
                "prompt": f"Habla sobre {spec['topic']} y justifica tu respuesta.",
                "minTokens": 6,
                "conceptClusters": [[keyword] for keyword in free_keywords],
                "modelAnswer": target_sentences[2],
                "selfAssessmentCriteria": [
                    "¿La respuesta es clara y responde a la tarea?",
                    "¿Usa las dos estructuras gramaticales de la lección?",
                ],
                "grammarRuleRefs": [grammar[0]["id"], grammar[1]["id"]],
                "timeLimitSeconds": 60,
                "feedbackOnError": feedback(
                    "grammar",
                    "La respuesta modelo usa una frase completa y responde a la tarea.",
                    rule_ref,
                ),
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
        if len(lesson["exercises"]) != 9:
            raise ValueError(f"{lesson['id']}: se esperaban 9 ejercicios")
        if len(lesson["grammarPoints"]) != 2:
            raise ValueError(f"{lesson['id']}: se esperaban 2 focos gramaticales")
        if sum(1 for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt") != 1:
            raise ValueError(f"{lesson['id']}: se esperaba un FreeSpeakingPrompt")
        if not lesson["culturalNotes"]:
            raise ValueError(f"{lesson['id']}: falta nota cultural")
        for exercise in lesson["exercises"]:
            if exercise.get("feedbackOnError") is None:
                raise ValueError(f"{exercise['id']}: falta feedbackOnError")
            if exercise["type"] in {"MultipleChoice", "ListenChoose"}:
                if len(set(exercise["options"])) != len(exercise["options"]):
                    raise ValueError(f"{exercise['id']}: opciones duplicadas")
            if exercise["type"] == "ClozeDragDrop":
                if len(exercise["blanks"]) < 2:
                    raise ValueError(f"{exercise['id']}: cloze con menos de 2 huecos")
                if "{{b1}}" not in exercise["passage"] or "{{b2}}" not in exercise["passage"]:
                    raise ValueError(f"{exercise['id']}: placeholders incompletos")
            texts: list[str] = []
            for key in ("audioText", "transcription", "targetPhrase", "sourcePhrase"):
                value = exercise.get(key)
                if value:
                    texts.append(value)
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            elif exercise["type"] == "FreeSpeakingPrompt":
                texts.append(exercise["modelAnswer"])
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
        "id": f"{course_id}-b1-u{unit_number:02d}",
        "title": f"B1 · {block_title}",
        "lessons": lessons,
    }
