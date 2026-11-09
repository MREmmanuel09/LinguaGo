# Plan de expansión progresiva C1–C2 (LinguaGo 2.1.0 → 2.5.0)

> Propuesta elaborada sobre el contenido de **2.0.0** (249 lecciones / 2.167
> ejercicios). Define cómo crecer **los cuatro cursos** manteniendo y escalando
> la dificultad de forma progresiva, con puerta de calidad automatizada en cada
> lote. **[Actualización 2026-11-08]** ADR-042 fue **aprobado** y se incorpora
> el hotfix de voz 2.0.1 (**ADR-043**) como prerrequisito: la evaluación oral
> tolerante beneficia directamente la producción de C1/C2 (sección 14).

## 0. Historial de revisiones

| Fecha | Versión | Cambio |
|---|---|---|
| 2026-11-07 | 1.0 | Plan inicial propuesto (borrador ADR-042). |
| 2026-11-08 | 1.1 | ADR-042 aprobado; el 2.0.1 (hotfix de voz, ADR-043) se integra como paso previo; referencias de invariantes corregidas. |

## 1. Objetivo y alcance

- **Añadir los niveles C1 y C2 a los cuatro cursos** (`es-en`, `es-fr`,
  `es-pt`, `es-ja`), **42 lecciones nuevas por curso** (24 C1 + 18 C2).
- Dificultad **monótona y verificada por máquinas y por tests**: la rampa
  CEFR actual (`A1→A2→B1→B2`) se extiende a `A1→A2→B1→B2→C1→C2` sin retrocesos
  y con métricas de carga (longitud, densidad, producción) que suben en cada
  nivel.
- Todo se sirve **offline dentro del APK** (I1, sin backend, sin red en
  rutas de aprendizaje) y como **contenido CC BY-SA 4.0 original** (I6).
- **Fuera de alcance:** nuevos tipos de ejercicio, nueva identidad visual y
  cambios de arquitectura de módulos. Solo se usan los tipos de ejercicio
  existentes (14).

## 2. Estado de partida (2.0.0)

| Curso | Lecciones | Ejercicios | Rampa |
|---|---:|---:|---|
| `es-en` | 69 | 589 | A1→B2 (B2: 22 lecciones/220 ejercicios) |
| `es-fr` | 60 | 526 | A1 10 · A2 8 · B1 20 · B2 22 |
| `es-pt` | 60 | 526 | A1 10 · A2 8 · B1 20 · B2 22 |
| `es-ja` | 60 | 526 | A1 10 · A2 8 · B1 20 · B2 22 |
| **Total** | **249** | **2.167** | |

Rampa por lección hoy (verificada por `ContentRampTest`):

| Nivel | Ejercicios/lección | Tope de tokens/frase | Focos gramaticales | FreeSpeaking |
|---|---:|---:|---:|---:|
| A1 | 7 | 8 | 0 | 0 |
| A2 | 7 | 12 | ≤ 2 | 0 |
| B1 | 9 | 24 | 2 | 1 |
| B2 | 10 | 30 | 2 | 1 |

## 3. Principios de escalado progresivo

1. **La rampa no retrocede.** C1 y C2 solo se añaden al final del curso; los
   tests de rampa (`ContentRampTest`) se extienden para vigilar el rango
   `B2→C1→C2`.
2. **Curriculum en espiral.** Cada nivel nuevo abre con una unidad de
   **consolidación** que recicla explícitamente lo aprendido (un 30–40 % del
   vocabulario y la gramática de las últimas unidades del nivel anterior). El
   contenido difícil se apoya en lo dominado, no sustituye lo no revisado.
3. **La carga sube en cada nivel.** Más ejercicios por lección, frases más
   largas y densas, más audio, más producción libre. La dificultad viene de la
   **composición**, no de añadir mecánicas nuevas.
4. **El apoyo en L1 se apaga gradualmente.** El % de ejercicios que necesitan
   traducción del español baja con el nivel (objetivo declarado por nivel,
   sección 4). En C2 el material es *target-only* y las instrucciones siguen
   en español (idioma de la app).
5. **Producción primero en C1/C2.** A partir de C1 toda lección lleva
   producción oral libre y en C2 lleva dos (monólogo + debate/rol); el prompt
   `FreeSpeakingPrompt` exige menciones declaradas (`grammarRuleRefs`). Las
   notas `feedbackOnError` dejan de corregir morfología y pasan a corregir
   **registro, matiz y adecuación** (`type: register`).
6. **Reciclaje asistido por FSRS, no por repetición mecánica.** Las unidades
   de consolidación reutilizan contenido para calibrar lo ya aprendido; no se
   duplican lecciones existentes ni se renumeran ids históricos (I3: el
   progreso guardado queda intacto).
7. **Un piloto antes de la reproducción masiva.** Cada nivel entra primero por
   `es-en` (curso insignia), se valida en emulador con humo manual, y solo
   después se replica en `es-fr`, `es-pt` y `es-ja` con la plantilla
   semánticamente adaptada.
8. **Cada lote se valida antes de avanzar.** Nada de acumular "deudas" de
   contenido: cada lote pasa el gate completo (sección 9).

## 4. Qué escalamos exactamente (parámetros por nivel)

| Parámetro | B2 (hoy) | **C1** | **C2** |
|---|---:|---:|---:|
| Ejercicios por lección | 10 | **11** | **12** |
| Tope de tokens por frase | 30 | **38** | **48** |
| Focos gramaticales declarados | 2 | **2** | **2** |
| `FreeSpeakingPrompt` por lección | 1 | **1** (monólogo 60–90 palabras modelo) | **2** (monólogo 90–130 + debate/rol) |
| `AudioOnlyPrompt` (densidad) | vacío–corto | **60–90 palabras** | **90–130 palabras** |
| Banda léxica (`vocabFrequencyBand`) | `5000+` | **`8000-12000`** (nueva) | **`12000+`** (nueva) |
| Guía pedagógica obligatoria (`scenario`/`canDo`/`culturalNotes`/`feedbackOnError`) | sí | **sí** | **sí** |
| Apoyo L1 objetivo | ~35 % | **~55 %** | **~70 %** |
| Longitud del glosario por lección | 6–10 términos | **8–12** | **10–14** |

El "apoyo L1" se define como fracción de ejercicios cuya premisa necesita
español (traducciones `Translate`, enunciados MCQ en español). Es una métrica
de construcción editorial, **no una regla de bloqueo**: se vigila en revisión
y en el informe de `validateContent` (informativo), no rompe el build.

Tipos permitidos:

- **C1** = conjunto de B2 (`MultipleChoice`, `OrderWords`, `ListenType`,
  `SpeakRepeat`, `ClozeDragDrop`, `SemanticMatch`, `AudioOnlyPrompt`,
  `FreeSpeakingPrompt`) **+** `ListenChoose`, `MatchPairs`, `Translate`
  (para matices de registro y predicados evaluativos).
- **C2** = mismo conjunto que C1.

`ParticleFill`, `KanaChoose`, `KanjiReading` no se usan en C1/C2 salvo en
`es-ja` donde pueden reaparecer como soporte de vocabulario del nivel, con
justificación en la micro-clase.

## 5. Diseño C1 (24 lecciones × 11 ejercicios, 264 por curso)

Estructura fija de unidades, idéntica en los cuatro cursos (títulos y escenarios
traducidos/adaptados por idioma):

| Unidad | Lecciones | Cometido |
|---|---:|---|
| U1 · Consolidación B2→C1 | 4 | Espiral: recicla las últimas unidades B2 (pasado narrativo, reformular, negociar, informar) y sube el listón hacia C1. |
| U2 · Argumentación y matiz | 4 | Concesión, atenuación, ironía, énfasis, disculpa matizada, polémica constructiva. |
| U3 · Trámites y autoridades | 4 | Litigio pequeño, escrituras, tarifas públicas, asuntos médicos complejos. |
| U4 · Trabajo y liderazgo | 4 | Dirección de equipos, feedback socrático, presentación de balance anual, negociación colectiva. |
| U5 · Proyecto integrador | 4 | Proyecto con alcance temporal: planificación, presupuesto, informe de avance, cierre. |
| U6 · Evaluación global C1 | 4 | Comprensión densa, reescritura con propósito, monólogo argumentativo y debate. |

Focos gramaticales representativos por idioma (2 por lección; el inventario
completo por unidad se documenta en `docs/CONTENT_FORMAT.md` v2 en el lote
correspondiente):

| Curso | Ejemplos de foco C1 |
|---|---|
| `es-en` | *inversion* (`little did I…`), *cleft sentences*, *hedging* y *epistemic modality*, nominalización y *participle clauses*, *discourse markers* (`admittedly`, `mind you`). |
| `es-fr` | indicativo vs *subjonctif* en régimen complejo, *concordance des temps*, *discours rapporté* al pasado, *mise en relief*, registro soutenu frente a courant y subordinadas concesivas. |
| `es-pt` | **infinitivo pessoal** frente a subjuntivo, futuro composto y perífrasis verbales, *se* apassivador frente a indeterminador, concordância verbal en registro formal y colocações del registro académico y jurídico. |
| `es-ja` | 敬語の一貫した使用 (尊敬語・謙譲語), 複合助詞と構文 (「〜ことになる」「〜わけがない」), **である調** y registro escrito, 複合動詞 y derivación apreciativa/peyorativa. |

## 6. Diseño C2 (18 lecciones × 12 ejercicios, 216 por curso)

| Unidad | Lecciones | Cometido |
|---|---:|---|
| U1 · Registros y matices | 4 | Ironía fina, hipérbole/atenuación, eufemismos, alusiones literarias, humor lateral. |
| U2 · Dominios especializados | 4 | Derecho/público, técnica-científico, humanidades, economía con jerga propia. |
| U3 · Lengua culta | 4 | Retórica clásica, argumentación y contraargumento, sintaxis culta en uso real, producción escrita evaluada. |
| U4 · Síntesis y evaluación C2 | 6 | 2 unidades de especialización avanzada + evaluación: examen oral (monólogo de 2 min), comprensión de audio de 100+ palabras sin transcripción y debate final. |

Focos gramaticales C2 por idioma:

| Curso | Ejemplos de foco C2 |
|---|---|
| `es-en` | registro literario, *understatement* e ironía, matiz aspectual en predicados del discurso, doble sentido y metáforas culturales, collocations fijas. |
| `es-fr` | estilo literario, *inversion* y elipsis, gradaciones del discurso indirecto, vocabulario culto (*registre soutenu*) y juegos de registros literarios. |
| `es-pt` | colocações y metáforas fijas, perifrasis temporales avanzadas, registo literário y jurídico, nominalización y transiciones del registro. |
| `es-ja` | 敬語の使い分け (尊敬語・謙譲語と二重敬語の回避), 文体の切り替え (である調/ですます調), 和語 vs 漢語の選択, 四字熟語 y proverbios, 話し言葉 vs 書き言葉. |

## 7. Infraestructura necesaria (código, en orden de ejecución)

Todo esto es **código, no contenido**: se hace en el release 2.1.0 antes de
meter C1/C2 al máximo; debe seguir las convenciones del repo (clean arch,
`explicitApi()`, tests por regla).

| # | Tarea | Módulo / fichero | Notas |
|---|---|---|---|
| 7.1 | Ampliar `CefrLevel` con `C1`, `C2` y `VocabFrequencyBand` con `8000-12000`, `12000+` | `core/core-domain/.../model/Course.kt` | `@Serializable`; KDoc en la API pública. |
| 7.2 | Extender el esquema v2 | `docs/schemas/course-v2.json` | Cambio **aditivo** (nuevos valores de enums); sin `schemaVersion: 3`; los cursos existentes siguen válidos. |
| 7.3 | El validador de metadatos B1/B2 pasa a B1/B2/C1/C2 | `core/core-content/.../CourseValidator.kt` | `requiresCefrMetadata` refleja el nuevo rango. |
| 7.4 | Rampa C1/C2 en los tests de contrato | `core/core-content/src/test/.../ContentRampTest.kt`, `PackagedContentTest.kt` | Añadir `C1 -> 11, C2 -> 12` en `expected`; topes `C1 -> 38, C2 -> 48`; `isAllowedFor` y `rank()`; recuento `STARTER_COURSES` y verificación de la rampa también en C1/C2. |
| 7.5 | Etapas del camino C1/C2 | `feature/feature-home/.../path/PathItems.kt` (`PathStage`), `PathItemsTest.kt` | Los mapas A1/B2 se amplían; comprobar etiquetas de accesibilidad. |
| 7.6 | Informe de `validateContent` más claro | `build.gradle.kts` (tarea `validateContent`) | Añadir un resumen informativo por curso: nº lecciones por CEFR, tokens máximos, % de soporte L1 y nº de `FreeSpeakingPrompt`. |
| 7.7 | ADR-042: registro de la decisión | `docs/DECISIONS.md` | Añadir la sección con el formato del ADR-000 y actualizar el índice. |
| 7.8 | Plantilla editorial v3 (C1/C2) | `docs/CONTENT_FORMAT.md` | Documentar en una sección separada los parámetros C1/C2 (tabla de la sección 4) y los ejemplos por idioma. |

**Puntos de riesgo detectados en la integración:**

- `ContentRampTest` requiere que los cursos de arranque (fr/pt/ja) cumplan
  sus totales exactos (60 lecciones, 526 ejercicios); con C1/C2 ese contrato
  de totales se actualiza junto con el contenido del nivel correspondiente y
  nunca con adelanto (evita builds rojos).
- `PathItems` y tests de captura en `core-ui` (`DesignGallery…`,
  `Path…`) están referenciados por `PathStage`; ampliarlo obliga a sustentar
  los fixtures de accesibilidad AA también.
- No hay tipos de ejercicio nuevos: **cero cambios en el grader de tipos de
  ejercicio ni en Room/FSRS** (invariante I4: progreso). La tolerancia del
  `FreeSpeakingHeuristicGrader` ya se ajustó en el hotfix de voz 2.0.1
  (ADR-043, sección 14), de modo que C1/C2 llega con el evaluador ya robusto.

## 8. Producción por lotes y releases

Cada release minor añade **el contenido completo de un nivel** y **el código
complementario**. El piloto siempre es `es-en`; la réplica se hace tras validar
el piloto (gate verde + humo manual en emulador API 37).

| Release | Contenido nuevo | Lecciones nuevas | Ejercicios nuevos | Totales acumulados (curso × cursos) |
|---|---|---:|---:|---|
| 2.0.1 | **Hotfix de voz (ADR-043): captura tolerante + evaluación oral sin falsos negativos** | 0 | 0 | 249 lecciones |
| 2.1.0 | Infra (7.1–7.8) + **C1 completo en `es-en`** | 24 (en) | 264 | 93 lecciones `es-en` |
| 2.2.0 | **C1 completo en `es-fr`, `es-pt`, `es-ja`** | 72 (fr/pt/ja) | 792 | 84 lecciones × 3 (fr/pt/ja) |
| 2.3.0 | **C2 completo en `es-en`** | 18 (en) | 216 | 111 lecciones `es-en` |
| 2.4.0 | **C2 completo en `es-fr`, `es-pt`, `es-ja`** | 54 (fr/pt/ja) | 648 | 102 lecciones × 3 (fr/pt/ja) |
| 2.5.0 | (Opcional) refinamiento nativo + revisión B2/C1/C2 | 0 | 0 | — |

### Estado de ejecución

- **2.1.0 — publicado** (tag `v2.1.0`, versionCode 9): infraestructura 7.1–7.8
  y C1 completo en `es-en` (24 lecciones/264 ejercicios; curso 93/853). Release
  de GitHub con APK universal y arm64.
- **2.2.0 — publicado** (tag `v2.2.0`, versionCode 10): C1 completo en `es-fr`,
  `es-pt` y `es-ja` (72 lecciones/792 ejercicios nuevos; 84/790 por curso).
- **2.3.0 — publicado** (tag `v2.3.0`, versionCode 11): C2 completo en `es-en`
  (18 lecciones/216 ejercicios; curso 111/1.069).
- **2.4.0 — pendiente:** C2 completo en `es-fr`, `es-pt` y `es-ja`.

**Total acumulado al final (versión 2.4.x):**

| Escenario | Lecciones | Ejercicios |
|---|---:|---:|
| Hoy (2.0.0) | 249 | 2.167 |
| **Después de C1 (2.1–2.2)** | 345 | 3.223 |
| **Después de C2 (2.3–2.4)** | **417** | **4.087** |

Impacto en peso del APK: el contenido es JSON; se estima **< 2 MB** adicionales
en total, con margen amplio frente al control de peso del APK (< 2 GB).

## 9. Protocolo de lote (una unidad de nivel del curso)

Cada unidad (4–6 lecciones) sigue el mismo protocolo antes de pasar a la
siguiente. Unidad × 4 cursos ≈ 16–24 lecciones / 176–300 ejercicios.

1. **Redacción del lote.** Se escriben las lecciones del lote en el
   `course.json` del curso correspondiente, respetando:
   - `ids` nuevos **nunca** reciclados, siguiendo el patrón del curso
     (p. ej. `es-fr-lv-1-un-1-ls-1`); cuando se añada un nivel se continúa
     con el mismo patrón del curso (cuidando el formato exacto ya existente).
   - `schemaVersion: 2`, todo en UTF-8 **sin BOM** (verificar con script).
   - `scenario`, `canDo`, `grammarPoints` (2), `vocabFrequencyBand`
     (`8000-12000`/`12000+`), `culturalNotes` ≥ 1, `feedbackOnError` en **todos**
     los ejercicios, `microLesson`, `glossary` 8–14 términos.
2. **Composición y rampa**: validar que las lecciones nuevas cumplan 11/12
   ejercicios y el tope de tokens (38/48), 1 (C1) o 2 (C2) `FreeSpeakingPrompt`,
   con `grammarRuleRefs` a los `grammarPoints[]` de la misma lección.
3. **Gate local:**
   ```powershell
   ./gradlew ktlintFormat        # sólo si hay código
   ./gradlew ktlintCheck detekt validateContent test
   ```
4. **Humo manual en emulador (API 37):** abrir las lecciones nuevas, revisar
   audio TTS (Piper) de los `AudioOnlyPrompt`, control de voz con las respuestas
   modelo de `FreeSpeakingPrompt`, y long-press del glosario.
5. **Commit Conventional en español** (`content:`/`feat:` según toque) y el
   PR con la plantilla.

## 10. Revisión lingüística nativa (criterio pedagógico)

- Es **el mayor riesgo abierto**: la revisión nativa B1–B2 de `es-fr`,
  `es-pt` y `es-ja` ya aparece como pendiente. El plan no la sustituye, la
  reordena por prioridad de impacto:
  1. **B2 de fr/pt/ja** (66 lecciones) — recomendable cerrar antes del 2.2.0.
  2. **C1 de `es-en`** (24) y réplicas fr/pt/ja — al aumentar el nivel de
     dificultad, los errores semánticos penalizan más.
  3. **C2** — revisión nativa obligatoria antes de cerrar 2.4.x (es el nivel
     con más riesgo de incoherencias léxicas y de registro).
- **Muestreo mínimo por nivel y curso:** 100 % de los `FreeSpeakingPrompt`
  y `AudioOnlyPrompt` (donde el STT/TTS y la heurística son más frágiles),
  100 % de las micro-clases y 20 % del resto de ejercicios.
- Cada hallazgo se reporta como **issue con cita exacta** (curso, lección,
  ejercicio, corrección propuesta). Las métricas automáticas (longitud, FSP,
  banda, tokens) complementan, no sustituyen, la revisión nativa.

## 11. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Revisión nativa insuficiente | Contenido con fallos de precisión lingüística | Piloto por curso, muestreo mínimo (sección 10), feedback con evidencia (issue + cita exacta). |
| STT/TTS en fr/pt/ja para producción C1/C2 | Heurística de voz más frágil con textos largos | Los `modelAnswer` y `audioText` de C1/C2 se limitan (90–130 palabras); mismos modelos por SHA-256 y validación manual en emulador en cada lote. |
| Esquema/gates pierden coherencia al añadir niveles | Build rojo y contenido inconsistente | 2.1.0 incluye el código de 7.1–7.8 antes de cualquier lección nueva; `validateContent` informa la rampa. |
| Fatiga de agentes generando 1.920 ejercicios | Contenido incoherente | Lotes de una unidad; revisión cruzada de cada lote contra la unidad previa con el informe de rampa. |
| Crecimiento del tamaño del release | APK fuera de rango | Estimación < 2 MB de JSON; verificación de peso con `assembleFullRelease` y medición en el release. |

## 12. Métricas de éxito al cerrar el plan (2.4.x estable)

- **Cuatro cursos con rampa continua A1→C2** verificado por tests (rampa
  monotónica y por nivel).
- `validateContent` verde en los cuatro cursos con schema v2 ampliado; JSON
  sin BOM.
- 417 lecciones / 4.087 ejercicios netos versus 249/2.167.
- **Release publicado en GitHub Releases** con `versionCode` secuencial, APK
  universal + arm64 y `SHA256SUMS.txt` verificado.
- Toda lección nueva C1/C2 con metadatos completos (`scenario`, `canDo`,
  2 `grammarPoints`, banda léxica, ≥ 1 `culturalNote`, micro-clase, glosario
  y `feedbackOnError`/`feedbackOnSuccess`).

## 13. ADR-042 (aprobado y registrado en `docs/DECISIONS.md`)

> **ADR-042: Expansión progresiva a C1/C2 con rampa verificada y protocolo
> de lotes** — **Estado: Aprobado (2026-11-08).** El texto definitivo vive en
> [docs/DECISIONS.md](DECISIONS.md); este resumen queda como referencia.

- **Estado:** Aprobado (2026-11-08).
- **Contexto:** El contenido 2.0.0 cubre A1–B2 en los cuatro cursos
  (249/2.167). El CEFR `CefrLevel` del dominio y el esquema v2 no admiten
  C1/C2; la rampa está verificada por `ContentRampTest` hasta B2.
- **Decisión:**
  1. Se extiende `CefrLevel` a `C1`, `C2` y `VocabFrequencyBand` con
     `8000-12000` y `12000+`. Cambio **aditivo**: sin `schemaVersion: 3`
     ni dumps de Room; los cursos existentes siguen siendo válidos, porque
     sólo se añaden valores que no usan.
  2. La regla de metadatos del validador se aplica igual a C1/C2 que a
     B1/B2 (scenario, canDo, 2 grammarPoints, banda, ≥ 1 culturalNote,
     feedbackOnError en todos los ejercicios).
  3. Los techos de dificultad se aplican por nivel: 11/12 ejercicios por
     lección en C1/C2, topes de 38/48 tokens y ≥ 1/2 `FreeSpeakingPrompt`,
     verificados por `ContentRampTest` extendido.
  4. Cada nivel entra por `es-en` como piloto y luego se replica en fr/pt/ja
     manteniendo el esqueleto de unidades (secciones 5 y 6).
  5. Los ids históricos no se tocan; los nuevos siguen el patrón del curso y
     nunca se recicla un id que ya existió (I4: progreso intocable), porque
     el progreso guardado depende de la identidad de los ejercicios.
- **Consecuencias:** +168 lecciones / +1.920 ejercicios (417 lecciones y
  4.087 ejercicios en total). El camino de la UI gana dos etapas (C1/C2) en
  la ruta de aprendizaje (`PathItems`). Los requisitos de producción crecen
  (lotes por unidad); el validador informa la rampa y evita regresiones de
  dificultad.

## 14. Hotfix de voz ya incorporado (2.0.1 | ADR-043)

En 2.0.0 la producción oral fallaba con pronunciación correcta: Vosk cortaba
en pausas breves (~0,3 s) dejando la transcripción truncada, y el
`FreeSpeakingHeuristicGrader` no toleraba diacríticos, marcaba WPM fuera de
rango sin margen y bloqueaba por autoevaluación baja aunque todo lo demás
estuviera bien (ejercicios "no pasa" por razones ajenas al estudiante).

**Qué cambió (ya implementado, testeado y publicado en 2.0.1):**

| Zona | Cambio |
|---|---|
| `SpeechCapture` (ADR-020) | Endpoints prematuros: antes de 1,5 s se concatena el enunciado y la captura sigue; tras un enunciado válido hay 1,2 s de gracia que se reinicia si llega otra frase; el tope de captura sube de 6 s a 10 s. |
| Concatenación | `merge` compone los enunciados (dedupe por `endsWith`): ya no se pierde el resto de la frase. |
| `tokenize` (ADR-039/043) | Normalización NFD sin marcas `[\u0300-\u036F]`: "café" ≡ "cafe" en ambas direcciones. La rama japonesa no se normaliza (dakuten). |
| Tolerancia difusa | Distancia ≤ 2 desde 4 caracteres (antes 5): absorbe la fonética de Vosk en palabras cortas. |
| Cadencia WPM | Margen del ±20 % (`minWpm/1.2`, `maxWpm*1.2`): el ritmo natural o el micrófono ya no fallan la respuesta. |
| Autoevaluación | Informativa (`advisories`): nunca bloquea `passes`; las únicas señales que fallan son las objetivas (longitud, cobertura, gramática, cadencia extrema). |
| Hedging | Catálogo ampliado (*i guess*, *i wonder*, *i suppose*, *it might*, *kind of*, *sort of*, *to be honest*). |

**Impacto en este plan:**

1. Los `FreeSpeakingPrompt` de C1/C2 (monólogo + debate en C2) se apoyan en
   un STT que ya no trunca la primera frase: menos `TOO_SHORT` y
   `LOW_COVERAGE` falsos en fr/pt/ja.
2. Las tildes que Vosk omite en francés y portugués ya no se cobran como
   error: la evaluación de producción queda alineada con la escrita
   (ADR-039).
3. **Restricción declarada para la redacción C1/C2:** la captura sigue
   teniendo un tope de 10 s. Los prompts se escriben para respuestas
   verificables de ~2–3 frases (12–20 tokens): el concepto se cubre con
   `conceptClusters` y `minTokens` calibrados (~8–12). Los monólogos largos
   y los audios de referencia viven en `AudioOnlyPrompt`/`modelAnswer`, no
   como exigencia del STT. Encadenar varias sesiones de captura queda
   diferido.
4. El humo manual del protocolo de lote (sección 9) incluye la prueba de
   voz: decir la respuesta modelo completa y verificar que la captura
   compone toda la frase y que el veredicto es `Pass`.
