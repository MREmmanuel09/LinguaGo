"""Fábrica de contenido C1/C2 (F5/F6) para los cuatro cursos."""

from __future__ import annotations

import json
import re
from pathlib import Path

from a2_factory import indent_block, safe_distractors
from b1_factory import B1_TARGET_INDEX, content_tokens, make_cloze, tokens_for

MAX_TOKENS = {"C1": 38, "C2": 48}
VOCAB_BAND = {"C1": "8000-12000", "C2": "12000+"}

GRAMMAR = {
    "diplomatic-hedging": {
        "id": "gp-c1-diplomatic-hedging",
        "label": "Diplomatic hedging",
        "description": "Matizar una postura con I wonder, it seems o might.",
    },
    "softening-requests": {
        "id": "gp-c1-softening-requests",
        "label": "Softening requests",
        "description": "Suavizar peticiones con could, would y I was wondering.",
    },
    "inversion": {
        "id": "gp-c1-inversion",
        "label": "Inversion",
        "description": "La inversión enfática coloca el auxiliar antes del sujeto.",
    },
    "cleft-sentences": {
        "id": "gp-c1-cleft-sentences",
        "label": "Cleft sentences",
        "description": "It is... / What... destaca una parte de la información.",
    },
    "subjunctive": {
        "id": "gp-c1-subjunctive",
        "label": "Subjunctive",
        "description": "El subjuntivo expresa deseos, dudas y valoraciones.",
    },
    "mixed-conditionals": {
        "id": "gp-c1-mixed-conditionals",
        "label": "Mixed conditionals",
        "description": "Combina pasado y presente en hipótesis complejas.",
    },
    "modal-perfect": {
        "id": "gp-c1-modal-perfect",
        "label": "Modal perfect",
        "description": "Must have, might have y should have especulan sobre el pasado.",
    },
    "passive-reporting": {
        "id": "gp-c1-passive-reporting",
        "label": "Passive reporting",
        "description": "It is said / believed + that presenta una fuente de forma impersonal.",
    },
    "nominalisation": {
        "id": "gp-c1-nominalisation",
        "label": "Nominalisation",
        "description": "Convertir verbos en nombres hace el texto más formal.",
    },
    "participle-clauses": {
        "id": "gp-c1-participle-clauses",
        "label": "Participle clauses",
        "description": "Las cláusulas de participio reducen y conectan ideas.",
    },
    "non-defining-relative": {
        "id": "gp-c1-non-defining-relative",
        "label": "Non-defining relative clauses",
        "description": "Las relativas explicativas añaden información con comas.",
    },
    "concession": {
        "id": "gp-c1-concession",
        "label": "Concession",
        "description": "Although, despite y however introducen concesiones.",
    },
    "purpose": {
        "id": "gp-c1-purpose",
        "label": "Purpose",
        "description": "To, in order to y so that expresan finalidad.",
    },
    "discourse-markers": {
        "id": "gp-c1-discourse-markers",
        "label": "Discourse markers",
        "description": "Los marcadores ordenan y conectan el discurso.",
    },
    "ellipsis": {
        "id": "gp-c1-ellipsis",
        "label": "Ellipsis",
        "description": "La elipsis evita repetir elementos ya conocidos.",
    },
    "emphasis": {
        "id": "gp-c1-emphasis",
        "label": "Emphasis",
        "description": "Do, indeed y absolutely refuerzan el mensaje.",
    },
    "distancing": {
        "id": "gp-c1-distancing",
        "label": "Distancing",
        "description": "It is said, apparently y according to matizan la fuente.",
    },
    "speculation": {
        "id": "gp-c1-speculation",
        "label": "Speculation",
        "description": "May, might y could expresan distintos grados de posibilidad.",
    },
    "advanced-comparatives": {
        "id": "gp-c1-advanced-comparatives",
        "label": "Advanced comparatives",
        "description": "The more..., the more... y comparaciones complejas.",
    },
    "advanced-quantifiers": {
        "id": "gp-c1-advanced-quantifiers",
        "label": "Advanced quantifiers",
        "description": "Little, few, plenty of y most matizan la cantidad.",
    },
    "register": {
        "id": "gp-c1-register",
        "label": "Register",
        "description": "El registro formal o informal condiciona las elecciones léxicas.",
    },
    "collocations": {
        "id": "gp-c1-collocations",
        "label": "Collocations",
        "description": "Las combinaciones léxicas naturales suenan más precisas.",
    },
    "phrasal-verbs": {
        "id": "gp-c1-phrasal-verbs",
        "label": "Phrasal verbs",
        "description": "Los verbos compuestos cambian de significado con la partícula.",
    },
    "rhetorical-questions": {
        "id": "gp-c2-rhetorical-questions",
        "label": "Rhetorical questions",
        "description": "Las preguntas retóricas orientan la argumentación.",
    },
    "dry-irony": {
        "id": "gp-c2-dry-irony",
        "label": "Dry irony",
        "description": "La ironía seca dice lo contrario de lo que sugiere el contexto.",
    },
    "understatement": {
        "id": "gp-c2-understatement",
        "label": "Understatement",
        "description": "La atenuación presenta algo grave como si fuera menor.",
    },
    "litotes": {
        "id": "gp-c2-litotes",
        "label": "Litotes",
        "description": "La litote afirma algo negando lo contrario.",
    },
    "hyperbole": {
        "id": "gp-c2-hyperbole",
        "label": "Hyperbole",
        "description": "La hipérbole exagera para reforzar una idea.",
    },
    "fronting": {
        "id": "gp-c2-fronting",
        "label": "Fronting",
        "description": "El fronting desplaza un elemento al inicio para destacarlo.",
    },
    "apposition": {
        "id": "gp-c2-apposition",
        "label": "Apposition",
        "description": "La aposición añade una explicación al sustantivo.",
    },
    "parallelism": {
        "id": "gp-c2-parallelism",
        "label": "Parallelism",
        "description": "El paralelismo equilibra estructuras equivalentes.",
    },
}

SUFFIXES = {
    "C1": {
        "es": [
            " Cabe señalar que este matiz resulta decisivo.",
            " En términos generales, la postura es defendible.",
            " Dicho de otro modo, conviene sopesar los costes.",
            " Si bien es cierto, no deberíamos obviar el contexto.",
            " A mi juicio, la interpretación más plausible es otra.",
            " En última instancia, prevalece el criterio profesional.",
            " No obstante, sería precipitado extraer conclusiones.",
            " Desde una perspectiva crítica, el argumento se sostiene.",
            " A efectos prácticos, la alternativa es viable.",
            " En definitiva, el debate sigue abierto.",
        ],
        "en": [
            " It is worth noting that this nuance is decisive.",
            " Broadly speaking, the position is defensible.",
            " In other words, we should weigh the costs.",
            " While this is true, we should not ignore the context.",
            " In my judgement, the most plausible interpretation is different.",
            " Ultimately, professional judgement prevails.",
            " Nevertheless, it would be rash to draw conclusions.",
            " From a critical perspective, the argument holds.",
            " In practical terms, the alternative is viable.",
            " In short, the debate remains open.",
        ],
        "fr": [
            " Il convient de souligner que cette nuance est décisive.",
            " De manière générale, la position est défendable.",
            " Autrement dit, il faut peser les coûts.",
            " Certes, mais nous ne devons pas ignorer le contexte.",
            " À mon sens, l'interprétation la plus plausible est autre.",
            " En dernière instance, le critère professionnel prévaut.",
            " Néanmoins, il serait prématuré de conclure.",
            " D'un point de vue critique, l'argument tient.",
            " En pratique, l'alternative est viable.",
            " En définitive, le débat reste ouvert.",
        ],
        "pt": [
            " Vale a pena notar que este matiz é decisivo.",
            " De um modo geral, a posição é defensável.",
            " Por outras palavras, convém pesar os custos.",
            " Embora seja verdade, não devemos ignorar o contexto.",
            " A meu ver, a interpretação mais plausível é outra.",
            " Em última instância, prevalece o critério profissional.",
            " No entanto, seria precipitado tirar conclusões.",
            " Numa perspetiva crítica, o argumento sustenta-se.",
            " Em termos práticos, a alternativa é viável.",
            " Em suma, o debate continua aberto.",
        ],
        "ja": [
            " この ニュアンス は 決定的 だ と 言え ます 。",
            " 一般的 に 言え ば 、その 立場 は 擁護 でき ます 。",
            " 言い換えれ ば 、コスト を 比較 検討 する 必要 が あります 。",
            " 確か に そう です が 、文脈 を 無視 する べき で は ありません 。",
            " 私 の 判断 で は 、最も 妥当 な 解釈 は 別 に あります 。",
            " 最終 的 に は 、専門 的 な 判断 が 優先 され ます 。",
            " それでも 、結論 を 急ぐ の は 早計 です 。",
            " 批判 的 な 観点 から 見る と 、その 議論 は 成り立ち ます 。",
            " 実務 的 に は 、その 代替案 は 実行 可能 です 。",
            " 要するに 、議論 は まだ 続い て います 。",
        ],
    },
    "C2": {
        "es": [
            " Lo cual, dicho sea de paso, no deja de ser irónico.",
            " Huelga decir que el matiz escapa a una lectura literal.",
            " En rigor, la afirmación admite lecturas encontradas.",
            " A riesgo de simplificar, el argumento roza la contradicción.",
            " Paradójicamente, el exceso de cautela resta credibilidad.",
            " Con la debida distancia crítica, la tesis resulta sugerente.",
            " Si se me permite la ironía, el resultado era previsible.",
            " En última instancia, subyace una tensión no resuelta.",
            " Dicho sin ambages, la propuesta carece de sustento.",
            " A todas luces, conviene releer el planteamiento.",
        ],
        "en": [
            " Which, incidentally, is more than a little ironic.",
            " Needless to say, the nuance escapes a literal reading.",
            " Strictly speaking, the claim admits conflicting readings.",
            " At the risk of oversimplifying, the argument borders on contradiction.",
            " Paradoxically, excessive caution undermines credibility.",
            " With due critical distance, the thesis is suggestive.",
            " If I may indulge in irony, the outcome was predictable.",
            " Ultimately, an unresolved tension underpins the debate.",
            " Frankly, the proposal lacks a sound basis.",
            " By all accounts, the approach deserves a second reading.",
        ],
        "fr": [
            " Ce qui, soit dit en passant, n'est pas sans ironie.",
            " Inutile de dire que la nuance échappe à une lecture littérale.",
            " À proprement parler, l'affirmation admet des lectures contradictoires.",
            " Au risque de simplifier, l'argument frôle la contradiction.",
            " Paradoxalement, l'excès de prudence nuit à la crédibilité.",
            " Avec la distance critique requise, la thèse est suggestive.",
            " Si l'on me permet l'ironie, le résultat était prévisible.",
            " En dernière instance, une tension irrésolue sous-tend le débat.",
            " Franchement, la proposition manque de fondement.",
            " De toute évidence, l'approche mérite une seconde lecture.",
        ],
        "pt": [
            " O que, a propósito, não deixa de ser irónico.",
            " Escusado será dizer que o matiz escapa a uma leitura literal.",
            " Em rigor, a afirmação admite leituras contraditórias.",
            " Com o risco de simplificar, o argumento roça a contradição.",
            " Paradoxalmente, o excesso de cautela retira credibilidade.",
            " Com a devida distância crítica, a tese é sugestiva.",
            " Se me é permitida a ironia, o resultado era previsível.",
            " Em última instância, subsiste uma tensão não resolvida.",
            " Francamente, a proposta carece de fundamento.",
            " A todos os títulos, convém reler a abordagem.",
        ],
        "ja": [
            " ちなみに 、それ は かなり 皮肉 な 話 です 。",
            " 言う まで も なく 、その ニュアンス は 字面 で は 捉えられ ません 。",
            " 厳密 に 言え ば 、その 主張 に は 相反 する 読み方 が あり ます 。",
            " 単純化 を 承知 で 言え ば 、その 議論 は 矛盾 に 近い です 。",
            " 逆説 的 に 、過度 な 慎重 さ は 信頼 を 損ない ます 。",
            " 適切 な 批判 的 距離 を 置け ば 、その 主張 は 示唆 に 富み ます 。",
            " 皮肉 を 承知 で 言え ば 、結果 は 予想 でき まし た 。",
            " 最終 的 に は 、未解決 の 緊張 が 議論 の 底流 に あり ます 。",
            " 率直 に 言え ば 、その 提案 に は 根拠 が 乏しい です 。",
            " どう 見て も 、その アプローチ は 再考 に 値し ます 。",
        ],
    },
}


def extend_sentence(sentence: str, lang: str, level: str, index: int) -> str:
    suffix = SUFFIXES[level][lang][index % len(SUFFIXES[level][lang])]
    text = sentence.rstrip()
    terminal = ("。", "！", "？") if lang == "ja" else (".", "!", "?")
    if not text.endswith(terminal):
        text += "。" if lang == "ja" else "."
    return text + suffix


def resolve_grammar(keys: list[str]) -> list[dict]:
    missing = [key for key in keys if key not in GRAMMAR]
    if missing:
        raise ValueError(f"Focos gramaticales C1/C2 desconocidos: {missing}")
    return [GRAMMAR[key] for key in keys]


def insert_unit_last_level(course_path: Path, unit: dict) -> bool:
    text = course_path.read_text(encoding="utf-8")
    if f'"id": "{unit["id"]}"' in text:
        return False
    marker = "\n        ]\n      }"
    position = text.rfind(marker)
    if position == -1:
        raise SystemExit("No se encontró el cierre de units del último nivel")
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 10)
    text = text[:position] + ",\n" + unit_text + text[position:]
    course_path.write_text(text, encoding="utf-8", newline="\n")
    json.loads(course_path.read_text(encoding="utf-8"))
    return True


def semantic_pairs(sentences: list[str], labels: list[tuple[str, str]]) -> list[dict]:
    return [
        {
            "left": sentence,
            "right": label,
            "register": register,
            "nuance": "Clasifica la función y el registro de la intervención.",
        }
        for sentence, (label, register) in zip(sentences, labels, strict=True)
    ]


def build_lesson(
    course_id: str,
    level: str,
    unit_number: int,
    lesson_number: int,
    spec: dict,
    lang: str,
) -> dict:
    prefix = "c1" if level == "C1" else "c2"
    lesson_id = f"{course_id}-{prefix}-u{unit_number:02d}-l{lesson_number:02d}"
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
        raise ValueError(f"{lesson_id}: se exigen 2 focos gramaticales")
    cultural = spec["cultural"]
    target_sentences = [sentence[1] for sentence in sentences]
    s0_tokens = tokens_for(target_sentences[0], lang)
    s1_tokens = tokens_for(target_sentences[1], lang)
    s2_tokens = tokens_for(target_sentences[2], lang)
    first_es, first_target = items[0][0], target_items[0]
    rule_ref = grammar[0]["id"]
    base_targets = spec["cloze_sentences"]
    cloze_one = make_cloze(base_targets[0], lang, grammar)
    cloze_two = make_cloze(base_targets[1], lang, grammar)
    cloze_three = make_cloze(base_targets[2], lang, grammar)
    free_keywords = content_tokens(" ".join(target_sentences), lang)[:3]
    if not free_keywords:
        free_keywords = [target_items[2]]
    model_answer = " ".join(target_sentences)

    def feedback(kind: str, explanation: str, rule: str = rule_ref) -> dict:
        return {"type": kind, "explanation": explanation, "ruleRef": rule}

    exercises = [
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
            "feedbackOnSuccess": {"explanation": "Has elegido la opción precisa."},
        },
    ]
    if level == "C1":
        exercises += [
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
                "type": "Translate",
                "id": f"{lesson_id}-e03",
                "sourcePhrase": sentences[1][0],
                "correctTokens": s1_tokens,
                "distractorTokens": safe_distractors(s1_tokens, [s0_tokens, s2_tokens], lang),
                "feedbackOnError": feedback(
                    "grammar",
                    f"El orden correcto es «{' '.join(s1_tokens)}».",
                    grammar[1]["id"],
                ),
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e04",
                "passage": cloze_one["passage"],
                "blanks": cloze_one["blanks"],
                "feedbackOnError": cloze_one["feedbackOnError"],
                "feedbackOnSuccess": cloze_one["feedbackOnSuccess"],
            },
            {
                "type": "SemanticMatch",
                "id": f"{lesson_id}-e05",
                "instructions": "Clasifica la función y el registro de cada intervención.",
                "pairs": semantic_pairs(
                    target_sentences,
                    [
                        ("Planteamiento", "formal"),
                        ("Concesión", "neutral"),
                        ("Conclusión", "informal"),
                    ],
                ),
                "feedbackOnError": feedback(
                    "register",
                    "Cada intervención cumple una función y un registro distintos.",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has identificado función y registro."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e06",
                "audioText": target_sentences[1],
                "options": [target_sentences[1], target_sentences[0], target_sentences[2]],
                "correctIndex": 0,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[1]}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
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
                "type": "MatchPairs",
                "id": f"{lesson_id}-e10",
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
                "type": "FreeSpeakingPrompt",
                "id": f"{lesson_id}-e11",
                "prompt": f"Argumenta con matices sobre {spec['topic']} y justifica tu postura.",
                "minTokens": 30,
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
                    "La respuesta modelo desarrolla una argumentación matizada.",
                    rule_ref,
                ),
            },
        ]
    else:
        exercises += [
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": sentences[0][0],
                "correctTokens": s0_tokens,
                "distractorTokens": safe_distractors(s0_tokens, [s1_tokens, s2_tokens], lang),
                "feedbackOnError": feedback(
                    "grammar",
                    f"El orden correcto es «{' '.join(s0_tokens)}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e03",
                "passage": cloze_one["passage"],
                "blanks": cloze_one["blanks"],
                "feedbackOnError": cloze_one["feedbackOnError"],
                "feedbackOnSuccess": cloze_one["feedbackOnSuccess"],
            },
            {
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e04",
                "passage": cloze_two["passage"],
                "blanks": cloze_two["blanks"],
                "feedbackOnError": cloze_two["feedbackOnError"],
                "feedbackOnSuccess": cloze_two["feedbackOnSuccess"],
            },
            {
                "type": "ClozeDragDrop",
                "id": f"{lesson_id}-e05",
                "passage": cloze_three["passage"],
                "blanks": cloze_three["blanks"],
                "feedbackOnError": cloze_three["feedbackOnError"],
                "feedbackOnSuccess": cloze_three["feedbackOnSuccess"],
            },
            {
                "type": "SemanticMatch",
                "id": f"{lesson_id}-e06",
                "instructions": "Clasifica la función y el registro de cada intervención.",
                "pairs": semantic_pairs(
                    target_sentences,
                    [
                        ("Ironía seca", "literary"),
                        ("Atenuación", "formal"),
                        ("Conclusión", "neutral"),
                    ],
                ),
                "feedbackOnError": feedback(
                    "register",
                    "Cada intervención cumple una función retórica distinta.",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has identificado función y registro."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e07",
                "audioText": target_sentences[1],
                "options": [target_sentences[1], target_sentences[0], target_sentences[2]],
                "correctIndex": 0,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[1]}».",
                    rule_ref,
                ),
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "AudioOnlyPrompt",
                "id": f"{lesson_id}-e08",
                "audioText": target_sentences[2],
                "question": "¿Qué implica el hablante?",
                "options": [target_sentences[2], target_sentences[0], target_sentences[1]],
                "correctIndex": 0,
                "allowSlowPlayback": False,
                "feedbackOnError": feedback(
                    "comprehension",
                    f"El audio dice «{target_sentences[2]}».",
                    grammar[1]["id"],
                ),
                "feedbackOnSuccess": {"explanation": "Has interpretado la implicación."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e09",
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
                "id": f"{lesson_id}-e10",
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
                "type": "MatchPairs",
                "id": f"{lesson_id}-e11",
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
                "type": "FreeSpeakingPrompt",
                "id": f"{lesson_id}-e12",
                "prompt": f"Defiende una interpretación matizada sobre {spec['topic']}.",
                "minTokens": 40,
                "conceptClusters": [[keyword] for keyword in free_keywords],
                "modelAnswer": model_answer,
                "selfAssessmentCriteria": [
                    "¿La respuesta argumenta y responde a la tarea?",
                    "¿Usa las dos estructuras gramaticales de la lección?",
                    "¿Incluye una concesión, una ironía o una atenuación?",
                ],
                "grammarRuleRefs": [grammar[0]["id"], grammar[1]["id"]],
                "timeLimitSeconds": 120,
                "minWpm": 70,
                "maxWpm": 160,
                "feedbackOnError": feedback(
                    "grammar",
                    "La respuesta modelo desarrolla una interpretación matizada.",
                    rule_ref,
                ),
            },
        ]
    expected = 11 if level == "C1" else 12
    if len(exercises) != expected:
        raise ValueError(f"{lesson_id}: se esperaban {expected} ejercicios")
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": level,
        "scenario": f"Situación comunicativa avanzada sobre {spec['topic']}.",
        "canDo": f"Puedo argumentar con matices y precisión sobre {spec['topic']}.",
        "grammarPoints": grammar,
        "vocabFrequencyBand": VOCAB_BAND[level],
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
                f"Argumentar con matices sobre {spec['topic']}.",
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
        "exercises": exercises,
    }


def build_unit(
    course_id: str,
    level: str,
    unit_number: int,
    block_title: str,
    specs: list[dict],
    lang: str,
) -> dict:
    prefix = "c1" if level == "C1" else "c2"
    lessons = [
        build_lesson(course_id, level, unit_number, index, spec, lang)
        for index, spec in enumerate(specs, start=1)
    ]
    expected = 11 if level == "C1" else 12
    seen_titles: set[str] = set()
    for lesson in lessons:
        if lesson["title"] in seen_titles:
            raise ValueError(f"Título duplicado en la unidad {unit_number}: {lesson['title']}")
        seen_titles.add(lesson["title"])
        if len(lesson["exercises"]) != expected:
            raise ValueError(f"{lesson['id']}: se esperaban {expected} ejercicios")
        if len(lesson["grammarPoints"]) != 2:
            raise ValueError(f"{lesson['id']}: se esperaban 2 focos gramaticales")
        if sum(1 for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt") != 1:
            raise ValueError(f"{lesson['id']}: se esperaba un FreeSpeakingPrompt")
        if not lesson["culturalNotes"]:
            raise ValueError(f"{lesson['id']}: falta nota cultural")
        for exercise in lesson["exercises"]:
            if exercise.get("feedbackOnError") is None:
                raise ValueError(f"{exercise['id']}: falta feedbackOnError")
            if exercise["type"] in {"MultipleChoice", "ListenChoose", "AudioOnlyPrompt"}:
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
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            for text in texts:
                longest = max(
                    len(part.split())
                    for part in re.split(r"[.!?。！？]+", text)
                    if part.strip()
                )
                if longest > MAX_TOKENS[level]:
                    raise ValueError(
                        f"{exercise['id']}: frase de {longest} tokens (máximo {MAX_TOKENS[level]})"
                    )
    return {
        "id": f"{course_id}-{prefix}-u{unit_number:02d}",
        "title": f"{level} · {block_title}",
        "lessons": lessons,
    }
