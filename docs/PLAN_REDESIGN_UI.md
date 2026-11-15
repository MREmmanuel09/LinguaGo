# Plan de rediseño de UI — prototipo «Warm Cream & Teal»

> **Estado:** R1–R7 **ejecutadas y verificadas en emulador** (Medium_Phone_API_37.0).
> Rediseño completo: ruta, lección, repaso, progreso, ajustes, onboarding,
> modo oscuro y gemas reales (DataStore + snapshot v4). QA nativo del usuario
> pendiente antes de publicar releases.
> **Referencia visual:** vídeo `C:\Users\emman\Downloads\gemini_generated_video_1b2a83df.mp4`
> y cuatro pantallas: ruta, lección, escucha/STT y repaso.
> **Fecha:** 2026-11-15.

Este plan convierte la identidad visual actual (violeta/fondo blanco) en el
prototipo cálido de crema, teal y terracota, **sin tocar las invariantes
I1–I9**: offline-first, sin LLM, progreso intocable (FSRS/Room intactos,
gemas vía DataStore) y contenido validado (1.020 lecciones por curso, ya
completas).

---

## 1. Inventario actual (lo que ya existe y se reutiliza)

`core-ui` ya tiene la mayoría de piezas; el rediseño es **reestilizar + añadir**:

| Pieza actual | Acción en el prototipo |
|---|---|
| `LinguaTokens`, `LinguaColors`, `LinguaExtendedColors` | Reestilizar (nueva paleta, ADR-055) |
| `Type.kt` | Cambiar a Manrope (pesos 400/500/700/800) |
| `LinguaMotion` | Ampliar duraciones/springs del prototipo |
| `PathNode`, `PathStage`, `PathLayout`, `LearningPath` | Reestilizar nodos (check/play/lock) + línea ondulada |
| `HeartsCounter`, `StreakIndicator`, `LinguaProgressBar` | Reestilizar (corazones, píldora «3 seguidas», barra teal/tan) |
| `TokenChip`, `OptionCard`, `AnswerKeyText` | Reestilizar chips 3D crema/tan con sombra marrón |
| `PrimaryButton3D`, `Tactile`, `FeedbackClickable` | Reestilizar (Continuar/FAB teal con sombra) |
| `AudioPlayerCard`, `VoiceWaveIndicator` | Reestilizar (onda bicolor teal/terracota, 1x/0,75x) |
| `FreeSpeakingPanel` | Reestilizar (micrófono con anillos, «Escuchando…») |
| `FeedbackBanner`, `MistakeReviewSheet` | Convertir en `FeedbackSheet` (fondo cálido, «Ver por qué») |
| `RewardSheet`, `ChestRewardSheet`, `ConfettiBurst` | Conservar con paleta nueva |
| `LinguaCard3D`, `FlipCard3D` | Base para la baraja de repaso |
| `WordGlossaryText`, `TranslationTooltip` | Conservar (resaltado de `It's` / `o'clock`) |
| `LumiMascot`, `LumiMood` | Ocultar en la ruta (R4); decisión final en R7 |
| Bottom bar (`LinguaApp.kt`) | Renombrar tabs: Ruta / Repaso / Progreso / Ajustes |
| Racha, corazones, XP | Ya existen en `core-domain`/DataStore; sin cambios |

**Gemas (💎) no existen**: se añaden en R6 (DataStore + snapshot de sync).

## 2. Fases y entregas

### R0 — Plan y decisión (hecho en este commit)

- ADR-055 con paleta, tipografía, formas, movimiento y gemas.
- Este plan con inventario, fases y criterios de aceptación.
- Firma de alcance: el vídeo es la referencia de movimiento; las capturas, la
  referencia de layout.

### R1 — Tokens, tema y tipografía (`design:`)

Archivos: `LinguaTokens.kt`, `LinguaColors.kt`, `LinguaExtendedColors.kt`,
`LinguaTheme.kt`, `Type.kt`, `LinguaMotion.kt`.

1. Sustituir los hex canónicos según el mapa de ADR-055 y añadir los tokens
   nuevos (tan, terracota, sheet cálido, tintas AA).
2. Modo oscuro derivado (valores propuestos en R7, pero compilar desde R1 con
   mapping provisional y tests de contraste).
3. Tipografía: **Nunito se mantiene** (ya coincide con el prototipo). Solo se
   ajustan dos escalas: `headlineMedium` 30/38 (título de ejercicio) y
   `bodyLarge` 16/24.
4. Radios y sombras: píldora 999, tarjeta 16, sheet 24; sombra 3D marrón para
   chips/botones (`TAN_SHADOW`).
5. Actualizar los tests de identidad de color de `core-ui` (JVM) para los hex
   nuevos y el contraste AA de las tintas.
6. Pase visual de todas las pantallas con la paleta nueva (sin tocar layout):
   `:app:assembleFullDebug` + smoke en emulador.

**Aceptación:** gates en verde; pantallas con paleta nueva; identidad testeada.

### R2 — Lección según prototipo (`feat: rediseñar la pantalla de lección`)

Archivos: `feature-lesson/LessonScreen.kt`, `LessonBriefScreen.kt`,
`MistakeReviewSheet.kt` → nuevo `FeedbackSheet` en `core-ui`.

1. **Cabecera:** título de la lección + píldoras «Guía» y «Salir», barra de
   progreso (relleno teal sobre pista tan), contador de corazones y píldora
   coral «N seguidas 🔥».
2. **Insignia de tarea:** píldora slate con el tipo («Traduce esta oración»,
   «Escucha y repite», «Ordena las palabras», …).
3. **Enunciado:** texto grande del prototipo.
4. **Ejercicios:**
   - `Translate`/`OrderWords`: chips 3D (crema, borde tan, sombra marrón),
     línea de respuesta con huecos subrayados, chips restantes debajo.
   - `ListenChoose`/`ListenType`/`AudioOnlyPrompt`: tarjeta de audio con onda
     bicolor y conmutador de velocidad `1x / 0,75x`.
   - `SpeakRepeat`/`FreeSpeakingPrompt`: botón de micrófono con anillos,
     «Escuchando…», «No puedo hablar ahora».
   - `ClozeDragDrop`, `SemanticMatch`, `MatchPairs`: reutilizar los boards
     existentes con tokens nuevos.
5. **Feedback:** bottom sheet cálido («Revisa la respuesta»), título
   terracota, cerrar con X, bloque «Ver por qué» desplegable con la
   explicación de `feedbackOnError` y las palabras objetivo en negrita, y
   botón teal «Continuar» con sombra 3D.
6. **Micro-clase «Guía»:** sheet con `microLesson` (secciones y ejemplos).
7. Estados de error/acierto correctos/incorrectos con `CORAL`/`SAGE` y haptics
   existentes.

**Aceptación:** cada tipo de ejercicio del contenido real (los 14 tipos) se
renderiza con el prototipo; sheet con «Ver por qué»; smoke en emulador.

### R3 — Repaso FSRS según prototipo (`feat: rediseñar el repaso`)

Archivos: `feature-review/ReviewScreen.kt`, `ReviewViewModel.kt` (sin cambios
de lógica), nuevos `ReviewDeck`, `GradeBar` en `core-ui`.

1. **Cabecera:** volver (←), píldora «Tarjeta N de M», corazones.
2. **Baraja:** pila de tarjetas crema con borde tan; primera tarjeta con
   etiqueta de tipo («Traducción»), término grande, traducción, audio con onda
   bicolor y conmutador de velocidad.
3. **Calificación:** «¿Qué tal te salió?» + 4 botones: «Otra vez» (coral),
   «Difícil» (ámbar), «Bien» (sage), «Fácil» (teal); mapeo 1:1 a la escala
   FSRS actual (sin cambiar graders ni I4).
4. Animación de transición entre tarjetas (260 ms) y haptics por botón.

**Aceptación:** el flujo FSRS funciona igual; la UI coincide con la captura;
tests instrumentados de repaso en verde.

### R4 — Ruta según prototipo (`feat: rediseñar la ruta`)

Archivos: `feature-home/HomeScreen.kt`, `PathLayout.kt`, `PathItems.kt`,
`LearningPath.kt`, `core-ui/path/*`.

1. **Stats superiores:** racha 🔥, gemas 💎 (placeholder «—» hasta R6),
   corazones ❤️ N/10.
2. **Cabecera de curso:** «Español → Inglés», «Nivel B1» (nivel actual real),
   píldora «Cambiar» (reutiliza `CourseSwitcherSheet`).
3. **Tarjeta «Repasar ahora»:** crema con barra izquierda terracota, icono
   marcador dorado y «N tarjetas pendientes» (del repaso real).
4. **Camino serpenteante:** nodos completado (sage ✓), actual (anillo teal +
   ▶) y bloqueados (tan 🔒), etiquetas alternadas a los lados; línea ondulada
   continua; **paginado por unidades** (ventana alrededor del nodo actual) para
   soportar 170 nodos por nivel sin degradar rendimiento.
5. **FAB «Ir a la lección»** anclado abajo a la derecha (teal, sombra 3D).
6. **Bottom bar:** tabs «Ruta / Repaso / Progreso / Ajustes» con badge en
   Repaso (ya existe) y color activo teal.
7. **Lumi:** ocultar de esta pantalla (R7 decide su futuro).

**Aceptación:** con el curso es-en B1 (170 nodos) el scroll es fluido
(`baselineprofile` opcional); FAB abre la lección actual; badge correcto.

### R5 — Progreso, Ajustes y Onboarding (`feat: rediseñar progreso y ajustes`)

1. `feature-profile` (Progreso): estadísticas con tarjetas crema/borde tan,
   gráficos con teal/terracota, racha y corazones.
2. `feature-settings`: filas con píldoras y switches reestilizados; «Sync»
   abre `feature-sync` reestilizado.
3. `feature-onboarding`: pantallas de selección de curso con el nuevo estilo.
4. Strings: `tab_home` → «Ruta», `tab_profile` → «Progreso».

**Aceptación:** todas las pantallas usan la paleta; sin TODO visuales.

### R6 — Gemas 💎 (`feat: añadir gemas a la gamificación`)

Archivos: `core-domain` (Snapshot + reglas), `core-data` (DataStore),
`core-sync` (merge), `feature-sync` (copy), tests.

1. Campo `gemsCurrent` (int) en el snapshot de progreso.
2. Fusión monótona: `max(local, remote)` como racha/corazones (I4).
3. Versionar el snapshot (bump de versión de schema de sync si aplica) y
   actualizar `SyncScreen` («Exporta un .json con racha, XP, gemas, vidas…»).
4. UI: chip 💎 en la ruta y en la hoja de recompensas de cofre (si aplica).
5. Tests: `SnapshotMergeTest` (gemas), repositorio DataStore, y un caso de
   conflicto de gemas.

**Aceptación:** gemas se exportan/importan sin perder datos; merge es máximo;
Room sin cambios.

### R7 — Cierre de calidad (`chore:`/`test:`)

1. **Modo oscuro** derivado de la paleta (valores definitivos + tests de
   contraste AA en JVM).
2. **Accesibilidad:** contentDescription en nodos/micrófono/onda, TalkBack
   recorre la lección y la ruta, targets ≥ 48 dp.
3. **Estados vacíos y errores** (sin conexión, sin audio, curso sin progreso).
4. **QA instrumentado:** `:core:core-ui:connectedDebugAndroidTest` y smoke en
   emulador `Medium_Phone_API_37.0` (job CI «Instrumented», API 34).
5. **Rendimiento:** recorrido del camino con 170 nodos; `baselineprofile`
   opcional para el arranque.
6. **Decisión sobre Lumi:** mantener como easter egg, mover a Progreso o
   retirar (ADR breve si se retira).
7. **Release:** sigue aparcado (3.3.0–4.0.0); el rediseño se publica cuando el
   usuario dé el visto bueno tras su QA manual.

## 3. Datos y sincronización (resumen)

- **Room:** sin cambios de schema. FSRS y progreso intocables (I4).
- **DataStore:** racha, XP, corazones y (nuevo) gemas.
- **Snapshot de sync:** + `gemsCurrent`, fusión máxima, tests de conflicto.
- **Contenido:** 4.080 lecciones ya validadas; el rediseño no cambia datos.

## 4. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Rendimiento del camino con 170 nodos/nivel | Ventana lazy de ±3 unidades + paginado |
| Romper tests de identidad de color | Actualizarlos en R1 junto con los tokens |
| Sheet «Ver por qué» con datos faltantes | Fallback a `grammarPoints[].description` |
| STT sin permiso / sin audio | Estado «No puedo hablar ahora» ya existente |
| Snapshot de sync incompatible | Bump de versión + migración tolerante + tests |
| R8 y recursos nuevos (fuentes) | Sin keeps nuevos; validar `assembleFullDebug`/`Release` |
| Alcance grande | Fases R1–R7 independientes, cada una con gate y commit |

## 5. Estrategia de commits (Conventional, en español)

- `docs:` ADR-055 + este plan (R0, ya hecho).
- `design:` tokens, tema, tipografía, motion, componentes base (R1).
- `feat:` lección (R2), repaso (R3), ruta (R4), progreso/ajustes/onboarding (R5).
- `feat:` gemas + snapshot (R6) — separado del UI.
- `chore:`/`test:` modo oscuro, accesibilidad, QA (R7).
- Nunca mezclar `design:` con `feat:` ni con datos.

## 6. Definición de hecho (por pantalla)

1. Lección: cabecera, progreso, corazones, racha, insignia, chips 3D, sheet
   «Revisa la respuesta» con «Ver por qué» y «Continuar».
2. STT: micrófono con anillos, «Escuchando…», «No puedo hablar ahora».
3. Ruta: stats (racha/gemas/corazones), cabecera + «Cambiar», «Repasar ahora»,
   camino serpenteante, FAB, bottom bar con badge.
4. Repaso: «Tarjeta N de M», baraja, onda bicolor, 1x/0,75x, 4 botones de
   calificación con los colores del prototipo.
5. Progreso/Ajustes/Onboarding: paleta y componentes nuevos.
6. Gates: `ktlintFormat` → `ktlintCheck detekt validateContent test`;
   `:app:assembleFullDebug`; smoke en emulador; instrumentados de `core-ui`.
7. QA nativo del usuario sobre la build de debug antes de considerar el
   rediseño cerrado.
