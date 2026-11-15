"""Currículo C1/C2 de F5/F6 derivado de la base semántica A2."""

from __future__ import annotations

from a2_factory import sentences_for
from a2_localized import TRANSLATIONS
from a2_specs import BLOCKS
from advanced_factory import extend_sentence

CULTURAL = {
    "en": [
        ("damn with faint praise", "Expresión para elogiar de forma que en realidad critica.", "elogiar con tibieza"),
        ("bury the lede", "Expresión para ocultar la información más importante.", "enterrar lo importante"),
        ("jump the shark", "Expresión para señalar que algo ha perdido calidad.", "tocar fondo"),
        ("throw shade", "Expresión para criticar de forma indirecta.", "lanzar una indirecta"),
        ("spill the tea", "Expresión para revelar un cotilleo.", "contar el cotilleo"),
        ("read the riot act", "Expresión para reprender con severidad.", "leer la cartilla"),
        ("move the goalposts", "Expresión para cambiar las condiciones a mitad del proceso.", "cambiar las reglas"),
        ("take umbrage", "Expresión para ofenderse por algo.", "ofenderse"),
        ("beg the question", "Expresión para dar por supuesto lo que se quiere probar.", "presuponer la conclusión"),
        ("have a chip on one's shoulder", "Expresión para describir un resentimiento persistente.", "tener un resentimiento"),
        ("keep a stiff upper lip", "Expresión para mantener la compostura.", "mantener la compostura"),
        ("put a spin on", "Expresión para presentar algo desde un ángulo interesado.", "darle un giro"),
        ("sweep under the rug", "Expresión para ocultar un problema.", "barrer debajo de la alfombra"),
        ("take the moral high ground", "Expresión para adoptar una postura moral superior.", "adoptar superioridad moral"),
    ],
    "pt": [
        ("elogiar com parcimónia", "Expresión para elogiar de forma que en realidad critica.", "elogiar con tibieza"),
        ("esconder o essencial", "Expresión para ocultar la información más importante.", "enterrar lo importante"),
        ("perder a qualidade", "Expresión para señalar que algo ha perdido calidad.", "tocar fondo"),
        ("mandar uma indireta", "Expresión para criticar de forma indirecta.", "lanzar una indirecta"),
        ("contar a fofoca", "Expresión para revelar un cotilleo.", "contar el cotilleo"),
        ("dar uma repreensão", "Expresión para reprender con severidad.", "leer la cartilla"),
        ("mudar as regras", "Expresión para cambiar las condiciones a mitad del proceso.", "cambiar las reglas"),
        ("ficar ofendido", "Expresión para ofenderse por algo.", "ofenderse"),
        ("pressupor a conclusão", "Expresión para dar por supuesto lo que se quiere probar.", "presuponer la conclusión"),
        ("guardar ressentimento", "Expresión para describir un resentimiento persistente.", "tener un resentimiento"),
        ("manter a compostura", "Expresión para mantener la compostura.", "mantener la compostura"),
        ("dar um giro", "Expresión para presentar algo desde un ángulo interesado.", "darle un giro"),
        ("varrer para debaixo do tapete", "Expresión para ocultar un problema.", "barrer debajo de la alfombra"),
        ("adotar superioridade moral", "Expresión para adoptar una postura moral superior.", "adoptar superioridad moral"),
    ],
    "fr": [
        ("louer du bout des lèvres", "Expresión para elogiar de forma que en realidad critica.", "elogiar con tibieza"),
        ("enterrer l'essentiel", "Expresión para ocultar la información más importante.", "enterrar lo importante"),
        ("perdre de sa superbe", "Expresión para señalar que algo ha perdido calidad.", "tocar fondo"),
        ("lancer une pique", "Expresión para criticar de forma indirecta.", "lanzar una indirecta"),
        ("révéler le pot aux roses", "Expresión para revelar un cotilleo.", "contar el cotilleo"),
        ("faire la leçon", "Expresión para reprender con severidad.", "leer la cartilla"),
        ("changer les règles du jeu", "Expresión para cambiar las condiciones a mitad del proceso.", "cambiar las reglas"),
        ("s'offusquer", "Expresión para ofenderse por algo.", "ofenderse"),
        ("présupposer la conclusion", "Expresión para dar por supuesto lo que se quiere probar.", "presuponer la conclusión"),
        ("garder rancune", "Expresión para describir un resentimiento persistente.", "tener un resentimiento"),
        ("garder contenance", "Expresión para mantener la compostura.", "mantener la compostura"),
        ("donner un tour de passe-passe", "Expresión para presentar algo desde un ángulo interesado.", "darle un giro"),
        ("balayer sous le tapis", "Expresión para ocultar un problema.", "barrer debajo de la alfombra"),
        ("prendre de haut", "Expresión para adoptar una postura moral superior.", "adoptar superioridad moral"),
    ],
    "ja": [
        ("控えめ に 褒める", "Expresión para elogiar de forma que en realidad critica.", "elogiar con tibieza"),
        ("本質 を 隠す", "Expresión para ocultar la información más importante.", "enterrar lo importante"),
        ("質 を 落とす", "Expresión para señalar que algo ha perdido calidad.", "tocar fondo"),
        ("遠回し に 批判 する", "Expresión para criticar de forma indirecta.", "lanzar una indirecta"),
        ("ゴシップ を 話す", "Expresión para revelar un cotilleo.", "contar el cotilleo"),
        ("厳しく 叱る", "Expresión para reprender con severidad.", "leer la cartilla"),
        ("ルール を 変える", "Expresión para cambiar las condiciones a mitad del proceso.", "cambiar las reglas"),
        ("気分 を 害する", "Expresión para ofenderse por algo.", "ofenderse"),
        ("結論 を 前提 に する", "Expresión para dar por supuesto lo que se quiere probar.", "presuponer la conclusión"),
        ("恨み を 持つ", "Expresión para describir un resentimiento persistente.", "tener un resentimiento"),
        ("平静 を 保つ", "Expresión para mantener la compostura.", "mantener la compostura"),
        ("見方 を 変える", "Expresión para presentar algo desde un ángulo interesado.", "darle un giro"),
        ("問題 を 隠す", "Expresión para ocultar un problema.", "barrer debajo de la alfombra"),
        ("道徳的 優位 に 立つ", "Expresión para adoptar una postura moral superior.", "adoptar superioridad moral"),
    ],
}

META_C1 = [
    ("Recuerdos y pasado", ["diplomatic-hedging", "mixed-conditionals"]),
    ("Viajes y transporte", ["inversion", "passive-reporting"]),
    ("Planes y futuro", ["mixed-conditionals", "modal-perfect"]),
    ("Ciudad y comparaciones", ["advanced-comparatives", "non-defining-relative"]),
    ("Salud y bienestar", ["subjunctive", "modal-perfect"]),
    ("Compras y servicios", ["softening-requests", "advanced-quantifiers"]),
    ("Trabajo y estudios", ["passive-reporting", "diplomatic-hedging"]),
    ("Casa y convivencia", ["concession", "participle-clauses"]),
    ("Comida y restaurantes", ["nominalisation", "advanced-quantifiers"]),
    ("Ocio y medios", ["cleft-sentences", "ellipsis"]),
    ("Medio ambiente", ["mixed-conditionals", "purpose"]),
    ("Relaciones y opiniones", ["inversion", "concession"]),
    ("Cultura y celebraciones", ["non-defining-relative", "participle-clauses"]),
    ("Tecnología y comunicación", ["passive-reporting", "speculation"]),
]

META_C2 = [
    ("Recuerdos y pasado", ["dry-irony", "mixed-conditionals"]),
    ("Viajes y transporte", ["understatement", "passive-reporting"]),
    ("Planes y futuro", ["mixed-conditionals", "modal-perfect"]),
    ("Ciudad y comparaciones", ["advanced-comparatives", "fronting"]),
    ("Salud y bienestar", ["litotes", "subjunctive"]),
    ("Compras y servicios", ["hyperbole", "advanced-quantifiers"]),
    ("Trabajo y estudios", ["passive-reporting", "nominalisation"]),
    ("Casa y convivencia", ["concession", "participle-clauses"]),
    ("Comida y restaurantes", ["litotes", "apposition"]),
    ("Ocio y medios", ["cleft-sentences", "ellipsis"]),
    ("Medio ambiente", ["rhetorical-questions", "mixed-conditionals"]),
    ("Relaciones y opiniones", ["inversion", "concession"]),
    ("Cultura y celebraciones", ["non-defining-relative", "parallelism"]),
    ("Tecnología y comunicación", ["fronting", "speculation"]),
    ("Proyecto final", ["dry-irony", "rhetorical-questions"]),
]


def spec_sentences(spec: dict, lang: str) -> list[tuple[str, str]]:
    if "sentences" in spec:
        index = {"en": 1, "fr": 2}[lang]
        return [(sentence[0], sentence[index]) for sentence in spec["sentences"]]
    return sentences_for(spec["pattern"], spec["items"], lang)


def build_specs(level: str, lang: str) -> list[tuple[str, list[dict]]]:
    meta = META_C1 if level == "C1" else META_C2
    blocks: list[tuple[str, list[dict]]] = []
    global_index = 0
    for block_index, (block_title, grammar) in enumerate(meta):
        if block_index < 14:
            _, source_specs = BLOCKS[block_index]
            if level == "C1" and block_index == 13:
                source_specs = source_specs[:6]
        else:
            source_specs = BLOCKS[14][1][:2]
        localized = []
        for lesson_index, source in enumerate(source_specs, start=1):
            en_pairs = spec_sentences(source, "en")
            fr_pairs = spec_sentences(source, "fr")
            es_sentences = [pair[0] for pair in en_pairs]
            pt_sentences = [TRANSLATIONS["pt"][es] for es in es_sentences]
            ja_sentences = [TRANSLATIONS["ja"][es] for es in es_sentences]
            target_index = {"en": 0, "fr": 1, "pt": 2, "ja": 3}[lang]
            base_sentences = [en_pairs[i][1] for i in range(3)]
            base_sentences_fr = [fr_pairs[i][1] for i in range(3)]
            base_sentences_pt = pt_sentences
            base_sentences_ja = ja_sentences
            cloze_sentences = [
                base_sentences,
                base_sentences_fr,
                base_sentences_pt,
                base_sentences_ja,
            ][target_index]
            idiom, note, translation = CULTURAL[lang][global_index % 14]
            items = []
            for sentence_index, es in enumerate(es_sentences):
                items.append(
                    (
                        extend_sentence(es, "es", level, lesson_index + sentence_index),
                        extend_sentence(en_pairs[sentence_index][1], "en", level, lesson_index + sentence_index),
                        extend_sentence(fr_pairs[sentence_index][1], "fr", level, lesson_index + sentence_index),
                        extend_sentence(pt_sentences[sentence_index], "pt", level, lesson_index + sentence_index),
                        extend_sentence(ja_sentences[sentence_index], "ja", level, lesson_index + sentence_index),
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
                    "cloze_sentences": cloze_sentences,
                    "items": items,
                }
            )
            global_index += 1
        blocks.append((block_title, localized))
    return blocks
