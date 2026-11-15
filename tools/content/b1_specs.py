"""Currículo B1 de F3 derivado de la base semántica A2.

Cada bloque reutiliza las frases A2 y las eleva con conectores B1. Las
traducciones PT/JA salen del diccionario de F2; EN/FR salen de `a2_specs`.
"""

from __future__ import annotations

from a2_factory import sentences_for
from a2_localized import TRANSLATIONS
from a2_specs import BLOCKS
from b1_factory import extend_sentence

CULTURAL = [
    {
        "en": ("touch base", "Expresión habitual en el trabajo para retomar contacto brevemente.", "retomar contacto"),
        "pt": ("dar um jeito", "Expresión coloquial para resolver un problema con ingenio.", "resolverlo"),
        "fr": ("mettre les points sur les i", "Expresión para aclarar algo con firmeza.", "dejar las cosas claras"),
        "ja": ("空気 を 読む", "Expresión para leer el ambiente antes de actuar.", "leer el ambiente"),
    },
    {
        "en": ("on the same page", "Expresión para confirmar que todos comparten la misma idea.", "estar de acuerdo"),
        "pt": ("estar na mesma página", "Expresión para confirmar que todos comparten la misma idea.", "estar de acuerdo"),
        "fr": ("être sur la même longueur d'onde", "Expresión para confirmar que todos comparten la misma idea.", "estar en sintonía"),
        "ja": ("同じ 考え を 持つ", "Expresión para confirmar que todos comparten la misma idea.", "compartir la idea"),
    },
    {
        "en": ("get the ball rolling", "Expresión para empezar una actividad o un proyecto.", "poner en marcha"),
        "pt": ("pôr a mão na massa", "Expresión para empezar a trabajar con decisión.", "ponerse manos a la obra"),
        "fr": ("se mettre au travail", "Expresión para empezar a trabajar.", "ponerse a trabajar"),
        "ja": ("腰 を 据える", "Expresión para ponerse a trabajar con constancia.", "ponerse en serio"),
    },
    {
        "en": ("under the weather", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
        "pt": ("estar de baixo do tempo", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
        "fr": ("ne pas être dans son assiette", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
        "ja": ("体調 が すぐれない", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
    },
    {
        "en": ("a blessing in disguise", "Expresión para describir algo malo que acaba siendo bueno.", "una bendición disfrazada"),
        "pt": ("um mal que vem para bem", "Expresión para describir algo malo que acaba siendo bueno.", "un mal que acaba bien"),
        "fr": ("une bénédiction déguisée", "Expresión para describir algo malo que acaba siendo bueno.", "una bendición disfrazada"),
        "ja": ("不幸中 の 幸い", "Expresión para describir algo malo que acaba siendo bueno.", "una suerte dentro de la desgracia"),
    },
    {
        "en": ("think outside the box", "Expresión para proponer ideas creativas.", "pensar de forma creativa"),
        "pt": ("pensar fora da caixa", "Expresión para proponer ideas creativas.", "pensar de forma creativa"),
        "fr": ("sortir des sentiers battus", "Expresión para proponer ideas creativas.", "salirse de lo habitual"),
        "ja": ("枠 に とらわれない", "Expresión para proponer ideas creativas.", "no limitarse a lo establecido"),
    },
    {
        "en": ("hit the nail on the head", "Expresión para decir que alguien ha acertado exactamente.", "dar en el clavo"),
        "pt": ("acertar na mosca", "Expresión para decir que alguien ha acertado exactamente.", "dar en el clavo"),
        "fr": ("mettre le doigt sur le problème", "Expresión para identificar exactamente un problema.", "dar con el problema"),
        "ja": ("的 を 射る", "Expresión para decir que alguien ha acertado exactamente.", "dar en el clavo"),
    },
    {
        "en": ("keep an eye on", "Expresión para vigilar algo con atención.", "estar pendiente"),
        "pt": ("ficar de olho", "Expresión para vigilar algo con atención.", "estar pendiente"),
        "fr": ("garder un œil sur", "Expresión para vigilar algo con atención.", "estar pendiente"),
        "ja": ("目 を 光らせる", "Expresión para vigilar algo con atención.", "estar atento"),
    },
    {
        "en": ("cost an arm and a leg", "Expresión para decir que algo es muy caro.", "costar un ojo de la cara"),
        "pt": ("custar os olhos da cara", "Expresión para decir que algo es muy caro.", "costar un ojo de la cara"),
        "fr": ("coûter les yeux de la tête", "Expresión para decir que algo es muy caro.", "costar un ojo de la cara"),
        "ja": ("目 が 飛び出る ほど 高い", "Expresión para decir que algo es muy caro.", "carísimo"),
    },
    {
        "en": ("burn the midnight oil", "Expresión para trabajar hasta muy tarde.", "trabajar hasta tarde"),
        "pt": ("virar a noite", "Expresión para trabajar hasta muy tarde.", "trasnochar"),
        "fr": ("travailler tard", "Expresión para trabajar hasta muy tarde.", "trabajar hasta tarde"),
        "ja": ("夜遅く まで 働く", "Expresión para trabajar hasta muy tarde.", "trabajar hasta tarde"),
    },
    {
        "en": ("the bottom line", "Expresión para referirse a lo esencial de un asunto.", "lo esencial"),
        "pt": ("o que importa", "Expresión para referirse a lo esencial de un asunto.", "lo esencial"),
        "fr": ("l'essentiel", "Expresión para referirse a lo esencial de un asunto.", "lo esencial"),
        "ja": ("要点", "Expresión para referirse a lo esencial de un asunto.", "lo esencial"),
    },
    {
        "en": ("break the ice", "Expresión para iniciar una conversación en un grupo.", "romper el hielo"),
        "pt": ("quebrar o gelo", "Expresión para iniciar una conversación en un grupo.", "romper el hielo"),
        "fr": ("briser la glace", "Expresión para iniciar una conversación en un grupo.", "romper el hielo"),
        "ja": ("場 を 和ませる", "Expresión para iniciar una conversación en un grupo.", "romper el hielo"),
    },
    {
        "en": ("call it a day", "Expresión para terminar una actividad por hoy.", "dejarlo por hoy"),
        "pt": ("encerrar o dia", "Expresión para terminar una actividad por hoy.", "dejarlo por hoy"),
        "fr": ("en rester là", "Expresión para terminar una actividad por hoy.", "dejarlo por hoy"),
        "ja": ("今日 は ここまで", "Expresión para terminar una actividad por hoy.", "dejarlo por hoy"),
    },
    {
        "en": ("cut corners", "Expresión para hacer algo con menos cuidado del necesario.", "hacerlo de forma rápida"),
        "pt": ("fazer nas coxas", "Expresión para hacer algo con poco cuidado.", "hacerlo de cualquier manera"),
        "fr": ("faire des raccourcis", "Expresión para hacer algo con menos cuidado del necesario.", "hacerlo de forma rápida"),
        "ja": ("手 を 抜く", "Expresión para hacer algo con poco cuidado.", "hacerlo de cualquier manera"),
    },
]

META = [
    ("Recuerdos y pasado", ["past-simple", "present-perfect"]),
    ("Viajes y transporte", ["past-continuous", "first-conditional"]),
    ("Planes y futuro", ["future-forms", "first-conditional"]),
    ("Ciudad y comparaciones", ["comparatives", "relative-clauses"]),
    ("Salud y bienestar", ["modals-obligation", "modals-advice"]),
    ("Compras y servicios", ["countable-uncountable", "comparatives"]),
    ("Trabajo y estudios", ["present-perfect", "passive-voice"]),
    ("Casa y convivencia", ["modals-obligation", "relative-clauses"]),
    ("Comida y restaurantes", ["countable-uncountable", "gerund-infinitive"]),
    ("Ocio y medios", ["present-perfect", "gerund-infinitive"]),
    ("Medio ambiente", ["first-conditional", "future-forms"]),
    ("Relaciones y opiniones", ["relative-clauses", "connectors-contrast"]),
    ("Cultura y celebraciones", ["used-to", "past-simple"]),
    ("Tecnología y comunicación", ["passive-voice", "reported-speech"]),
]


def spec_sentences(spec: dict, lang: str) -> list[tuple[str, str]]:
    if "sentences" in spec:
        index = {"en": 1, "fr": 2}[lang]
        return [(sentence[0], sentence[index]) for sentence in spec["sentences"]]
    return sentences_for(spec["pattern"], spec["items"], lang)


def build_specs(lang: str) -> list[tuple[str, list[dict]]]:
    blocks: list[tuple[str, list[dict]]] = []
    for block_index, (block_title, grammar) in enumerate(META):
        if block_index >= len(BLOCKS):
            break
        _, source_specs = BLOCKS[block_index]
        localized = []
        for lesson_index, source in enumerate(source_specs, start=1):
            en_pairs = spec_sentences(source, "en")
            fr_pairs = spec_sentences(source, "fr")
            es_sentences = [pair[0] for pair in en_pairs]
            pt_sentences = [TRANSLATIONS["pt"][es] for es in es_sentences]
            ja_sentences = [TRANSLATIONS["ja"][es] for es in es_sentences]
            target_index = {"en": 0, "fr": 1, "pt": 2, "ja": 3}[lang]
            base_targets = [
                en_pairs[0][1],
                fr_pairs[0][1],
                pt_sentences[0],
                ja_sentences[0],
            ]
            items = []
            for sentence_index, es in enumerate(es_sentences):
                items.append(
                    (
                        extend_sentence(es, "es", lesson_index + sentence_index),
                        extend_sentence(en_pairs[sentence_index][1], "en", lesson_index + sentence_index),
                        extend_sentence(fr_pairs[sentence_index][1], "fr", lesson_index + sentence_index),
                        extend_sentence(pt_sentences[sentence_index], "pt", lesson_index + sentence_index),
                        extend_sentence(ja_sentences[sentence_index], "ja", lesson_index + sentence_index),
                    )
                )
            localized.append(
                {
                    "title": source["title"],
                    "topic": source["topic"],
                    "grammar": grammar,
                    "cultural": {
                        "idiom": CULTURAL[block_index][lang][0],
                        "note": CULTURAL[block_index][lang][1],
                        "translation": CULTURAL[block_index][lang][2],
                    },
                    "cloze_sentence": base_targets[target_index],
                    "items": items,
                }
            )
        blocks.append((block_title, localized))
    return blocks
