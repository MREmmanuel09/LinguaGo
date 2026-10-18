# ROADMAP_V2.md — Contrato Arquitectónico hacia LinguaGo v2.0 (Escala B2+ Offline)

> **Estado:** Propuesto (Contrato de Ejecución para Orquestación de Agentes IA)  
> **Versión Base Inspeccionada:** `v0.2.0` (`versionCode = 2`, 21 módulos Gradle, Room DB v2, Schema JSON v1)  
> **Invariantes Innegociables:** `I1` (100% Offline) · `I2` (Sin backend) · `I3` (Sin fricción) · `I4` (Progreso intocable) · `I5` (Identidad original) · `I6` (FOSS MIT/CC BY-SA 4.0) · `I7` (Testabilidad JVM) · `I8` (Accesibilidad TalkBack/48dp/2x) · `I9` (Validación estática en CI)

---

## 0. Reconocimiento del Workspace y Línea Base (BASELINE v0.2.0)

### 0.1 Auditoría de Artefactos y Modelos en Disco (Medición Real)
Medición ejecutada sobre `app/build/outputs/apk/` y `models/`:

| Artefacto / Directorio | Ruta en Workspace | Peso Medido | Observación Arquitectónica |
|---|---|---|---|
| **APK Debug (Monolítico)** | `app/build/outputs/apk/debug/app-debug.apk` | **528.28 MB** (`553,937,565 B`) | Empaqueta los 4 idiomas (`models/`) sin filtro por curso |
| **APK Release (Monolítico R8)** | `app/build/outputs/apk/release/app-release.apk` | **508.36 MB** (`533,057,785 B`) | Supera el límite razonable de F-Droid; exige split por Flavor |
| **Total `models/`** | `models/` (`tts/` + `stt/`) | **510.55 MB** | `stt/` = **278.27 MB** · `tts/` = **232.26 MB** |
| **TTS Inglés (Piper ONNX)** | `models/tts/en_US-amy-medium` | **77.44 MB** | `sherpa-onnx` VITS + `espeak-ng-data` |
| **TTS Francés (Piper ONNX)** | `models/tts/fr_FR-siwis-medium` | **77.39 MB** | `sherpa-onnx` VITS + `espeak-ng-data` |
| **TTS Portugués (Piper ONNX)** | `models/tts/pt_BR-faber-medium` | **77.44 MB** | `sherpa-onnx` VITS + `espeak-ng-data` |
| **STT Inglés (Vosk Small)** | `models/stt/vosk-model-small-en-us-0.15` | **67.61 MB** | Kaldi/Vosk vía JNA (`org.vosk.Model`) |
| **STT Francés (Vosk Small)** | `models/stt/vosk-model-small-fr-0.22` | **65.31 MB** | Kaldi/Vosk vía JNA |
| **STT Japonés (Vosk Small)** | `models/stt/vosk-model-small-ja-0.22` | **94.27 MB** | Kaldi/Vosk vía JNA (`ja` usa `SystemTtsEngine` para TTS) |
| **STT Portugués (Vosk Small)** | `models/stt/vosk-model-small-pt-0.3` | **51.07 MB** | Kaldi/Vosk vía JNA |

### 0.2 Diagnóstico Técnico de Módulos Actuales (`@app`, `@core`, `@feature`)
1. **`core-domain` (`Course.kt`, `Exercise.kt`, `ExerciseGrader.kt`)**: Jerarquía inmutable serializada directamente (`ADR-009`). Soporta 10 tipos de ejercicio orientados a A1/A2. Carece de metadatos pedagógicos CEFR por lección (`scenario`, `canDo`, `grammarPoints`, `vocabFrequencyBand`, `feedbackOnError`) y de mecánicas de producción libre (`FreeSpeakingPrompt`) o emparejamiento semántico/pragmático.
2. **`core-content` (`CourseJson.kt`, `CourseValidator.kt`) + `build.gradle.kts` (`validateContent`)**: Valida únicamente contra `docs/schemas/course-v1.json` (`"schemaVersion": { "const": 1 }`) con `ignoreUnknownKeys = false`.
3. **`core-audio` (`PiperTtsEngine.kt`, `VoskSttEngine.kt`)**: **Cuello de botella detectado**: `LessonViewModel.startLesson()` invoca `ttsEngine.isReadyFor()` y `sttEngine.isReadyFor()` de forma síncrona al abrir la lección; por dentro, `locateVoice()` y `locateModel()` extraen árboles de `assets` a `filesDir`, calculan hashes SHA-256 de hasta 94 MB e instancian `org.vosk.Model(path)` bloqueando el hilo llamante. Además, `SttEngine.transcribe()` es una suspensión opaca de 6 segundos que no expone `partialResult` ni RMS a la UI.
4. **`core-data` (`LinguagoDatabase.kt`, `Entities.kt`, `DataModule.kt`)**: Room `version = 2` (`StreakProgressEntity`, `LessonProgressEntity`, `WordProgressEntity` con `MIGRATION_1_2`). Esquemas exportados en `core-data/schemas/.../1.json` y `2.json`.
5. **`feature-lesson` (`LessonScreen.kt`, `LessonViewModel.kt`)**: Renderizador declarativo sin `HapticFeedback`, sin animaciones físicas (`spring`) entre ejercicios, `TokenBoard` basado solo en tap simple, y `FeedbackBanner` con string plano estático (`"Revisa la respuesta y sigue"`).

---

## 1. Capa Estratégica (ADR Introductorio v2.0)

- **Contexto y Objetivo:** Escalar LinguaGo de un MVP A1/A2 de 508.36 MB a una plataforma con profundidad real **CEFR B2+** en el curso `es-en`, manteniendo los cursos `es-pt`, `es-fr` y `es-ja` en paridad estable v1 (17 lecciones A1/A2) y reduciendo el peso de distribución a **100 MB – 170 MB por APK** mediante Product Flavors por idioma (`en`, `pt`, `fr`, `ja`) para F-Droid y GitHub Releases.
- **Permiso de Imposibilidad (Evaluación de Producción Oral Libre B2 vs. Invariante I1 Offline sin LLMs):**
  - *Conflicto:* Evaluar semántica abierta y pragmática libre en nivel B2 (`FreeSpeakingPrompt`) es teóricamente incompatible con un entorno 100% offline sin LLMs on-device (vetados por consumo de >1.5 GB RAM/almacenamiento y drenaje térmico) ni APIs cloud (vetadas por `I1`/`I2`).
  - *Opción A (Descartada):* Embeber un SLM cuantizado (ej. Qwen/Llama 1B GGUF vía JNI). Rompe el presupuesto de APK (<170 MB), dispara OOMs en dispositivos de 3–4 GB RAM y degrada la batería.
  - *Opción B (Elegida — Heurística Acústico-Léxica Determinista + Autoevaluación Guiada):* Combinar el grafo de transcripción de `VoskSttEngine` con un motor determinista en `core-domain` (`FreeSpeakingHeuristicGrader`) que evalúa 4 señales objetivas: (1) longitud mínima de tokens producidos, (2) cobertura de `conceptClusters` (sinónimos/palabras clave esperadas), (3) detección por expresiones regulares/patrones de tokens de los `grammarPoints` exigidos (ej. condicionales, conectores concesivos), y (4) cadencia/fluidez en palabras por minuto (`WPM`) libre de silencios largos; complementado con una rúbrica obligatoria de **autoevaluación del estudiante** (comparación contra respuesta modelo en audio/texto) antes de registrar el resultado.
  - *Trade-off asumido:* El motor puede aceptar una frase gramaticalmente correcta con keywords válidas aunque tenga una incoherencia pragmática sutil (falso positivo semántico ~10–15%), lo cual se mitiga pedagógicamente mediante la autoevaluación reflexiva con respuesta canónica audible.
- **Trade-offs Globales v2.0:**
  1. *Flavors por Idioma vs. APK Único:* Se sacrifica el cambio instantáneo de idioma con voz neural dentro de un único APK de tienda a cambio de reducir el APK de **508.36 MB a ~135–160 MB** por flavor (`linguago-en-release.apk`, etc.), permitiendo indexación limpia en F-Droid sin Play Feature Delivery. En caso de cambiar a un segundo curso desde Ajustes, el curso de texto funciona al 100% (JSON liviano incluido) y el audio degrada limpiamente a `SystemTtsEngine` (`I3`).
  2. *Coexistencia Dual Schema v1/v2:* `core-content` soportará ambos contratos en paralelo para no forzar una reescritura artificial de `es-pt`, `es-fr` y `es-ja` sin revisores nativos B2.
  3. *Inmutabilidad UI en Mecánicas Avanzadas:* El Drag & Drop B2 operará estrictamente sobre listas inmutables e índices canónicos en `LessonUiState`, prohibiendo mutaciones directas en estados locales de Compose que desincronicen TalkBack (`I8`).
- **Estimación Global de Esfuerzo:** **4.5 a 6 meses a tiempo parcial** (~12–15 h/semana), distribuidos en 5 fases secuenciales bloqueadas por la Regla de Gravedad: Fase 0 (3 sem), Fase 1 (4 sem), Fase 2 (4 sem), Fase 3 (6–8 sem de ingeniería curricular B2 + validación), Fase 4 (3 sem de QA físico y perfilado nativo).

---

## 2. Capa Táctica (Fases Secuenciales Ejecutables)

> **Regla de Gravedad:** Fase $N+1$ no puede iniciar hasta que el `[CHECKPOINT HUMANO]` y todos los DoD locales de la Fase $N$ estén en verde (`./gradlew ktlintCheck detekt validateContent test`).

### Fase 0: Contrato de Datos (Schema v2), Retrocompatibilidad, Room v3 y Flavors de Distribución
*Objetivo: Cimentar el contrato formal JSON Schema v2, el validador dual v1/v2, el blindaje de migración de progreso Room v2→v3 y el particionamiento del APK (<170 MB) antes de tocar una sola línea de UI o contenido B2.*

- [ ] **F0.1 — Redacción de `docs/schemas/course-v2.json` y actualización de `CONTENT_FORMAT.md`** | `[DeepSeek V4.1 Flash]` | N/A (Estándar para contratos JSON Schema y documentación técnica) | `docs/schemas/course-v2.json` válido en draft-07 con `schemaVersion: 2`, soporte de `locale` opcional/BCP-47 (`en-US`, `en-GB`, `pt-BR`, `pt-PT`), metadatos obligatorios por lección cuando `cefrLevel` ∈ `{B1, B2}` (`cefrLevel`, `scenario`, `canDo`, `grammarPoints[]`, `vocabFrequencyBand`, `culturalNotes[]`), `feedbackOnError` estructurado por ejercicio, y nuevas variantes `AudioOnlyPrompt`, `SemanticMatch`, `ClozeDragDrop` y `FreeSpeakingPrompt`; 0 errores de sintaxis JSON | `[M]`
- [ ] **F0.2 — Evolución de modelos en `core-domain` (`Course.kt`, `Exercise.kt`, `Language.kt`) con compatibilidad v1/v2** | `[DeepSeek V4.1 Flash]` | N/A | Modelos `@Serializable` actualizados con valores por defecto seguros para cursos v1 (`cefrLevel = CefrLevel.A1`, `locale = null`, `feedbackOnError = null`); nuevos subtipos sellados en `Exercise`; `./gradlew :core:core-domain:test` pasa al 100% con 8 tests unitarios nuevos de serialización polimórfica | `[M]`
- [ ] **F0.3 — Refactor del Validador Gradle (`validateContent` en `build.gradle.kts`) y `CourseValidator.kt` (`core-content`)** | `[DeepSeek V4.1 Flash]` | N/A | `validateContent` inspecciona `schemaVersion` de cada `course.json` y enruta automáticamente a `course-v1.json` (`es-pt`, `es-fr`, `es-ja`) o `course-v2.json` (`es-en`), materializando `List<File>` para respetar el Configuration Cache de Gradle; `CourseValidator.kt` aplica la **Regla de No-Aplanamiento Pedagógico** en lecciones `B1`/`B2` (falla si falta `scenario`, `canDo`, `grammarPoints`, `feedbackOnError`, `<1 FreeSpeakingPrompt` o `<1 culturalNote/idiom`); +15 tests unitarios en `CourseValidatorTest.kt` | `[M]`
- [ ] **F0.4 — Evolución de Persistencia Room v2 → v3 (`LinguagoDatabase.kt`, `Entities.kt`, `schemas/3.json`)** | `[DeepSeek V4.1 Flash]` | N/A | Subida de `LinguagoDatabase` a `version = 3` (soporte para métricas locales de producción oral y registro de errores frecuentes en `lesson_progress` / `word_progress` sin romper `SnapshotMerge` monótono `I4`); `MIGRATION_2_3` explícita en `DataModule.kt`; exportación verificada de `core-data/schemas/dev.linguago.core.data.database.LinguagoDatabase/3.json` | `[S]`
- [ ] **F0.5 — Test Instrumentado Bloqueante de Preservación de Progreso (v0.2.0 → v2.0)** | `[DeepSeek V4.1 Flash]` | N/A | Nuevo `Migration2To3Test.kt` en `core/core-data/src/androidTest` usando `MigrationTestHelper`: crea DB en v2, inserta exactamente **8/17 lecciones completadas**, **3 tarjetas FSRS en estado `learning`** (`reviewCount = 1`, `stability < 2.5f`) y **racha de 5 días** (`current = 5`), ejecuta `MIGRATION_1_2` + `MIGRATION_2_3`, y verifica igualdad bit-a-bit del estado tras el upgrade; 100% pass en emulador API 34/37 | `[M]`
- [ ] **F0.6 — Arquitectura de Product Flavors por Idioma y ABI Splits en `app/build.gradle.kts` y `build.gradle.kts`** | `[DeepSeek V4 Pro]` | Configurar sourceSets dinámicos de `models/tts/<lang>` y `models/stt/<lang>` por flavor (`en`, `pt`, `fr`, `ja`, `full`) sin romper el Configuration Cache de AGP 9.4.1 ni el grafo de Hilt requiere resolución cuidadosa de variantes | Flavors `en`, `pt`, `fr`, `ja` compilando con `./gradlew :app:assembleEnRelease`; APK resultante de cada flavor (arm64-v8a / universal por idioma) medido individualmente. Sin límite duro por flavor; el total de los 4 flavors debe caber bajo 2 GB (límite de GitHub Releases) — holgura esperada ~700-800 MB total. Monitorizar pero no bloquear release por ±5 MB de diferencia; verificación SHA-256 intacta en `ModelIntegrityTest` | `[L]`

> 🛑 **[CHECKPOINT HUMANO — FIN DE FASE 0]**
> 1. Verificar que `./gradlew ktlintCheck detekt validateContent test` pasa con `es-pt`, `es-fr`, `es-ja` en v1 y un fixture de prueba en v2.
> 2. Ejecutar `./gradlew :core:core-data:connectedDebugAndroidTest` y comprobar que el test de usuario v0.2.0 (8/17 lecciones, 3 FSRS learning, racha 5 días) pasa en verde.
> 3. Verificar en `app/build/outputs/apk/en/release/` que el APK pesa entre 100 MB y 170 MB.

---

### Fase 1: Motores de Dominio B2+, Evaluación Heurística Offline y Concurrencia Audio (JNA/ONNX)
*Objetivo: Implementar la lógica pura de calificación B2+ (incluyendo `FreeSpeakingPrompt` sin LLMs) y erradicar el bloqueo del hilo principal en la inicialización de Vosk (JNA) y Piper (sherpa-onnx).*

- [ ] **F1.1 — Implementación de `FreeSpeakingHeuristicGrader` en `core-domain`** | `[DeepSeek V4 Pro]` | Diseño algorítmico puro multi-señal (cobertura de `conceptClusters` con distancia de edición acotada para homófonos de Vosk, matching de patrones sintácticos de `grammarPoints`, cálculo de WPM sobre ventana temporal de voz activa y combinación con rúbrica de autoevaluación) evitando falsos bloqueos (`I3`) | Clase pura en `core-domain` sin dependencias de Android; evalúa `SpokenFreeResponse(transcript, durationMs, selfAssessmentScore)` contra `Exercise.FreeSpeakingPrompt`; ≥25 tests unitarios parametrizados en `ExerciseGraderTest.kt` cubriendo casos borde (silencio, muletillas, paráfrasis B2 válidas, omisión de gramática objetivo); ejecución de suite `<150 ms` | `[L]`
- [ ] **F1.2 — Extensión de `ExerciseGrader` y `VocabularyExtractor` para mecánicas B2+ (`SemanticMatch`, `ClozeDragDrop`, `AudioOnlyPrompt`)** | `[DeepSeek V4.1 Flash]` | N/A | `ExerciseGrader.kt` devuelve `GradingResult(isCorrect, matchedErrorRuleId, diagnosticFeedback)` permitiendo seleccionar el `feedbackOnError` específico de la opción/token fallado; `VocabularyExtractor.kt` extrae modismos y colocaciones B2 con su nota cultural hacia `word_progress`; +18 tests unitarios en `core-domain` | `[M]`
- [ ] **F1.3 — Refactor Asíncrono y Warmup No-Bloqueante en `PiperTtsEngine` y `VoskSttEngine` (`core-audio`)** | `[DeepSeek V4 Pro]` | Concurrencia crítica sobre JNA (`org.vosk.Model`) y JNI (`com.k2fsa.sherpa.onnx.OfflineTts`): requiere `Mutex` por idioma, extracción atómica a directorio temporal + `renameTo` para evitar corrupción si el proceso muere a mitad de copia, y máquina de estados `StateFlow<AudioEngineState>` sin bloquear `Main` | `isReadyFor()` deja de hacer I/O en el hilo principal (tiempo de llamada en `Main` `<1 ms`); la extracción de assets, verificación SHA-256 e instanciación nativa ocurren en `Dispatchers.IO` pre-calentados al abrir el curso; 0 bloqueos detectables con `StrictMode.detectDiskReads()` en `MainActivity` | `[L]`
- [ ] **F1.4 — Streaming de `partialResult` y Energía Acústica (RMS dB) en `VoskSttEngine`** | `[DeepSeek V4 Pro]` | Manejo del buffer PCM 16 kHz mono en tiempo real dentro del bucle de `AudioRecord`, cálculo de amplitud RMS sin asignaciones en el heap por frame (0 GC churn) y emisión de `Flow<SpeechCaptureUpdate>` con cancelación cooperativa segura antes de `recognizer.close()` | `SttEngine` expone `observeTranscription(language): Flow<SpeechCaptureUpdate>` emitiendo amplitud RMS cada ~50 ms y tokens parciales de Vosk en vivo; cierre garantizado de `AudioRecord` y `Recognizer` en bloque `finally` verificado con 10 tests unitarios con fakes de stream de audio | `[M]`

> 🛑 **[CHECKPOINT HUMANO — FIN DE FASE 1]**
> 1. Auditar las pruebas de `FreeSpeakingHeuristicGrader` y validar que la rúbrica heurística + autoevaluación cumple el estándar pedagógico CEFR B2 sin requerir red ni LLMs.
> 2. Verificar en emulador con `StrictMode` activado en debug que entrar a `LessonScreen` produce **0 lecturas de disco en el Main Thread**.

---

### Fase 2: Capa Sensorial UI y Componentes Gamificados B2+ (Jetpack Compose)
*Objetivo: Elevar la experiencia táctil, auditiva y visual en `core-ui` y `feature-lesson` manteniendo 60 FPS constantes, inmutabilidad UDF y accesibilidad TalkBack (`I8`).*

- [ ] **F2.1 — Sistema de `HapticFeedback` Semántico y Estados de Carga Resilientes en `core-ui` y `feature-lesson`** | `[Claude Haiku 5.5]` | N/A (Obrero UI/Compose por defecto) | Wrapper `LinguaHaptics` sobre `LocalHapticFeedback` / `View.performHapticFeedback` disparado en eventos de `LessonUiState` (confirmación en acierto, doble pulso en error, tick ligero al acoplar token/pareja); banner no intrusivo de "Preparando motor de voz offline…" cuando `AudioEngineState == WarmingUp` sin bloquear el avance de la lección (`I3`) | `[S]`
- [ ] **F2.2 — Transiciones Físicas (`spring`) entre Pasos de Lección y Animación de `FeedbackBanner` Estructurado** | `[Claude Haiku 5.5]` | N/A | Transición entre ejercicios en `LessonScreen` mediante `AnimatedContent` con `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)`; `FeedbackBanner` expandible que muestra el `feedbackOnError` específico del error cometido y la `culturalNote` / modismo cuando aplica; respeta `ANIMATOR_duration_scale` del sistema | `[M]`
- [x] **F2.3 — Componente `ClozeDragDropBoard` Inmutable con Física de Reordenamiento y Accesibilidad Dual** | `[Gemini 4 Argon]` | **Justificación Argon:** La coordinación de gestos `pointerInput(detectDragGestures)` multi-hueco sobre un párrafo B2 con layout fluido (`FlowRow`), colisión geométrica (`LayoutCoordinates`), física de retorno elástico (`Animatable` 2D) sin mutar estado fuera de `StateFlow` y manteniendo equivalencia 1:1 por tap para TalkBack (`I8`) y `fontScale = 2.0f` es el mayor riesgo de regresiones de UI/recomposiciones infinitas | Componente en `core-ui` / `feature-lesson` con 0 recomposiciones innecesarias durante el arrastre (lectura de offsets en fase de `graphicsLayer` / layout); modo alternativo por toque activado automáticamente con TalkBack; 0 frames perdidos (>16.6 ms) en Macrobenchmark local + test instrumentado en `DesignSystemAccessibilityTest.kt` | `[L]`
- [x] **F2.4 — UI de `SemanticMatchBoard` y `AudioOnlyPromptPanel` (Velocidad Normal / Lenta 0.75x)** | `[Claude Haiku 5.5]` | N/A | `SemanticMatchBoard` soporta tarjetas multilínea para matices de registro/modismos B2 con `heightIn(min = 48.dp)` y soporte `fontScale = 2.0f`; `AudioOnlyPromptPanel` oculta toda transcripción previa a la respuesta y añade botón de reproducción lenta (`speed = 0.75f` en `PiperTtsEngine`); +6 tests instrumentados de UI/accesibilidad | `[M]`
- [x] **F2.5 — UI de `FreeSpeakingPanel` con Visualizador de Onda en Tiempo Real y Tarjeta de Autoevaluación B2** | `[Claude Haiku 5.5]` | N/A | `Canvas` reactivo al `Flow` de RMS de `VoskSttEngine` dibujando barras de energía vocal a 60 FPS; visualización de palabras clave detectadas en vivo; paso post-grabación con reproducción de respuesta modelo (Piper TTS), checklist de autoevaluación de 3 niveles y desglose heurístico (cobertura léxica, gramática detectada y WPM) | `[M]`

> 🛑 **[CHECKPOINT HUMANO — FIN DE FASE 2]**
> 1. Probar manualmente en dispositivo/emulador la fluidez del Drag & Drop, el feedback háptico, el modo de audio puro (1.0x / 0.75x) y el flujo completo de `FreeSpeakingPrompt`.
> 2. Ejecutar `./gradlew :core:core-ui:connectedDebugAndroidTest` con `fontScale = 2.0f` y TalkBack simulado: **0 fallos de accesibilidad (`I8`)**.

---

### Fase 3: Pipeline de Contenido Pedagógico (Scope Restringido: `es-en` a B2 Profundo)
*Objetivo: Escalar exclusivamente el curso `es-en` a una arquitectura curricular CEFR A1→B2 profunda y rigurosa bajo la Regla de No-Aplanamiento Pedagógico, manteniendo `es-pt`, `es-fr` y `es-ja` intactos en v1.*

- [x] **F3.1 — Migración del Contenido Base `es-en` (Lecciones 1–17, Niveles A1/A2) a `schemaVersion: 2`** | `[DeepSeek V4.1 Flash]` | N/A (Ideal para transformación estructurada de JSON y enriquecimiento de `feedbackOnError`) | Las 17 lecciones existentes de `content-es-en/src/main/assets/courses/es-en/course.json` migran a `schemaVersion: 2` preservando el 100% de sus `lessonId` y `exerciseId` históricos (garantizando compatibilidad con `lesson_progress` en Room `I4`); codificación UTF-8 sin BOM verificada; `./gradlew validateContent` en verde | `[M]`
- [x] **F3.2 — Diseño de la Matriz Curricular `es-en` B1 y B2 (Syllabus CEFR Can-Do & Escenarios)** | `[DeepSeek V4.1 Flash]` | N/A | Especificación estructurada de **40 lecciones nuevas** (20 lecciones Nivel B1 repartidas en 4 unidades + 20 lecciones Nivel B2 repartidas en 4 unidades): cada lección define `scenario` realista (negociación laboral, debate ético, consulta médica compleja, ironía/registro), descriptor `canDo` oficial CEFR, `grammarPoints[]` (ej. *Third Conditional*, *Mixed Conditionals*, *Inversion with negative adverbials*, *Passive reporting structures*, *Cleft sentences*, *Subjunctive mood*) y banda de frecuencia `vocabFrequencyBand` (`3000-5000`) | `[M]`
- [x] **F3.3 — Autoría y Validación del Bloque `es-en` Nivel B1 (Unidades 1–4, 20 Lecciones)** | `[DeepSeek V4.1 Flash]` | N/A | 20 lecciones B1 integradas en `content-es-en/.../course.json` (8–10 ejercicios por lección = ~180 ejercicios nuevos); 100% cumplen la **Regla de No-Aplanamiento Pedagógico** validada automáticamente por `validateContent` y `CourseValidator` (≥1 `FreeSpeakingPrompt`, ≥1 modismo con `culturalNote`, `feedbackOnError` específico por distractor); 0 bytes BOM | `[L]`
- [ ] **F3.4 — Autoría y Validación del Bloque `es-en` Nivel B2 (Unidades 5–8, 20 Lecciones)** | `[DeepSeek V4.1 Flash]` | N/A | 20 lecciones B2 integradas en `content-es-en/.../course.json` (~200 ejercicios nuevos con predominio de `ClozeDragDrop`, `SemanticMatch`, `AudioOnlyPrompt` y `FreeSpeakingPrompt` con `conceptClusters` y patrones de `grammarPoints`); `PackagedContentTest.kt` valida unicidad global de IDs, no-colisión de vocabulario FSRS y cobertura de todas las reglas del validador v2 | `[XL]`
- [ ] **F3.5 — Congelación Verificada de Paridad en `es-pt`, `es-fr`, `es-ja` + Test de Contrato Dialectal v2** | `[DeepSeek V4.1 Flash]` | N/A | Tests en `PackagedContentTest.kt` que aseguran que `es-pt`, `es-fr` y `es-ja` permanecen intactos en `schemaVersion: 1` (17 lecciones A1/A2 sin regresiones) más un test de contrato en `CourseJsonTest.kt` que verifica la resolución de variantes dialectales (`pt-BR` vs `pt-PT`, `en-US` vs `en-GB`) listas para activarse en v2.1 | `[XS]`

> 🛑 **[CHECKPOINT HUMANO — FIN DE FASE 3]**
> 1. Revisión pedagógica humana (muestreo de al menos 5 lecciones B1 y 5 lecciones B2) verificando naturalidad de modismos, precisión de `feedbackOnError` y ausencia de aplanamiento tipo A1.
> 2. Ejecutar `./gradlew validateContent test` y confirmar **0 errores** en los ~500+ ejercicios totales del curso `es-en`.

---

### Fase 4: QA en Hardware Físico, Stress Térmico/RAM (JNA/ONNX) y Baseline Profiles
*Objetivo: Auditar el comportamiento nativo C++ (Vosk JNA + sherpa-onnx) en silicio ARM real, eliminar fugas de memoria nativa y sellar el rendimiento con Baseline Profiles.*

- [ ] **F4.1 — Auditoría de Ciclo de Vida Nativo JNA/JNI y Stress Test de Memoria (`core-audio`)** | `[DeepSeek V4 Pro]` | El manejo de punteros nativos en JNA (`org.vosk.Model`, `org.vosk.Recognizer`) y JNI (`OfflineTts`) ante cancelaciones abruptas de corrutinas (`ViewModel.onCleared()` en mitad de grabación o síntesis) requiere sincronización determinista de cierre para evitar `SIGSEGV` o fugas en el `Native Heap` | Implementación de liberación segura con ref-counting o cola serializada en `VoskSttEngine` y `PiperTtsEngine`; test instrumentado de stress que ejecuta 50 ciclos consecutivos de `startLesson → playAudio → transcribeSpoken (cancelado a los 500ms) → exit` en dispositivo/emulador con delta de Native Heap `< 15 MB` tras GC y **0 crashes nativos (`Tombstones`)** | `[L]`
- [ ] **F4.2 — Generación de Baseline Profiles y Macrobenchmark de Arranque/Jank (`:baselineprofile`)** | `[DeepSeek V4.1 Flash]` | N/A | Módulo/configuración de Baseline Profile que recorre: Cold Start → Home → Apertura de Lección B2 → Interacción `ClozeDragDrop` → Pantalla de Repaso FSRS; archivo `app/src/main/baseline-prof.txt` generado y commiteado; reducción de tiempo de Cold Start ≥15% en dispositivo físico | `[M]`
- [ ] **F4.3 — Verificación de Reglas R8/ProGuard para Nuevos Modelos v2 y Smoke Test de Flavors Release** | `[DeepSeek V4.1 Flash]` | N/A | Revisión de `app/proguard-rules.pro` asegurando keeps para todos los nuevos subtipos `@Serializable` de `Exercise` y estructuras de `course-v2.json` sin debilitar la ofuscación; `./gradlew :app:assembleEnRelease :app:assemblePtRelease :app:assembleFrRelease :app:assembleJaRelease` compilan limpiamente y pasan smoke test de inicio de lección + STT/TTS en release minificado | `[S]`

> 🛑 **[CHECKPOINT HUMANO — FIN DE FASE 4 / RELEASE GATE v2.0]**
> 1. Ejecutar sesión de 20 minutos continuos en dispositivo físico Android (arm64-v8a) alternando ejercicios `SpeakRepeat`, `FreeSpeakingPrompt` y `AudioOnlyPrompt`: verificar ausencia de throttling térmico severo y RAM total `< 280 MB` en Android Studio Profiler.
> 2. Confirmar que los 4 APKs firmados por flavor cumplen `100 MB <= tamaño <= 170 MB` y pasan el gate completo de CI.

---

## 3. Anexos Obligatorios

### Anexo A: Decision Log (ADR-017 a ADR-021)

| ADR | Título | Decisión Arquitectónica | Invariantes Protegidos | Estado |
|---|---|---|---|---|
| **ADR-017** | **Particionamiento por Product Flavors de Idioma (`en`, `pt`, `fr`, `ja`)** | Se descarta Play Feature Delivery (incompatible con F-Droid/GitHub Releases). Se definen Product Flavors en `:app` que empaquetan solo el subdirectorio `models/tts/<lang>` y `models/stt/<lang>` correspondiente, reduciendo el APK de **508.36 MB a 110–165 MB**. | `I1` (Offline), `I6` (FOSS/F-Droid) | Propuesto |
| **ADR-018** | **Schema JSON v2 Dual y Regla de No-Aplanamiento Pedagógico** | `core-content` y `validateContent` soportan simultáneamente `course-v1.json` y `course-v2.json`. Toda lección declarada como `B1` o `B2` en v2 exige por contrato estático: `scenario`, `canDo`, `grammarPoints[]`, `vocabFrequencyBand`, `feedbackOnError`, ≥1 `FreeSpeakingPrompt` y ≥1 nota cultural/modismo. | `I4` (Progreso), `I9` (Validación CI) | Propuesto |
| **ADR-019** | **Evaluación de Producción Oral Libre B2 sin LLMs (Permiso de Imposibilidad)** | Se prohíben LLMs on-device y cloud. `FreeSpeakingPrompt` se califica mediante un motor heurístico determinista en `core-domain` (longitud, cobertura de `conceptClusters`, detección de patrones de `grammarPoints` y fluidez `WPM` sobre transcripción Vosk) combinado con autoevaluación guiada del usuario frente a un modelo de audio Piper. | `I1` (Offline), `I2` (Sin backend), `I3` (Sin fricción), `I7` (Testabilidad) | Propuesto |
| **ADR-020** | **Inicialización Asíncrona No-Bloqueante de Motores Nativos (Vosk/Piper)** | Se prohíbe realizar extracción de assets, hashing SHA-256 o carga de modelos C++ dentro de llamadas síncronas de `isReadyFor()`. `core-audio` expone un estado reactivo de warmup en `Dispatchers.IO` y streaming de `partialResult` + RMS. | `I3` (Sin fricción), `I8` (UX/Accesibilidad) | Propuesto |
| **ADR-021** | **Migración Room v2→v3 con Test Instrumentado de Regresión de Estado** | Ningún cambio en `LinguagoDatabase` se fusiona sin el test instrumentado bloqueante (`Migration2To3Test`) que verifica la preservación íntegra de 8/17 lecciones, 3 tarjetas FSRS en `learning` y racha de 5 días. | `I4` (Progreso intocable) | Propuesto |

---

### Anexo B: Presupuesto Estimado de Cuotas por Fase (Disciplina de Fallback Económico)

| Fase | Modelo Asignado | Tareas Cubiertas | Requests Est. | Costo Est. (USD) |
|---|---|---|---|---|
| **Fase 0** | `DeepSeek V4.1 Flash` | F0.1, F0.2, F0.3, F0.4, F0.5 (Schema v2, Validator, Room v3, MigrationTest) | ~35 reqs | ~$0.45 |
| **Fase 0** | `DeepSeek V4 Pro` | F0.6 (Product Flavors + filtrado dinámico de `models/` en AGP 9.4) | ~12 reqs | ~$0.90 |
| **Fase 1** | `DeepSeek V4.1 Flash` | F1.2 (Graders `SemanticMatch`/`ClozeDragDrop` + `VocabularyExtractor`) | ~15 reqs | ~$0.20 |
| **Fase 1** | `DeepSeek V4 Pro` | F1.1, F1.3, F1.4 (`FreeSpeakingHeuristicGrader`, Concurrencia JNA/ONNX, Streaming RMS) | ~30 reqs | ~$2.40 |
| **Fase 2** | `Claude Haiku 5.5` | F2.1, F2.2, F2.4, F2.5 (Haptics, `spring` transitions, `SemanticMatch`, `FreeSpeakingPanel`) | ~45 reqs | ~$1.80 |
| **Fase 2** | `Gemini 4 Argon` | F2.3 (`ClozeDragDropBoard` inmutable 2D + TalkBack dual + 60 FPS) | ~10 reqs | ~$2.50 |
| **Fase 3** | `DeepSeek V4.1 Flash` | F3.1, F3.2, F3.3, F3.4, F3.5 (Migración v1→v2 y autoría de 40 lecciones B1/B2 `es-en`) | ~110 reqs | ~$1.60 |
| **Fase 4** | `DeepSeek V4 Pro` | F4.1 (Blindaje de punteros JNA/JNI C++ y test de stress de Native Heap) | ~15 reqs | ~$1.20 |
| **Fase 4** | `DeepSeek V4.1 Flash` | F4.2, F4.3 (Baseline Profiles y reglas R8/ProGuard de release) | ~18 reqs | ~$0.25 |
| **TOTAL** | **Mix Optimizado** | **19 Tareas Tácticas (F0–F4)** | **~290 reqs** | **~$11.30 USD** |

> **Factor de seguridad:** el presupuesto nominal asume ejecución ideal al primer intento. Con factor 2.5x (realista para debugging de JNA, concurrencia y autoría de contenido): ~$28 USD totales. Ambos números son informativos; el coste real se controla por fase en cada CHECKPOINT.

---

### Anexo C: Candidatos a Escalación a `Gemini 4 Argon` (Máximo 5 Tareas con Justificación de Riesgo)

| # | Tarea / Componente | Modelo Base | Condición de Disparo hacia `Gemini 4 Argon` | Riesgo Técnico Mitigado |
|---|---|---|---|---|
| **1** | **F2.3 — `ClozeDragDropBoard` Multi-Hueco B2** *(Asignado de inicio)* | `Gemini 4 Argon` | Asignación directa por complejidad de física 2D + `FlowRow` + accesibilidad TalkBack (`I8`). | Desincronización de índices canónicos en `StateFlow`, jank (>16ms/frame) por recomposición en cada evento táctil o ruptura de foco en lectores de pantalla. |
| **2** | **F2.5 — Visualizador de Onda Acústica STT a 60 FPS (`FreeSpeakingPanel`)** | `Claude Haiku 5.5` | Si el perfilado en Fase 2 muestra frames >16.6 ms o alocaciones en el hilo UI al renderizar el `Canvas` reactivo al `Flow` de RMS de Vosk. | Sobrecarga de recomposición en `LessonScreen` mientras el micrófono captura PCM a 16 kHz. |
| **3** | **F1.3 — Sincronización de Warmup + Transición Visual Resiliente Vosk/Piper** | `DeepSeek V4 Pro` | Si ocurren condiciones de carrera entre el estado de navegación de Compose (`AnimatedContent`) y la liberación/carga de modelos nativos al cambiar rápido de lección. | Crash nativo (`UnsatisfiedLinkError` / `SIGABRT` en JNA) o congelamiento de la animación de entrada a la lección. |
| **4** | **F4.1 — Cierre Determinista de Punteros JNA (`org.vosk.Recognizer`) bajo Cancelación** | `DeepSeek V4 Pro` | Si el stress test de 50 ciclos en hardware físico arroja `SIGSEGV` en `libvosk.so` cuando el usuario cancela `transcribeSpoken()` a mitad de `acceptWaveForm()`. | Corrupción de memoria C++ cuando un hilo de corrutina bloqueado en `AudioRecord.read()` compite con `recognizer.close()`. |

---

### Anexo D: Métricas Deterministas Locales Consolidadas por Fase (0% Métricas Cloud)

| Fase | Peso APK Release (por Flavor) | Cold Start Time (API 34/37) | Frame Time P95 en Transiciones/UI | Footprint RAM (Java + Native Heap) | Suite de Tests (Unit + Instrumented) |
|---|---|---|---|---|---|
| **Baseline (v0.2.0)** | `508.36 MB` (monolítico) | ~950 ms (sin perfilar) | No medido | ~240 MB (pico al cargar Vosk+Piper) | 19 archivos de test · 0 regresiones |
| **Fase 0 (Contrato + Flavors)** | **`100 MB – 170 MB`** por flavor (`en`, `pt`, `fr`, `ja`) | ≤ 950 ms | N/A (sin cambios UI) | ≤ 240 MB | +24 tests (incluye `Migration2To3Test` bloqueante) · 0 regresiones |
| **Fase 1 (Dominio B2 + Audio Async)** | `100 MB – 170 MB` | **≤ 700 ms** (0 I/O en Main Thread) | ≤ 16.6 ms al abrir `LessonScreen` | **≤ 250 MB** (carga perezosa por idioma activo) | +53 tests acumulados (graders B2 + concurrencia audio) · 0 regresiones |
| **Fase 2 (UI Sensorial + Gamificación)** | `100 MB – 170 MB` | ≤ 700 ms | **≤ 16.6 ms (60 FPS estables)** en Drag & Drop y onda STT | ≤ 265 MB | +65 tests acumulados (+ tests de accesibilidad `fontScale 2.0f`) · 0 regresiones |
| **Fase 3 (Contenido `es-en` B2)** | `105 MB – 170 MB` (JSON B2 añade <2 MB) | ≤ 720 ms (parseo JSON `<40 ms` en IO) | ≤ 16.6 ms | ≤ 270 MB | +75 tests acumulados (`validateContent` valida 57 lecciones `es-en`) · 0 regresiones |
| **Fase 4 (QA Hardware + Baseline Profile)** | **`105 MB – 170 MB`** | **≤ 550 ms** (con Baseline Profile activo) | **≤ 14.0 ms P95** · <1% jank frames | **≤ 280 MB** tras 50 ciclos de stress JNA/ONNX | **≥ 80 tests nuevos totales** · 0 leaks nativos · 0 regresiones |

---

### Anexo E: Backlog v2.1 (Fuera del Scope de v2.0 — Máximo 5 Ideas)

1. **Escalamiento de `es-pt`, `es-fr` y `es-ja` a Schema v2 y Nivel B1/B2:** Migrar los 3 cursos restantes desde las 17 lecciones A1/A2 actuales una vez que se cuente con validación lingüística nativa para sus modismos, notas culturales y variedades dialectales (`pt-BR` vs `pt-PT`, `fr-FR` vs `fr-CA`).
2. **Voz Neural Offline para Japonés (`es-ja`):** Incorporar un modelo VITS/Kokoro ligero compatible con `sherpa-onnx` para reemplazar el fallback actual a `SystemTtsEngine` en el flavor `ja` (`ADR-010`), manteniéndose dentro del presupuesto de 170 MB.
3. **Sincronización Cifrada del Estado FSRS (Snapshot v2):** Diseñar la función de reconciliación determinista para sincronizar la tabla `word_progress` (`stability`, `difficulty`, `nextReviewEpochMillis`) en Google Drive (`ADR-016`) sin violar la monotonía del progreso (`I4`).
4. **Proveedor Cloud Opcional OneDrive (`ADR-015`):** Implementar `OneDriveCloudProvider` sobre la carpeta `appfolder` mediante OAuth2 PKCE puro en `core-auth` (sin depender de SDKs propietarios pesados), completando la alternativa a Google Drive.
5. ** Evaluación de Prosodia y Acento Léxico (Pitch Contour Offline):** Extraer la frecuencia fundamental ($F_0$) desde el buffer PCM de `AudioRecord` para dar feedback visual de entonación en inglés y acento tonal (*pitch accent*) en japonés sin usar modelos de IA adicionales.
