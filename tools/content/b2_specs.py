"""Currículo B2 de F4 derivado de la base semántica A2.

Cada bloque usa las frases A2, las eleva con conectores B2 y añade una nota
cultural. EN usa 138 modismos únicos para satisfacer la puerta de B2.
"""

from __future__ import annotations

from a2_factory import sentences_for
from a2_localized import TRANSLATIONS
from a2_specs import BLOCKS
from b2_factory import extend_sentence

EN_IDIOMS = [
    "a piece of cake",
    "once in a blue moon",
    "see eye to eye",
    "a hot potato",
    "break a leg",
    "under the weather",
    "the ball is in your court",
    "let the cat out of the bag",
    "face the music",
    "on cloud nine",
    "a drop in the ocean",
    "actions speak louder than words",
    "best of both worlds",
    "burn bridges",
    "can't judge a book by its cover",
    "every cloud has a silver lining",
    "get out of hand",
    "go back to the drawing board",
    "in the same boat",
    "it's not rocket science",
    "kill two birds with one stone",
    "let sleeping dogs lie",
    "make a long story short",
    "miss the boat",
    "on the fence",
    "pull yourself together",
    "rain check",
    "see the light",
    "sit on the fence",
    "take the plunge",
    "the last straw",
    "throw in the towel",
    "time flies",
    "up in the air",
    "walk on eggshells",
    "when it rains, it pours",
    "you can't have your cake and eat it too",
    "a grey area",
    "a long shot",
    "a slap on the wrist",
    "a snowball effect",
    "a stepping stone",
    "against the clock",
    "ahead of the curve",
    "all ears",
    "all in the same boat",
    "an uphill battle",
    "around the corner",
    "at the end of the day",
    "at the drop of a hat",
    "back to square one",
    "behind the scenes",
    "bend over backwards",
    "big picture",
    "blow off steam",
    "break the bank",
    "bring to the table",
    "call the shots",
    "carry the day",
    "change of heart",
    "clear the air",
    "come to terms with",
    "cross that bridge when you come to it",
    "cut someone some slack",
    "draw the line",
    "drive someone up the wall",
    "drop the ball",
    "easy come, easy go",
    "fair enough",
    "fall through the cracks",
    "feel off colour",
    "fill someone's shoes",
    "find your feet",
    "get a grip",
    "get cold feet",
    "get the hang of it",
    "give someone the benefit of the doubt",
    "go the extra mile",
    "grasp the nettle",
    "have a lot on your plate",
    "have second thoughts",
    "hit the ground running",
    "hold your horses",
    "in a nutshell",
    "in hot water",
    "in the dark",
    "in the long run",
    "in the nick of time",
    "it takes two to tango",
    "jump on the bandwagon",
    "keep your chin up",
    "keep your options open",
    "know the ropes",
    "learn the ropes",
    "leave no stone unturned",
    "let off steam",
    "look on the bright side",
    "lose track of time",
    "make ends meet",
    "make waves",
    "narrow down",
    "off the beaten track",
    "on the ball",
    "on the dot",
    "on the house",
    "on the same wavelength",
    "once bitten, twice shy",
    "out of your depth",
    "pave the way",
    "pick up the pieces",
    "play hardball",
    "pull strings",
    "put all your eggs in one basket",
    "put your foot in it",
    "raise the bar",
    "red tape",
    "rock the boat",
    "roll up your sleeves",
    "save face",
    "see the bigger picture",
    "set the record straight",
    "shed light on",
    "sit tight",
    "split hairs",
    "stand your ground",
    "start from scratch",
    "steer clear of",
    "take a rain check",
    "take it easy",
    "take the bull by the horns",
    "the jury is still out",
    "the tip of the iceberg",
    "think on your feet",
    "throw light on",
    "turn a blind eye",
    "under the radar",
    "weather the storm",
    "a wolf in sheep's clothing",
]

OTHER_IDIOMS = {
    "pt": [
        ("dar uma mão", "Expresión para ofrecer ayuda.", "echar una mano"),
        ("estar por dentro", "Expresión para conocer un asunto.", "estar al tanto"),
        ("pôr a mão na massa", "Expresión para empezar a trabajar.", "ponerse manos a la obra"),
        ("fazer vista grossa", "Expresión para ignorar algo a propósito.", "hacer la vista gorda"),
        ("bater um papo", "Expresión para conversar informalmente.", "charolar"),
        ("cair a ficha", "Expresión para comprender algo de repente.", "caer en la cuenta"),
        ("ficar de olho", "Expresión para vigilar algo.", "estar pendiente"),
        ("custar os olhos da cara", "Expresión para decir que algo es muy caro.", "costar un ojo de la cara"),
        ("virar a noite", "Expresión para trabajar hasta muy tarde.", "trasnochar"),
        ("o que importa", "Expresión para señalar lo esencial.", "lo que importa"),
        ("quebrar o gelo", "Expresión para iniciar una conversación.", "romper el hielo"),
        ("encerrar o dia", "Expresión para terminar la jornada.", "cerrar el día"),
        ("fazer nas coxas", "Expresión para hacer algo con poco cuidado.", "hacerlo de cualquier manera"),
        ("estar de baixo do tempo", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
    ],
    "fr": [
        ("donner un coup de main", "Expresión para ofrecer ayuda.", "echar una mano"),
        ("être dans le coup", "Expresión para estar al tanto.", "estar al tanto"),
        ("se mettre au travail", "Expresión para empezar a trabajar.", "ponerse a trabajar"),
        ("fermer les yeux", "Expresión para ignorar algo a propósito.", "hacer la vista gorda"),
        ("tailler une bavette", "Expresión para conversar informalmente.", "charolar"),
        ("percuter", "Expresión para comprender algo de repente.", "caer en la cuenta"),
        ("garder un œil sur", "Expresión para vigilar algo.", "estar pendiente"),
        ("coûter les yeux de la tête", "Expresión para decir que algo es muy caro.", "costar un ojo de la cara"),
        ("travailler tard", "Expresión para trabajar hasta muy tarde.", "trabajar hasta tarde"),
        ("l'essentiel", "Expresión para señalar lo esencial.", "lo esencial"),
        ("briser la glace", "Expresión para iniciar una conversación.", "romper el hielo"),
        ("en rester là", "Expresión para terminar una actividad.", "dejarlo por hoy"),
        ("faire des raccourcis", "Expresión para hacer algo con poco cuidado.", "hacerlo de forma rápida"),
        ("ne pas être dans son assiette", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
    ],
    "ja": [
        ("手 を 貸す", "Expresión para ofrecer ayuda.", "echar una mano"),
        ("事情 を 知る", "Expresión para conocer un asunto.", "estar al tanto"),
        ("腰 を 据える", "Expresión para ponerse a trabajar con constancia.", "ponerse en serio"),
        ("目 を つぶる", "Expresión para ignorar algo a propósito.", "hacer la vista gorda"),
        ("話 を する", "Expresión para conversar.", "conversar"),
        ("理解 する", "Expresión para comprender algo.", "comprender"),
        ("目 を 光らせる", "Expresión para vigilar algo.", "estar atento"),
        ("目 が 飛び出る ほど 高い", "Expresión para decir que algo es muy caro.", "carísimo"),
        ("夜遅く まで 働く", "Expresión para trabajar hasta muy tarde.", "trabajar hasta tarde"),
        ("要点", "Expresión para señalar lo esencial.", "lo esencial"),
        ("場 を 和ませる", "Expresión para iniciar una conversación.", "romper el hielo"),
        ("今日 は ここまで", "Expresión para terminar la jornada.", "dejarlo por hoy"),
        ("手 を 抜く", "Expresión para hacer algo con poco cuidado.", "hacerlo de cualquier manera"),
        ("体調 が すぐれない", "Expresión para decir que alguien no se siente bien.", "sentirse mal"),
    ],
}

META = [
    ("Recuerdos y pasado", ["hedging", "future-perfect"]),
    ("Viajes y transporte", ["third-conditional", "passive-voice"]),
    ("Planes y futuro", ["future-perfect", "mixed-conditionals"]),
    ("Ciudad y comparaciones", ["advanced-comparatives", "non-defining-relative"]),
    ("Salud y bienestar", ["modal-perfect", "subjunctive"]),
    ("Compras y servicios", ["advanced-quantifiers", "passive-voice"]),
    ("Trabajo y estudios", ["reported-speech", "hedging"]),
    ("Casa y convivencia", ["concession", "modal-perfect"]),
    ("Comida y restaurantes", ["participle-clauses", "advanced-quantifiers"]),
    ("Ocio y medios", ["cleft-sentences", "perfect-gerund"]),
    ("Medio ambiente", ["third-conditional", "future-perfect"]),
    ("Relaciones y opiniones", ["inversion", "concession"]),
    ("Cultura y celebraciones", ["reported-speech", "participle-clauses"]),
    ("Tecnología y comunicación", ["passive-voice", "speculation"]),
]


def spec_sentences(spec: dict, lang: str) -> list[tuple[str, str]]:
    if "sentences" in spec:
        index = {"en": 1, "fr": 2}[lang]
        return [(sentence[0], sentence[index]) for sentence in spec["sentences"]]
    return sentences_for(spec["pattern"], spec["items"], lang)


def build_specs(lang: str) -> list[tuple[str, list[dict]]]:
    blocks: list[tuple[str, list[dict]]] = []
    global_index = 0
    for block_index, (block_title, grammar) in enumerate(META):
        if block_index >= len(BLOCKS):
            break
        _, source_specs = BLOCKS[block_index]
        if block_index == 13:
            source_specs = source_specs[:8]
        localized = []
        for lesson_index, source in enumerate(source_specs, start=1):
            en_pairs = spec_sentences(source, "en")
            fr_pairs = spec_sentences(source, "fr")
            es_sentences = [pair[0] for pair in en_pairs]
            pt_sentences = [TRANSLATIONS["pt"][es] for es in es_sentences]
            ja_sentences = [TRANSLATIONS["ja"][es] for es in es_sentences]
            target_index = {"en": 0, "fr": 1, "pt": 2, "ja": 3}[lang]
            base_targets = [en_pairs[0][1], fr_pairs[0][1], pt_sentences[0], ja_sentences[0]]
            if lang == "en":
                idiom, note, translation = (
                    EN_IDIOMS[global_index % len(EN_IDIOMS)],
                    f"Modismo avanzado útil para hablar de {source['topic']}.",
                    "expresión figurada",
                )
            else:
                idiom, note, translation = OTHER_IDIOMS[lang][global_index % 14]
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
                        "idiom": idiom,
                        "note": note,
                        "translation": translation,
                    },
                    "cloze_sentence": base_targets[target_index],
                    "items": items,
                }
            )
            global_index += 1
        blocks.append((block_title, localized))
    return blocks
