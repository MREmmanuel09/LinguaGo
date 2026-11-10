"""Genera el bloque piloto C1 de ES→EN (F0, ADR-052)."""

from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-c1-p0-un-1"
MARKER = '\n        ]\n      },\n      {\n        "id": "es-en-lv-5"'

LESSONS = [
    {
        "slug": "tacto",
        "title": "Negociar con tacto",
        "scenario": "Dos equipos negocian una fusión y deben señalar riesgos sin romper la relación.",
        "can_do": "Puedo formular objeciones diplomáticas, condicionar el acuerdo y proponer garantías.",
        "topic": "la negociación",
        "grammar": [
            ("gp-c1p-tacto-hedging", "Diplomatic hedging", "Atenuar objeciones con I'm afraid, it seems y may."),
            ("gp-c1p-tacto-softening", "Softening requests", "Suavizar peticiones con would, could e I wonder if."),
        ],
        "glossary": [("fusión", "merger"), ("garantía", "guarantee"), ("calendario", "timeline")],
        "mc": (
            "Completa: I wonder ___ we could revisit the timeline.",
            ["if", "that", "whether or not"],
        ),
        "order": (
            "Me pregunto si podríamos revisar el calendario",
            ["I", "wonder", "if", "we", "could", "revisit", "the", "timeline"],
        ),
        "translate": (
            "Me temo que la fusión podría afectar a la confianza",
            ["I'm", "afraid", "the", "merger", "might", "affect", "trust"],
            ["fear", "confidence"],
        ),
        "cloze": (
            "I {{b1}} if we {{b2}} the terms before signing.",
            [
                ("b1", "wonder", ["wonder", "wondered", "wondering"]),
                ("b2", "could revisit", ["could revisit", "can revisit", "revisited"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención en la negociación.",
            [
                ("I'm afraid the timeline may be too tight.", "Objeción atenuada", "formal", "Presenta el riesgo como posibilidad."),
                ("I wonder if we could add a guarantee.", "Petición diplomática", "formal", "Pide sin exigir."),
                ("That said, the merger makes strategic sense.", "Concesión", "formal", "Reconoce el valor antes de añadir una reserva."),
            ],
        ),
        "listen_choose": (
            "I'm afraid the merger might affect trust, but I wonder if a guarantee could solve it.",
            [
                "A guarantee could address the trust concern",
                "The merger is cancelled",
                "Trust is not important",
                "The timeline is the only issue",
            ],
        ),
        "audio_only": (
            "I wonder if we could revisit the terms; that said, the strategic case remains strong.",
            "What is the speaker doing?",
            [
                "Softening a request while keeping support",
                "Rejecting the merger",
                "Demanding a new CEO",
                "Ending the negotiation",
            ],
        ),
        "listen_type": "I wonder if we could revisit the terms before signing",
        "speak": "I'm afraid the merger might affect trust.",
        "speak_es": "Me temo que la fusión podría afectar a la confianza.",
        "match": [
            ("call the shots", "tomar las decisiones"),
            ("bridge the gap", "tender puentes"),
            ("weigh the pros and cons", "sopesar pros y contras"),
            ("strike a balance", "encontrar el equilibrio"),
        ],
        "free": (
            "Negocia una fusión: reconoce el valor estratégico, expresa una objeción y propón una garantía.",
            40,
            [
                ["merger", "trust", "guarantee"],
                ["I'm afraid", "might", "wonder"],
                ["that said", "in other words", "balance"],
            ],
            "I'm afraid the merger might affect trust, although the strategic case is strong. "
            "I wonder if we could add a guarantee and revisit the timeline before signing. "
            "That said, we need to strike a balance; in other words, the deal should protect both growth and confidence.",
            [
                "¿Reconociste el valor estratégico?",
                "¿Expresaste una objeción con cautela?",
                "¿Propusiste una garantía concreta?",
            ],
        ),
        "cultural": (
            "call the shots",
            "Tomar las decisiones importantes en una negociación o en un equipo.",
            "tomar las decisiones",
        ),
    },
    {
        "slug": "informe",
        "title": "Informe ejecutivo",
        "scenario": "La dirección pide un informe breve que resuma datos complejos sin perder matices.",
        "can_do": "Puedo nominalizar procesos, atribuir conclusiones y resumir evidencia con precisión.",
        "topic": "la comunicación corporativa",
        "grammar": [
            ("gp-c1p-informe-nominalization", "Nominalization", "Convertir acciones en sustantivos para un registro formal."),
            ("gp-c1p-informe-passive-report", "Passive report structures", "Atribuir conclusiones con it is reported that y is said to."),
        ],
        "glossary": [("hallazgo", "finding"), ("tendencia", "trend"), ("muestra", "sample")],
        "mc": (
            "Completa: The report ___ to suggest a gradual recovery.",
            ["is said", "are said", "said"],
        ),
        "order": (
            "Se dice que el informe subestima la tendencia",
            ["The", "report", "is", "said", "to", "underestimate", "the", "trend"],
        ),
        "translate": (
            "Los hallazgos apuntan a una recuperación gradual",
            ["The", "findings", "point", "to", "a", "gradual", "recovery"],
            ["pointed", "sudden"],
        ),
        "cloze": (
            "The {{b1}} of the sample {{b2}} the reliability of the findings.",
            [
                ("b1", "size", ["size", "sizes", "sizing"]),
                ("b2", "undermines", ["undermines", "undermine", "undermined"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada frase del informe.",
            [
                ("The findings point to a gradual recovery.", "Conclusión basada en datos", "formal", "Resume la evidencia sin exagerar."),
                ("It is reported that demand has fallen.", "Atribución pasiva", "formal", "La fuente no es el autor del informe."),
                ("In other words, the trend is weaker than expected.", "Reformulación", "neutral", "Traduce el dato a una idea clara."),
            ],
        ),
        "listen_choose": (
            "It is reported that demand has fallen, although the sample is small.",
            [
                "Demand has fallen but the sample is small",
                "Demand has risen sharply",
                "The sample is large",
                "The report is cancelled",
            ],
        ),
        "audio_only": (
            "The findings point to a gradual recovery; that said, the sample size undermines their reliability.",
            "How does the speaker qualify the findings?",
            [
                "They are promising but limited by the sample",
                "They are completely reliable",
                "They are irrelevant",
                "They prove a sudden recovery",
            ],
        ),
        "listen_type": "The report is said to underestimate the trend",
        "speak": "The findings point to a gradual recovery.",
        "speak_es": "Los hallazgos apuntan a una recuperación gradual.",
        "match": [
            ("executive summary", "resumen ejecutivo"),
            ("key finding", "hallazgo clave"),
            ("sample size", "tamaño de la muestra"),
            ("underlying trend", "tendencia subyacente"),
        ],
        "free": (
            "Resume un informe ejecutivo: presenta el hallazgo principal, matiza su alcance y recomienda un siguiente paso.",
            40,
            [
                ["findings", "trend", "sample"],
                ["is said", "point to", "undermines"],
                ["in other words", "recommend", "evidence"],
            ],
            "The findings point to a gradual recovery, but the sample size undermines their reliability. "
            "It is said that demand has fallen, although the trend remains uneven across regions. "
            "In other words, we should recommend a second study before we commit more resources.",
            [
                "¿Presentaste el hallazgo principal?",
                "¿Matizaste su alcance?",
                "¿Recomendaste un siguiente paso?",
            ],
        ),
        "cultural": (
            "bear in mind",
            "Tener algo en cuenta al interpretar datos o tomar una decisión.",
            "tener en cuenta",
        ),
    },
    {
        "slug": "academico",
        "title": "Debate académico",
        "scenario": "Un seminario discute una teoría controvertida y los participantes deben rebatir sin descalificar.",
        "can_do": "Puedo rebatir una tesis, conceder un punto y reformular el desacuerdo con evidencia.",
        "topic": "el debate académico",
        "grammar": [
            ("gp-c1p-academico-concession", "Concession clauses", "Conceder antes de rebatir con although, while y even if."),
            ("gp-c1p-academico-inversion", "Inversion", "Enfatizar con not only... but also y rarely."),
        ],
        "glossary": [("tesis", "thesis"), ("refutar", "refute"), ("evidencia", "evidence")],
        "mc": (
            "Completa: Not only ___ the theory incomplete, but it also ignores context.",
            ["is", "it is", "was"],
        ),
        "order": (
            "Aunque la teoría es sugerente carece de pruebas",
            ["Although", "the", "theory", "is", "suggestive", "it", "lacks", "evidence"],
        ),
        "translate": (
            "La evidencia no respalda una conclusión tan rotunda",
            ["The", "evidence", "does", "not", "support", "such", "a", "sweeping", "conclusion"],
            ["broad", "proof"],
        ),
        "cloze": (
            "While the {{b1}} is provocative, it {{b2}} a crucial distinction.",
            [
                ("b1", "thesis", ["thesis", "theses", "theoretical"]),
                ("b2", "overlooks", ["overlooks", "overlook", "overlooked"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención en el seminario.",
            [
                ("While the thesis is provocative, it overlooks context.", "Concesión y reserva", "formal", "Reconoce el valor y añade una objeción."),
                ("Not only is the sample small, but it is also biased.", "Énfasis por inversión", "formal", "Refuerza la crítica con dos razones."),
                ("In other words, the claim needs qualification.", "Reformulación", "neutral", "Resume la objeción en una fórmula clara."),
            ],
        ),
        "listen_choose": (
            "While the thesis is provocative, it overlooks a crucial distinction.",
            [
                "The thesis is interesting but incomplete",
                "The thesis is fully proven",
                "The thesis is irrelevant",
                "The distinction is minor",
            ],
        ),
        "audio_only": (
            "Not only is the evidence limited, but it also comes from a single case; in other words, the claim needs qualification.",
            "What is the speaker's criticism?",
            [
                "The evidence is limited and narrow",
                "The claim is fully supported",
                "The case is too broad",
                "The theory is irrelevant",
            ],
        ),
        "listen_type": "Not only is the evidence limited but it also comes from a single case",
        "speak": "While the thesis is provocative, it overlooks context.",
        "speak_es": "Aunque la tesis es sugerente, pasa por alto el contexto.",
        "match": [
            ("sweeping claim", "afirmación categórica"),
            ("empirical evidence", "pruebas empíricas"),
            ("counterargument", "contraargumento"),
            ("qualify a claim", "matizar una afirmación"),
        ],
        "free": (
            "Rebate una tesis: concede un punto, presenta una objeción y reformula tu postura con evidencia.",
            40,
            [
                ["thesis", "evidence", "claim"],
                ["while", "not only", "overlooks"],
                ["in other words", "qualify", "context"],
            ],
            "While the thesis is provocative, it overlooks the context of the study and relies on a single case. "
            "Not only is the evidence limited, but the claim is also broader than the data allow. "
            "In other words, we should qualify the conclusion until we have stronger empirical support.",
            [
                "¿Concediste un punto válido?",
                "¿Presentaste una objeción con evidencia?",
                "¿Reformulaste tu postura?",
            ],
        ),
        "cultural": (
            "throw light on",
            "Aclarar o explicar un aspecto complejo de un debate.",
            "arrojar luz sobre",
        ),
    },
    {
        "slug": "etica",
        "title": "Ética y tecnología",
        "scenario": "Una empresa debate regular el uso de IA generativa sin frenar la innovación.",
        "can_do": "Puedo valorar riesgos éticos, especular sobre consecuencias y proponer salvaguardas.",
        "topic": "la ética tecnológica",
        "grammar": [
            ("gp-c1p-etica-speculation", "Modals of speculation", "Valorar riesgos con may, might y could have."),
            ("gp-c1p-etica-conditionals", "Conditionals", "Escenarios hipotéticos con second y third conditional."),
        ],
        "glossary": [("salvaguarda", "safeguard"), ("sesgo", "bias"), ("responsabilidad", "accountability")],
        "mc": (
            "Completa: Without safeguards, the model ___ amplify existing bias.",
            ["could", "must", "should"],
        ),
        "order": (
            "Si hubiéramos previsto el riesgo habríamos actuado antes",
            ["If", "we", "had", "anticipated", "the", "risk", "we", "would", "have", "acted", "sooner"],
        ),
        "translate": (
            "La empresa podría haber evitado el sesgo",
            ["The", "company", "could", "have", "avoided", "the", "bias"],
            ["might", "prevented"],
        ),
        "cloze": (
            "If regulators {{b1}} earlier, the model {{b2}} less bias.",
            [
                ("b1", "had intervened", ["had intervened", "intervened", "have intervened"]),
                ("b2", "might have shown", ["might have shown", "might show", "showed"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención en el comité.",
            [
                ("The model could amplify existing bias.", "Riesgo especulativo", "formal", "Señala una consecuencia posible."),
                ("If we had audited the data, we would have caught it.", "Condicional irreal", "formal", "Imagina una medida que no se tomó."),
                ("In other words, accountability must be designed in.", "Conclusión normativa", "formal", "Cierra con una obligación de diseño."),
            ],
        ),
        "listen_choose": (
            "Without safeguards, the model could amplify existing bias.",
            [
                "The model may increase bias",
                "The model is already fair",
                "The model cannot be audited",
                "Safeguards are unnecessary",
            ],
        ),
        "audio_only": (
            "If we had audited the data earlier, we might have caught the bias; in other words, accountability must be built into the process.",
            "What does the speaker recommend?",
            [
                "Building accountability into the process",
                "Ignoring the bias",
                "Replacing the model immediately",
                "Removing all safeguards",
            ],
        ),
        "listen_type": "Without safeguards the model could amplify existing bias",
        "speak": "Accountability must be built into the process.",
        "speak_es": "La responsabilidad debe integrarse en el proceso.",
        "match": [
            ("safeguard", "salvaguarda"),
            ("algorithmic bias", "sesgo algorítmico"),
            ("informed consent", "consentimiento informado"),
            ("accountability framework", "marco de responsabilidad"),
        ],
        "free": (
            "Evalúa un riesgo ético de la IA: describe el escenario, especula sobre su impacto y propón una salvaguarda.",
            40,
            [
                ["bias", "risk", "model"],
                ["could", "might have", "if"],
                ["in other words", "safeguard", "accountability"],
            ],
            "If we had audited the training data, we might have detected the bias before launch. "
            "Without safeguards, the model could amplify that risk and damage public trust. "
            "In other words, we need an accountability framework and an independent audit before we scale the system.",
            [
                "¿Describiste el escenario?",
                "¿Especulaste sobre el impacto?",
                "¿Propusiste una salvaguarda?",
            ],
        ),
        "cultural": (
            "turn a blind eye",
            "Ignorar deliberadamente un problema o una conducta.",
            "hacer la vista gorda",
        ),
    },
    {
        "slug": "clima",
        "title": "Cambio climático",
        "scenario": "Un comité municipal evalúa un plan de adaptación costoso y debe justificar prioridades.",
        "can_do": "Puedo argumentar sobre políticas climáticas, imaginar escenarios y priorizar medidas.",
        "topic": "la política climática",
        "grammar": [
            ("gp-c1p-clima-third-conditional", "Third conditional", "Escenarios pasados con had + participio y would have."),
            ("gp-c1p-clima-inversion", "Inversion", "Condicional invertido con Had we..., ..."),
        ],
        "glossary": [("adaptación", "adaptation"), ("resiliencia", "resilience"), ("emisiones", "emissions")],
        "mc": (
            "Completa: Had the city acted sooner, the damage ___ lower.",
            ["would have been", "will be", "had been"],
        ),
        "order": (
            "Si hubiéramos invertido antes la ciudad sería más resiliente",
            ["If", "we", "had", "invested", "earlier", "the", "city", "would", "be", "more", "resilient"],
        ),
        "translate": (
            "La adaptación exige inversión sostenida",
            ["Adaptation", "requires", "sustained", "investment"],
            ["demands", "continuous"],
        ),
        "cloze": (
            "Had we {{b1}} emissions earlier, the city {{b2}} less exposed today.",
            [
                ("b1", "cut", ["cut", "cutting", "cuts"]),
                ("b2", "would be", ["would be", "will be", "had been"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención en el comité.",
            [
                ("Had we acted sooner, the damage would have been lower.", "Condicional irreal invertido", "formal", "Imagina un pasado alternativo."),
                ("Adaptation requires sustained investment.", "Afirmación de política", "formal", "Define una condición estructural."),
                ("In other words, prevention is cheaper than repair.", "Conclusión", "neutral", "Sintetiza el argumento económico."),
            ],
        ),
        "listen_choose": (
            "Had we invested earlier, the city would be more resilient today.",
            [
                "Earlier investment would improve resilience now",
                "The city is already resilient",
                "Investment is unnecessary",
                "The plan failed completely",
            ],
        ),
        "audio_only": (
            "Had we cut emissions earlier, the city would be less exposed today; in other words, prevention is cheaper than repair.",
            "What is the main argument?",
            [
                "Prevention is more economical than repair",
                "Repair is the only option",
                "Emissions are irrelevant",
                "The city is fully prepared",
            ],
        ),
        "listen_type": "Had we invested earlier the city would be more resilient today",
        "speak": "Adaptation requires sustained investment.",
        "speak_es": "La adaptación exige una inversión sostenida.",
        "match": [
            ("resilience", "resiliencia"),
            ("emissions target", "objetivo de emisiones"),
            ("climate adaptation", "adaptación climática"),
            ("sustained investment", "inversión sostenida"),
        ],
        "free": (
            "Argumenta sobre adaptación climática: plantea un escenario, justifica una prioridad y anticipa el coste de no actuar.",
            40,
            [
                ["adaptation", "resilience", "investment"],
                ["had", "would", "if"],
                ["in other words", "cost", "prevention"],
            ],
            "Had we invested in adaptation earlier, the city would be more resilient today. "
            "The plan requires sustained investment, but the cost of inaction is higher than the cost of prevention. "
            "In other words, protecting the most exposed neighbourhoods should be our first priority.",
            [
                "¿Planteaste un escenario?",
                "¿Justificaste una prioridad?",
                "¿Anticipaste el coste de no actuar?",
            ],
        ),
        "cultural": (
            "in the long run",
            "A largo plazo; expresa el horizonte de una política o decisión.",
            "a largo plazo",
        ),
    },
    {
        "slug": "liderazgo",
        "title": "Liderazgo",
        "scenario": "Una nueva directora quiere cambiar la cultura de equipo sin imponer sus ideas.",
        "can_do": "Puedo proponer un cambio de cultura, reconocer resistencias y formular una visión compartida.",
        "topic": "el liderazgo",
        "grammar": [
            ("gp-c1p-liderazgo-cleft", "Cleft sentences", "Enfatizar con what we need is e it is... that."),
            ("gp-c1p-liderazgo-gerund", "Gerunds and infinitives", "Elegir -ing o to + infinitivo tras verbos de cambio."),
        ],
        "glossary": [("cultura", "culture"), ("resistencia", "resistance"), ("visión", "vision")],
        "mc": (
            "Completa: What the team needs ___ a clearer vision.",
            ["is", "are", "be"],
        ),
        "order": (
            "Lo que necesitamos es una visión compartida",
            ["What", "we", "need", "is", "a", "shared", "vision"],
        ),
        "translate": (
            "La directora propuso revisar los procesos",
            ["The", "director", "proposed", "reviewing", "the", "processes"],
            ["suggested", "procedures"],
        ),
        "cloze": (
            "What {{b1}} need is {{b2}} trust across teams.",
            [
                ("b1", "we", ["we", "us", "our"]),
                ("b2", "to build", ["to build", "building", "build"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención de la directora.",
            [
                ("What we need is a shared vision.", "Énfasis con cleft", "formal", "Destaca la prioridad del equipo."),
                ("It is trust that makes change possible.", "Énfasis con it-cleft", "formal", "Señala la condición del cambio."),
                ("In other words, leadership is about listening.", "Reformulación", "neutral", "Resume la idea en una frase clara."),
            ],
        ),
        "listen_choose": (
            "What the team needs is a clearer vision, not another reorganisation.",
            [
                "A clearer vision rather than another reorganisation",
                "Another reorganisation",
                "A new office",
                "Less communication",
            ],
        ),
        "audio_only": (
            "It is trust that makes change possible; in other words, leadership starts with listening.",
            "What does the speaker believe?",
            [
                "Leadership begins with listening",
                "Change is impossible",
                "Trust is irrelevant",
                "Reorganisation is the answer",
            ],
        ),
        "listen_type": "What the team needs is a clearer vision",
        "speak": "It is trust that makes change possible.",
        "speak_es": "Es la confianza lo que hace posible el cambio.",
        "match": [
            ("shared vision", "visión compartida"),
            ("build trust", "generar confianza"),
            ("overcome resistance", "superar la resistencia"),
            ("lead by example", "predicar con el ejemplo"),
        ],
        "free": (
            "Propón un cambio de cultura: describe la situación, reconoce una resistencia y formula una visión compartida.",
            40,
            [
                ["culture", "trust", "vision"],
                ["what", "is", "it is"],
                ["in other words", "resistance", "listening"],
            ],
            "What the team needs is a clearer vision and a culture of trust, not another reorganisation. "
            "It is listening that will help us overcome resistance and turn the change into a shared project. "
            "In other words, leadership means building trust before demanding results.",
            [
                "¿Describiste la situación?",
                "¿Reconociste una resistencia?",
                "¿Formulaste una visión compartida?",
            ],
        ),
        "cultural": (
            "on the same wavelength",
            "Compartir la misma forma de pensar o comunicarse.",
            "estar en sintonía",
        ),
    },
    {
        "slug": "arte",
        "title": "Arte y crítica",
        "scenario": "Una crítica de arte debe valorar una exposición polémica sin caer en el elogio vacío.",
        "can_do": "Puedo describir una obra, interpretar su intención y emitir un juicio matizado.",
        "topic": "la crítica de arte",
        "grammar": [
            ("gp-c1p-arte-participle", "Participle clauses", "Condensar causas y secuencias con -ing y -ed."),
            ("gp-c1p-arte-relative", "Relative clauses", "Definir una obra con that, which y who."),
        ],
        "glossary": [("exposición", "exhibition"), ("obra", "work"), ("intención", "intention")],
        "mc": (
            "Completa: ___ by the controversy, the artist refused to comment.",
            ["Surprised", "Surprising", "Surprise"],
        ),
        "order": (
            "La obra que causó polémica atrajo a miles de visitantes",
            ["The", "work", "that", "caused", "controversy", "attracted", "thousands", "of", "visitors"],
        ),
        "translate": (
            "La exposición invita a cuestionar el consumo",
            ["The", "exhibition", "invites", "us", "to", "question", "consumption"],
            ["encourages", "spending"],
        ),
        "cloze": (
            "{{b1}} by the controversy, the artist {{b2}} to explain the work.",
            [
                ("b1", "Surprised", ["Surprised", "Surprising", "Surprise"]),
                ("b2", "refused", ["refused", "refuses", "refusing"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada frase de la crítica.",
            [
                ("Having seen the exhibition twice, I still find it unsettling.", "Cláusula participial", "formal", "Condensa una experiencia previa."),
                ("The work that caused controversy is also the most powerful.", "Cláusula relativa", "formal", "Identifica la obra dentro de la exposición."),
                ("In other words, the provocation serves a purpose.", "Reformulación", "neutral", "Explica el sentido de la provocación."),
            ],
        ),
        "listen_choose": (
            "The work that caused controversy is also the most powerful.",
            [
                "The controversial work is the strongest",
                "The controversy weakened the work",
                "The work is unremarkable",
                "The artist avoided controversy",
            ],
        ),
        "audio_only": (
            "Having seen the exhibition twice, I still find it unsettling; in other words, the provocation serves a purpose.",
            "What is the critic's view?",
            [
                "The provocation is meaningful",
                "The exhibition is boring",
                "The artist should apologise",
                "The work lacks intention",
            ],
        ),
        "listen_type": "The work that caused controversy is also the most powerful",
        "speak": "The exhibition invites us to question consumption.",
        "speak_es": "La exposición nos invita a cuestionar el consumo.",
        "match": [
            ("visual impact", "impacto visual"),
            ("artist's intention", "intención del artista"),
            ("critical response", "respuesta crítica"),
            ("provocative work", "obra provocadora"),
        ],
        "free": (
            "Escribe una crítica breve: describe la obra, interpreta su intención y emite un juicio matizado.",
            40,
            [
                ["exhibition", "work", "intention"],
                ["having seen", "that", "which"],
                ["in other words", "provocation", "judgement"],
            ],
            "The work that caused controversy is also the most powerful piece in the exhibition. "
            "Having seen it twice, I still find its provocation purposeful rather than empty. "
            "In other words, my judgement is positive, although the artist's intention remains deliberately ambiguous.",
            [
                "¿Describiste la obra?",
                "¿Interpretaste su intención?",
                "¿Emitiste un juicio matizado?",
            ],
        ),
        "cultural": (
            "raise eyebrows",
            "Provocar sorpresa o desaprobación; útil para valorar una obra polémica.",
            "llamar la atención",
        ),
    },
    {
        "slug": "contratos",
        "title": "Derecho y contratos",
        "scenario": "Un equipo legal revisa un contrato internacional y detecta cláusulas ambiguas.",
        "can_do": "Puedo identificar ambigüedades, describir obligaciones y proponer una redacción precisa.",
        "topic": "el Derecho contractual",
        "grammar": [
            ("gp-c1p-contratos-passive", "Passive voice", "Pasiva formal para describir obligaciones y procesos."),
            ("gp-c1p-contratos-formal-conditional", "Formal conditionals", "Condicionales con should para obligaciones contractuales."),
        ],
        "glossary": [("cláusula", "clause"), ("incumplimiento", "breach"), ("responsabilidad", "liability")],
        "mc": (
            "Completa: Should the supplier fail to deliver, the contract ___ terminated.",
            ["will be", "is being", "had been"],
        ),
        "order": (
            "El contrato será revisado por un abogado independiente",
            ["The", "contract", "will", "be", "reviewed", "by", "an", "independent", "lawyer"],
        ),
        "translate": (
            "La cláusula exige una notificación por escrito",
            ["The", "clause", "requires", "written", "notice"],
            ["demands", "notification"],
        ),
        "cloze": (
            "Should the parties {{b1}} to agree, the dispute {{b2}} to arbitration.",
            [
                ("b1", "fail", ["fail", "fails", "failed"]),
                ("b2", "will be referred", ["will be referred", "is referring", "referred"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada frase del contrato.",
            [
                ("The contract will be reviewed by an independent lawyer.", "Pasiva formal", "formal", "Describe el proceso sin señalar al responsable."),
                ("Should the supplier fail to deliver, the contract will be terminated.", "Condicional formal", "formal", "Fija una consecuencia jurídica."),
                ("In other words, liability remains with the supplier.", "Reformulación", "formal", "Resume la consecuencia en lenguaje claro."),
            ],
        ),
        "listen_choose": (
            "Should the supplier fail to deliver, the contract will be terminated.",
            [
                "Late delivery can end the contract",
                "The contract is already terminated",
                "The supplier sets the deadline",
                "Delivery is optional",
            ],
        ),
        "audio_only": (
            "The contract will be reviewed by an independent lawyer; in other words, liability will be assessed before we sign.",
            "What will happen before signing?",
            [
                "An independent review of liability",
                "An immediate signature",
                "A new supplier",
                "A court hearing",
            ],
        ),
        "listen_type": "The contract will be reviewed by an independent lawyer",
        "speak": "Should the supplier fail to deliver, the contract will be terminated.",
        "speak_es": "Si el proveedor no entrega, el contrato se rescindirá.",
        "match": [
            ("breach of contract", "incumplimiento del contrato"),
            ("liability clause", "cláusula de responsabilidad"),
            ("written notice", "notificación por escrito"),
            ("binding agreement", "acuerdo vinculante"),
        ],
        "free": (
            "Revisa una cláusula ambigua: describe la obligación, señala el riesgo y propón una redacción precisa.",
            40,
            [
                ["clause", "contract", "liability"],
                ["should", "will be", "if"],
                ["in other words", "notice", "precise"],
            ],
            "The clause requires written notice, but it does not specify a deadline or the person responsible. "
            "Should the supplier fail to comply, liability will be difficult to establish. "
            "In other words, we need a precise wording that names the deadline, the recipient and the consequences of a breach.",
            [
                "¿Describiste la obligación?",
                "¿Señalaste el riesgo?",
                "¿Propusiste una redacción precisa?",
            ],
        ),
        "cultural": (
            "cover all bases",
            "Asegurar todos los detalles para evitar riesgos o lagunas.",
            "cubrir todas las bases",
        ),
    },
    {
        "slug": "medicina",
        "title": "Medicina y comunicación",
        "scenario": "Un médico debe explicar un riesgo estadístico a un paciente sin alarmarlo.",
        "can_do": "Puedo explicar probabilidades, matizar un riesgo y comprobar que el paciente comprende.",
        "topic": "la comunicación sanitaria",
        "grammar": [
            ("gp-c1p-medicina-hedging", "Hedging", "Atenuar diagnósticos y probabilidades con may y likely."),
            ("gp-c1p-medicina-probability", "Modals of probability", "Expresar probabilidad con may, might y is likely to."),
        ],
        "glossary": [("riesgo", "risk"), ("probabilidad", "probability"), ("tratamiento", "treatment")],
        "mc": (
            "Completa: The treatment ___ cause mild side effects.",
            ["may", "must", "should"],
        ),
        "order": (
            "Es probable que el tratamiento funcione en la mayoría de los casos",
            ["The", "treatment", "is", "likely", "to", "work", "in", "most", "cases"],
        ),
        "translate": (
            "El riesgo de complicaciones es bajo pero no nulo",
            ["The", "risk", "of", "complications", "is", "low", "but", "not", "zero"],
            ["small", "nil"],
        ),
        "cloze": (
            "The treatment {{b1}} work, although the risk {{b2}} not be zero.",
            [
                ("b1", "is likely to", ["is likely to", "likely", "may be"]),
                ("b2", "may", ["may", "must", "should"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada frase de la consulta.",
            [
                ("The risk is low but not zero.", "Matización de riesgo", "formal", "Evita prometer seguridad absoluta."),
                ("The treatment is likely to work.", "Probabilidad", "neutral", "Expresa una expectativa razonable."),
                ("In other words, most patients recover without complications.", "Reformulación", "neutral", "Traduce la estadística a un caso comprensible."),
            ],
        ),
        "listen_choose": (
            "The risk is low but not zero, and most patients recover without complications.",
            [
                "The risk is small but real",
                "The risk is zero",
                "Most patients suffer complications",
                "The treatment never works",
            ],
        ),
        "audio_only": (
            "The treatment is likely to work, although the risk may not be zero; in other words, most patients recover without complications.",
            "How does the doctor describe the treatment?",
            [
                "Likely to work with a small but real risk",
                "Completely risk-free",
                "Ineffective for most patients",
                "Too risky to use",
            ],
        ),
        "listen_type": "The risk is low but not zero",
        "speak": "The treatment is likely to work in most cases.",
        "speak_es": "Es probable que el tratamiento funcione en la mayoría de los casos.",
        "match": [
            ("side effect", "efecto secundario"),
            ("informed decision", "decisión informada"),
            ("risk assessment", "evaluación de riesgo"),
            ("treatment plan", "plan de tratamiento"),
        ],
        "free": (
            "Explica un riesgo médico: describe el tratamiento, matiza la probabilidad y comprueba la comprensión del paciente.",
            40,
            [
                ["risk", "treatment", "probability"],
                ["may", "likely", "might"],
                ["in other words", "understand", "complications"],
            ],
            "The treatment is likely to work in most cases, but the risk of complications is low rather than zero. "
            "It may cause mild side effects, and I want to make sure you understand the probability before we decide. "
            "In other words, most patients recover well, but you should tell me if anything changes.",
            [
                "¿Describiste el tratamiento?",
                "¿Matizaste la probabilidad?",
                "¿Comprobaste la comprensión?",
            ],
        ),
        "cultural": (
            "under the weather",
            "Sentirse ligeramente enfermo; registro informal en contextos sanitarios.",
            "sentirse mal",
        ),
    },
    {
        "slug": "globalizacion",
        "title": "Globalización y cultura",
        "scenario": "Un ensayo debate si la globalización homogeneiza la cultura o abre nuevas formas de intercambio.",
        "can_do": "Puedo sostener una tesis matizada, integrar contraargumentos y cerrar con una síntesis.",
        "topic": "la globalización cultural",
        "grammar": [
            ("gp-c1p-globalizacion-concession", "Concession clauses", "Equilibrar con while, although y even if."),
            ("gp-c1p-globalizacion-discourse", "Discourse markers", "Estructurar con on the one hand, on the other hand y ultimately."),
        ],
        "glossary": [("homogeneización", "homogenisation"), ("intercambio", "exchange"), ("identidad", "identity")],
        "mc": (
            "Completa: While globalisation ___ access to culture, it can also flatten local identities.",
            ["widens", "widen", "widening"],
        ),
        "order": (
            "Por un lado la globalización amplía el acceso por otro lado puede homogeneizar",
            ["On", "the", "one", "hand", "globalisation", "widens", "access", "on", "the", "other", "hand", "it", "can", "homogenise"],
        ),
        "translate": (
            "La identidad local no desaparece sino que se transforma",
            ["Local", "identity", "does", "not", "disappear", "but", "transforms"],
            ["vanish", "changes"],
        ),
        "cloze": (
            "{{b1}} globalisation widens access, it {{b2}} flatten local identities.",
            [
                ("b1", "While", ["While", "However", "Despite"]),
                ("b2", "can", ["can", "must", "should"]),
            ],
        ),
        "semantic": (
            "Clasifica la función de cada intervención del ensayo.",
            [
                ("While globalisation widens access, it can flatten identities.", "Concesión", "formal", "Equilibra un beneficio y un riesgo."),
                ("On the one hand, exchange enriches culture.", "Equilibrio argumental", "formal", "Abre una perspectiva matizada."),
                ("Ultimately, identity transforms rather than disappears.", "Síntesis", "formal", "Cierra con una tesis integradora."),
            ],
        ),
        "listen_choose": (
            "While globalisation widens access to culture, it can also flatten local identities.",
            [
                "Globalisation has benefits and risks for identity",
                "Globalisation destroys all local culture",
                "Access to culture has fallen",
                "Identity is unrelated to globalisation",
            ],
        ),
        "audio_only": (
            "On the one hand, cultural exchange enriches us; on the other hand, globalisation can flatten local identities. Ultimately, identity transforms rather than disappears.",
            "What is the speaker's conclusion?",
            [
                "Identity changes but survives",
                "Identity disappears completely",
                "Cultural exchange is harmful",
                "Globalisation is irrelevant",
            ],
        ),
        "listen_type": "Local identity does not disappear but transforms",
        "speak": "While globalisation widens access, it can flatten local identities.",
        "speak_es": "Aunque la globalización amplía el acceso, puede homogeneizar las identidades locales.",
        "match": [
            ("cultural exchange", "intercambio cultural"),
            ("local identity", "identidad local"),
            ("cultural homogenisation", "homogeneización cultural"),
            ("global reach", "alcance global"),
        ],
        "free": (
            "Sostén una tesis sobre globalización y cultura: reconoce un beneficio, señala un riesgo y cierra con una síntesis.",
            40,
            [
                ["globalisation", "identity", "exchange"],
                ["while", "can", "on the other hand"],
                ["in other words", "transform", "ultimately"],
            ],
            "While globalisation widens access to cultural exchange, it can also flatten local identities. "
            "On the one hand, audiences discover new voices; on the other hand, dominant platforms shape what counts as global. "
            "In other words, local identity does not disappear, but it transforms under pressure from global markets.",
            [
                "¿Reconociste un beneficio?",
                "¿Señalaste un riesgo?",
                "¿Cerraste con una síntesis?",
            ],
        ),
        "cultural": (
            "think globally, act locally",
            "Frase para equilibrar una perspectiva global con una acción concreta.",
            "pensar globalmente y actuar localmente",
        ),
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-c1-p0-u1-l{index:02d}-e{suffix:02d}"


def semantic_pair(left: str, right: str, register: str, nuance: str) -> dict:
    return {"left": left, "right": right, "register": register, "nuance": nuance}


def build_exercises(index: int, spec: dict) -> list[dict]:
    grammar_one, grammar_two = spec["grammar"]
    answer = spec["order"][1]
    rotated = answer[2:] + answer[:2]
    free_prompt, free_tokens, free_clusters, free_model, free_criteria = spec["free"]
    return [
        {
            "type": "MultipleChoice",
            "id": exercise_id(index, 1),
            "question": spec["mc"][0],
            "options": spec["mc"][1],
            "correctIndex": 0,
            "feedbackOnError": {
                "type": "grammar",
                "explanation": f"La forma correcta aparece en la micro-clase: «{' '.join(answer)}».",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has elegido la forma gramatical precisa."},
        },
        {
            "type": "OrderWords",
            "id": exercise_id(index, 2),
            "tokens": rotated,
            "answerTokens": answer,
            "sourcePhrase": spec["order"][0],
            "feedbackOnError": {
                "type": "word_order",
                "explanation": f"El orden natural es «{' '.join(answer)}».",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "El orden produce una frase natural y precisa."},
        },
        {
            "type": "Translate",
            "id": exercise_id(index, 3),
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
        {
            "type": "ClozeDragDrop",
            "id": exercise_id(index, 4),
            "passage": spec["cloze"][0],
            "blanks": [
                {"id": blank_id, "answer": blank_answer, "options": blank_options}
                for blank_id, blank_answer, blank_options in spec["cloze"][1]
            ],
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "Cada hueco debe encajar con la estructura y el registro del texto.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Los huecos completan la frase con precisión."},
        },
        {
            "type": "SemanticMatch",
            "id": exercise_id(index, 5),
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
            "id": exercise_id(index, 6),
            "audioText": spec["listen_choose"][0],
            "options": spec["listen_choose"][1],
            "correctIndex": 0,
            "feedbackOnError": {
                "type": "comprehension",
                "explanation": "El audio presenta la idea principal con una reserva o matiz.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has reconocido la idea principal del audio."},
        },
        {
            "type": "AudioOnlyPrompt",
            "id": exercise_id(index, 7),
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
            "id": exercise_id(index, 8),
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
            "id": exercise_id(index, 9),
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
            "id": exercise_id(index, 10),
            "pairs": [{"left": left, "right": right} for left, right in spec["match"]],
            "feedbackOnError": {
                "type": "vocabulary",
                "explanation": "Cada colocación tiene una traducción precisa en el contexto profesional.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has asociado cada colocación con su equivalente."},
        },
        {
            "type": "FreeSpeakingPrompt",
            "id": exercise_id(index, 11),
            "prompt": free_prompt,
            "minTokens": free_tokens,
            "conceptClusters": free_clusters,
            "grammarRuleRefs": [grammar_one[0], grammar_two[0]],
            "timeLimitSeconds": 90,
            "minWpm": 70,
            "maxWpm": 160,
            "modelAnswer": free_model,
            "selfAssessmentCriteria": free_criteria,
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "La respuesta modelo cubre la tarea, usa el vocabulario clave y demuestra los dos focos gramaticales.",
                "ruleRef": grammar_one[0],
            },
        },
    ]


def build_lesson(index: int, spec: dict) -> dict:
    grammar_one, grammar_two = spec["grammar"]
    order_source = spec["order"][0]
    order_target = " ".join(spec["order"][1])
    return {
        "id": f"es-en-c1-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "C1",
        "scenario": spec["scenario"],
        "canDo": spec["can_do"],
        "grammarPoints": [
            {"id": grammar_one[0], "label": grammar_one[1], "description": grammar_one[2]},
            {"id": grammar_two[0], "label": grammar_two[1], "description": grammar_two[2]},
        ],
        "vocabFrequencyBand": "8000-12000",
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
                f"Usar vocabulario avanzado de {spec['topic']}.",
                f"Resolver una tarea comunicativa sobre {spec['topic']}.",
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
                    "examples": [{"source": spec["speak_es"], "target": spec["speak"]}],
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
    assert len(lesson["exercises"]) == 11, f"{lesson_id}: se esperaban 11 ejercicios"
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
                assert length <= 38, f"{lesson_id}/{exercise['id']}: frase de {length} tokens"
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
        "title": "Piloto C1 · Precisión y argumentación",
        "lessons": lessons,
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 10)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel C1 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(lessons)} lecciones y {len(lessons) * 11} ejercicios.")


if __name__ == "__main__":
    main()
