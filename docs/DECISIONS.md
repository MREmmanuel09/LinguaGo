# Decisiones de arquitectura (ADRs)

> Registro cronológico de decisiones técnicas de LinguaGo. Cada decisión no
> trivial debe tener aquí su ADR. Formato: Contexto → Decisión → Consecuencias.

---

## ADR-000: Convención del registro de decisiones

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** el RFC-001 exige trazabilidad de decisiones en formato ADR.
Necesitamos una convención pareja para todas las decisiones futuras.

**Decisión:** un único archivo `docs/DECISIONS.md` con ADRs numerados
(ADR-001, ADR-002, ...). Cada entrada tiene: título, estado, fecha, contexto,
decisión y consecuencias. Un ADR reemplazado queda marcado y enlazado a su
reemplazo, sin borrarse.

**Consecuencias:** historial lineal; si el archivo crece demasiado se reparte
en `docs/adr/` con índice aquí.

---

## ADR-001: Modularización Gradle (21 módulos + build-logic)

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** el proyecto necesita 4 cursos, 7 features, y motores de audio y
sync intercambiables (RFC §2.2). Un solo módulo escalable sería inmanejable y
lentísimo en build.

**Decisión:** Clean Architecture con 21 módulos Gradle (1 app, 7 core, 7
feature, 4 content + `build-logic` como included build con convention
plugins). Namespaces `dev.linguago.*` mapeados a la ruta del módulo. KSP en
lugar de kapt; built-in Kotlin de AGP 9 (no se aplica `kotlin-android`).
Config mínima: minSdk 26 / targetSdk 37 / compileSdk 37 / JVM 17.

**Consecuencias:**
- Builds paralelos;规矩 de dependencia verificables.
- Convention plugins evita repetición de DSL en 21 módulos.
- El costo: más archivos de build; mitigado por plugins de convención.

---

## ADR-002: Estado de presentación MVVM+MVI unidireccional

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** se necesita un patrón de estado predecible y testeable para
compositor pantalla rápida, con invariante I7 (testabilidad) fuerte.

**Decisión:** cada feature expone estado inmutable).(class + eventos (sealed
interface)), eventos bajan por callbacks y estado sube por `StateFlow`
(colectado con `collectAsStateWithLifecycle()`). `ViewModel` de Android en
`feature-*`; UseCases del dominio sin Android. estado de lección modelado
como máquina de estados finita (ver ADR-007).

**Consecuencias:**
- Sin Hilt adie ViewModel o composable perde testable.
- Modelo mental único para el equipo; reproducible para futuras pantallas.
- Riesgo de sobre-ingeniería en pantallas simples; mitigado haciendo el
  estado plano y explícito.

---

## ADR-003: Audio offline — Strategy Pattern, sherpa-onnx runtime

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** los ejercicios de conversación requieren TTS (lección textos)
y STT (evaluar al estudiante hablando) 100% offline. El RFC §4 manda
sherpa-onnx como runtime y deja la elección de modelos al agente (Piper /
Kokoro / Supertonic; Vosk / Whisper).

**Decisión:**
- Contratos **`TtsEngine`** y **`SttEngine`** en `core-audio`, completamente
  desacoplados de cualquier SDK (Strategy Pattern).
- **Default:** Piper (TTS) + Vosk Small (STT) por idioma — mejor balance
  calidad/tamaño/latencia para presupuestos móviles.
- Implementación concreta vía **sherpa-onnx** runtime AAR descargada en build
  (ver ADR-006), versionada de manera estricta para reproducibilidad.
- Alternativas (Kokoro, Supertonic, Whisper, Vosk Large) intercambiables sin
  tocar capas superiores, modeladas como variantes de provider.

**Consecuencias:**
- La capa de presentación consume casos por interface; cambiar el proveedor
  no toca otra cosa.
- sherpa-onnx agrega ~46 MB en AAR al APK (compartida entre las 4 voces);
  aceptado como costo base (AGP ABI filters para optimizar si es necesario).
- Nativos de sherpa-onnx y vosk-android pueden chocar por libasolación — se
  prueba antisympt_templates en el build.

---

## ADR-004: FSRS con implementación propia (sin lib externa)

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** se necesita repaso espaciado de vocabulario (RFC §3.2
`GetNextReviewUseCase`). Existen portings de FSRS de la especificación de
open-spaced-repetition (py-fsrs, fsrs-kotlin, ts-fsrs), todos licencia MIT,
pero viables con estados de margen de riesgo por su adopción en aplicaciones
de terceros.

**Decisión:** implementar en `core-domain` el kernel FSRS-5 (fórmulas分别是
de la especificación abierta) en Kotlin puro, con parámetros por defecto
(libres) y unit tests que ejecutan tanto la fórmula como la estabilidad.
Persistencia en Room con `word_progress` (revisados_count, next_review,
ease?). ADR re-evalúa a Phase 2 si la implementación propia se vuelve un
punto de proble.

**Consecuencias:**
- Sin dependencia externa; todo testeable en aislamiento (I7).
- Riesgo de implementación-off-con-spec; mitigado con vector de tests
  basado en casos numeréricas publicados.

---

## ADR-005: Persistencia Room + DataStore con timestamps de resolución

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** el progreso del usuario se persiste localmente (Room) y se
sincroniza con cloud opcional (invariante I4: progreso intocable). El merge
necesita resolución determinista.

**Decisión:** toda entidad de progreso lleva `createdAt = updatedAt` en
columna ISO-8601 UTC. Contratos de repositorio en `core-domain`; implementación
en `core-data` (Room + DataStore). Merge de sync (ADR-013, futuro) comparará
`updatedAt` por entidad y jamás borrar registros; en conflicto consenso
conservar y marcar para revisión.

**Consecuencias:**
- Merge determinista, sin relojes muertos del dispositivo (usamos el más
  nuevo `updatedAt` + reglas por entidad del RFC §6.2).
- Filas Room `word_progress`, `lesson_progress`, `streak_state`, `sync_state`
  preparadas para import/export JSON.

---

## ADR-006: Distribución de modelos — descarga en build/CI, nunca commiteada

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** Piper + Vosk Small = ~272 MB de modelos, presupuesto repo
< 500 MB, sin archivo > 100 MB (RFC §5.2). Commitearlos con Git LFS agota el
cupo mensual de banda de GitHub LFS al primer pocas descargas.

**Decisión:** `git` no commita binarios de modelos . La herramienta:
- `gradle/model-fetch` script (Kotlin + ) que descarga AAR de sherpa-onnx y
  modelos según `models/manifest.json`.
- Verificación `sha256` en cada asset, fuente oficial (GitHub Releases de
  k2-fsa, alphacephei.com) y Release.asset_files de LinguaGo para los
  zip de modelos que no existe release oficial.
- Cache CI (actions/cache) + caché local `~/.gradle/model-cache`.
- Repo queda < 100 MB; APK envermecidoosexo ~430 MB.

**Consecuencias:**
- El CI necesita Internet en build (esperado, no rompe I1 — se comple con
  cache locales cuando está disponible).
- Manifest versionado por cursificador tom cada modelos con checksums.

---

## ADR-007: Motor de lección como máquina de estados finita (FSM)

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** RFC §2.3 exige máquina de estados para el motor de lección; el
estado de lección debe ser finito y bien definido.

**Decisión:** FSM con estados puros (elementos testables):
`LOADING → PLAYING → FEEDBACK_CORRECT | FEEDBACK_WRONG → NEXT | COMPLETED`.
Los UseCases del dominio operan sobre la FSM; el ViewModel dispara eventos
(`SubmitAnswer`, `SelectNext`) y la FSM decide transiciones. XP y progreso
se computan al finalizar la lección.

**Consecuencias:**
- Transiciones inválidas imposible; errores atrapados en duración de build.
- Composición de types de ejercicios directories (azaderías solo de estrategias).

---

## ADR-008: Licencias y atribución de terceros

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** mezclamos componentes con distintas licencias (MIT, Apache,
CC0, OFL). Invariante I6: código abierto real.

**Decisión:** código Kotlin bajo `LICENSE` (MIT, © 2026 MREmmanuel09).
Contenido pedagógico bajo `LICENSE-CONTENT` (CC BY-SA 4.0). Cada dependencia
de terceros se acredita en la pantalla Ajustes → Licencias y en el
`THIRD_PARTY_NOTICES.md` que se genera en CI a partir del lockfile de dependencias.

**Consecuencias:**
- Cumplimiento legal limpio; contribuidores ven el límites antes de añadir
  dependencias (ver CONTRIBUTING.md).
- Los activos incluidos (Nunito OFL, Piper MIT/variedad, Vosk Apache) quedan
  de coste legal documentado.

---

## Registro de decisiones pendientes

| Tema | Se decide en | ADR previsto |
|---|---|---|
| Plugin serialization con built-in Kotlin (KGP 2.4 embebida en AGP) | Fase 1 | ADR-009 |
| Elección voz por idioma (lista exacta int8/medium) | Fase 2 | ADR-010 |
| Google Drive/OneDrive OAuth flow y almacenamiento de tokens | Fase 4 | ADR-011 |
| Formato Content JSON v1 y validador schemático | Fase 3 | ADR-012 |
