"""Genera el bloque piloto C2 de ES→EN (F0, ADR-052)."""

from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-c2-p0-un-1"
MARKER = '\n        ]\n      }\n  ]\n}'

LESSONS = [
    {
        "slug": "ironia",
        "title": "Ironía y crítica",
        "scenario": "Una consultora presenta resultados mediocres y la crítica debe ser mordaz sin insultar.",
        "can_do": "Puedo valorar un resultado con ironía seca, elogio tibio y atenuación deliberada.",
        "topic": "la ironía",
        "grammar": [
            ("gp-c2p-ironia-dry", "Dry irony", "Decir lo contrario de lo que se evalúa sin cambiar el tono."),
            ("gp-c2p-ironia-understatement", "Understatement", "Atenuar deliberadamente para intensificar la crítica."),
        ],
        "glossary": [("mediocre", "mediocre"), ("elogio", "praise"), ("atenuación", "understatement")],
        "mc": (
            "Completa: The results were, ___ to speak, less than spectacular.",
            ["so", "such", "as"],
        ),
        "translate": (
            "Los resultados fueron, por así decirlo, modestos",
            ["The", "results", "were", "so", "to", "speak", "modest"],
            ["spectacular", "frankly"],
        ),
        "cloze1": (
            "The presentation was, {{b1}} to speak, less than {{b2}}.",
            [
                ("b1", "so", ["so", "such", "as"]),
                ("b2", "electrifying", ["electrifying", "electric", "electricity"]),
            ],
        ),
        "cloze2": (
            "I wouldn't call the results {{b1}}; rather, they are a {{b2}} triumph of low expectations.",
            [
                ("b1", "disappointing", ["disappointing", "disappointed", "disappoint"]),
                ("b2", "modest", ["modest", "modesty", "modestly"]),
            ],
        ),
        "cloze3": (
            "The report {{b1}} little to the imagination, and its conclusions are, to put it {{b2}}, unambitious.",
            [
                ("b1", "leaves", ["leaves", "leave", "left"]),
                ("b2", "mildly", ["mildly", "mild", "milder"]),
            ],
        ),
        "semantic": (
            "Clasifica la estrategia irónica de cada intervención.",
            [
                ("I wouldn't call it a disaster.", "Atenuación irónica", "formal", "Niega la etiqueta dura para sugerirla."),
                ("The results were less than spectacular.", "Elogio tibio", "formal", "Un elogio mínimo equivale a una crítica."),
                ("So to speak, it was a modest triumph.", "Ironía seca", "literary", "El contraste entre triunfo y modestia crea el sarcasmo."),
            ],
        ),
        "listen_choose": (
            "The results were, so to speak, less than spectacular.",
            [
                "The results were disappointing",
                "The results were excellent",
                "The results were cancelled",
                "The results were secret",
            ],
        ),
        "audio_only": (
            "I wouldn't call the presentation disappointing; rather, it was a modest triumph of low expectations.",
            "What does the speaker imply?",
            [
                "The presentation was mediocre",
                "The presentation was outstanding",
                "The presentation never happened",
                "The results were excellent",
            ],
        ),
        "listen_type": "The results were so to speak less than spectacular",
        "speak": "I wouldn't call the results disappointing.",
        "match": [
            ("faint praise", "elogio tibio"),
            ("dry irony", "ironía seca"),
            ("less than spectacular", "menos que espectacular"),
            ("modest triumph", "triunfo modesto"),
        ],
        "free": (
            "Defiende una valoración irónica de un resultado mediocre: usa elogio tibio y atenuación, sin insultar.",
            45,
            [
                ["results", "mediocre", "praise"],
                ["wouldn't call", "less than", "so to speak"],
                ["irony", "judgement", "understatement"],
            ],
            "I wouldn't call the results disappointing; rather, they are a modest triumph of low expectations. "
            "The presentation was, so to speak, less than electrifying, although it did end on time. "
            "In other words, my judgement is ironic rather than hostile, and the understatement does the critical work.",
            [
                "¿Usaste elogio tibio?",
                "¿Atenuaste sin perder el juicio?",
                "¿Evitaste el insulto directo?",
            ],
        ),
        "cultural": (
            "cut corners",
            "Hacer algo con prisa y mal, ahorrando esfuerzo donde no se debe.",
            "hacer las cosas a medias",
        ),
    },
    {
        "slug": "eufemismos",
        "title": "Eufemismos",
        "scenario": "La dirección anuncia despidos con lenguaje corporativo y el comité pide claridad.",
        "can_do": "Puedo identificar un eufemismo, explicar qué oculta y traducirlo a lenguaje claro.",
        "topic": "el lenguaje corporativo",
        "grammar": [
            ("gp-c2p-eufemismo-euphemism", "Euphemism", "Sustituir términos duros por fórmulas que los suavizan."),
            ("gp-c2p-eufemismo-nominalization", "Nominalization", "Convertir acciones en sustantivos para diluir la responsabilidad."),
        ],
        "glossary": [("despido", "layoff"), ("reestructuración", "restructuring"), ("impacto", "impact")],
        "mc": (
            "Completa: The company announced a ___ of its workforce.",
            ["restructuring", "restructure", "restructured"],
        ),
        "translate": (
            "La empresa anunció una reestructuración de la plantilla",
            ["The", "company", "announced", "a", "restructuring", "of", "the", "workforce"],
            ["layoff", "staff"],
        ),
        "cloze1": (
            "The company {{b1}} a restructuring of its workforce, which {{b2}} to a reduction in staff.",
            [
                ("b1", "announced", ["announced", "announces", "announcing"]),
                ("b2", "led", ["led", "lead", "leading"]),
            ],
        ),
        "cloze2": (
            "Management prefers the {{b1}} 'rightsizing' to the word {{b2}}.",
            [
                ("b1", "euphemism", ["euphemism", "euphoria", "emphasis"]),
                ("b2", "layoffs", ["layoffs", "layers", "layouts"]),
            ],
        ),
        "cloze3": (
            "The {{b1}} of the decision was presented as a {{b2}} rather than a crisis.",
            [
                ("b1", "impact", ["impact", "impacts", "impacted"]),
                ("b2", "restructuring", ["restructuring", "restructure", "restructured"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada fórmula en la nota de prensa.",
            [
                ("We are rightsizing the organisation.", "Eufemismo corporativo", "formal", "Sustituye el despido por una imagen técnica."),
                ("The restructuring will affect 200 roles.", "Nominalización", "formal", "Convierte a las personas en roles y la acción en sustantivo."),
                ("In other words, people will lose their jobs.", "Traducción directa", "neutral", "Deshace el eufemismo con lenguaje claro."),
            ],
        ),
        "listen_choose": (
            "The company announced a restructuring of its workforce.",
            [
                "The company is reorganising staff",
                "The company is hiring",
                "The company is closing",
                "The company is expanding",
            ],
        ),
        "audio_only": (
            "Management calls it rightsizing, but in other words, the restructuring will lead to layoffs.",
            "What does the euphemism hide?",
            ["Job losses", "Higher profits", "New offices", "A merger"],
        ),
        "listen_type": "The company announced a restructuring of its workforce",
        "speak": "Management calls it rightsizing.",
        "match": [
            ("rightsizing", "ajuste de plantilla"),
            ("workforce reduction", "reducción de plantilla"),
            ("corporate euphemism", "eufemismo corporativo"),
            ("severance package", "indemnización por despido"),
        ],
        "free": (
            "Analiza un eufemismo corporativo: identifica la fórmula, explica qué oculta y propón una expresión clara.",
            40,
            [
                ["euphemism", "restructuring", "layoffs"],
                ["rightsizing", "in other words", "impact"],
                ["clarity", "language", "transparency"],
            ],
            "The company calls the measure rightsizing, but the euphemism hides a restructuring that will lead to layoffs. "
            "In other words, the impact falls on the workforce while the language protects management. "
            "Clarity matters here: transparent language should name the job losses directly.",
            [
                "¿Identificaste la fórmula?",
                "¿Explicaste qué oculta?",
                "¿Propusiste una expresión clara?",
            ],
        ),
        "cultural": (
            "the devil is in the details",
            "Los problemas reales aparecen al examinar los detalles.",
            "el diablo está en los detalles",
        ),
    },
    {
        "slug": "retorica",
        "title": "Retórica persuasiva",
        "scenario": "Un discurso político busca movilizar a la audiencia con preguntas y repeticiones.",
        "can_do": "Puedo construir un cierre persuasivo con preguntas retóricas, tríadas y llamada a la acción.",
        "topic": "la retórica",
        "grammar": [
            ("gp-c2p-retorica-question", "Rhetorical questions", "Preguntar sin esperar respuesta para orientar al público."),
            ("gp-c2p-retorica-parallelism", "Parallelism", "Repetir estructuras para crear ritmo y énfasis."),
        ],
        "glossary": [("retórica", "rhetoric"), ("persuasión", "persuasion"), ("promesa", "promise")],
        "mc": (
            "Completa: ___ we really afford to wait any longer?",
            ["Can", "Do", "Are"],
        ),
        "translate": (
            "¿Podemos permitirnos esperar más tiempo?",
            ["Can", "we", "afford", "to", "wait", "any", "longer"],
            ["should", "delay"],
        ),
        "cloze1": (
            "{{b1}} we really afford to wait, and {{b2}} we accept the cost of inaction?",
            [
                ("b1", "Can", ["Can", "Do", "Are"]),
                ("b2", "can", ["can", "do", "are"]),
            ],
        ),
        "cloze2": (
            "We need vision, {{b1}} and courage; in other words, we need {{b2}}.",
            [
                ("b1", "clarity", ["clarity", "clear", "clarify"]),
                ("b2", "leadership", ["leadership", "leader", "leading"]),
            ],
        ),
        "cloze3": (
            "The speech {{b1}} on repetition and {{b2}} to build momentum.",
            [
                ("b1", "relies", ["relies", "rely", "relied"]),
                ("b2", "parallelism", ["parallelism", "parallel", "paragraph"]),
            ],
        ),
        "semantic": (
            "Clasifica el recurso retórico de cada fragmento.",
            [
                ("Can we really afford to wait?", "Pregunta retórica", "formal", "Orienta la respuesta sin pedirla."),
                ("Vision, clarity and courage.", "Tríada", "formal", "Tres términos crean ritmo y memorabilidad."),
                ("We need leadership, not excuses.", "Contraste persuasivo", "formal", "Opone dos conceptos para reforzar la tesis."),
            ],
        ),
        "listen_choose": (
            "Can we really afford to wait any longer?",
            [
                "The speaker urges action",
                "The speaker urges patience",
                "The speaker rejects change",
                "The speaker asks for evidence",
            ],
        ),
        "audio_only": (
            "We need vision, clarity and courage; in other words, we need leadership, not excuses.",
            "What is the speaker's message?",
            [
                "Leadership requires vision and courage",
                "Excuses are acceptable",
                "Waiting is the best option",
                "Vision is unnecessary",
            ],
        ),
        "listen_type": "Can we really afford to wait any longer",
        "speak": "We need vision, clarity and courage.",
        "match": [
            ("rhetorical question", "pregunta retórica"),
            ("call to action", "llamada a la acción"),
            ("parallel structure", "estructura paralela"),
            ("persuasive appeal", "apelación persuasiva"),
        ],
        "free": (
            "Escribe un cierre de discurso: plantea una pregunta retórica, usa una tríada y cierra con una llamada a la acción.",
            35,
            [
                ["vision", "courage", "leadership"],
                ["can we", "really afford", "in other words"],
                ["action", "future", "together"],
            ],
            "Can we really afford to wait any longer? "
            "We need vision, clarity and courage, and we need them now. "
            "In other words, leadership is not about excuses but about action; let us build the future together, starting today.",
            [
                "¿Planteaste una pregunta retórica?",
                "¿Usaste una tríada?",
                "¿Cerraste con una llamada a la acción?",
            ],
        ),
        "cultural": (
            "tip the scales",
            "Inclinar la balanza a favor de una opción o decisión.",
            "inclinar la balanza",
        ),
    },
    {
        "slug": "humor",
        "title": "Humor y doble sentido",
        "scenario": "Una columna cultural juega con la ambigüedad de las palabras y deja una crítica implícita.",
        "can_do": "Puedo construir un juego de palabras, sugerir una crítica y dejar que el lector la complete.",
        "topic": "el humor verbal",
        "grammar": [
            ("gp-c2p-humor-pun", "Puns and double meaning", "Explotar la ambigüedad de una palabra para crear humor."),
            ("gp-c2p-humor-ellipsis", "Ellipsis", "Omitir lo obvio para que el lector complete el sentido."),
        ],
        "glossary": [("juego de palabras", "pun"), ("insinuación", "innuendo"), ("ambigüedad", "ambiguity")],
        "mc": (
            "Completa: The joke ___ on the double meaning of 'bank'.",
            ["depends", "depends on", "depending"],
        ),
        "translate": (
            "El chiste depende del doble sentido",
            ["The", "joke", "depends", "on", "the", "double", "meaning"],
            ["relies", "pun"],
        ),
        "cloze1": (
            "The joke {{b1}} on the double meaning of 'bank', which {{b2}} the audience.",
            [
                ("b1", "depends", ["depends", "depend", "depended"]),
                ("b2", "amuses", ["amuses", "amuse", "amused"]),
            ],
        ),
        "cloze2": (
            "His {{b1}} was subtle: he said one thing and {{b2}} another.",
            [
                ("b1", "innuendo", ["innuendo", "intention", "insight"]),
                ("b2", "implied", ["implied", "imply", "implying"]),
            ],
        ),
        "cloze3": (
            "The columnist {{b1}} the ambiguity, leaving the {{b2}} to the reader.",
            [
                ("b1", "exploits", ["exploits", "exploit", "exploited"]),
                ("b2", "inference", ["inference", "infer", "inferred"]),
            ],
        ),
        "semantic": (
            "Clasifica el mecanismo de humor de cada frase.",
            [
                ("The joke depends on the double meaning of 'bank'.", "Juego de palabras", "neutral", "La ambigüedad sostiene el chiste."),
                ("He said one thing and implied another.", "Insinuación", "informal", "El sentido real queda sugerido."),
                ("The ambiguity is left to the reader.", "Elipsis cómplice", "literary", "El lector completa lo que no se dice."),
            ],
        ),
        "listen_choose": (
            "The joke depends on the double meaning of 'bank'.",
            [
                "The humour comes from ambiguity",
                "The joke is about money",
                "The joke is offensive",
                "The joke has no meaning",
            ],
        ),
        "audio_only": (
            "He said one thing and implied another; in other words, the innuendo does the work.",
            "What makes the remark humorous?",
            [
                "The implied second meaning",
                "The literal meaning",
                "The speaker's accent",
                "The length of the sentence",
            ],
        ),
        "listen_type": "The joke depends on the double meaning of bank",
        "speak": "The joke depends on the double meaning.",
        "match": [
            ("double meaning", "doble sentido"),
            ("subtle innuendo", "insinuación sutil"),
            ("wordplay", "juego de palabras"),
            ("implied criticism", "crítica implícita"),
        ],
        "free": (
            "Escribe una columna breve con doble sentido: plantea una ambigüedad, insinúa una crítica y deja que el lector la complete.",
            40,
            [
                ["joke", "meaning", "ambiguity"],
                ["implied", "innuendo", "wordplay"],
                ["reader", "inference", "subtle"],
            ],
            "The joke depends on the double meaning of the word, and the ambiguity is entirely deliberate. "
            "He said one thing and implied another, leaving the innuendo for the reader to complete. "
            "In other words, the wordplay is subtle enough to survive a second reading.",
            [
                "¿Planteaste una ambigüedad?",
                "¿Insinuaste una crítica?",
                "¿Dejaste algo a la inferencia?",
            ],
        ),
        "cultural": (
            "a double-edged sword",
            "Algo que tiene ventajas y desventajas a la vez.",
            "arma de doble filo",
        ),
    },
    {
        "slug": "estilo",
        "title": "Estilo literario",
        "scenario": "Un traductor debe preservar la metáfora y la cadencia de un pasaje literario.",
        "can_do": "Puedo describir una escena con una metáfora y cuidar el ritmo de la prosa.",
        "topic": "el estilo literario",
        "grammar": [
            ("gp-c2p-estilo-metaphor", "Metaphor and rhythm", "Construir imágenes y ajustar la cadencia de la frase."),
            ("gp-c2p-estilo-participle", "Participle clauses", "Condensar acciones simultáneas con -ing y -ed."),
        ],
        "glossary": [("metáfora", "metaphor"), ("cadencia", "cadence"), ("imagen", "image")],
        "mc": (
            "Completa: The river ___ through the valley like a silver thread.",
            ["winds", "winding", "is winding"],
        ),
        "translate": (
            "El río serpentea por el valle como un hilo de plata",
            ["The", "river", "winds", "through", "the", "valley", "like", "a", "silver", "thread"],
            ["flows", "golden"],
        ),
        "cloze1": (
            "The river {{b1}} through the valley like a silver {{b2}}.",
            [
                ("b1", "winds", ["winds", "winding", "wound"]),
                ("b2", "thread", ["thread", "threat", "bread"]),
            ],
        ),
        "cloze2": (
            "{{b1}} the light fade, the narrator {{b2}} the image of a dying fire.",
            [
                ("b1", "Watching", ["Watching", "Watched", "Watch"]),
                ("b2", "extends", ["extends", "extend", "extended"]),
            ],
        ),
        "cloze3": (
            "The prose {{b1}} a slow cadence, and each {{b2}} lingers in the mind.",
            [
                ("b1", "acquires", ["acquires", "acquire", "acquired"]),
                ("b2", "image", ["image", "imagine", "imagery"]),
            ],
        ),
        "semantic": (
            "Clasifica el recurso estilístico de cada fragmento.",
            [
                ("The river winds like a silver thread.", "Metáfora", "literary", "Compara el río con un hilo sin usar 'like' como nexo lógico."),
                ("Watching the light fade, the narrator...", "Cláusula participial", "literary", "Condensa una acción simultánea."),
                ("The cadence slows the reading.", "Ritmo", "literary", "La forma de la frase modula el tiempo de lectura."),
            ],
        ),
        "listen_choose": (
            "The river winds through the valley like a silver thread.",
            [
                "The river is described with a metaphor",
                "The river is frozen",
                "The valley is silver",
                "The thread is real",
            ],
        ),
        "audio_only": (
            "Watching the light fade, the narrator extends the image of a dying fire; in other words, the cadence carries the mood.",
            "What creates the mood?",
            ["The rhythm and imagery", "The plot twist", "The dialogue", "The historical setting"],
        ),
        "listen_type": "The river winds through the valley like a silver thread",
        "speak": "The river winds through the valley like a silver thread.",
        "match": [
            ("silver thread", "hilo de plata"),
            ("slow cadence", "cadencia lenta"),
            ("dying fire", "fuego moribundo"),
            ("literary image", "imagen literaria"),
        ],
        "free": (
            "Traduce una imagen literaria: describe una escena, usa una metáfora y cuida la cadencia de la frase.",
            40,
            [
                ["river", "light", "image"],
                ["like", "watching", "cadence"],
                ["metaphor", "rhythm", "scene"],
            ],
            "The river winds through the valley like a silver thread, and the light fades slowly behind the hills. "
            "Watching the scene, I wanted to preserve its cadence rather than explain it. "
            "In other words, the metaphor and the rhythm do more than any literal description could.",
            [
                "¿Describiste una escena?",
                "¿Usaste una metáfora?",
                "¿Cuidaste la cadencia?",
            ],
        ),
        "cultural": (
            "the calm before the storm",
            "El periodo de calma que precede a una crisis.",
            "la calma antes de la tormenta",
        ),
    },
    {
        "slug": "matices",
        "title": "Matices de significado",
        "scenario": "Dos diplomáticos eligen palabras casi sinónimas para no dañar una negociación.",
        "can_do": "Puedo distinguir connotaciones, cambiar de registro y elegir la palabra que protege el acuerdo.",
        "topic": "los matices del lenguaje",
        "grammar": [
            ("gp-c2p-matices-connotation", "Connotation", "Elegir palabras por su carga emotiva además de su significado."),
            ("gp-c2p-matices-register", "Register shift", "Ajustar el tono a la relación y al contexto."),
        ],
        "glossary": [("matiz", "nuance"), ("connotación", "connotation"), ("registro", "register")],
        "mc": (
            "Completa: The word 'cheap' has a more negative ___ than 'affordable'.",
            ["connotation", "connotate", "connotative"],
        ),
        "translate": (
            "La palabra tiene una connotación negativa",
            ["The", "word", "has", "a", "negative", "connotation"],
            ["positive", "meaning"],
        ),
        "cloze1": (
            "The word 'cheap' {{b1}} a more negative connotation than {{b2}}.",
            [
                ("b1", "carries", ["carries", "carry", "carried"]),
                ("b2", "affordable", ["affordable", "afford", "affording"]),
            ],
        ),
        "cloze2": (
            "In a formal {{b1}}, 'economical' sounds more {{b2}} than 'cheap'.",
            [
                ("b1", "register", ["register", "registers", "registered"]),
                ("b2", "neutral", ["neutral", "neutrally", "neutrality"]),
            ],
        ),
        "cloze3": (
            "Diplomats {{b1}} their language to avoid unintended {{b2}}.",
            [
                ("b1", "adjust", ["adjust", "adjusts", "adjusted"]),
                ("b2", "connotations", ["connotations", "connotate", "connotative"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada observación sobre el lenguaje.",
            [
                ("'Cheap' carries a negative connotation.", "Connotación", "formal", "Describe la carga emotiva de una palabra."),
                ("'Economical' sounds more neutral.", "Registro", "formal", "Ajusta la palabra al tono de la situación."),
                ("In other words, word choice shapes perception.", "Reformulación", "neutral", "Resume el efecto de la elección léxica."),
            ],
        ),
        "listen_choose": (
            "The word 'cheap' carries a more negative connotation than 'affordable'.",
            [
                "'Cheap' sounds more negative",
                "'Cheap' sounds more formal",
                "Both words are identical",
                "'Affordable' is negative",
            ],
        ),
        "audio_only": (
            "In a formal register, 'economical' sounds more neutral than 'cheap'; in other words, word choice shapes perception.",
            "What is the speaker's point?",
            [
                "Word choice affects how a message is received",
                "All synonyms are equal",
                "Formal language is always better",
                "'Cheap' is the safest option",
            ],
        ),
        "listen_type": "The word cheap carries a negative connotation",
        "speak": "The word has a negative connotation.",
        "match": [
            ("negative connotation", "connotación negativa"),
            ("formal register", "registro formal"),
            ("word choice", "elección de palabras"),
            ("neutral tone", "tono neutro"),
        ],
        "free": (
            "Elige una palabra con carga negativa, propón una alternativa neutra y explica el cambio de matiz.",
            35,
            [
                ["connotation", "word", "meaning"],
                ["cheap", "affordable", "negative"],
                ["register", "neutral", "perception"],
            ],
            "The word cheap carries a negative connotation, while affordable sounds more neutral in a formal register. "
            "In other words, the meaning is similar but the perception changes. "
            "That is why word choice matters when we negotiate with clients.",
            [
                "¿Identificaste la carga negativa?",
                "¿Propusiste una alternativa neutra?",
                "¿Explicaste el cambio de matiz?",
            ],
        ),
        "cultural": (
            "the jury is still out",
            "Todavía no hay una conclusión definitiva sobre un asunto.",
            "aún no hay veredicto",
        ),
    },
    {
        "slug": "filosofia",
        "title": "Argumentación filosófica",
        "scenario": "Un seminario de ética debate si la tecnología neutral puede tener consecuencias injustas.",
        "can_do": "Puedo plantear una paradoja, conceder una objeción y cerrar con una tesis ética.",
        "topic": "la ética filosófica",
        "grammar": [
            ("gp-c2p-filosofia-paradox", "Paradox", "Presentar una contradicción aparente con valor argumental."),
            ("gp-c2p-filosofia-concession", "Concession", "Conceder una objeción antes de reforzar la tesis."),
        ],
        "glossary": [("paradoja", "paradox"), ("premisa", "premise"), ("ética", "ethics")],
        "mc": (
            "Completa: The more freedom we give the system, ___ control we seem to lose.",
            ["the more", "more", "the most"],
        ),
        "translate": (
            "Cuanta más libertad damos al sistema menos control tenemos",
            ["The", "more", "freedom", "we", "give", "the", "system", "the", "less", "control", "we", "have"],
            ["greater", "power"],
        ),
        "cloze1": (
            "The more freedom we give the system, {{b1}} control we seem to {{b2}}.",
            [
                ("b1", "the less", ["the less", "less", "lesser"]),
                ("b2", "lose", ["lose", "loose", "lost"]),
            ],
        ),
        "cloze2": (
            "{{b1}} the technology is neutral, its {{b2}} are not.",
            [
                ("b1", "Although", ["Although", "However", "Despite"]),
                ("b2", "consequences", ["consequences", "consequent", "consequently"]),
            ],
        ),
        "cloze3": (
            "The paradox {{b1}} in the tension between autonomy and {{b2}}.",
            [
                ("b1", "lies", ["lies", "lays", "lied"]),
                ("b2", "accountability", ["accountability", "accountable", "account"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada paso del argumento.",
            [
                ("The more freedom we give, the less control we have.", "Paradoja", "formal", "La relación inversa parece contradecir el sentido común."),
                ("Although the technology is neutral...", "Concesión", "formal", "Acepta una premisa antes de matizarla."),
                ("In other words, autonomy without accountability is dangerous.", "Conclusión", "formal", "Cierra el argumento con una tesis normativa."),
            ],
        ),
        "listen_choose": (
            "The more freedom we give the system, the less control we seem to have.",
            [
                "Greater autonomy can reduce control",
                "Freedom always increases control",
                "The system has no freedom",
                "Control is irrelevant",
            ],
        ),
        "audio_only": (
            "Although the technology is neutral, its consequences are not; in other words, autonomy without accountability is dangerous.",
            "What is the philosophical claim?",
            [
                "Neutral tools can have non-neutral consequences",
                "Technology is always evil",
                "Accountability is optional",
                "Autonomy guarantees safety",
            ],
        ),
        "listen_type": "The more freedom we give the system the less control we have",
        "speak": "Autonomy without accountability is dangerous.",
        "match": [
            ("moral premise", "premisa moral"),
            ("unintended consequence", "consecuencia no deseada"),
            ("ethical dilemma", "dilema ético"),
            ("accountability gap", "vacío de responsabilidad"),
        ],
        "free": (
            "Defiende una tesis ética: plantea una paradoja, concede una objeción y cierra con una conclusión.",
            45,
            [
                ["freedom", "control", "accountability"],
                ["although", "more", "less"],
                ["in other words", "paradox", "consequence"],
            ],
            "The more freedom we give the system, the less control we seem to have, and that is the paradox at the heart of the debate. "
            "Although the technology may be neutral, its consequences are not. "
            "In other words, autonomy without accountability is dangerous, so we need a clear ethical framework.",
            [
                "¿Planteaste una paradoja?",
                "¿Concediste una objeción?",
                "¿Cerraste con una conclusión?",
            ],
        ),
        "cultural": (
            "a skeleton in the closet",
            "Un secreto oculto que puede causar vergüenza si se descubre.",
            "un secreto oculto",
        ),
    },
    {
        "slug": "narrativa",
        "title": "Narrativa y punto de vista",
        "scenario": "Un memoirista reconstruye un recuerdo y el lector sospecha que omite algo.",
        "can_do": "Puedo narrar un recuerdo, cambiar de tiempo verbal y sugerir la falibilidad del narrador.",
        "topic": "la narrativa personal",
        "grammar": [
            ("gp-c2p-narrativa-viewpoint", "Narrative viewpoint", "Construir la voz y la fiabilidad del narrador."),
            ("gp-c2p-narrativa-tense", "Tense shift", "Alternar pasado y presente para revelar incertidumbre."),
        ],
        "glossary": [("narrador", "narrator"), ("recuerdo", "memory"), ("fiabilidad", "reliability")],
        "mc": (
            "Completa: The narrator claims ___ everything, but his memory is selective.",
            ["to remember", "remembering", "remember"],
        ),
        "translate": (
            "El narrador afirma recordarlo todo",
            ["The", "narrator", "claims", "to", "remember", "everything"],
            ["recall", "nothing"],
        ),
        "cloze1": (
            "The narrator {{b1}} to remember everything, but his memory {{b2}} selective.",
            [
                ("b1", "claims", ["claims", "claim", "claimed"]),
                ("b2", "is", ["is", "are", "was"]),
            ],
        ),
        "cloze2": (
            "{{b1}} the story, the narrator moves between past and {{b2}}.",
            [
                ("b1", "Telling", ["Telling", "Told", "Tell"]),
                ("b2", "present", ["present", "presence", "presented"]),
            ],
        ),
        "cloze3": (
            "The {{b1}} of the account depends on what the narrator {{b2}} to mention.",
            [
                ("b1", "reliability", ["reliability", "reliable", "reliably"]),
                ("b2", "chooses", ["chooses", "choose", "chose"]),
            ],
        ),
        "semantic": (
            "Clasifica el recurso narrativo de cada fragmento.",
            [
                ("The narrator claims to remember everything.", "Punto de vista", "literary", "La voz del narrador condiciona el relato."),
                ("Telling the story, he moves between past and present.", "Salto temporal", "literary", "El cambio de tiempo revela inseguridad."),
                ("His reliability depends on what he omits.", "Fiabilidad", "literary", "Lo no dicho cuestiona la versión."),
            ],
        ),
        "listen_choose": (
            "The narrator claims to remember everything, but his memory is selective.",
            [
                "The narrator may not be reliable",
                "The narrator remembers everything",
                "The narrator is omniscient",
                "The memory is complete",
            ],
        ),
        "audio_only": (
            "Telling the story, the narrator moves between past and present; in other words, the tense shift reveals his uncertainty.",
            "What does the tense shift suggest?",
            ["The narrator is uncertain", "The narrator is omniscient", "The story is fictional", "The past is irrelevant"],
        ),
        "listen_type": "The narrator claims to remember everything",
        "speak": "The narrator claims to remember everything.",
        "match": [
            ("unreliable narrator", "narrador poco fiable"),
            ("tense shift", "cambio de tiempo verbal"),
            ("selective memory", "memoria selectiva"),
            ("point of view", "punto de vista"),
        ],
        "free": (
            "Escribe un fragmento de memoria: presenta un recuerdo, cambia de tiempo verbal y sugiere que el narrador no es del todo fiable.",
            45,
            [
                ["narrator", "memory", "reliable"],
                ["remember", "past", "present"],
                ["in other words", "omits", "uncertain"],
            ],
            "The narrator claims to remember everything, but his memory is selective and his reliability is uncertain. "
            "Telling the story, he moves between past and present, as if the tense shift could protect him from what he omits. "
            "In other words, the account reveals more than the narrator intends.",
            [
                "¿Presentaste un recuerdo?",
                "¿Cambiaste de tiempo verbal?",
                "¿Sugeriste la falta de fiabilidad?",
            ],
        ),
        "cultural": (
            "burn bridges",
            "Romper una relación o cerrar una puerta para siempre.",
            "quemar puentes",
        ),
    },
    {
        "slug": "discurso",
        "title": "Discurso público",
        "scenario": "Una graduación necesita un cierre memorable que combine emoción y llamada a la acción.",
        "can_do": "Puedo cerrar un discurso con anáfora, énfasis y una llamada a la acción compartida.",
        "topic": "el discurso público",
        "grammar": [
            ("gp-c2p-discurso-anaphora", "Anaphora", "Repetir el inicio de varias cláusulas para crear impulso."),
            ("gp-c2p-discurso-cleft", "Cleft sentences", "Enfatizar una idea con it is... that y what... is."),
        ],
        "glossary": [("discurso", "speech"), ("cadencia", "cadence"), ("llamada", "call")],
        "mc": (
            "Completa: It is ___ we do now that will define us.",
            ["what", "that", "which"],
        ),
        "translate": (
            "Lo que hagamos ahora nos definirá",
            ["What", "we", "do", "now", "will", "define", "us"],
            ["did", "them"],
        ),
        "cloze1": (
            "It is {{b1}} we do now that will {{b2}} us.",
            [
                ("b1", "what", ["what", "that", "which"]),
                ("b2", "define", ["define", "defines", "defining"]),
            ],
        ),
        "cloze2": (
            "We will {{b1}}, we will {{b2}}, and we will build together.",
            [
                ("b1", "listen", ["listen", "listened", "listening"]),
                ("b2", "learn", ["learn", "learned", "learning"]),
            ],
        ),
        "cloze3": (
            "The {{b1}} of the speech comes from its {{b2}}, not from its length.",
            [
                ("b1", "power", ["power", "powers", "powered"]),
                ("b2", "cadence", ["cadence", "cadent", "cadenza"]),
            ],
        ),
        "semantic": (
            "Clasifica el recurso del discurso.",
            [
                ("It is what we do now that will define us.", "Cleft de énfasis", "formal", "Destaca la acción presente."),
                ("We will listen, we will learn, we will build.", "Anáfora", "formal", "La repetición crea ritmo y compromiso."),
                ("In other words, action matters more than words.", "Conclusión", "neutral", "Resume la llamada a la acción."),
            ],
        ),
        "listen_choose": (
            "It is what we do now that will define us.",
            [
                "Present action will shape the future",
                "The past defines us",
                "Words are enough",
                "The future is fixed",
            ],
        ),
        "audio_only": (
            "We will listen, we will learn, and we will build together; in other words, action matters more than words.",
            "What is the speaker's call to action?",
            ["Act together now", "Wait for the future", "Remember the past", "Avoid change"],
        ),
        "listen_type": "It is what we do now that will define us",
        "speak": "We will listen, we will learn, and we will build together.",
        "match": [
            ("call to action", "llamada a la acción"),
            ("rhetorical cadence", "cadencia retórica"),
            ("shared future", "futuro compartido"),
            ("public speech", "discurso público"),
        ],
        "free": (
            "Escribe el cierre de un discurso: usa una anáfora, una cleft sentence y una llamada a la acción.",
            40,
            [
                ["future", "action", "together"],
                ["what", "it is", "we will"],
                ["in other words", "listen", "learn"],
            ],
            "It is what we do now that will define our shared future. "
            "We will listen to those who disagree, we will learn from our mistakes, and we will build together. "
            "In other words, action matters more than words, so let us begin today.",
            [
                "¿Usaste una anáfora?",
                "¿Usaste una cleft sentence?",
                "¿Cerraste con una llamada a la acción?",
            ],
        ),
        "cultural": (
            "put all your eggs in one basket",
            "Depender de una sola opción sin tener alternativa.",
            "poner todos los huevos en una cesta",
        ),
    },
    {
        "slug": "critica",
        "title": "Crítica cultural",
        "scenario": "Un ensayo satiriza el consumismo imitando el lenguaje de la publicidad.",
        "can_do": "Puedo construir una crítica cultural con alusión literaria, ironía y una reflexión final.",
        "topic": "la crítica cultural",
        "grammar": [
            ("gp-c2p-critica-satire", "Satire", "Criticar mediante exageración, ironía e imitación."),
            ("gp-c2p-critica-allusion", "Allusion", "Referirse a una obra o figura sin explicarla del todo."),
        ],
        "glossary": [("consumismo", "consumerism"), ("sátira", "satire"), ("alusión", "allusion")],
        "mc": (
            "Completa: The essay ___ to Orwell to frame its critique.",
            ["alludes", "allude", "alluded"],
        ),
        "translate": (
            "El ensayo alude a Orwell para enmarcar su crítica",
            ["The", "essay", "alludes", "to", "Orwell", "to", "frame", "its", "critique"],
            ["refers", "praise"],
        ),
        "cloze1": (
            "The essay {{b1}} to Orwell to {{b2}} its critique of consumerism.",
            [
                ("b1", "alludes", ["alludes", "allude", "alluded"]),
                ("b2", "frame", ["frame", "frames", "framed"]),
            ],
        ),
        "cloze2": (
            "The satire {{b1}} exaggeration, but its target is {{b2}}.",
            [
                ("b1", "uses", ["uses", "use", "used"]),
                ("b2", "serious", ["serious", "seriously", "seriousness"]),
            ],
        ),
        "cloze3": (
            "By {{b1}} the language of advertising, the critic exposes its {{b2}}.",
            [
                ("b1", "mimicking", ["mimicking", "mimic", "mimicked"]),
                ("b2", "emptiness", ["emptiness", "empty", "emptied"]),
            ],
        ),
        "semantic": (
            "Clasifica la estrategia de la crítica cultural.",
            [
                ("The essay alludes to Orwell.", "Alusión", "formal", "La referencia literaria enmarca el argumento."),
                ("The satire uses exaggeration.", "Sátira", "formal", "La exageración revela lo absurdo."),
                ("In other words, the critique works by imitation.", "Reformulación", "neutral", "Explica el mecanismo del texto."),
            ],
        ),
        "listen_choose": (
            "The essay alludes to Orwell to frame its critique of consumerism.",
            [
                "The essay uses a literary reference to criticise consumerism",
                "The essay praises Orwell's advertising",
                "The essay is about farming",
                "The essay rejects all criticism",
            ],
        ),
        "audio_only": (
            "By mimicking the language of advertising, the critic exposes its emptiness; in other words, the satire works through imitation.",
            "How does the satire work?",
            ["By imitating advertising language", "By praising consumerism", "By avoiding irony", "By listing statistics"],
        ),
        "listen_type": "The essay alludes to Orwell to frame its critique",
        "speak": "The essay alludes to Orwell to frame its critique.",
        "match": [
            ("cultural critique", "crítica cultural"),
            ("satirical tone", "tono satírico"),
            ("literary allusion", "alusión literaria"),
            ("consumer society", "sociedad de consumo"),
        ],
        "free": (
            "Escribe una crítica cultural: alude a una obra, usa la ironía y cierra con una reflexión sobre el consumo.",
            45,
            [
                ["consumerism", "satire", "critique"],
                ["alludes", "mimicking", "irony"],
                ["in other words", "emptiness", "reflection"],
            ],
            "The essay alludes to Orwell to frame its critique of consumerism, but its real weapon is satire. "
            "By mimicking the language of advertising, the critic exposes its emptiness without ever raising his voice. "
            "In other words, the irony invites a reflection on what we actually need.",
            [
                "¿Aludiste a una obra?",
                "¿Usaste la ironía?",
                "¿Cerraste con una reflexión?",
            ],
        ),
        "cultural": (
            "a silver lining",
            "El lado positivo de una situación difícil.",
            "el lado positivo",
        ),
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-c2-p0-u1-l{index:02d}-e{suffix:02d}"


def semantic_pair(left: str, right: str, register: str, nuance: str) -> dict:
    return {"left": left, "right": right, "register": register, "nuance": nuance}


def build_exercises(index: int, spec: dict) -> list[dict]:
    grammar_one, grammar_two = spec["grammar"]
    free_prompt, free_tokens, free_clusters, free_model, free_criteria = spec["free"]

    def cloze(number: int, rule: str) -> dict:
        passage, blanks = spec[f"cloze{number}"]
        return {
            "type": "ClozeDragDrop",
            "id": exercise_id(index, number + 2),
            "passage": passage,
            "blanks": [
                {"id": blank_id, "answer": blank_answer, "options": blank_options}
                for blank_id, blank_answer, blank_options in blanks
            ],
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "Cada hueco debe encajar con la estructura y el registro del texto.",
                "ruleRef": rule,
            },
            "feedbackOnSuccess": {"explanation": "Los huecos completan la frase con precisión."},
        }

    return [
        {
            "type": "MultipleChoice",
            "id": exercise_id(index, 1),
            "question": spec["mc"][0],
            "options": spec["mc"][1],
            "correctIndex": 0,
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "La forma correcta aparece en la micro-clase y en el modelo de producción oral.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has elegido la forma gramatical precisa."},
        },
        {
            "type": "Translate",
            "id": exercise_id(index, 2),
            "sourcePhrase": spec["translate"][0],
            "correctTokens": spec["translate"][1],
            "distractorTokens": spec["translate"][2],
            "feedbackOnError": {
                "type": "grammar",
                "explanation": f"La traducción correcta es «{' '.join(spec['translate'][1])}».",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "La traducción conserva el significado y el registro."},
        },
        cloze(1, grammar_one[0]),
        cloze(2, grammar_two[0]),
        cloze(3, grammar_one[0]),
        {
            "type": "SemanticMatch",
            "id": exercise_id(index, 6),
            "instructions": spec["semantic"][0],
            "pairs": [semantic_pair(*pair) for pair in spec["semantic"][1]],
            "feedbackOnError": {
                "type": "register",
                "explanation": "Cada intervención cumple una función distinta en el discurso.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has identificado la función de cada intervención."},
        },
        {
            "type": "ListenChoose",
            "id": exercise_id(index, 7),
            "audioText": spec["listen_choose"][0],
            "options": spec["listen_choose"][1],
            "correctIndex": 0,
            "feedbackOnError": {
                "type": "comprehension",
                "explanation": "El audio presenta la idea principal con una reserva o un matiz irónico.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has reconocido la idea principal del audio."},
        },
        {
            "type": "AudioOnlyPrompt",
            "id": exercise_id(index, 8),
            "audioText": spec["audio_only"][0],
            "question": spec["audio_only"][1],
            "options": spec["audio_only"][2],
            "correctIndex": 0,
            "allowSlowPlayback": True,
            "feedbackOnError": {
                "type": "comprehension",
                "explanation": "La segunda parte del audio reformula o cualifica la primera.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has comprendido la postura completa del hablante."},
        },
        {
            "type": "ListenType",
            "id": exercise_id(index, 9),
            "transcription": spec["listen_type"],
            "audioText": spec["listen_type"],
            "feedbackOnError": {
                "type": "spelling",
                "explanation": f"Escribe exactamente «{spec['listen_type']}».",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
        },
        {
            "type": "SpeakRepeat",
            "id": exercise_id(index, 10),
            "targetPhrase": spec["speak"],
            "audioText": spec["speak"],
            "feedbackOnError": {
                "type": "pronunciation",
                "explanation": f"Repite «{spec['speak']}» manteniendo el ritmo y las pausas.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "La frase se entiende y conserva la entonación."},
        },
        {
            "type": "MatchPairs",
            "id": exercise_id(index, 11),
            "pairs": [{"left": left, "right": right} for left, right in spec["match"]],
            "feedbackOnError": {
                "type": "vocabulary",
                "explanation": "Cada colocación tiene una traducción precisa en el contexto literario o profesional.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has asociado cada colocación con su equivalente."},
        },
        {
            "type": "FreeSpeakingPrompt",
            "id": exercise_id(index, 12),
            "prompt": free_prompt,
            "minTokens": free_tokens,
            "conceptClusters": free_clusters,
            "grammarRuleRefs": [grammar_one[0], grammar_two[0]],
            "timeLimitSeconds": 120,
            "minWpm": 70,
            "maxWpm": 160,
            "modelAnswer": free_model,
            "selfAssessmentCriteria": free_criteria,
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "La respuesta modelo cubre la tarea, usa el vocabulario clave y demuestra los dos focos estilísticos.",
                "ruleRef": grammar_one[0],
            },
        },
    ]


def build_lesson(index: int, spec: dict) -> dict:
    grammar_one, grammar_two = spec["grammar"]
    order_source = spec["translate"][0]
    order_target = " ".join(spec["translate"][1])
    return {
        "id": f"es-en-c2-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "C2",
        "scenario": spec["scenario"],
        "canDo": spec["can_do"],
        "grammarPoints": [
            {"id": grammar_one[0], "label": grammar_one[1], "description": grammar_one[2]},
            {"id": grammar_two[0], "label": grammar_two[1], "description": grammar_two[2]},
        ],
        "vocabFrequencyBand": "12000+",
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
                f"Usar vocabulario de nivel C2 sobre {spec['topic']}.",
                f"Resolver una tarea de estilo y matiz sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": f"Foco 1: {grammar_one[1]}",
                    "body": grammar_one[2],
                    "examples": [{"source": order_source, "target": order_target}],
                },
                {
                    "heading": f"Foco 2: {grammar_two[1]}",
                    "body": grammar_two[2],
                    "examples": [{"source": spec["speak"], "target": spec["speak"]}],
                },
            ],
        },
        "glossary": [
            {"term": target, "translations": [source]}
            for source, target in spec["glossary"]
        ],
        "exercises": build_exercises(index, spec),
    }


def fold(text: str) -> str:
    decomposed = unicodedata.normalize("NFD", text.lower())
    return "".join(char for char in decomposed if unicodedata.category(char) != "Mn")


def tokenize(text: str) -> list[str]:
    return [token for token in re.split(r"[^a-z0-9']+", fold(text)) if token]


def sentence_lengths(text: str) -> list[int]:
    return [len(part.split()) for part in re.split(r"[.!?]+", text) if part.strip()]


def edit_distance(first: str, second: str) -> int:
    previous = list(range(len(second) + 1))
    for i, char_one in enumerate(first, start=1):
        current = [i]
        for j, char_two in enumerate(second, start=1):
            current.append(
                min(
                    previous[j] + 1,
                    current[j - 1] + 1,
                    previous[j - 1] + (char_one != char_two),
                )
            )
        previous = current
    return previous[-1]


def self_check(lesson: dict) -> None:
    lesson_id = lesson["id"]
    assert len(lesson["exercises"]) == 12, f"{lesson_id}: se esperaban 12 ejercicios"
    assert len(lesson["grammarPoints"]) == 2, f"{lesson_id}: se esperaban 2 focos"
    for exercise in lesson["exercises"]:
        texts: list[str] = []
        if exercise["type"] == "ClozeDragDrop":
            passage = exercise["passage"]
            for blank in exercise["blanks"]:
                passage = passage.replace("{{" + blank["id"] + "}}", "x")
            texts.append(passage)
        elif exercise["type"] in {"ListenType", "SpeakRepeat"}:
            texts.append(exercise.get("transcription") or exercise.get("targetPhrase"))
        elif exercise["type"] in {"AudioOnlyPrompt", "ListenChoose"}:
            texts.append(exercise["audioText"])
        elif exercise["type"] == "FreeSpeakingPrompt":
            texts.append(exercise["modelAnswer"])
        elif exercise["type"] == "SemanticMatch":
            texts.extend(pair["left"] for pair in exercise["pairs"])
        for text in texts:
            for length in sentence_lengths(text):
                assert length <= 48, f"{lesson_id}/{exercise['id']}: frase de {length} tokens"
    free = next(exercise for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt")
    tokens = tokenize(free["modelAnswer"])
    assert len(tokens) >= free["minTokens"], f"{lesson_id}: modelo demasiado corto ({len(tokens)})"
    covered = 0
    for cluster in free["conceptClusters"]:
        if any(
            all(any(token == word or (len(word) >= 4 and edit_distance(token, word) <= 2) for token in tokens) for word in tokenize(term))
            for term in cluster
        ):
            covered += 1
    required = max(1, (len(free["conceptClusters"]) + 1) // 2)
    assert covered >= required, f"{lesson_id}: cobertura insuficiente ({covered}/{len(free['conceptClusters'])})"
    for exercise in lesson["exercises"]:
        if exercise["type"] == "Translate":
            correct = set(exercise["correctTokens"])
            overlap = correct.intersection(exercise["distractorTokens"])
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
        "title": "Piloto C2 · Estilo y matices",
        "lessons": lessons,
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del curso es-en")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(lessons)} lecciones y {len(lessons) * 12} ejercicios.")


if __name__ == "__main__":
    main()
