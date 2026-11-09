# Changelog

Todos los cambios notables de LinguaGo se documentan aquí.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado sigue [SemVer](https://semver.org/lang/es/).
Pre-1.0: los cambios incompatibles pueden ocurrir en `0.y.0`.

## [2.3.0] - 2026-11-08

C2 completo en `es-en` (ADR-042): 18 lecciones y 216 ejercicios nuevos en el
nivel de maestría, con doce ejercicios por lección y frases de hasta 48 tokens.

### Añadido

- **C2 en `es-en`:** nivel `es-en-lv-5` con cuatro unidades — registros y
  matices (ironía seca, eufemismo, doble sentido, alusiones literarias),
  dominios especializados (derecho, ciencia, humanidades, economía), lengua
  culta (retórica, contraargumento, sintaxis, escritura evaluada) y síntesis y
  evaluación (registro literario, metáforas culturales, especialización,
  monólogo de examen y debate final).
- **Cada lección** incluye micro-clase, glosario, nota cultural original, dos
  focos gramaticales y un `FreeSpeakingPrompt` dimensionado para ≤10 s de voz.
- **Contrato C2** fijado en los tests: 18 lecciones, 216 ejercicios, banda
  `12000+`, 12 ejercicios por lección, `feedbackOnError` en todos los
  ejercicios y un oral libre por lección.

### Cambiado

- `es-en` pasa de 93 a **111 lecciones** y de 853 a **1.069 ejercicios**; el
  repositorio acumula **363 lecciones y 3.439 ejercicios**.

## [2.2.0] - 2026-11-08

C1 completo en los tres cursos de arranque (ADR-042): francés, portugués y
japonés reciben 24 lecciones y 264 ejercicios cada uno.

### Añadido

- **C1 completo en `es-fr`, `es-pt` y `es-ja`:** nivel `es-*-lv-c1` con seis
  unidades de cuatro lecciones (consolidación, argumentación, trámites,
  liderazgo, proyecto integrador y evaluación global). Cada lección incluye
  micro-clase, glosario curado, nota cultural y un `FreeSpeakingPrompt`.
- **Enfoques gramaticales propios de cada idioma:** en francés, concordancia
  de tiempos, subjuntivo en régimen complejo, condicionales mixtos y registro
  soutenu; en portugués, futuro composto, condicional misto, discurso indireto
  y colocaciones administrativas; en japonés, keigo, 〜ものの/〜にもかかわらず,
  〜とのことです, 〜場合に限り y registro です・ます/である.
- **Contrato de calidad C1** para los tres cursos: 24 lecciones, 264
  ejercicios, banda `8000-12000`, 11 ejercicios por lección, 2 focos
  gramaticales, `feedbackOnError` en todos los ejercicios y un oral libre por
  lección.

### Cambiado

- Cada curso de arranque pasa de 60 a **84 lecciones** y de 526 a **790
  ejercicios**; el repositorio acumula **345 lecciones y 3.223 ejercicios**.

## [2.1.0] - 2026-11-08

Expansión a C1 (ADR-042): infraestructura de niveles avanzados y piloto C1
completo en `es-en`, con el evaluador oral ya robustecido por el hotfix 2.0.1.

### Añadido

- **Infraestructura C1/C2:** `CefrLevel` incorpora `C1` y `C2`, y
  `VocabFrequencyBand` las bandas `8000-12000` y `12000+`. El schema v2 y el
  validador amplían la regla de No-Aplanamiento a B1/B2/C1/C2 **sin**
  `schemaVersion: 3` (cambio aditivo; los cursos existentes siguen válidos).
- **Rampa C1/C2 verificada:** `ContentRampTest` fija los escalones (C1 11
  ejercicios/38 tokens; C2 12/48) y `validateContent` imprime un informe por
  curso con lecciones por CEFR, ejercicios, cobertura de feedback y frase más
  larga.
- **Etapas C1/C2 en el camino** (`PathStage`) con etiquetas y contraste AA,
  derivadas del CEFR real de cada nivel.
- **C1 completo en `es-en`:** nivel `es-en-lv-4` con 24 lecciones y 264
  ejercicios, organizados en seis unidades (consolidación, argumentación,
  trámites, liderazgo, proyecto integrador y evaluación global). Cada lección
  incluye micro-clase, glosario curado, nota cultural y un `FreeSpeakingPrompt`;
  el curso acumula ahora **93 lecciones y 853 ejercicios**.

### Cambiado

- Los totales de contrato de `es-en` se actualizan junto con el contenido
  (69→93 lecciones, 589→853 ejercicios) y las lecciones C1 usan la banda léxica
  `8000-12000` con dos focos gramaticales.

## [2.0.1] - 2026-11-08

Hotfix de voz (ADR-043): la evaluación oral ya no falla cuando la
pronunciación es correcta y la captura completa frases truncadas.

### Corregido

- **Captura de voz tolerante (`SpeechCapture`):** el endpoint prematuro de
  Vosk (pausas de ~0,3 s) ya no corta la grabación a mitad de frase: los
  enunciados que llegan antes de 1,5 s se concatenan y la captura continúa;
  tras un enunciado válido hay 1,2 s de gracia que se reinicia si llega otra
  frase. El tope de captura sube de 6 s a **10 s**.
- **Evaluación oral sin falsos negativos (`FreeSpeakingHeuristicGrader`):**
  transcripciones con tildes omitidas por Vosk ya pasan (normalización NFD
  como la evaluación escrita), la tolerancia difusa aplica desde 4
  caracteres, la cadencia WPM admite un margen del ±20 % y la autoevaluación
  baja es informativa (`advisories`) en lugar de bloquear el paso.

## [2.0.0] - 2026-11-02

Release de contenido y experiencia: B2 completo en los cuatro cursos, cursos
de arranque migrados a schema v2, glosario por lección y accesibilidad AA.

### Cambiado

- **Cursos de arranque `es-pt`, `es-fr` y `es-ja` migrados a schema v2**
  (ADR-041) conservando sus ids históricos: ahora declaran `cefrLevel: A1`,
  micro-clase, glosario curado y `feedbackOnError` específico.
- **`es-pt`, `es-fr` y `es-ja` ampliados a 38 lecciones/306 ejercicios**
  cada uno: 10 A1, 8 A2 y **B1 completo** (20 lecciones), con pasado, planes,
  comparaciones, viajes, salud, trabajo, opiniones, trámites, bienestar,
  cultura, ocio, ciudad y servicios, además de kana y partículas en japonés.
- **`es-pt`, `es-fr` y `es-ja` inician B2** con la Unidad 1 de negociación
  y decisiones (4 lecciones/40 ejercicios cada uno): condicional, hipótesis,
  pasiva reportativa y estructuras cleft.
- **B2.2 de opinión y debate** en los tres idiomas (4 lecciones/40 ejercicios
  cada uno): concesión, atenuación de la crítica, ironía/registro y rebatir
  con evidencia.
- **B2.3 de narrativa y matices** en los tres idiomas (4 lecciones/40
  ejercicios cada uno): tiempos del relato, decisiones difíciles, discurso
  reportado y arrepentimiento.
- **B2.4 de sociedad y medios** en los tres idiomas (5 lecciones/50
  ejercicios cada uno): leer entre líneas, manipulación mediática, temas
  incómodos, privacidad y propuestas de futuro.
- **B2.5 de proyectos y futuro** en los tres idiomas (5 lecciones/50
  ejercicios cada uno): presentar un proyecto de barrio, defender una causa,
  negociar con la administración, presentar un balance y cerrar un debate
  sobre el futuro. Con esto B2 queda completo (22 lecciones/220 ejercicios
  por curso).

### Corregido

- **Evaluación oral japonesa:** el heurístico ya no cuenta una frase japonesa
  entera como un solo token; tokeniza por caracteres y cubre los clústeres
  conceptuales sin depender de espacios.
- **`es-en` ampliado y reorganizado** en A1 10 / A2 17 / B1 20 / B2 22 (69
  lecciones y 589 ejercicios), con la unidad A1 “Vida cotidiana y entorno”,
  la unidad puente B1→B2 y `ContentRampTest`.

## [1.2.1] - 2026-10-31

Parche centrado en la claridad pedagógica de la lección (ADR-036).

### Agregado

- **Traducción al mantener pulsada una palabra** en opciones y fichas: nota
  emergente encima de la palabra, oculta al soltar y con acción de
  accesibilidad “Ver traducción”.
- **Tabla final de resultados** con aciertos, errores y nota en porcentaje
  sobre el primer intento (2 errores de 10 → 80 %).
- **Ventana de corrección antes de terminar** para repetir solo los ejercicios
  fallados, sin XP ni vidas; la nota no cambia.
- Tests de `LessonScoring`, del brief temático y del emparejamiento inverso.

### Cambiado

- **“Antes de empezar”** explica el tema (gramática, objetivos y ejemplos) y
  ya no incluye mecánicas de interfaz.
- Los **ejercicios de opción** validan solo al pulsar “Siguiente”; tocar una
  opción ya no revela acierto/error.
- **Formar frases** (`Translate`) usa una tira horizontal con scroll, igual
  que `OrderWords`.
- **Emparejamiento bidireccional:** se puede empezar por la columna izquierda
  o por la derecha.
- Glosario de apoyo derivado de `MatchPairs`, notas culturales y ejemplos de
  micro-clase (cobertura parcial hasta autorizar `glossary` en el contenido).

### Corregido

- Validación inmediata al seleccionar una palabra.
- Disposición vertical al construir frases.
- Selección limitada a la columna izquierda en el emparejamiento.
- Falta de medición porcentual y de corrección de errores al finalizar.

## [1.2.0] - 2026-10-31

Versión centrada en la experiencia visual, sonora e interactiva ("Feather
2.0"): profundidad táctil, Lumi expresiva, lección viva, repaso 3D y pantallas
de perfil y onboarding renovadas.

### Agregado

- **Física táctil 3D** (ADR-031): hundimiento sobre el borde inferior, bisel de
  luz y `TokenChip` compartido; SFX cálidos v2 (22,05 kHz) y nuevos cues
  `STREAK_COMBO`, `CARD_FLIP`, `HEART_LOST` y `CHEST_OPEN`.
- **Lumi multicapa y expresiva** (ADR-032) con moods `IDLE`, `HAPPY`,
  `ENCOURAGING` y `LISTENING`; `PathStage` A1–B2, banners de unidad 3D y punto
  animado en el conector del camino.
- **Lección viva** (ADR-033): panel de feedback deslizante con Lumi, combo de
  aciertos, ondas de audio TTS/STT y animaciones de fichas.
- **Repaso 3D** (ADR-034): `FlipCard3D`, pronunciación TTS y botonera FSRS
  semántica (coral, ámbar, turquesa y púrpura).
- **Perfil Bento y onboarding ilustrado** (ADR-035): anillo de dominio, nivel,
  estadísticas 3D, racha con mejor marca, insignias por idioma y transiciones
  de navegación.
- Iconos originales `ic_star`/`ic_cards`; tests de combo, etapas, tokens,
  háptica, perfil y accesibilidad de la tarjeta 3D.

### Cambiado

- `PrimaryButton3D` acepta colores semánticos y variante compacta.
- `ReviewViewModel` inyecta `CourseRepository` y `TtsEngine` para pronunciar
  la traducción; `feature-review` depende de `core-audio`.
- `ProfileUiState` gana nivel y avance de XP; `LessonUiState` gana combo y
  estado de reproducción.
- Documentación: ADR-031 a ADR-035 y `docs/DESIGN_SYSTEM.md`.

### Notas

- Todo sigue siendo 100% local y offline; sin dependencias nuevas ni cambios
  de esquema. Reduced motion y TalkBack se mantienen (I1/I5/I8).

## [1.1.0] - 2026-10-25

Versión centrada en la experiencia de aprendizaje: feedback sensorial,
micro-clases de gramática, explicación de aciertos, reintentos y tableros
manipulativos.

### Agregado

- **Feedback sensorial global** (ADR-027): SFX originales para selección,
  encaje, acierto, error y completado; háptica con `VibrationEffect`; ajustes
  independientes de sonido y vibración.
- **Micro-clases de gramática y reglas** (ADR-028) antes de los ejercicios,
  con objetivos, ejemplos, mecánicas y botón “Guía” para reabrir. Si la
  lección no trae `microLesson`, se genera desde sus metadatos.
- **Feedback de acierto**: `feedbackOnSuccess` opcional y explicación de
  respaldo con la respuesta correcta visible; opciones correcta/incorrecta
  marcadas con color e icono.
- **Reintentos pedagógicos** (ADR-030) para escucha, dictado y voz: un
  reintento sin consumir otra vida, XP máximo una vez y respuesta oculta
  hasta agotar el reintento o pedir “Ver respuesta”.
- **Audio repetible y a 0.75x** para escucha, dictado y repetición oral.
- **OrderWords horizontal**: frase en tira con scroll y banco de palabras que
  envuelve, sin desbordes.
- **Emparejamiento tipo rompecabezas** (ADR-029) con piezas de pestaña/ranura
  del mismo color, toque o arrastre, separación de parejas y corrección
  visual al comprobar.
- Tests de `AnswerKey`, `RetryPolicy`, micro-clases, schema/validador y
  tablero puzzle.

### Cambiado

- Schema v2 aditivo: `microLesson` y `feedbackOnSuccess`, validados en build
  y en runtime sin cambiar `schemaVersion`.
- Las respuestas se bloquean al enviarse y tras el feedback; se evita el
  doble envío y el doble XP.
- Gate F4 y baseline profile cierran la micro-clase y reconocen los nuevos
  tableros por etiquetas estables.
- README, design system y ADRs 027–030 actualizados.

### Corregido

- Reenvío de respuestas en opciones y dictado tras el feedback.
- Revelado prematuro de la respuesta correcta en ejercicios reintentables.
- Desborde horizontal del banco de palabras.
- Detección del gate para `SemanticMatch`/`MatchPairs` con el tablero puzzle.

## [1.0.0] — 2026-10-25

Primera versión final. Rediseño UI/UX "Feather" y camino de aprendizaje
guiado (ADR-026), con vidas no bloqueantes y XP diferido (ADR-025).

### Agregado

- **Camino de aprendizaje lineal** en Home: zigzag con nodos flotantes,
  cabeceras de unidad, conectores curvos y cofres al completar cada unidad.
  Activo en release con `LEARNING_PATH_V2`.
- **Vidas (10) con regeneración rápida** (1/min): el fallo cuesta una vida;
  a 0 la lección se cancela sin guardar XP ni progreso, y el acceso nunca
  queda bloqueado de forma permanente.
- **HUD del alumno**: racha, corazones, nivel visual y barra de XP líquida.
- **Recompensas**: hoja de victoria con contador de XP animado, confeti y
  cofre de unidad; hoja de lección cancelada con salida inmediata.
- **Lumi**, mascota original (colibrí vectorial), junto al nodo activo y en
  la victoria.
- **Repaso FSRS**: `Otra vez` consume una vida, sin bloquear la sesión.
- **Baseline profile regenerado** con el journey del Camino (Cold Start →
  Home → primer nodo → MultipleChoice → Repaso FSRS).
- **Release Gate F4** adaptado al camino y a las vidas: conduce las 5
  primeras lecciones A1, presupuesto de 2 fallos por lección y navegación
  por nodos; corrida completa de 20 min PASS (`failed=[]`).

### Cambiado

- **XP diferido**: cada respuesta solo califica; el XP se persiste una vez
  al completar la lección (ADR-025).
- Room **v4** (`hearts_progress`) con migración 3→4 y test instrumentado.
- `SyncSnapshot` **v2** con vidas y compatibilidad con snapshots v1.
- README: "sin vidas" pasa a "vidas con recuperación rápida" (I3
  reinterpretado en ADR-025).

### Corregido

- **Doble conteo de XP** (suma por respuesta + al completar).
- Contraste del conector del camino en modo oscuro.
- Icono de cofre ilegible a tamaño grande (rediseñado con cerradura).
- Navegación del Release Gate con la UI del camino.

## [0.2.0] — 2026-10-17

Fases 4.2 → 7: seguridad, release, repaso espaciado, accesibilidad y pulido.

### Agregado

- **Cifrado AES-GCM-256 con Android Keystore** (ADR-014): el snapshot se
  cifra antes de subirlo y los tokens OAuth en reposo; migración automática
  de tokens en claro.
- **Release config**: R8 + resource shrinker con keeps de JNA/Vosk,
  sherpa-onnx y kotlinx-serialization; firma por gradle properties;
  `assembleRelease` en CI.
- **Repaso espaciado FSRS-4.5** (ADR-016): scheduler puro en dominio,
  extracción de vocabulario (`MatchPairs`/`KanaChoose`/`KanjiReading`),
  tabla Room `word_progress` (DB v2 con migración 1→2), pantalla de repaso
  con 4 ratings, entrada desde Home ("Repasar ahora · N").
- **Accesibilidad I8**: roles TalkBack (Button/RadioButton), live region del
  feedback, `ProgressBarRangeInfo`, racha con descripción, touch targets
  ≥48 dp en tokens/tiles, soporte de `fontScale` 2x.
- **Tests instrumentados** del design system (7) + job de CI en emulador
  API 34 (`:core:core-ui:connectedDebugAndroidTest`).
- **Estadísticas de Progreso**: XP acumulados, lecciones `x de y`, tarjetas
  de repaso y pendientes de hoy.
- Documentación completa: `ARCHITECTURE.md` actualizado, `CONTENT_FORMAT.md`,
  `SECURITY.md`, `CODE_OF_CONDUCT.md`, `CHANGELOG.md`, `AGENTS.md`,
  `llms.txt`, plantillas de issues y de PR.

### Corregido

- Crash en release al abrir una lección: `ConcurrentHashMap.getOrPut` con
  valor `null` (`putIfAbsent(key, null)` → NPE) en los motores STT y TTS;
  ahora con patrón null-safe y logs de diagnóstico.
- R8 rompía el STT: faltaban reglas de **JNA**
  (`UnsatisfiedLinkError: com.sun.jna.Pointer`).
- Mojibake y caracteres perdidos (U+FFFD) en el contenido: acentos de los
  4 manifests restaurados, "sœur" recuperada y **curso japonés reescrito**
  con kana/kanji reales (あ/い/く, 山/水, partículas).
- `Espresso 3.7.0` para compatibilidad con APIs nuevas
  (`InputManager.getInstance` eliminado en API 36+).
- Estado obsoleto en Sincronización al volver de Cuenta; tarjeta del
  proveedor de laboratorio no aparecía (semáforo reactivo).

### Cambiado

- Versión `0.2.0` (versionCode 2).
- El repaso no otorga XP en v1 (decisión documentada en ADR-016).
- El estado FSRS (`word_progress`) queda fuera del snapshot de sync v1.

## [0.1.0-alpha] — 2026-10-16

Fases 0 → 4.1: base del proyecto.

### Agregado

- Repositorio, convention plugins de Gradle (`build-logic`), catálogo de
  versiones y CI (ktlint + detekt + tests).
- `core-domain` puro (lecciones, graders, racha, FSRS como contrato),
  `core-data` (Room + DataStore), `core-ui` (design system), `core-content`
  (loader + validador) y 4 cursos ES→EN/PT/FR/JA como assets.
- Shell de la app con navegación, onboarding, Home con ruta de aprendizaje,
  motor de lección (FSM), XP (+10 por acierto) y racha diaria.
- **Audio offline**: TTS del sistema con fallback por idioma, TTS **Piper**
  (sherpa-onnx) con verificación SHA-256, STT **Vosk** y ejercicio
  SpeakRepeat con degradación limpia.
- **Contenido v1**: JSON Schema formal (`docs/schemas/course-v1.json`),
  tarea `validateContent` en CI.
- **Sync Fase 3**: snapshot v1 JSON, merge monótono (nunca borra progreso),
  `SyncScreen` y `NoopCloudProvider` (ADR-013).
- **Auth Fase 4.1**: OAuth 2.0 + PKCE con Google Drive `appDataFolder`
  (ADR-011), `AuthScreen` real, `OAuthCallbackActivity` con redirect custom
  y proveedor de laboratorio para debug.

[0.2.0]: https://github.com/MREmmanuel09/LinguaGo/compare/v0.1.0-alpha...v0.2.0
[0.1.0-alpha]: https://github.com/MREmmanuel09/LinguaGo/releases/tag/v0.1.0-alpha
