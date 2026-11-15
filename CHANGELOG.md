# Changelog

Todos los cambios notables de LinguaGo se documentan aquÃ­.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado sigue [SemVer](https://semver.org/lang/es/).
Pre-1.0: los cambios incompatibles pueden ocurrir en `0.y.0`.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [3.2.0-rc1] - 2026-11-15

Release candidate tÃ©cnico de F1. Incluye F0 (240 lecciones piloto) y F1
(600 lecciones A1 nuevas: 150 por curso). Total: 1.257/4.080 lecciones.

### AÃ±adido

- A1 completo en ESâ†’EN, ESâ†’PT, ESâ†’FR y ESâ†’JA (170/170 por curso).
- 60 bloques nuevos de A1 (15 por curso) con 4.200 ejercicios.
- AuditorÃ­a F1: 0 lecciones duplicadas, 19 tests de contenido y 11 de rampa.
- `contentScaleReport`: 1.257/4.080 lecciones.
- Kit de revisiÃ³n nativa: 840 lecciones y 40 modismos B2 en
  `docs/qa/fase1.5-revision-nativa.md`.
- AutomatizaciÃ³n de publicaciÃ³n en `tools/release/` con checks de revisiÃ³n
  nativa, firma y GitHub CLI.

### Pendiente / bloqueado

- RevisiÃ³n nativa humana de F0/F1 (ADR-053).
- Firma de release: sin keystore, el APK release usa la clave debug; no
  distribuir.
- PublicaciÃ³n en GitHub: `gh` sin autenticar.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [3.0.0] - 2026-11-09

RediseÃ±o de experiencia (ADR-048/050) en cuatro fases. Fases 1â€“4 completadas.

### AÃ±adido

- Barra inferior de 4 destinos (`Inicio Â· Repaso Â· Progreso Â· Ajustes`) con
  badge de tarjetas vencidas.
- `CourseSwitcherSheet` en Home y Ajustes para cambiar de curso en 2 toques.
- TÃ­tulos bajo los nodos del camino y botÃ³n flotante Â«Ir a mi lecciÃ³nÂ».
- Cofres de unidad con `+20 XP` Ãºnico y estado Â«Cofre ya reclamadoÂ».
- RecuperaciÃ³n de `+1 â¤` cada 5 tarjetas superadas en Repaso.
- Nota cultural en el reverso de la tarjeta y audio a `0.75x`.
- CuadrÃ­cula `2Ã—2` de calificaciÃ³n cuando `fontScale >= 1.5`.
- Room v5: `daily_activity`, nota cultural persistida y mÃ©tricas reales de
  voz/precisiÃ³n con migraciÃ³n `4â†’5` y test instrumentado.
- `SyncSnapshot v3` con el mazo FSRS y merge monÃ³tono por tarjeta.
- Pantalla unificada Â«Respaldo y datosÂ» con Google Drive opcional y
  exportaciÃ³n/importaciÃ³n local `.json` vÃ­a SAF.
- Perfil con mejor fluidez oral, precisiÃ³n media, tasa de superaciÃ³n oral y
  calendario de actividad de 28 dÃ­as.
- Desglose CEFR A1â€“C2 por curso activo en el perfil.
- `espeak-ng-data` compartido entre las tres voces Piper y librerÃ­as nativas
  comprimidas; gestor de voces en Ajustes para liberar la cachÃ© extraÃ­da.

### Pendiente

- RevisiÃ³n editorial humana/nativa de B1â€“C2, planificada para `v3.1` en
  commits `content:` separados.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.5.0] - 2026-11-09

Pulido profesional y paridad multilingÃ¼e (ADR-044â€¦ADR-049): grader oral
localizado para EN/FR/PT/JA, avance neutro al omitir ejercicios de voz,
tolerancia a palabras repetidas en tableros de tokens, resistencia de la
sesiÃ³n a rotaciÃ³n/proceso muerto, tipografÃ­a 100% Nunito y contraste AA.

### AÃ±adido

- Contrato de contenido para que ningÃºn `modelAnswer` marque fallo gramatical o
  de cobertura en los cuatro cursos.
- `LicensesSheet` en Ajustes (ADR-008) y confirmaciÃ³n al salir de una lecciÃ³n.
- `ActionRowCard3D` con semÃ¡ntica de botÃ³n y `LinguaTopBar` en pantallas
  secundarias.

### Corregido

- `VocabularyExtractor` normaliza frente ES â†’ reverso meta en todos los cursos,
  incluidos `MatchPairs` japoneses invertidos.
- `MainActivity` espera la primera lectura de settings y sincroniza las barras
  del sistema con el tema; perfil filtra el dominio por el curso activo.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.4.0] - 2026-11-08

C2 completo en `es-fr`, `es-pt` y `es-ja` (ADR-042): los tres cursos de
arranque cierran la rampa con 18 lecciones y 216 ejercicios cada uno.

### AÃ±adido

- **C2 en `es-fr`, `es-pt` y `es-ja`:** nivel `es-*-lv-c2` con cuatro unidades â€”
  registros y matices, dominios especializados, lengua culta y sÃ­ntesis y
  evaluaciÃ³n C2 (con monÃ³logo de examen y debate final).
- **GramÃ¡tica propia de cada idioma:** ironÃ­a seca y litote, registro jurÃ­dico
  y estilo nominal en francÃ©s; eufemismo, discurso indireto y jerga econÃ³mica
  en portuguÃ©s; keigo, æ•¬èªžã®ä¸€è²«ã—ãŸä½¿ç”¨, ã€œã‚‚ã®ã® y ã§ã‚ã‚‹èª¿ en japonÃ©s.
- **Contrato C2** para los tres cursos: 18 lecciones, 216 ejercicios, banda
  `12000+`, 12 ejercicios por lecciÃ³n, 2 focos gramaticales, `feedbackOnError`
  en todos los ejercicios y un oral libre por lecciÃ³n.

### Cambiado

- Cada curso de arranque pasa de 84 a **102 lecciones** y de 790 a **1.006
  ejercicios**. El repositorio cierra el plan con **417 lecciones y 4.087
  ejercicios**.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.3.0] - 2026-11-08

C2 completo en `es-en` (ADR-042): 18 lecciones y 216 ejercicios nuevos en el
nivel de maestrÃ­a, con doce ejercicios por lecciÃ³n y frases de hasta 48 tokens.

### AÃ±adido

- **C2 en `es-en`:** nivel `es-en-lv-5` con cuatro unidades â€” registros y
  matices (ironÃ­a seca, eufemismo, doble sentido, alusiones literarias),
  dominios especializados (derecho, ciencia, humanidades, economÃ­a), lengua
  culta (retÃ³rica, contraargumento, sintaxis, escritura evaluada) y sÃ­ntesis y
  evaluaciÃ³n (registro literario, metÃ¡foras culturales, especializaciÃ³n,
  monÃ³logo de examen y debate final).
- **Cada lecciÃ³n** incluye micro-clase, glosario, nota cultural original, dos
  focos gramaticales y un `FreeSpeakingPrompt` dimensionado para â‰¤10 s de voz.
- **Contrato C2** fijado en los tests: 18 lecciones, 216 ejercicios, banda
  `12000+`, 12 ejercicios por lecciÃ³n, `feedbackOnError` en todos los
  ejercicios y un oral libre por lecciÃ³n.

### Cambiado

- `es-en` pasa de 93 a **111 lecciones** y de 853 a **1.069 ejercicios**; el
  repositorio acumula **363 lecciones y 3.439 ejercicios**.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.2.0] - 2026-11-08

C1 completo en los tres cursos de arranque (ADR-042): francÃ©s, portuguÃ©s y
japonÃ©s reciben 24 lecciones y 264 ejercicios cada uno.

### AÃ±adido

- **C1 completo en `es-fr`, `es-pt` y `es-ja`:** nivel `es-*-lv-c1` con seis
  unidades de cuatro lecciones (consolidaciÃ³n, argumentaciÃ³n, trÃ¡mites,
  liderazgo, proyecto integrador y evaluaciÃ³n global). Cada lecciÃ³n incluye
  micro-clase, glosario curado, nota cultural y un `FreeSpeakingPrompt`.
- **Enfoques gramaticales propios de cada idioma:** en francÃ©s, concordancia
  de tiempos, subjuntivo en rÃ©gimen complejo, condicionales mixtos y registro
  soutenu; en portuguÃ©s, futuro composto, condicional misto, discurso indireto
  y colocaciones administrativas; en japonÃ©s, keigo, ã€œã‚‚ã®ã®/ã€œã«ã‚‚ã‹ã‹ã‚ã‚‰ãš,
  ã€œã¨ã®ã“ã¨ã§ã™, ã€œå ´åˆã«é™ã‚Š y registro ã§ã™ãƒ»ã¾ã™/ã§ã‚ã‚‹.
- **Contrato de calidad C1** para los tres cursos: 24 lecciones, 264
  ejercicios, banda `8000-12000`, 11 ejercicios por lecciÃ³n, 2 focos
  gramaticales, `feedbackOnError` en todos los ejercicios y un oral libre por
  lecciÃ³n.

### Cambiado

- Cada curso de arranque pasa de 60 a **84 lecciones** y de 526 a **790
  ejercicios**; el repositorio acumula **345 lecciones y 3.223 ejercicios**.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.1.0] - 2026-11-08

ExpansiÃ³n a C1 (ADR-042): infraestructura de niveles avanzados y piloto C1
completo en `es-en`, con el evaluador oral ya robustecido por el hotfix 2.0.1.

### AÃ±adido

- **Infraestructura C1/C2:** `CefrLevel` incorpora `C1` y `C2`, y
  `VocabFrequencyBand` las bandas `8000-12000` y `12000+`. El schema v2 y el
  validador amplÃ­an la regla de No-Aplanamiento a B1/B2/C1/C2 **sin**
  `schemaVersion: 3` (cambio aditivo; los cursos existentes siguen vÃ¡lidos).
- **Rampa C1/C2 verificada:** `ContentRampTest` fija los escalones (C1 11
  ejercicios/38 tokens; C2 12/48) y `validateContent` imprime un informe por
  curso con lecciones por CEFR, ejercicios, cobertura de feedback y frase mÃ¡s
  larga.
- **Etapas C1/C2 en el camino** (`PathStage`) con etiquetas y contraste AA,
  derivadas del CEFR real de cada nivel.
- **C1 completo en `es-en`:** nivel `es-en-lv-4` con 24 lecciones y 264
  ejercicios, organizados en seis unidades (consolidaciÃ³n, argumentaciÃ³n,
  trÃ¡mites, liderazgo, proyecto integrador y evaluaciÃ³n global). Cada lecciÃ³n
  incluye micro-clase, glosario curado, nota cultural y un `FreeSpeakingPrompt`;
  el curso acumula ahora **93 lecciones y 853 ejercicios**.

### Cambiado

- Los totales de contrato de `es-en` se actualizan junto con el contenido
  (69â†’93 lecciones, 589â†’853 ejercicios) y las lecciones C1 usan la banda lÃ©xica
  `8000-12000` con dos focos gramaticales.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.0.1] - 2026-11-08

Hotfix de voz (ADR-043): la evaluaciÃ³n oral ya no falla cuando la
pronunciaciÃ³n es correcta y la captura completa frases truncadas.

### Corregido

- **Captura de voz tolerante (`SpeechCapture`):** el endpoint prematuro de
  Vosk (pausas de ~0,3 s) ya no corta la grabaciÃ³n a mitad de frase: los
  enunciados que llegan antes de 1,5 s se concatenan y la captura continÃºa;
  tras un enunciado vÃ¡lido hay 1,2 s de gracia que se reinicia si llega otra
  frase. El tope de captura sube de 6 s a **10 s**.
- **EvaluaciÃ³n oral sin falsos negativos (`FreeSpeakingHeuristicGrader`):**
  transcripciones con tildes omitidas por Vosk ya pasan (normalizaciÃ³n NFD
  como la evaluaciÃ³n escrita), la tolerancia difusa aplica desde 4
  caracteres, la cadencia WPM admite un margen del Â±20 % y la autoevaluaciÃ³n
  baja es informativa (`advisories`) en lugar de bloquear el paso.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [2.0.0] - 2026-11-02

Release de contenido y experiencia: B2 completo en los cuatro cursos, cursos
de arranque migrados a schema v2, glosario por lecciÃ³n y accesibilidad AA.

### Cambiado

- **Cursos de arranque `es-pt`, `es-fr` y `es-ja` migrados a schema v2**
  (ADR-041) conservando sus ids histÃ³ricos: ahora declaran `cefrLevel: A1`,
  micro-clase, glosario curado y `feedbackOnError` especÃ­fico.
- **`es-pt`, `es-fr` y `es-ja` ampliados a 38 lecciones/306 ejercicios**
  cada uno: 10 A1, 8 A2 y **B1 completo** (20 lecciones), con pasado, planes,
  comparaciones, viajes, salud, trabajo, opiniones, trÃ¡mites, bienestar,
  cultura, ocio, ciudad y servicios, ademÃ¡s de kana y partÃ­culas en japonÃ©s.
- **`es-pt`, `es-fr` y `es-ja` inician B2** con la Unidad 1 de negociaciÃ³n
  y decisiones (4 lecciones/40 ejercicios cada uno): condicional, hipÃ³tesis,
  pasiva reportativa y estructuras cleft.
- **B2.2 de opiniÃ³n y debate** en los tres idiomas (4 lecciones/40 ejercicios
  cada uno): concesiÃ³n, atenuaciÃ³n de la crÃ­tica, ironÃ­a/registro y rebatir
  con evidencia.
- **B2.3 de narrativa y matices** en los tres idiomas (4 lecciones/40
  ejercicios cada uno): tiempos del relato, decisiones difÃ­ciles, discurso
  reportado y arrepentimiento.
- **B2.4 de sociedad y medios** en los tres idiomas (5 lecciones/50
  ejercicios cada uno): leer entre lÃ­neas, manipulaciÃ³n mediÃ¡tica, temas
  incÃ³modos, privacidad y propuestas de futuro.
- **B2.5 de proyectos y futuro** en los tres idiomas (5 lecciones/50
  ejercicios cada uno): presentar un proyecto de barrio, defender una causa,
  negociar con la administraciÃ³n, presentar un balance y cerrar un debate
  sobre el futuro. Con esto B2 queda completo (22 lecciones/220 ejercicios
  por curso).

### Corregido

- **EvaluaciÃ³n oral japonesa:** el heurÃ­stico ya no cuenta una frase japonesa
  entera como un solo token; tokeniza por caracteres y cubre los clÃºsteres
  conceptuales sin depender de espacios.
- **`es-en` ampliado y reorganizado** en A1 10 / A2 17 / B1 20 / B2 22 (69
  lecciones y 589 ejercicios), con la unidad A1 â€œVida cotidiana y entornoâ€,
  la unidad puente B1â†’B2 y `ContentRampTest`.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [1.2.1] - 2026-10-31

Parche centrado en la claridad pedagÃ³gica de la lecciÃ³n (ADR-036).

### Agregado

- **TraducciÃ³n al mantener pulsada una palabra** en opciones y fichas: nota
  emergente encima de la palabra, oculta al soltar y con acciÃ³n de
  accesibilidad â€œVer traducciÃ³nâ€.
- **Tabla final de resultados** con aciertos, errores y nota en porcentaje
  sobre el primer intento (2 errores de 10 â†’ 80 %).
- **Ventana de correcciÃ³n antes de terminar** para repetir solo los ejercicios
  fallados, sin XP ni vidas; la nota no cambia.
- Tests de `LessonScoring`, del brief temÃ¡tico y del emparejamiento inverso.

### Cambiado

- **â€œAntes de empezarâ€** explica el tema (gramÃ¡tica, objetivos y ejemplos) y
  ya no incluye mecÃ¡nicas de interfaz.
- Los **ejercicios de opciÃ³n** validan solo al pulsar â€œSiguienteâ€; tocar una
  opciÃ³n ya no revela acierto/error.
- **Formar frases** (`Translate`) usa una tira horizontal con scroll, igual
  que `OrderWords`.
- **Emparejamiento bidireccional:** se puede empezar por la columna izquierda
  o por la derecha.
- Glosario de apoyo derivado de `MatchPairs`, notas culturales y ejemplos de
  micro-clase (cobertura parcial hasta autorizar `glossary` en el contenido).

### Corregido

- ValidaciÃ³n inmediata al seleccionar una palabra.
- DisposiciÃ³n vertical al construir frases.
- SelecciÃ³n limitada a la columna izquierda en el emparejamiento.
- Falta de mediciÃ³n porcentual y de correcciÃ³n de errores al finalizar.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [1.2.0] - 2026-10-31

VersiÃ³n centrada en la experiencia visual, sonora e interactiva ("Feather
2.0"): profundidad tÃ¡ctil, Lumi expresiva, lecciÃ³n viva, repaso 3D y pantallas
de perfil y onboarding renovadas.

### Agregado

- **FÃ­sica tÃ¡ctil 3D** (ADR-031): hundimiento sobre el borde inferior, bisel de
  luz y `TokenChip` compartido; SFX cÃ¡lidos v2 (22,05 kHz) y nuevos cues
  `STREAK_COMBO`, `CARD_FLIP`, `HEART_LOST` y `CHEST_OPEN`.
- **Lumi multicapa y expresiva** (ADR-032) con moods `IDLE`, `HAPPY`,
  `ENCOURAGING` y `LISTENING`; `PathStage` A1â€“B2, banners de unidad 3D y punto
  animado en el conector del camino.
- **LecciÃ³n viva** (ADR-033): panel de feedback deslizante con Lumi, combo de
  aciertos, ondas de audio TTS/STT y animaciones de fichas.
- **Repaso 3D** (ADR-034): `FlipCard3D`, pronunciaciÃ³n TTS y botonera FSRS
  semÃ¡ntica (coral, Ã¡mbar, turquesa y pÃºrpura).
- **Perfil Bento y onboarding ilustrado** (ADR-035): anillo de dominio, nivel,
  estadÃ­sticas 3D, racha con mejor marca, insignias por idioma y transiciones
  de navegaciÃ³n.
- Iconos originales `ic_star`/`ic_cards`; tests de combo, etapas, tokens,
  hÃ¡ptica, perfil y accesibilidad de la tarjeta 3D.

### Cambiado

- `PrimaryButton3D` acepta colores semÃ¡nticos y variante compacta.
- `ReviewViewModel` inyecta `CourseRepository` y `TtsEngine` para pronunciar
  la traducciÃ³n; `feature-review` depende de `core-audio`.
- `ProfileUiState` gana nivel y avance de XP; `LessonUiState` gana combo y
  estado de reproducciÃ³n.
- DocumentaciÃ³n: ADR-031 a ADR-035 y `docs/DESIGN_SYSTEM.md`.

### Notas

- Todo sigue siendo 100% local y offline; sin dependencias nuevas ni cambios
  de esquema. Reduced motion y TalkBack se mantienen (I1/I5/I8).

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [1.1.0] - 2026-10-25

VersiÃ³n centrada en la experiencia de aprendizaje: feedback sensorial,
micro-clases de gramÃ¡tica, explicaciÃ³n de aciertos, reintentos y tableros
manipulativos.

### Agregado

- **Feedback sensorial global** (ADR-027): SFX originales para selecciÃ³n,
  encaje, acierto, error y completado; hÃ¡ptica con `VibrationEffect`; ajustes
  independientes de sonido y vibraciÃ³n.
- **Micro-clases de gramÃ¡tica y reglas** (ADR-028) antes de los ejercicios,
  con objetivos, ejemplos, mecÃ¡nicas y botÃ³n â€œGuÃ­aâ€ para reabrir. Si la
  lecciÃ³n no trae `microLesson`, se genera desde sus metadatos.
- **Feedback de acierto**: `feedbackOnSuccess` opcional y explicaciÃ³n de
  respaldo con la respuesta correcta visible; opciones correcta/incorrecta
  marcadas con color e icono.
- **Reintentos pedagÃ³gicos** (ADR-030) para escucha, dictado y voz: un
  reintento sin consumir otra vida, XP mÃ¡ximo una vez y respuesta oculta
  hasta agotar el reintento o pedir â€œVer respuestaâ€.
- **Audio repetible y a 0.75x** para escucha, dictado y repeticiÃ³n oral.
- **OrderWords horizontal**: frase en tira con scroll y banco de palabras que
  envuelve, sin desbordes.
- **Emparejamiento tipo rompecabezas** (ADR-029) con piezas de pestaÃ±a/ranura
  del mismo color, toque o arrastre, separaciÃ³n de parejas y correcciÃ³n
  visual al comprobar.
- Tests de `AnswerKey`, `RetryPolicy`, micro-clases, schema/validador y
  tablero puzzle.

### Cambiado

- Schema v2 aditivo: `microLesson` y `feedbackOnSuccess`, validados en build
  y en runtime sin cambiar `schemaVersion`.
- Las respuestas se bloquean al enviarse y tras el feedback; se evita el
  doble envÃ­o y el doble XP.
- Gate F4 y baseline profile cierran la micro-clase y reconocen los nuevos
  tableros por etiquetas estables.
- README, design system y ADRs 027â€“030 actualizados.

### Corregido

- ReenvÃ­o de respuestas en opciones y dictado tras el feedback.
- Revelado prematuro de la respuesta correcta en ejercicios reintentables.
- Desborde horizontal del banco de palabras.
- DetecciÃ³n del gate para `SemanticMatch`/`MatchPairs` con el tablero puzzle.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [1.0.0] â€” 2026-10-25

Primera versiÃ³n final. RediseÃ±o UI/UX "Feather" y camino de aprendizaje
guiado (ADR-026), con vidas no bloqueantes y XP diferido (ADR-025).

### Agregado

- **Camino de aprendizaje lineal** en Home: zigzag con nodos flotantes,
  cabeceras de unidad, conectores curvos y cofres al completar cada unidad.
  Activo en release con `LEARNING_PATH_V2`.
- **Vidas (10) con regeneraciÃ³n rÃ¡pida** (1/min): el fallo cuesta una vida;
  a 0 la lecciÃ³n se cancela sin guardar XP ni progreso, y el acceso nunca
  queda bloqueado de forma permanente.
- **HUD del alumno**: racha, corazones, nivel visual y barra de XP lÃ­quida.
- **Recompensas**: hoja de victoria con contador de XP animado, confeti y
  cofre de unidad; hoja de lecciÃ³n cancelada con salida inmediata.
- **Lumi**, mascota original (colibrÃ­ vectorial), junto al nodo activo y en
  la victoria.
- **Repaso FSRS**: `Otra vez` consume una vida, sin bloquear la sesiÃ³n.
- **Baseline profile regenerado** con el journey del Camino (Cold Start â†’
  Home â†’ primer nodo â†’ MultipleChoice â†’ Repaso FSRS).
- **Release Gate F4** adaptado al camino y a las vidas: conduce las 5
  primeras lecciones A1, presupuesto de 2 fallos por lecciÃ³n y navegaciÃ³n
  por nodos; corrida completa de 20 min PASS (`failed=[]`).

### Cambiado

- **XP diferido**: cada respuesta solo califica; el XP se persiste una vez
  al completar la lecciÃ³n (ADR-025).
- Room **v4** (`hearts_progress`) con migraciÃ³n 3â†’4 y test instrumentado.
- `SyncSnapshot` **v2** con vidas y compatibilidad con snapshots v1.
- README: "sin vidas" pasa a "vidas con recuperaciÃ³n rÃ¡pida" (I3
  reinterpretado en ADR-025).

### Corregido

- **Doble conteo de XP** (suma por respuesta + al completar).
- Contraste del conector del camino en modo oscuro.
- Icono de cofre ilegible a tamaÃ±o grande (rediseÃ±ado con cerradura).
- NavegaciÃ³n del Release Gate con la UI del camino.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [0.2.0] â€” 2026-10-17

Fases 4.2 â†’ 7: seguridad, release, repaso espaciado, accesibilidad y pulido.

### Agregado

- **Cifrado AES-GCM-256 con Android Keystore** (ADR-014): el snapshot se
  cifra antes de subirlo y los tokens OAuth en reposo; migraciÃ³n automÃ¡tica
  de tokens en claro.
- **Release config**: R8 + resource shrinker con keeps de JNA/Vosk,
  sherpa-onnx y kotlinx-serialization; firma por gradle properties;
  `assembleRelease` en CI.
- **Repaso espaciado FSRS-4.5** (ADR-016): scheduler puro en dominio,
  extracciÃ³n de vocabulario (`MatchPairs`/`KanaChoose`/`KanjiReading`),
  tabla Room `word_progress` (DB v2 con migraciÃ³n 1â†’2), pantalla de repaso
  con 4 ratings, entrada desde Home ("Repasar ahora Â· N").
- **Accesibilidad I8**: roles TalkBack (Button/RadioButton), live region del
  feedback, `ProgressBarRangeInfo`, racha con descripciÃ³n, touch targets
  â‰¥48 dp en tokens/tiles, soporte de `fontScale` 2x.
- **Tests instrumentados** del design system (7) + job de CI en emulador
  API 34 (`:core:core-ui:connectedDebugAndroidTest`).
- **EstadÃ­sticas de Progreso**: XP acumulados, lecciones `x de y`, tarjetas
  de repaso y pendientes de hoy.
- DocumentaciÃ³n completa: `ARCHITECTURE.md` actualizado, `CONTENT_FORMAT.md`,
  `SECURITY.md`, `CODE_OF_CONDUCT.md`, `CHANGELOG.md`, `AGENTS.md`,
  `llms.txt`, plantillas de issues y de PR.

### Corregido

- Crash en release al abrir una lecciÃ³n: `ConcurrentHashMap.getOrPut` con
  valor `null` (`putIfAbsent(key, null)` â†’ NPE) en los motores STT y TTS;
  ahora con patrÃ³n null-safe y logs de diagnÃ³stico.
- R8 rompÃ­a el STT: faltaban reglas de **JNA**
  (`UnsatisfiedLinkError: com.sun.jna.Pointer`).
- Mojibake y caracteres perdidos (U+FFFD) en el contenido: acentos de los
  4 manifests restaurados, "sÅ“ur" recuperada y **curso japonÃ©s reescrito**
  con kana/kanji reales (ã‚/ã„/ã, å±±/æ°´, partÃ­culas).
- `Espresso 3.7.0` para compatibilidad con APIs nuevas
  (`InputManager.getInstance` eliminado en API 36+).
- Estado obsoleto en SincronizaciÃ³n al volver de Cuenta; tarjeta del
  proveedor de laboratorio no aparecÃ­a (semÃ¡foro reactivo).

### Cambiado

- VersiÃ³n `0.2.0` (versionCode 2).
- El repaso no otorga XP en v1 (decisiÃ³n documentada en ADR-016).
- El estado FSRS (`word_progress`) queda fuera del snapshot de sync v1.

## [3.2.0] - 2026-11-15

Estable tras la revisiÃ³n nativa de F0/F1. Mismo contenido que 3.2.0-rc1.

## [0.1.0-alpha] â€” 2026-10-16

Fases 0 â†’ 4.1: base del proyecto.

### Agregado

- Repositorio, convention plugins de Gradle (`build-logic`), catÃ¡logo de
  versiones y CI (ktlint + detekt + tests).
- `core-domain` puro (lecciones, graders, racha, FSRS como contrato),
  `core-data` (Room + DataStore), `core-ui` (design system), `core-content`
  (loader + validador) y 4 cursos ESâ†’EN/PT/FR/JA como assets.
- Shell de la app con navegaciÃ³n, onboarding, Home con ruta de aprendizaje,
  motor de lecciÃ³n (FSM), XP (+10 por acierto) y racha diaria.
- **Audio offline**: TTS del sistema con fallback por idioma, TTS **Piper**
  (sherpa-onnx) con verificaciÃ³n SHA-256, STT **Vosk** y ejercicio
  SpeakRepeat con degradaciÃ³n limpia.
- **Contenido v1**: JSON Schema formal (`docs/schemas/course-v1.json`),
  tarea `validateContent` en CI.
- **Sync Fase 3**: snapshot v1 JSON, merge monÃ³tono (nunca borra progreso),
  `SyncScreen` y `NoopCloudProvider` (ADR-013).
- **Auth Fase 4.1**: OAuth 2.0 + PKCE con Google Drive `appDataFolder`
  (ADR-011), `AuthScreen` real, `OAuthCallbackActivity` con redirect custom
  y proveedor de laboratorio para debug.

[0.2.0]: https://github.com/MREmmanuel09/LinguaGo/compare/v0.1.0-alpha...v0.2.0
[0.1.0-alpha]: https://github.com/MREmmanuel09/LinguaGo/releases/tag/v0.1.0-alpha
