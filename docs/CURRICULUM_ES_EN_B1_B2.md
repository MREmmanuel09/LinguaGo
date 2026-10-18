# Currículo `es-en` B1–B2 — Matriz CEFR y contrato de autoría (F3.2)

> Especificación de las 40 lecciones nuevas de `content/content-es-en` para
> `schemaVersion: 2` (ADR-018). Es el contrato que guían F3.3 (B1) y F3.4 (B2):
> toda lección debe cumplir aquí sus metadatos, foco gramatical, escenario,
> can-do, banda léxica, modismo y reparto de ejercicios. La fuente formal de
> campos es [`docs/schemas/course-v2.json`](schemas/course-v2.json).

## 1. Estructura objetivo

| Nivel | `level.id` | Nombre | Unidades | Lecciones | Ejercicios |
|---|---|---|---|---|---|
| B1 | `es-en-lv-2` | B1 · Autonomía en contextos reales | 4 | 20 (5/unidad) | 180 (9/lección) |
| B2 | `es-en-lv-3` | B2 · Matices y argumentación | 4 | 20 (5/unidad) | 200 (10/lección) |

- El nivel A1/A2 existente (`es-en-lv-1`) no se toca: sus ids históricos están
  congelados por el test de regresión de F3.1 (I4).
- `locale` se mantiene en `en-US` para todo el curso.
- Los ids de lección siguen el patrón existente:
  `es-en-lv-2-un-<u>-ls-<l>` y `es-en-lv-3-un-<u>-ls-<l>`.
- Los ejercicios usan rangos consecutivos por lección:
  B1 `es-en-b1-e1`…`es-en-b1-e180`; B2 `es-en-b2-e1`…`es-en-b2-e200`.
  El rango exacto de cada lección figura en las matrices; no se reutiliza
  ningún id y `PackagedContentTest` lo verifica (F3.4).
- Prohibido en `es-en` v2: `KanaChoose`, `KanjiReading`, `ParticleFill`
  (variantes del curso de japonés).

## 2. Inventario gramatical referenciable

`feedbackOnError.ruleRef` y `FreeSpeakingPrompt.grammarRuleRefs` solo pueden
apuntar a un `grammarPoint.id` declarado en la misma lección. Este inventario
evita duplicados y fija la etiqueta visible.

### B1

| `id` | `label` | Uso |
|---|---|---|
| `gp-present-perfect-vs-past-simple` | Present perfect vs past simple | Experiencias y eventos terminados con marcadores temporales. |
| `gp-used-to` | Used to | Hábitos pasados que ya no ocurren. |
| `gp-first-conditional` | First conditional | Condición real con consecuencia futura. |
| `gp-second-conditional` | Second conditional | Hipótesis improbable y consejo. |
| `gp-relative-clauses` | Defining relative clauses | who/which/that para identificar personas y cosas. |
| `gp-passive-voice` | Passive voice | Enfoque en la acción o el resultado. |
| `gp-reported-speech` | Reported speech | Ajuste de tiempos y pronombres al contar lo dicho. |
| `gp-indirect-questions` | Indirect questions | Cortesía con Could you tell me…? |
| `gp-modals-obligation` | Modals of obligation | have to, should, ought to. |
| `gp-modals-deduction` | Modals of deduction | must, might, can't para deducir. |
| `gp-question-tags` | Question tags | Confirmar información con entonación. |
| `gp-future-forms` | Future forms | will, going to y presente continuo. |
| `gp-gerund-infinitive` | Gerund vs infinitive | Verbos que rigen -ing o to + infinitivo. |
| `gp-linkers-contrast` | Contrast linkers | although, however, despite. |
| `gp-linkers-purpose` | Purpose linkers | to, so that, in order to. |
| `gp-past-continuous-vs-past-simple` | Past continuous vs past simple | Acciones interrumpidas y contexto. |
| `gp-present-perfect-for-since` | Present perfect with for/since | Duración hasta el presente. |
| `gp-comparatives` | Comparatives and superlatives | Comparar opciones y servicios. |
| `gp-countable-uncountable` | Countable/uncountable quantifiers | some, any, much, many, a lot of. |
| `gp-prepositions-place-movement` | Prepositions of place and movement | Indicaciones y ubicación. |

### B2

| `id` | `label` | Uso |
|---|---|---|
| `gp-third-conditional` | Third conditional | Hipótesis pasadas no realizadas. |
| `gp-mixed-conditionals` | Mixed conditionals | Condición presente con consecuencia pasada o futura. |
| `gp-inversion-negative-adverbials` | Inversion with negative adverbials | Not only…, Under no circumstances…, Rarely… |
| `gp-passive-reporting` | Passive reporting structures | It is said that…, He is believed to… |
| `gp-cleft-sentences` | Cleft sentences | What matters is…, It was X that… |
| `gp-subjunctive-mood` | Subjunctive mood | I suggest that he be…, It is essential that… |
| `gp-hedging` | Hedging | I'm afraid, it seems, perhaps, would. |
| `gp-narrative-tenses` | Narrative tenses | Pasado simple, continuo y perfecto en relatos. |
| `gp-reported-speech-b2` | Advanced reported speech | Verbos de habla y desplazamiento de tiempos. |
| `gp-participle-clauses` | Participle clauses | Having said that…, Faced with… |
| `gp-nondefining-relative-clauses` | Non-defining relative clauses | Añadir información con who/which, entre comas. |
| `gp-modals-speculation-past` | Past modals of speculation | must have, might have, can't have. |
| `gp-ellipsis-substitution` | Ellipsis and substitution | so do I, neither did she, one/ones. |
| `gp-discourse-markers` | Discourse markers | However, that said, in other words, all in all. |
| `gp-fronting-emphasis` | Fronting for emphasis | That I can't accept; Such was the impact… |
| `gp-causative-have-get` | Causative have/get | have something done, get someone to do. |
| `gp-wish-if-only` | Wish / if only | Deseos y arrepentimientos. |
| `gp-register-shifts` | Register shifts | Formal/informal, atenuación y distancia. |
| `gp-nominalization` | Nominalization | Convertir acciones en sustantivos para un registro formal. |
| `gp-conditional-linkers` | Conditional linkers | unless, provided that, as long as, otherwise. |

## 3. Matriz B1 — `es-en-lv-2`

### Unidad 1 — `es-en-lv-2-un-1`: Trabajo y comunicación

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-2-un-1-ls-1` | La reunión que no empieza | Una reunión de equipo se retrasa y hay que aclarar qué se dijo y acordar el siguiente paso. | Puedo resumir lo acordado en una reunión, aclarar un malentendido y confirmar acciones con cortesía. | `gp-reported-speech`, `gp-indirect-questions` | 2000-3000 | `touch base` — retomar contacto breve para alinear; frecuente en oficinas estadounidenses | agenda, minutes, reschedule, follow up, clarify, action item, deadline, stakeholder | `es-en-b1-e1`–`e9` |
| `es-en-lv-2-un-1-ls-2` | Correos que no se malinterpretan | Un correo con un plazo ajustado puede sonar brusco; hay que reescribirlo sin perder firmeza. | Puedo redactar un correo profesional claro, justificar un plazo y suavizar una petición. | `gp-passive-voice`, `gp-linkers-contrast` | 2000-3000 | `on the same page` — compartir la misma interpretación antes de actuar | attach, recipient, subject line, draft, proofread, respond, acknowledge, loop in | `es-en-b1-e10`–`e18` |
| `es-en-lv-2-un-1-ls-3` | Pequeñas conversaciones de oficina | Pausa de café con una persona recién incorporada al equipo. | Puedo iniciar y mantener small talk, preguntar por experiencia previa y cerrar la conversación con naturalidad. | `gp-question-tags`, `gp-present-perfect-vs-past-simple` | 1000-2000 | `break the ice` — romper la tensión inicial en una conversación nueva | colleague, break room, small talk, catch up, workload, remote, commute, onboarding | `es-en-b1-e19`–`e27` |
| `es-en-lv-2-un-1-ls-4` | Una llamada difícil | Un cliente enfadado llama por un error de facturación y exige una solución inmediata. | Puedo gestionar una queja telefónica, disculparme sin excusas y proponer una salida concreta. | `gp-modals-obligation`, `gp-modals-deduction` | 2000-3000 | `the ball is in your court` — la siguiente decisión corresponde a la otra persona | complaint, apology, resolve, escalate, refund, policy, hold on, put through | `es-en-b1-e28`–`e36` |
| `es-en-lv-2-un-1-ls-5` | El proyecto se retrasa | El equipo no llegará a la fecha comprometida y debe explicarlo con un plan alternativo. | Puedo explicar un retraso, asumir responsabilidad y presentar un plan con hitos y condiciones. | `gp-future-forms`, `gp-first-conditional` | 2000-3000 | `cut it fine` — dejar un margen demasiado ajustado | delay, milestone, estimate, allocate, contingency, prioritize, deliverable, revise | `es-en-b1-e37`–`e45` |

### Unidad 2 — `es-en-lv-2-un-2`: Viajes y trámites

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-2-un-2-ls-1` | En el aeropuerto | Un vuelo se cancela y la aerolínea ofrece una ruta con escala; hay que reclamar y reorganizar. | Puedo explicar un problema de viaje, pedir una alternativa y entender condiciones de reembolso. | `gp-present-perfect-for-since`, `gp-modals-obligation` | 2000-3000 | `check in` — registrarse en un vuelo u hotel; también significa reportarse con alguien | boarding pass, gate, layover, cancelled, rebook, compensation, baggage claim, customs | `es-en-b1-e46`–`e54` |
| `es-en-lv-2-un-2-ls-2` | Reservar y cambiar un hotel | El hotel confirma una habitación equivocada y hay que negociar un cambio sin pagar de más. | Puedo reservar, modificar y confirmar alojamiento, comparando opciones y condiciones. | `gp-second-conditional`, `gp-indirect-questions` | 2000-3000 | `up in the air` — sin decidir o sin confirmar todavía | reservation, upgrade, availability, confirm, cancel, deposit, check-in, suite | `es-en-b1-e55`–`e63` |
| `es-en-lv-2-un-2-ls-3` | Perderse en la ciudad | El GPS falla y hay que pedir indicaciones para llegar a una dirección concreta. | Puedo pedir y dar indicaciones, describir un recorrido y confirmar que entendí. | `gp-relative-clauses`, `gp-prepositions-place-movement` | 1000-2000 | `off the beaten track` — fuera de las rutas habituales o turísticas | intersection, roundabout, landmark, pedestrian, block, subway, fare, directions | `es-en-b1-e64`–`e72` |
| `es-en-lv-2-un-2-ls-4` | Una queja con solución | Un pedido llegó dañado y la tienda propone un cambio; hay que decidir qué exigir. | Puedo describir un defecto, reclamar una solución y aceptar o rechazar una propuesta. | `gp-past-continuous-vs-past-simple`, `gp-linkers-purpose` | 2000-3000 | `the last straw` — el problema final que agota la paciencia | defect, receipt, warranty, replacement, exchange, faulty, shipping, return label | `es-en-b1-e73`–`e81` |
| `es-en-lv-2-un-2-ls-5` | Trámites y papeleo | Renovar un documento exige formularios, fotocopias y una cita; falta un requisito. | Puedo explicar un trámite, preguntar requisitos y resolver una incidencia administrativa. | `gp-passive-voice`, `gp-countable-uncountable` | 2000-3000 | `red tape` — burocracia excesiva que retrasa un proceso | form, application, requirement, renewal, paperwork, signature, photocopy, submit | `es-en-b1-e82`–`e90` |

### Unidad 3 — `es-en-lv-2-un-3`: Salud y bienestar

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-2-un-3-ls-1` | En la consulta | En una consulta médica hay que describir síntomas y responder preguntas de seguimiento. | Puedo describir síntomas, indicar cuándo empezaron y entender instrucciones de tratamiento. | `gp-present-perfect-vs-past-simple`, `gp-modals-deduction` | 2000-3000 | `under the weather` — sentirse mal de salud, sin especificar gravedad | symptom, prescription, appointment, dizzy, rash, fever, pharmacy, dosage | `es-en-b1-e91`–`e99` |
| `es-en-lv-2-un-3-ls-2` | Consejos de bienestar | Una amiga no duerme por el estrés y pide consejo sin sentirse juzgada. | Puedo dar consejos con suavidad, sugerir cambios concretos y valorar opciones. | `gp-modals-obligation`, `gp-first-conditional` | 1000-2000 | `burn the candle at both ends` — descansar poco por hacer demasiadas cosas | insomnia, screen time, caffeine, routine, balanced diet, stress, workout, hydration | `es-en-b1-e100`–`e108` |
| `es-en-lv-2-un-3-ls-3` | Cambiar hábitos | Un plan de ejercicio fracasó el mes pasado y hay que ajustarlo a la rutina real. | Puedo hablar de hábitos pasados y presentes, explicar un cambio y proponer un plan sostenible. | `gp-used-to`, `gp-gerund-infinitive` | 1000-2000 | `get back on track` — retomar un plan después de una interrupción | habit, motivation, progress, consistent, goal, track, strength, stretch | `es-en-b1-e109`–`e117` |
| `es-en-lv-2-un-3-ls-4` | Una urgencia | Alguien se ha lesionado y hay que llamar a urgencias y describir la situación con calma. | Puedo pedir ayuda urgente, describir qué pasó y responder preguntas de forma breve y clara. | `gp-past-continuous-vs-past-simple`, `gp-indirect-questions` | 2000-3000 | `in good hands` — estar atendido por alguien competente y confiable | emergency, ambulance, injury, breathe, conscious, bleeding, paramedic, urgent | `es-en-b1-e118`–`e126` |
| `es-en-lv-2-un-3-ls-5` | Comparar opciones de salud | Dos seguros médicos ofrecen coberturas distintas y hay que elegir con presupuesto limitado. | Puedo comparar servicios, preguntar por coberturas y justificar una elección. | `gp-comparatives`, `gp-linkers-contrast` | 2000-3000 | `cost an arm and a leg` — ser muy caro; se usa con tono informal | insurance, coverage, premium, deductible, clinic, provider, benefit, compare | `es-en-b1-e127`–`e135` |

### Unidad 4 — `es-en-lv-2-un-4`: Cultura y ocio

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-2-un-4-ls-1` | Planear el fin de semana | Dos amigos tienen planes incompatibles y deben llegar a una propuesta común. | Puedo proponer planes, mostrar acuerdo o desacuerdo y cerrar una cita concreta. | `gp-future-forms`, `gp-question-tags` | 1000-2000 | `have a blast` — disfrutar mucho de una actividad | plans, hang out, cinema, available, prefer, join, suggestion, flexible | `es-en-b1-e136`–`e144` |
| `es-en-lv-2-un-4-ls-2` | Recomendar una serie | Alguien pide una recomendación y hay que describir la trama sin destriparla. | Puedo recomendar una serie, describir personajes y argumentar por qué vale la pena. | `gp-relative-clauses`, `gp-present-perfect-vs-past-simple` | 2000-3000 | `a must-see` — algo que merece mucho la pena ver | plot, character, episode, season, gripping, predictable, cast, cliffhanger | `es-en-b1-e145`–`e153` |
| `es-en-lv-2-un-4-ls-3` | Un concierto inolvidable | Contar a un grupo de amigos cómo fue un concierto al que no pudieron ir. | Puedo narrar una experiencia pasada, describir el ambiente y expresar una reacción. | `gp-past-continuous-vs-past-simple`, `gp-used-to` | 2000-3000 | `once in a blue moon` — algo que ocurre muy rara vez | concert, audience, encore, perform, venue, atmosphere, memorable, crowd | `es-en-b1-e154`–`e162` |
| `es-en-lv-2-un-4-ls-4` | Cultura y choque de costumbres | Una costumbre local se interpretó mal y hay que aclararlo sin estereotipos. | Puedo describir diferencias culturales, preguntar con respeto y corregir un malentendido. | `gp-comparatives`, `gp-modals-deduction` | 2000-3000 | `culture shock` — desorientación al enfrentarse a costumbres distintas | custom, gesture, polite, taboo, misunderstand, adapt, tradition, greeting | `es-en-b1-e163`–`e171` |
| `es-en-lv-2-un-4-ls-5` | Reseñar un restaurante | Hay que escribir una reseña equilibrada de un restaurante recién abierto. | Puedo describir una experiencia gastronómica, valorar servicio y precio, y recomendar o advertir. | `gp-passive-voice`, `gp-linkers-purpose` | 2000-3000 | `hole in the wall` — local pequeño y sencillo, a menudo con comida excelente | review, rating, service, portion, cozy, overpriced, flavorful, recommend | `es-en-b1-e172`–`e180` |

## 4. Matriz B2 — `es-en-lv-3`

### Unidad 1 — `es-en-lv-3-un-1`: Negociación y decisiones

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-3-un-1-ls-1` | Plazos y concesiones | Reunión con un cliente para renegociar una entrega sin romper la relación comercial. | Puedo rechazar una petición con cortesía, justificar concesiones y proponer una alternativa condicionada. | `gp-mixed-conditionals`, `gp-hedging` | 3000-5000 | `play it by ear` — decidir sobre la marcha sin comprometerse; suaviza la falta de plan | concession, timeline, leverage, flexibility, counteroffer, scope, phase, commitment | `es-en-b2-e1`–`e10` |
| `es-en-lv-3-un-1-ls-2` | Presupuesto bajo presión | El cliente exige una rebaja que afecta el margen; hay que explorar escenarios sin ceder de inmediato. | Puedo cuantificar una propuesta, plantear escenarios hipotéticos y defender un límite comercial. | `gp-third-conditional`, `gp-hedging` | 3000-5000 | `ballpark figure` — cifra aproximada suficiente para orientar una conversación | budget, forecast, overhead, negotiation, discount, margin, quote, cash flow | `es-en-b2-e11`–`e20` |
| `es-en-lv-3-un-1-ls-3` | Quién decide qué | Dos áreas no coinciden sobre quién aprueba un gasto y la decisión se bloquea. | Puedo atribuir responsabilidades con precisión, aclarar procesos y destrabar una decisión. | `gp-passive-reporting`, `gp-hedging` | 3000-5000 | `in the loop` — mantener informada a una persona clave de un proceso | authority, delegate, consensus, approval, decision-making, ownership, autonomy, sign off | `es-en-b2-e21`–`e30` |
| `es-en-lv-3-un-1-ls-4` | Concesiones sin perder terreno | Una negociación avanza y cada parte debe ceder algo sin aparecer como débil. | Puedo plantear un intercambio equilibrado, condicionar una concesión y proteger mis prioridades. | `gp-wish-if-only`, `gp-mixed-conditionals` | 3000-5000 | `win-win` — acuerdo que ambas partes consideran beneficioso | trade-off, condition, alternative, mutual, reluctant, compromise, guarantee, renegotiate | `es-en-b2-e31`–`e40` |
| `es-en-lv-3-un-1-ls-5` | Cerrar el trato | La negociación llega al tramo final y hay que confirmar términos y próximos pasos por escrito. | Puedo resumir un acuerdo, precisar condiciones y confirmar compromisos con registro formal. | `gp-inversion-negative-adverbials`, `gp-cleft-sentences` | 3000-5000 | `cut to the chase` — ir directo al punto importante sin rodeos | agreement, clause, finalize, terms, handshake, close, contract, binding | `es-en-b2-e41`–`e50` |

### Unidad 2 — `es-en-lv-3-un-2`: Opinión y debate

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-3-un-2-ls-1` | Un debate ético | Un equipo discute si aceptar un contrato dudoso; hay que argumentar sin atacar a nadie. | Puedo presentar una postura ética, reconocer matices y responder a un contraargumento. | `gp-cleft-sentences`, `gp-passive-reporting` | 3000-5000 | `play devil's advocate` — defender una postura contraria para probar un argumento | ethics, dilemma, argument, evidence, assumption, consequence, principle, justify | `es-en-b2-e51`–`e60` |
| `es-en-lv-3-un-2-ls-2` | Matizar una crítica | Hay que señalar un fallo en el trabajo de un colega sin desmotivarlo. | Puedo formular una crítica constructiva, atenuar el tono y proponer una mejora concreta. | `gp-hedging`, `gp-register-shifts` | 3000-5000 | `beat around the bush` — evitar decir algo incómodo de forma indirecta | feedback, nuance, criticize, tone, wording, defensive, constructive, reframe | `es-en-b2-e61`–`e70` |
| `es-en-lv-3-un-2-ls-3` | Ironía y registro | Un comentario irónico se interpretó literalmente en una reunión formal. | Puedo reconocer ironía, ajustar el registro y explicar un malentendido pragmático. | `gp-register-shifts`, `gp-discourse-markers` | 3000-5000 | `read the room` — interpretar el ambiente antes de hablar | irony, sarcasm, understatement, context, implication, register, literal, exaggeration | `es-en-b2-e71`–`e80` |
| `es-en-lv-3-un-2-ls-4` | Rebatir con evidencia | Alguien defiende una afirmación con datos incompletos y hay que responder con rigor. | Puedo evaluar una fuente, señalar sesgos y rebatir con datos sin sonar dogmático. | `gp-passive-reporting`, `gp-inversion-negative-adverbials` | 3000-5000 | `take it with a grain of salt` — recibir una información con escepticismo razonable | claim, source, bias, rebuttal, statistic, credibility, counterargument, misleading | `es-en-b2-e81`–`e90` |
| `es-en-lv-3-un-2-ls-5` | Llegar a un acuerdo | Dos posturas enfrentadas deben converger en una propuesta común para presentar al equipo. | Puedo mediar entre posturas, reformular concesiones y redactar una conclusión compartida. | `gp-subjunctive-mood`, `gp-hedging` | 3000-5000 | `meet halfway` — ceder parcialmente para alcanzar un acuerdo | common ground, proposal, objection, settle, outcome, facilitate, mediate, respect | `es-en-b2-e91`–`e100` |

### Unidad 3 — `es-en-lv-3-un-3`: Narrativa y matices

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-3-un-3-ls-1` | El día que todo cambió | Contar un giro inesperado en una carrera profesional y lo que se aprendió después. | Puedo narrar un cambio vital con detalle, ordenar los hechos y reflexionar sobre sus efectos. | `gp-narrative-tenses`, `gp-mixed-conditionals` | 3000-5000 | `out of the blue` — de forma totalmente inesperada | turning point, coincidence, flashback, sudden, realize, regret, sequence, hindsight | `es-en-b2-e101`–`e110` |
| `es-en-lv-3-un-3-ls-2` | Una decisión que pesa | Hay que elegir entre dos ofertas laborales con costes personales distintos. | Puedo sopesar opciones, expresar arrepentimiento hipotético y justificar una decisión difícil. | `gp-third-conditional`, `gp-wish-if-only` | 3000-5000 | `bite the bullet` — afrontar una decisión difícil y desagradable | decision, hesitate, risk, second thoughts, commit, sacrifice, weigh up, doubt | `es-en-b2-e111`–`e120` |
| `es-en-lv-3-un-3-ls-3` | Contar un malentendido | Un mensaje ambiguo provocó una discusión; hay que reconstruir qué dijo cada parte. | Puedo reconstruir un diálogo, atribuir palabras con precisión y aclarar una interpretación errónea. | `gp-reported-speech-b2`, `gp-narrative-tenses` | 3000-5000 | `get the wrong end of the stick` — interpretar algo al revés | misunderstanding, assume, misinterpret, version, blame, apologize, clear up, account | `es-en-b2-e121`–`e130` |
| `es-en-lv-3-un-3-ls-4` | Humor y sarcasmo | En una sobremesa alguien bromea y otra persona se lo toma en serio. | Puedo distinguir humor de ofensa, aclarar una broma y adaptar el tono a la audiencia. | `gp-register-shifts`, `gp-ellipsis-substitution` | 3000-5000 | `pull someone's leg` — gastar una broma cariñosa, no engañar de verdad | joke, tease, offend, playful, deadpan, banter, take seriously, self-deprecating | `es-en-b2-e131`–`e140` |
| `es-en-lv-3-un-3-ls-5` | El hubiera no existe | Alguien se lamenta por una oportunidad perdida y hay que ayudarle a cerrar el tema. | Puedo consolar con empatía, reformular un arrepentimiento y proponer una perspectiva futura. | `gp-mixed-conditionals`, `gp-participle-clauses` | 3000-5000 | `no use crying over spilt milk` — lamentarse no cambia lo ocurrido | look back, accept, move on, lesson, forgive, closure, perspective, resilience | `es-en-b2-e141`–`e150` |

### Unidad 4 — `es-en-lv-3-un-4`: Sociedad y medios

| Lección | Título | Escenario | CanDo | Gramática | Banda | Modismo (`culturalNotes`) | Léxico foco (FSRS) | Ejercicios |
|---|---|---|---|---|---|---|---|---|
| `es-en-lv-3-un-4-ls-1` | Leer entre líneas | Un titular sugiere algo que el artículo no afirma; hay que detectar la intención editorial. | Puedo analizar un titular, distinguir hecho de interpretación y explicar una omisión. | `gp-passive-reporting`, `gp-hedging` | 3000-5000 | `read between the lines` — inferir lo que no se dice explícitamente | headline, coverage, outlet, framing, omission, insinuate, spin, subtext | `es-en-b2-e151`–`e160` |
| `es-en-lv-3-un-4-ls-2` | Noticias que manipulan | Una noticia viral mezcla datos reales con exageraciones; hay que verificar antes de compartir. | Puedo evaluar la fiabilidad de una noticia, describir técnicas de manipulación y argumentar con fuentes. | `gp-passive-reporting`, `gp-nominalization` | 3000-5000 | `sugarcoat` — presentar algo negativo de forma más agradable de lo que es | manipulate, narrative, clickbait, sensationalism, verify, fact-check, distort, readership | `es-en-b2-e161`–`e170` |
| `es-en-lv-3-un-4-ls-3` | El elefante en la sala | En una reunión familiar se evita un tema incómodo que todos conocen. | Puedo nombrar un tema incómodo con tacto, describir tensiones y proponer hablarlo. | `gp-subjunctive-mood`, `gp-cleft-sentences` | 3000-5000 | `the elephant in the room` — problema evidente que nadie menciona | privacy, surveillance, consent, transparency, data, expose, uncomfortable, confront | `es-en-b2-e171`–`e180` |
| `es-en-lv-3-un-4-ls-4` | Debate sobre privacidad | Un panel discute límites de la vigilancia digital; hay que defender derechos y riesgos. | Puedo debatir sobre privacidad, matizar riesgos y proponer salvaguardas con registro formal. | `gp-inversion-negative-adverbials`, `gp-hedging` | 3000-5000 | `slippery slope` — objeción de que un paso llevará inevitablemente a un extremo peor | privacy, regulation, breach, encryption, track, anonymity, debate, protection | `es-en-b2-e181`–`e190` |
| `es-en-lv-3-un-4-ls-5` | Propuestas para el futuro | Un foro ciudadano pide propuestas concretas sobre tecnología y convivencia. | Puedo formular propuestas, anticipar consecuencias y cerrar con una llamada a la acción. | `gp-third-conditional`, `gp-modals-speculation-past` | 3000-5000 | `game changer` — innovación que transforma las reglas de un ámbito | prediction, trend, innovation, society, impact, framework, sustainable, equality | `es-en-b2-e191`–`e200` |

## 5. Contrato de autoría por lección (F3.3 y F3.4)

### B1 (todas las lecciones)

1. **9 ejercicios** con este reparto obligatorio:
   1. `MultipleChoice` (gramática o vocabulario)
   2. `Translate`
   3. `OrderWords`
   4. `MatchPairs`
   5. `ListenChoose`
   6. `ListenType`
   7. `SpeakRepeat`
   8. `ClozeDragDrop` o `SemanticMatch`
   9. `FreeSpeakingPrompt`
2. `feedbackOnError` en **todos** los ejercicios, con `type` coherente y
   explicación específica del distractor o del error esperado. Si se usa
   `ruleRef`, debe existir en `grammarPoints[]` de la misma lección.
3. `FreeSpeakingPrompt` con `minTokens` 18–25, 3 `conceptClusters`, los dos
   `grammarPoints` en `grammarRuleRefs` cuando sean productivos, `modelAnswer`
   de 30–45 palabras y 3 criterios de autoevaluación.
4. Un `culturalNotes[]` con el modismo de la matriz: `idiom`, `note` (uso y
   registro, no solo traducción) y `translation`.
5. Escenario y can-do exactamente como figuran en la matriz (se puede pulir
   redacción sin cambiar el foco).

### B2 (todas las lecciones)

1. **10 ejercicios** con este reparto obligatorio (predominio de mecánicas v2):
   1. `ClozeDragDrop` (2 huecos)
   2. `ClozeDragDrop` (2–3 huecos)
   3. `SemanticMatch` (3 pares con `register` y `nuance`)
   4. `SemanticMatch` (3 pares con matiz irónico o de registro)
   5. `AudioOnlyPrompt` (`allowSlowPlayback: true`)
   6. `AudioOnlyPrompt` (`allowSlowPlayback: false`, audio más largo)
   7. `MultipleChoice` o `Translate` (foco gramatical)
   8. `OrderWords` o `ListenType` (precisión)
   9. `SpeakRepeat` (frase del modismo o del cierre)
   10. `FreeSpeakingPrompt`
2. `feedbackOnError` obligatorio en los 10 (schema v2 para B1/B2), con
   `ruleRef` a uno de los `grammarPoints[]` de la lección.
3. `FreeSpeakingPrompt` con `minTokens` 35–45, 3–4 `conceptClusters`,
   `timeLimitSeconds` 60–90, `minWpm` 70 y `maxWpm` 160, `modelAnswer` de
   45–70 palabras y 3 criterios de autoevaluación.
4. Un `culturalNotes[]` con el modismo de la matriz y su nota de uso/registro.
5. `vocabFrequencyBand: "3000-5000"` en todas las lecciones B2.

### Reglas transversales

- **FSRS sin colisiones:** el léxico foco de cada lección es la lista base de
  `word_progress`; no se repite un lema entre lecciones. Los plurales,
  conjugaciones y variantes del mismo lema cuentan como colisión.
- **Ids históricos intocables:** no se renombra nada de `es-en-lv-1` ni se
  reutilizan ids reservados en las matrices.
- **UTF-8 sin BOM** y sin claves extra (`additionalProperties: false`).
- **Validación:** `./gradlew validateContent` y luego
  `./gradlew ktlintCheck detekt validateContent test`; `PackagedContentTest`
  debe verificar unicidad global de ids y no-colisión léxica (F3.4).
- **audioText** es texto para TTS, nunca una pista visible en
  `AudioOnlyPrompt`.
- **Sin LLM** en `FreeSpeakingPrompt` (ADR-019): solo heurística determinista
  y autoevaluación.

## 6. Criterios de aceptación de F3.3/F3.4

- [ ] 20 lecciones B1 integradas con 9 ejercicios cada una (180) y los ids de
      los rangos de la matriz.
- [ ] 20 lecciones B2 integradas con 10 ejercicios cada una (200) y predominio
      de `ClozeDragDrop`, `SemanticMatch`, `AudioOnlyPrompt` y
      `FreeSpeakingPrompt`.
- [ ] Cada lección B1/B2 con `scenario`, `canDo`, `grammarPoints[]`,
      `vocabFrequencyBand`, `culturalNotes[]` y `feedbackOnError` en todos los
      ejercicios.
- [ ] `validateContent` en verde para `es-en` v2 y sin regresiones en los otros
      tres cursos (v1).
- [ ] Test de unicidad global de `lesson.id`/`exercise.id` y de no-colisión de
      vocabulario FSRS.
