"""Genera el bloque piloto B2 de ES→EN (F0, ADR-052)."""

from __future__ import annotations

import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
UNIT_ID = "es-en-b2-p0-un-1"
MARKER = '\n      ]\n    },\n      {\n        "id": "es-en-lv-4"'

LESSONS = [
    {
        "slug": "plazo",
        "title": "Negociar un plazo",
        "scenario": "Un proyecto se ha retrasado y el equipo debe renegociar la fecha de entrega con el cliente.",
        "can_do": "Puedo pedir una prórroga con cautela, ofrecer una alternativa y fijar una fecha concreta.",
        "topic": "la gestión de proyectos",
        "grammar": [
            ("gp-b2p-plazo-hedging", "Hedging", "Matizar peticiones con I'm afraid, might e it seems."),
            ("gp-b2p-plazo-future-perfect", "Future perfect", "Situar un resultado antes de una fecha con will have + participio."),
        ],
        "glossary": [("plazo", "deadline"), ("entregable", "deliverable"), ("prórroga", "extension")],
        "mc": (
            "Completa: By Friday, we ___ the first draft.",
            ["will have finished", "finish", "finished"],
        ),
        "order": (
            "Habremos terminado el informe para el viernes",
            ["We", "will", "have", "finished", "the", "report", "by", "Friday"],
        ),
        "cloze": (
            "If we {{b1}} the scope now, we {{b2}} the deadline.",
            [
                ("b1", "reduce", ["reduce", "reduced", "reducing"]),
                ("b2", "will meet", ["will meet", "met", "meeting"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención.",
            [
                ("It seems to me that the deadline may be too tight.", "Objeción atenuada", "formal", "Presenta el problema como posibilidad."),
                ("We might need an extra week.", "Petición de prórroga", "neutral", "Pide más tiempo sin exigir."),
                ("That said, quality remains the priority.", "Concesión", "formal", "Acepta una parte y añade una reserva."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada forma de comunicar el retraso.",
            [
                ("I'm afraid we won't meet the original deadline.", "Formal y atenuado", "formal", "Suaviza la mala noticia."),
                ("We're going to be a bit late.", "Neutral", "neutral", "Informa sin dramatizar."),
                ("No way we'll make it on time.", "Informal", "informal", "Expresión coloquial y directa."),
            ],
        ),
        "audio1": (
            "I'm afraid we might need an extra week; that said, we can deliver the core features by Friday.",
            "What is the speaker's position?",
            [
                "An extension may be needed, but the core features can arrive on Friday",
                "The project is cancelled",
                "The core features are already finished",
                "The deadline is not important",
            ],
        ),
        "audio2": (
            "If we reduce the scope, we will meet the deadline; in other words, we need to prioritise.",
            "What does the speaker recommend?",
            ["Prioritising the work", "Extending the deadline", "Cancelling the project", "Hiring more people"],
        ),
        "listen": "We will have finished the report by Friday",
        "speak": "I'm afraid we might need more time to finish the report.",
        "speak_es": "Me temo que podríamos necesitar más tiempo para terminar el informe.",
        "free": (
            "Negocia una prórroga: reconoce el problema, pide más tiempo y ofrece una alternativa.",
            40,
            [
                ["deadline", "extension", "time"],
                ["I'm afraid", "might", "seems"],
                ["that said", "in other words", "alternative"],
            ],
            "I'm afraid we might need a short extension, because the review is taking longer than expected. "
            "That said, we can deliver the core features by Friday. "
            "In other words, the extension would only affect the final polish, and we can offer a clear alternative plan.",
            [
                "¿Reconociste el problema?",
                "¿Pediste más tiempo con cautela?",
                "¿Ofreciste una alternativa concreta?",
            ],
        ),
        "cultural": (
            "get the ball rolling",
            "Get the ball rolling significa poner en marcha un proyecto o una conversación.",
            "poner en marcha algo",
        ),
    },
    {
        "slug": "reclamacion",
        "title": "Reclamación formal",
        "scenario": "La habitación del hotel no estaba limpia al llegar y recepción no responde con claridad.",
        "can_do": "Puedo describir un problema con la reserva, pedir una solución y aclarar plazos.",
        "topic": "las reclamaciones",
        "grammar": [
            ("gp-b2p-reclamacion-passive", "Passive voice", "Describir acciones con be + participio y centrar el foco en el hecho."),
            ("gp-b2p-reclamacion-indirect", "Indirect questions", "Formular preguntas corteses con could you tell me."),
        ],
        "glossary": [("reclamación", "complaint"), ("reembolso", "refund"), ("comprobante", "receipt")],
        "mc": (
            "Completa: The room ___ before we arrived.",
            ["had been cleaned", "has been cleaned", "was cleaning"],
        ),
        "order": (
            "¿Podrías decirme cuándo se procesará el reembolso?",
            ["Could", "you", "tell", "me", "when", "the", "refund", "will", "be", "processed"],
        ),
        "cloze": (
            "The booking {{b1}} confirmed, but the room {{b2}} ready when we arrived.",
            [
                ("b1", "had been", ["had been", "has been", "was"]),
                ("b2", "was not", ["was not", "is not", "had not"]),
            ],
        ),
        "semantic1": (
            "Empareja cada intervención con su función en la reclamación.",
            [
                ("I'm afraid the room was not ready.", "Queja atenuada", "formal", "Presenta el problema sin atacar a la persona."),
                ("Could you tell me when the refund will arrive?", "Pregunta indirecta", "formal", "Pide información con cortesía."),
                ("I would like to make a formal complaint.", "Reclamación formal", "formal", "Anuncia el propósito de forma explícita."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada forma de expresar descontento.",
            [
                ("I am writing to express my dissatisfaction.", "Formal", "formal", "Fórmula propia de una reclamación escrita."),
                ("We were not happy with the service.", "Neutral", "neutral", "Informa del descontento sin dramatizar."),
                ("The service was a complete disaster.", "Informal", "informal", "Valoración coloquial y contundente."),
            ],
        ),
        "audio1": (
            "I'm afraid the room had not been cleaned when we arrived, so we would like a refund.",
            "What does the guest want?",
            [
                "A refund because the room was not cleaned",
                "A late check-out",
                "A different hotel",
                "An apology only",
            ],
        ),
        "audio2": (
            "Could you tell me when the refund will be processed? I was told it would take five days.",
            "What does the guest ask about?",
            ["When the refund will be processed", "Where the hotel is", "How to cancel the booking", "Who cleaned the room"],
        ),
        "listen": "The room had not been cleaned when we arrived",
        "speak": "I would like to make a formal complaint about the service.",
        "speak_es": "Me gustaría presentar una reclamación formal sobre el servicio.",
        "free": (
            "Explica una queja formal: describe el problema, pide una solución y aclara el plazo.",
            40,
            [
                ["complaint", "refund", "service"],
                ["had been", "was not", "I'm afraid"],
                ["could you tell me", "in other words", "five days"],
            ],
            "I'm afraid the room had not been cleaned when we arrived, and the service was far below the standard we expected. "
            "Could you tell me when the refund will be processed? "
            "In other words, we would like the refund within five days, and we would appreciate a written confirmation.",
            [
                "¿Describiste el problema con claridad?",
                "¿Pediste una solución concreta?",
                "¿Aclaraste el plazo?",
            ],
        ),
        "cultural": (
            "on the same page",
            "Estar en la misma página significa compartir el mismo entendimiento sobre un tema.",
            "estar alineados",
        ),
    },
    {
        "slug": "medios",
        "title": "Medios y desinformación",
        "scenario": "Un titular viral cita una fuente anónima y el equipo debe decidir si lo publica.",
        "can_do": "Puedo atribuir información, matizar una afirmación y recomendar una verificación.",
        "topic": "los medios de comunicación",
        "grammar": [
            ("gp-b2p-medios-reported", "Reported speech", "Reformular lo que dijo otra persona con said that y backshift."),
            ("gp-b2p-medios-hedging", "Hedging", "Atribuir información sin afirmarla como cierta."),
        ],
        "glossary": [("titular", "headline"), ("fuente", "source"), ("bulo", "hoax")],
        "mc": (
            "Completa: The article said the story ___ been verified.",
            ["had", "has", "was"],
        ),
        "order": (
            "Ella dijo que el informe no había sido verificado",
            ["She", "said", "that", "the", "report", "had", "not", "been", "verified"],
        ),
        "cloze": (
            "The journalist {{b1}} that the source {{b2}} reliable.",
            [
                ("b1", "admitted", ["admitted", "admits", "admitting"]),
                ("b2", "might not be", ["might not be", "must not be", "is not"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada frase en el análisis de la noticia.",
            [
                ("According to the article, the source is anonymous.", "Atribución", "formal", "La información se atribuye a otra fuente."),
                ("It seems that the claim has not been verified.", "Cautela", "formal", "Evita presentar la afirmación como un hecho."),
                ("In other words, we should wait.", "Reformulación", "neutral", "Explica la misma idea con palabras más claras."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada valoración.",
            [
                ("The claim is unsubstantiated.", "Formal", "formal", "Vocabulario técnico propio de un análisis."),
                ("There is no proof for that.", "Neutral", "neutral", "Constata la falta de pruebas sin exagerar."),
                ("That is total nonsense.", "Informal", "informal", "Rechazo coloquial y directo."),
            ],
        ),
        "audio1": (
            "She said the report had not been verified; in other words, we should not share it yet.",
            "What does the speaker recommend?",
            ["Waiting before sharing the report", "Publishing the report now", "Deleting the report", "Interviewing the journalist"],
        ),
        "audio2": (
            "According to the article, the source may not be reliable, so read the headline with caution.",
            "Why should we be cautious?",
            ["The source may not be reliable", "The article is too short", "The headline is missing", "The journalist is on holiday"],
        ),
        "listen": "She said the report had not been verified",
        "speak": "According to the article, the source may not be reliable.",
        "speak_es": "Según el artículo, puede que la fuente no sea fiable.",
        "free": (
            "Analiza un titular dudoso: cita la fuente, matiza la afirmación y recomienda una acción.",
            40,
            [
                ["article", "source", "headline"],
                ["said", "might", "seems"],
                ["in other words", "verify", "wait"],
            ],
            "According to the article, the headline is based on an anonymous source, and it seems the claim has not been verified. "
            "She said the report might be accurate, but we should not share it yet. "
            "In other words, we need to verify the facts and wait for a second source before we publish anything.",
            [
                "¿Citaste la fuente?",
                "¿Matizaste la afirmación?",
                "¿Recomendaste una acción concreta?",
            ],
        ),
        "cultural": (
            "blow something out of proportion",
            "Exagerar la importancia de algo; útil al analizar titulares alarmistas.",
            "exagerar algo",
        ),
    },
    {
        "slug": "ciudad",
        "title": "Sostenibilidad urbana",
        "scenario": "El ayuntamiento debate invertir en transporte público o ampliar los carriles bici.",
        "can_do": "Puedo valorar una medida urbana, imaginar otro pasado y anticipar su impacto.",
        "topic": "la sostenibilidad urbana",
        "grammar": [
            ("gp-b2p-ciudad-third-conditional", "Third conditional", "Condicional irreal del pasado con had + participio y would have."),
            ("gp-b2p-ciudad-obligation", "Modals of obligation", "Obligación y recomendación con should, ought to y must."),
        ],
        "glossary": [("residuos", "waste"), ("transporte público", "public transport"), ("huella", "footprint")],
        "mc": (
            "Completa: If the city ___ more trees, the air would be cleaner.",
            ["planted", "plants", "will plant"],
        ),
        "order": (
            "Deberíamos haber invertido más en transporte público",
            ["We", "should", "have", "invested", "more", "in", "public", "transport"],
        ),
        "cloze": (
            "If the council {{b1}} earlier, the problem {{b2}} so serious.",
            [
                ("b1", "had acted", ["had acted", "acted", "has acted"]),
                ("b2", "would not have become", ["would not have become", "will not become", "did not become"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención en el debate.",
            [
                ("We ought to reduce waste at home.", "Obligación moral", "formal", "Señala una responsabilidad compartida."),
                ("If the council had acted earlier, traffic would be lower.", "Condicional irreal", "formal", "Imagina un pasado distinto."),
                ("That said, the budget is limited.", "Concesión", "neutral", "Acepta una parte y añade una reserva."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada valoración de la medida.",
            [
                ("The initiative is environmentally sustainable.", "Formal", "formal", "Lenguaje técnico de políticas públicas."),
                ("It is a greener option.", "Neutral", "neutral", "Valoración sencilla y comprensible."),
                ("It is much greener, honestly.", "Informal", "informal", "Opinión personal con marca coloquial."),
            ],
        ),
        "audio1": (
            "If the council had invested in public transport, traffic would be lower; that said, the new bike lanes are a blessing in disguise.",
            "What does the speaker suggest about the bike lanes?",
            ["They bring an unexpected benefit", "They caused the traffic", "They were cancelled", "They cost too much"],
        ),
        "audio2": (
            "We ought to reduce waste at home; in other words, small habits matter.",
            "What is the main message?",
            ["Small habits make a difference", "Waste is unavoidable", "Home habits are irrelevant", "The council should act alone"],
        ),
        "listen": "If the city planted more trees, the air would be cleaner",
        "speak": "We should have invested more in public transport.",
        "speak_es": "Deberíamos haber invertido más en transporte público.",
        "free": (
            "Propón una medida urbana sostenible: describe el problema, plantea la medida y anticipa su impacto.",
            40,
            [
                ["waste", "transport", "city"],
                ["if", "would", "should"],
                ["that said", "in other words", "impact"],
            ],
            "The city should invest more in public transport, because traffic and waste are growing every year. "
            "If we had planned better, we would not have this problem today. "
            "That said, a new bus lane is a realistic first step; in other words, it would reduce cars and improve air quality.",
            [
                "¿Describiste el problema?",
                "¿Planteaste una medida concreta?",
                "¿Anticipaste su impacto?",
            ],
        ),
        "cultural": (
            "a blessing in disguise",
            "Algo que parece malo pero acaba teniendo un efecto positivo.",
            "una bendición disfrazada",
        ),
    },
    {
        "slug": "remoto",
        "title": "Trabajo remoto",
        "scenario": "El equipo debate si mantener tres días de teletrabajo o volver a la oficina a tiempo completo.",
        "can_do": "Puedo comparar ventajas y dificultades del teletrabajo y proponer un modelo híbrido.",
        "topic": "el trabajo remoto",
        "grammar": [
            ("gp-b2p-remoto-gerund", "Gerunds and infinitives", "Verbos que rigen -ing o to + infinitivo."),
            ("gp-b2p-remoto-modals", "Modals of possibility", "Valorar opciones con might, may y could."),
        ],
        "glossary": [("teletrabajo", "remote work"), ("jornada", "workday"), ("desconexión", "disconnection")],
        "mc": (
            "Completa: I'm used to ___ from home.",
            ["working", "work", "worked"],
        ),
        "order": (
            "Podríamos necesitar replantear la reunión semanal",
            ["We", "might", "need", "to", "rethink", "the", "weekly", "meeting"],
        ),
        "cloze": (
            "She suggested {{b1}} the meeting online, but we decided {{b2}} it in person.",
            [
                ("b1", "holding", ["holding", "to hold", "hold"]),
                ("b2", "to hold", ["to hold", "holding", "hold"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención en la reunión.",
            [
                ("It might be better to meet online.", "Sugerencia atenuada", "formal", "Propone sin imponer."),
                ("I'm used to working remotely.", "Hábito", "neutral", "Describe una rutina consolidada."),
                ("That said, we miss the office.", "Concesión", "informal", "Reconoce la otra cara del teletrabajo."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada valoración del teletrabajo.",
            [
                ("Remote work enhances flexibility.", "Formal", "formal", "Vocabulario propio de un informe."),
                ("Working from home is convenient.", "Neutral", "neutral", "Opinión sencilla y equilibrada."),
                ("Home office rocks.", "Informal", "informal", "Entusiasmo coloquial."),
            ],
        ),
        "audio1": (
            "I'm used to working from home, but I might need a better chair; that said, the flexibility is worth it.",
            "What is the speaker's view?",
            ["The flexibility outweighs the drawback", "The chair is the only problem", "Remote work is impossible", "The office is better in every way"],
        ),
        "audio2": (
            "She suggested holding the meeting online; in other words, we can avoid the commute.",
            "Why meet online?",
            ["To avoid the commute", "To cancel the project", "To hire new staff", "To extend the deadline"],
        ),
        "listen": "She suggested holding the meeting online",
        "speak": "I'm used to working from home.",
        "speak_es": "Estoy acostumbrado a trabajar desde casa.",
        "free": (
            "Describe tu experiencia con el trabajo remoto: ventajas, dificultades y una propuesta.",
            40,
            [
                ["remote work", "home", "flexibility"],
                ["used to", "might", "suggested"],
                ["that said", "in other words", "proposal"],
            ],
            "I am used to working from home, and the flexibility is the biggest advantage for me. "
            "That said, I sometimes miss the office, so I suggested holding one team meeting in person every week. "
            "In other words, a hybrid routine might give us the best of both worlds.",
            [
                "¿Mencionaste una ventaja?",
                "¿Reconociste una dificultad?",
                "¿Hiciste una propuesta concreta?",
            ],
        ),
        "cultural": (
            "think outside the box",
            "Buscar soluciones creativas fuera de lo habitual.",
            "pensar de forma creativa",
        ),
    },
    {
        "slug": "cine",
        "title": "Cine y cultura",
        "scenario": "Un club de cine compara una película reciente con su tráiler y debate si recomendarla.",
        "can_do": "Puedo resumir una película, valorar la actuación y recomendar o no verla.",
        "topic": "el cine",
        "grammar": [
            ("gp-b2p-cine-relative", "Relative clauses", "Cláusulas relativas con which, who y that."),
            ("gp-b2p-cine-participle", "Participle clauses", "Cláusulas participiales para condensar información."),
        ],
        "glossary": [("trama", "plot"), ("actuación", "performance"), ("subtítulo", "subtitle")],
        "mc": (
            "Completa: The film, ___ was released last year, won several awards.",
            ["which", "who", "where"],
        ),
        "order": (
            "El director cuya película ganó el premio dio un discurso",
            ["The", "director", "whose", "film", "won", "the", "award", "gave", "a", "speech"],
        ),
        "cloze": (
            "The film {{b1}} we watched last night {{b2}} directed by a young filmmaker.",
            [
                ("b1", "that", ["that", "who", "where"]),
                ("b2", "was", ["was", "were", "is"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada frase en la reseña.",
            [
                ("The film, which was released last year, won an award.", "Cláusula relativa", "formal", "Añade información sobre la película."),
                ("Having seen the trailer, I expected more action.", "Cláusula participial", "formal", "Condensa una acción previa."),
                ("That said, the ending felt rushed.", "Concesión", "neutral", "Añade una reserva tras el elogio."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada valoración de la actuación.",
            [
                ("The performance was compelling.", "Formal", "formal", "Crítica con vocabulario cuidado."),
                ("The acting was really good.", "Neutral", "neutral", "Valoración clara y sencilla."),
                ("The acting was awesome.", "Informal", "informal", "Entusiasmo coloquial."),
            ],
        ),
        "audio1": (
            "The film, which was released last year, is worth watching; that said, the ending felt rushed.",
            "What is the reviewer's opinion?",
            ["The film is good but the ending is weak", "The film is a complete failure", "The ending is the best part", "The film was never released"],
        ),
        "audio2": (
            "Having seen the trailer, I expected more action; in other words, the plot was slower than advertised.",
            "What surprised the speaker?",
            ["The plot was slower than expected", "The trailer had no music", "The film was too short", "The actors were unknown"],
        ),
        "listen": "The film which we watched last night was directed by a young filmmaker",
        "speak": "The film, which was released last year, won several awards.",
        "speak_es": "La película, que se estrenó el año pasado, ganó varios premios.",
        "free": (
            "Reseña una película: describe la trama, valora la actuación y recomienda o no verla.",
            40,
            [
                ["film", "plot", "performance"],
                ["which", "having seen", "was"],
                ["that said", "in other words", "recommend"],
            ],
            "The film, which was released last year, tells a simple story about a family moving to a new city. "
            "Having seen the trailer, I expected more action, but the performance was compelling. "
            "That said, the plot felt slow in the middle; in other words, I would recommend it for the acting rather than the story.",
            [
                "¿Resumiste la trama?",
                "¿Valoraste la actuación?",
                "¿Recomendaste o no verla?",
            ],
        ),
        "cultural": (
            "hit the nail on the head",
            "Dar exactamente en el clavo con una observación o una crítica.",
            "dar en el clavo",
        ),
    },
    {
        "slug": "privacidad",
        "title": "Tecnología y privacidad",
        "scenario": "Una aplicación pide acceso a los contactos y el usuario revisa qué datos comparte.",
        "can_do": "Puedo explicar cómo se protegen los datos, describir un riesgo y proponer una medida.",
        "topic": "la privacidad digital",
        "grammar": [
            ("gp-b2p-privacidad-passive", "Passive voice", "Pasiva para describir procesos y sistemas."),
            ("gp-b2p-privacidad-second-conditional", "Second conditional", "Hipótesis con if + past simple y would + infinitivo."),
        ],
        "glossary": [("contraseña", "password"), ("rastro", "trace"), ("consentimiento", "consent")],
        "mc": (
            "Completa: Your data ___ stored on a secure server.",
            ["is", "are", "be"],
        ),
        "order": (
            "Si yo fuera tú cambiaría la contraseña",
            ["If", "I", "were", "you", "I", "would", "change", "the", "password"],
        ),
        "cloze": (
            "If users {{b1}} the settings, their data {{b2}} shared with third parties.",
            [
                ("b1", "changed", ["changed", "change", "will change"]),
                ("b2", "would not be", ["would not be", "will not be", "is not"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada frase sobre la aplicación.",
            [
                ("Your data is stored on a secure server.", "Afirmación pasiva", "formal", "Describe el sistema sin señalar al agente."),
                ("If I were you, I would change the password.", "Consejo hipotético", "neutral", "Recomienda una acción concreta."),
                ("In other words, you are in control.", "Reformulación", "formal", "Resume la idea anterior con claridad."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada explicación sobre los datos.",
            [
                ("We obtain explicit consent before sharing data.", "Formal", "formal", "Lenguaje de política de privacidad."),
                ("We ask before sharing your data.", "Neutral", "neutral", "Explicación directa y comprensible."),
                ("We do not snoop on you.", "Informal", "informal", "Expresión coloquial de confianza."),
            ],
        ),
        "audio1": (
            "Your data is stored on a secure server; that said, you should keep an eye on unusual activity.",
            "What should the user do?",
            ["Monitor unusual activity", "Share the password", "Delete the account", "Ignore the settings"],
        ),
        "audio2": (
            "If you changed the settings, your data would not be shared; in other words, you are in control.",
            "What do the settings control?",
            ["Whether data is shared", "The password length", "The server location", "The price of the service"],
        ),
        "listen": "If I were you, I would change the password",
        "speak": "Your data is stored on a secure server.",
        "speak_es": "Tus datos se guardan en un servidor seguro.",
        "free": (
            "Explica cómo proteger tu privacidad: identifica un riesgo, propone una medida y justifícala.",
            40,
            [
                ["privacy", "password", "data"],
                ["if", "would", "should"],
                ["in other words", "settings", "risk"],
            ],
            "Your data is stored on a secure server, but there is still a privacy risk if you share your password. "
            "If I were you, I would change the password today and review the settings on every app. "
            "In other words, small actions can reduce the risk and give you more control over your data.",
            [
                "¿Identificaste un riesgo?",
                "¿Propusiste una medida?",
                "¿La justificaste?",
            ],
        ),
        "cultural": (
            "keep an eye on",
            "Vigilar o prestar atención a algo durante un tiempo.",
            "vigilar algo",
        ),
    },
    {
        "slug": "viaje",
        "title": "Viajes y reclamaciones",
        "scenario": "Un vuelo se retrasa, se pierde la conexión y el equipaje no llega al destino.",
        "can_do": "Puedo contar un problema de viaje, pedir información y presentar una reclamación.",
        "topic": "los viajes",
        "grammar": [
            ("gp-b2p-viaje-past-perfect", "Past perfect", "Acción anterior a otra en el pasado con had + participio."),
            ("gp-b2p-viaje-indirect", "Indirect questions", "Preguntas corteses con could you tell me."),
        ],
        "glossary": [("escala", "layover"), ("equipaje", "luggage"), ("reclamación", "claim")],
        "mc": (
            "Completa: The flight ___ before we reached the gate.",
            ["had left", "has left", "leaves"],
        ),
        "order": (
            "¿Podrías decirme dónde puedo recoger mi equipaje?",
            ["Could", "you", "tell", "me", "where", "I", "can", "collect", "my", "luggage"],
        ),
        "cloze": (
            "We {{b1}} our connection because the flight {{b2}} late.",
            [
                ("b1", "missed", ["missed", "miss", "have missed"]),
                ("b2", "had been", ["had been", "has been", "was being"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención en el aeropuerto.",
            [
                ("Could you tell me where I can collect my luggage?", "Pregunta indirecta", "formal", "Pide información con cortesía."),
                ("The flight had already left.", "Acción anterior", "neutral", "Sitúa el hecho antes de otro pasado."),
                ("I'm afraid we missed the connection.", "Mala noticia atenuada", "formal", "Comunica el problema con suavidad."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada forma de reclamar.",
            [
                ("I would like to submit a claim.", "Formal", "formal", "Fórmula propia de una reclamación escrita."),
                ("I want to complain about my luggage.", "Neutral", "neutral", "Expresa el problema de forma directa."),
                ("My bag never showed up.", "Informal", "informal", "Relato coloquial de lo ocurrido."),
            ],
        ),
        "audio1": (
            "The flight had left before we reached the gate, and to make matters worse, our luggage never arrived.",
            "What two problems did the traveller have?",
            ["A missed flight and lost luggage", "A delayed train and a cold meal", "A cancelled tour and a lost ticket", "A late bus and a broken suitcase"],
        ),
        "audio2": (
            "Could you tell me where I can collect my luggage? I'm afraid it did not arrive with the flight.",
            "What does the traveller need?",
            ["Information about collecting luggage", "A new flight ticket", "A hotel reservation", "A taxi to the city"],
        ),
        "listen": "The flight had left before we reached the gate",
        "speak": "Could you tell me where I can collect my luggage?",
        "speak_es": "¿Podrías decirme dónde puedo recoger mi equipaje?",
        "free": (
            "Explica un problema de viaje: cuenta qué pasó, pide información y solicita una solución.",
            40,
            [
                ["flight", "luggage", "connection"],
                ["had left", "missed", "could you tell me"],
                ["in other words", "claim", "solution"],
            ],
            "The flight had left before we reached the gate, so we missed our connection and our luggage did not arrive. "
            "Could you tell me where I can collect it and how I can submit a claim? "
            "In other words, I need a clear solution today, because we have no clothes for the next two days.",
            [
                "¿Contaste qué pasó?",
                "¿Pediste información?",
                "¿Solicitaste una solución?",
            ],
        ),
        "cultural": (
            "cost an arm and a leg",
            "Costar muchísimo dinero; útil para hablar de precios abusivos.",
            "costar un ojo de la cara",
        ),
    },
    {
        "slug": "estres",
        "title": "Salud y estrés",
        "scenario": "Un compañero lleva semanas durmiendo mal y su mánager nota el agotamiento.",
        "can_do": "Puedo describir señales de estrés, recomendar una pausa y explicar sus beneficios.",
        "topic": "el bienestar laboral",
        "grammar": [
            ("gp-b2p-estres-perfect-continuous", "Present perfect continuous", "Acciones que continúan hasta ahora con have been + -ing."),
            ("gp-b2p-estres-advice", "Modals of advice", "Consejos con should, ought to y had better."),
        ],
        "glossary": [("agotamiento", "burnout"), ("descanso", "rest"), ("límite", "boundary")],
        "mc": (
            "Completa: You ___ take a break before you burn out.",
            ["should", "would", "used to"],
        ),
        "order": (
            "He estado trabajando hasta tarde todas las noches este mes",
            ["I", "have", "been", "working", "late", "every", "night", "this", "month"],
        ),
        "cloze": (
            "I {{b1}} badly for weeks, so I {{b2}} talk to my manager.",
            [
                ("b1", "have been sleeping", ["have been sleeping", "slept", "sleep"]),
                ("b2", "should", ["should", "would", "could"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención en la conversación.",
            [
                ("You should take a break.", "Consejo", "neutral", "Recomienda una acción beneficiosa."),
                ("I have been working late for weeks.", "Acción continua", "neutral", "Describe un hábito que sigue activo."),
                ("That said, I enjoy the project.", "Concesión", "informal", "Reconoce un aspecto positivo pese al problema."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada forma de describir el agotamiento.",
            [
                ("I am experiencing symptoms of burnout.", "Formal", "formal", "Descripción propia de un contexto médico."),
                ("I feel exhausted lately.", "Neutral", "neutral", "Expresión clara y contenida."),
                ("I'm totally wiped out.", "Informal", "informal", "Expresión coloquial de cansancio extremo."),
            ],
        ),
        "audio1": (
            "I have been working late for weeks, and I'm afraid I'm heading for burnout; that said, I enjoy the project.",
            "What is the speaker's situation?",
            ["At risk of burnout despite enjoying the work", "Ready to leave the project", "Bored with the project", "On holiday for a week"],
        ),
        "audio2": (
            "You should set a boundary and take a break; in other words, rest is part of the work.",
            "What does the speaker recommend?",
            ["Resting as part of the work", "Working longer hours", "Ignoring the problem", "Changing careers immediately"],
        ),
        "listen": "I have been working late every night this month",
        "speak": "You should take a break before you burn out.",
        "speak_es": "Deberías tomarte un descanso antes de agotarte.",
        "free": (
            "Da un consejo a alguien con estrés laboral: describe las señales, recomienda una acción y explica por qué.",
            40,
            [
                ["stress", "burnout", "rest"],
                ["have been", "should", "might"],
                ["in other words", "boundary", "break"],
            ],
            "I have been sleeping badly for weeks, and I am afraid I am heading for burnout. "
            "You should set a boundary and take a real break, because rest is part of the work. "
            "In other words, a short pause now might prevent a much longer absence later.",
            [
                "¿Describiste las señales?",
                "¿Recomendaste una acción?",
                "¿Explicaste por qué?",
            ],
        ),
        "cultural": (
            "burn the midnight oil",
            "Trabajar hasta muy tarde; se usa para describir un esfuerzo intenso.",
            "trasnochar trabajando",
        ),
    },
    {
        "slug": "educacion",
        "title": "Educación y debate",
        "scenario": "El centro debate un plan caro para reducir el abandono escolar y pide evidencias antes de votar.",
        "can_do": "Puedo reconocer una objeción, presentar evidencia y cerrar con una conclusión.",
        "topic": "la educación",
        "grammar": [
            ("gp-b2p-educacion-concession", "Concession clauses", "Aunque con although, even though y while."),
            ("gp-b2p-educacion-conditional", "Conditionals", "Condicionales para valorar una propuesta."),
        ],
        "glossary": [("debate", "debate"), ("evidencia", "evidence"), ("acuerdo", "agreement")],
        "mc": (
            "Completa: Although the proposal is expensive, it ___ worth it.",
            ["might be", "might", "maybe"],
        ),
        "order": (
            "Aunque el plan es caro podría mejorar los resultados",
            ["Although", "the", "plan", "is", "expensive", "it", "could", "improve", "results"],
        ),
        "cloze": (
            "{{b1}} the proposal is expensive, it {{b2}} improve results if we implement it carefully.",
            [
                ("b1", "Although", ["Although", "However", "Despite"]),
                ("b2", "could", ["could", "must", "should"]),
            ],
        ),
        "semantic1": (
            "Clasifica la función de cada intervención en el debate.",
            [
                ("Although the plan is expensive, it could work.", "Concesión", "formal", "Acepta una objeción y mantiene la propuesta."),
                ("If we implemented it carefully, it would improve results.", "Condicional", "formal", "Valora un escenario hipotético."),
                ("The bottom line is that we need evidence.", "Conclusión", "neutral", "Cierra el argumento con lo esencial."),
            ],
        ),
        "semantic2": (
            "Distingue el registro de cada valoración de la evidencia.",
            [
                ("The evidence suggests a positive impact.", "Formal", "formal", "Fórmula propia de un informe."),
                ("It looks like it could work.", "Neutral", "neutral", "Valoración prudente y sencilla."),
                ("It's worth a shot.", "Informal", "informal", "Propuesta coloquial de probar algo."),
            ],
        ),
        "audio1": (
            "Although the proposal is expensive, it could improve results; the bottom line is that we need evidence before deciding.",
            "What does the speaker conclude?",
            ["More evidence is needed before deciding", "The proposal is definitely too expensive", "The results are already proven", "The decision has been made"],
        ),
        "audio2": (
            "If we implemented the plan carefully, it might improve results; in other words, the design matters.",
            "What is the key point?",
            ["How the plan is designed matters", "The plan is impossible", "Results do not matter", "The plan is too cheap"],
        ),
        "listen": "Although the proposal is expensive it could improve results",
        "speak": "The bottom line is that we need more evidence.",
        "speak_es": "Lo esencial es que necesitamos más pruebas.",
        "free": (
            "Debate una propuesta educativa: reconoce una objeción, presenta evidencia y cierra con una conclusión.",
            40,
            [
                ["proposal", "evidence", "results"],
                ["although", "if", "could"],
                ["in other words", "bottom line", "decide"],
            ],
            "Although the proposal is expensive, it could improve results if we implement it carefully. "
            "The evidence from similar schools suggests a positive impact, but we still need local data. "
            "In other words, the bottom line is that we should run a small pilot before we decide.",
            [
                "¿Reconociste una objeción?",
                "¿Presentaste evidencia?",
                "¿Cerraste con una conclusión?",
            ],
        ),
        "cultural": (
            "the bottom line",
            "La conclusión o el dato decisivo de un debate.",
            "lo esencial",
        ),
    },
]


def exercise_id(index: int, suffix: int) -> str:
    return f"es-en-b2-p0-u1-l{index:02d}-e{suffix:02d}"


def semantic_pair(left: str, right: str, register: str, nuance: str) -> dict:
    return {"left": left, "right": right, "register": register, "nuance": nuance}


def build_exercises(index: int, spec: dict) -> list[dict]:
    grammar_one, grammar_two = spec["grammar"]
    sentence = " ".join(spec["order"][1])
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
                "explanation": f"La forma correcta aparece en la micro-clase: «{sentence}».",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has elegido la forma gramatical adecuada."},
        },
        {
            "type": "OrderWords",
            "id": exercise_id(index, 2),
            "tokens": rotated,
            "answerTokens": answer,
            "sourcePhrase": spec["order"][0],
            "feedbackOnError": {
                "type": "word_order",
                "explanation": f"El orden natural es «{sentence}».",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
        },
        {
            "type": "ClozeDragDrop",
            "id": exercise_id(index, 3),
            "passage": spec["cloze"][0],
            "blanks": [
                {"id": blank_id, "answer": blank_answer, "options": blank_options}
                for blank_id, blank_answer, blank_options in spec["cloze"][1]
            ],
            "feedbackOnError": {
                "type": "grammar",
                "explanation": "Cada hueco debe encajar con el tiempo verbal y el contexto.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Los huecos completan la frase con coherencia."},
        },
        {
            "type": "SemanticMatch",
            "id": exercise_id(index, 4),
            "instructions": spec["semantic1"][0],
            "pairs": [semantic_pair(*pair) for pair in spec["semantic1"][1]],
            "feedbackOnError": {
                "type": "vocabulary",
                "explanation": "Cada intervención cumple una función comunicativa distinta.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has identificado la función de cada intervención."},
        },
        {
            "type": "SemanticMatch",
            "id": exercise_id(index, 5),
            "instructions": spec["semantic2"][0],
            "pairs": [semantic_pair(*pair) for pair in spec["semantic2"][1]],
            "feedbackOnError": {
                "type": "register",
                "explanation": "El registro cambia según el contexto y la relación con el interlocutor.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has distinguido el registro de cada expresión."},
        },
        {
            "type": "AudioOnlyPrompt",
            "id": exercise_id(index, 6),
            "audioText": spec["audio1"][0],
            "question": spec["audio1"][1],
            "options": spec["audio1"][2],
            "correctIndex": 0,
            "allowSlowPlayback": True,
            "feedbackOnError": {
                "type": "comprehension",
                "explanation": "El audio matiza la información principal y añade una reserva.",
                "ruleRef": grammar_one[0],
            },
            "feedbackOnSuccess": {"explanation": "Has comprendido la postura del hablante."},
        },
        {
            "type": "AudioOnlyPrompt",
            "id": exercise_id(index, 7),
            "audioText": spec["audio2"][0],
            "question": spec["audio2"][1],
            "options": spec["audio2"][2],
            "correctIndex": 0,
            "allowSlowPlayback": False,
            "feedbackOnError": {
                "type": "comprehension",
                "explanation": "La segunda parte del audio reformula la idea principal.",
                "ruleRef": grammar_two[0],
            },
            "feedbackOnSuccess": {"explanation": "Has identificado la recomendación del audio."},
        },
        {
            "type": "ListenType",
            "id": exercise_id(index, 8),
            "transcription": spec["listen"],
            "audioText": spec["listen"],
            "feedbackOnError": {
                "type": "spelling",
                "explanation": f"Escribe exactamente «{spec['listen']}».",
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
            "type": "FreeSpeakingPrompt",
            "id": exercise_id(index, 10),
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
        "id": f"es-en-b2-p0-u1-l{index:02d}",
        "title": spec["title"],
        "cefrLevel": "B2",
        "scenario": spec["scenario"],
        "canDo": spec["can_do"],
        "grammarPoints": [
            {"id": grammar_one[0], "label": grammar_one[1], "description": grammar_one[2]},
            {"id": grammar_two[0], "label": grammar_two[1], "description": grammar_two[2]},
        ],
        "vocabFrequencyBand": "3000-5000",
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
                f"Usar vocabulario de {spec['topic']}.",
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


def self_check(lesson: dict) -> None:
    lesson_id = lesson["id"]
    assert len(lesson["exercises"]) == 10, f"{lesson_id}: se esperaban 10 ejercicios"
    assert len(lesson["grammarPoints"]) == 2, f"{lesson_id}: se esperaban 2 focos"
    for exercise in lesson["exercises"]:
        texts: list[str] = []
        if exercise["type"] == "ClozeDragDrop":
            texts.append(exercise["passage"].replace("{{b1}}", "x").replace("{{b2}}", "x").replace("{{b3}}", "x"))
        elif exercise["type"] in {"ListenType", "SpeakRepeat", "AudioOnlyPrompt"}:
            texts.append(exercise.get("transcription") or exercise.get("targetPhrase") or exercise["audioText"])
        elif exercise["type"] == "FreeSpeakingPrompt":
            texts.append(exercise["modelAnswer"])
        elif exercise["type"] == "SemanticMatch":
            texts.extend(pair["left"] for pair in exercise["pairs"])
        for text in texts:
            for length in sentence_lengths(text):
                assert length <= 30, f"{lesson_id}/{exercise['id']}: frase de {length} tokens"
    free = next(exercise for exercise in lesson["exercises"] if exercise["type"] == "FreeSpeakingPrompt")
    tokens = tokenize(free["modelAnswer"])
    assert len(tokens) >= free["minTokens"], f"{lesson_id}: modelo demasiado corto ({len(tokens)})"
    covered = 0
    for cluster in free["conceptClusters"]:
        if any(all(any(token == word or (len(word) >= 4 and edit_distance(token, word) <= 2) for token in tokens) for word in tokenize(term)) for term in cluster):
            covered += 1
    required = max(1, (len(free["conceptClusters"]) + 1) // 2)
    assert covered >= required, f"{lesson_id}: cobertura insuficiente ({covered}/{len(free['conceptClusters'])})"


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
        "title": "Piloto B2 · Argumentación y matices",
        "lessons": lessons,
    }
    unit_text = indent_block(json.dumps(unit, ensure_ascii=False, indent=2), 8)
    if MARKER not in course_text:
        raise SystemExit("No se encontró el cierre del nivel B2 en course.json")
    course_text = course_text.replace(MARKER, ",\n" + unit_text + MARKER, 1)
    COURSE.write_text(course_text, encoding="utf-8", newline="\n")
    print(f"Insertado {UNIT_ID} con {len(lessons)} lecciones y {len(lessons) * 10} ejercicios.")


if __name__ == "__main__":
    main()
