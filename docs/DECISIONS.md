# Decisiones de arquitectura (ADRs)

> Registro cronológico de decisiones técnicas de LinguaGo. Cada decisión no
> trivial debe tener aquí su ADR. Formato: Contexto → Decisión → Consecuencias.

---

## Índice

| ADR | Título | Estado |
|---|---|---|
| 000 | Convención del registro de decisiones | Aprobado |
| 001 | Modularización Gradle (21 módulos + build-logic) | Aprobado |
| 002 | Estado de presentación MVVM+MVI unidireccional | Aprobado |
| 003 | Audio offline - Strategy Pattern, sherpa-onnx runtime | Aprobado |
| 004 | FSRS con implementación propia (sin lib externa) | Aprobado |
| 005 | Persistencia Room + DataStore | Aprobado |
| 006 | Distribución de modelos - descarga en build/CI, nunca commiteada | Aprobado |
| 007 | Motor de lección como máquina de estados finita (FSM) | Aprobado |
| 008 | Licencias y atribución de terceros | Aprobado |
| 009 | kotlinx.serialization directa sobre el dominio + formato Content JSON v1 | Aprobado |
| 010 | Voces TTS por idioma y fallback al sistema | Aprobado |
| 011 | Autenticación Google Drive vía OAuth 2.0 + PKCE | Aprobado |
| 012 | JSON Schema formal y validación del contenido | Aprobado |
| 013 | Sincronización cloud - snapshot v1, merge monótono y proveedores | Aprobado |
| 014 | Cifrado del snapshot y tokens - AES-GCM-256 con Keystore | Aprobado |
| 015 | OneDrive (appfolder vía MSAL) | Diferido |
| 016 | Repaso espaciado FSRS-4.5 con vocabulario del contenido | Aprobado |
| 017 | Particionamiento por Product Flavors de idioma | Propuesto |
| 018 | Schema JSON v2 dual y Regla de No-Aplanamiento Pedagógico | Propuesto |
| 019 | Evaluación de producción oral libre B2 sin LLMs (heurística + autoevaluación) | Propuesto |
| 020 | Inicialización asíncrona no bloqueante de motores nativos (Vosk/Piper) | Propuesto |
| 021 | Migración Room v2→v3 con test instrumentado de regresión de estado | Propuesto |
| 022 | Cierre determinista de recursos nativos JNA/JNI bajo cancelación | Propuesto |
| 023 | Baseline Profiles y Macrobenchmark del flujo crítico | Propuesto |
| 024 | RELEASE GATE F4: módulo QA de sesión instrumentada contra `enRelease` | Propuesto |
| 025 | Vidas con penalización no bloqueante y XP diferido | Aprobado |
| 026 | Learning Path lineal y lenguaje visual flat-3D («Feather») | Aprobado |

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
- Builds paralelos; reglas de dependencia verificables.
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
**Implementado en:** ADR-016 (Fase 5), con el conjunto de pesos FSRS-4.5.

**Contexto:** se necesita repaso espaciado de vocabulario (RFC §3.2,
`GetNextReviewUseCase`). Existen portings de FSRS de la especificación
open-spaced-repetition (py-fsrs, fsrs-kotlin, ts-fsrs), todos con licencia
MIT, pero con riesgo de adopción y mantenimiento para el caso de uso.

**Decisión:** implementar en `core-domain` el kernel FSRS (fórmulas de la
especificación abierta) en Kotlin puro, con parámetros por defecto y unit
tests sobre fórmula, intervalos y estabilidad. Persistencia del estado en
Room (`word_progress`). Se re-evaluará si la implementación propia se vuelve
un problema de mantenimiento.

**Consecuencias:**
- Sin dependencia externa; todo testeable en aislamiento (I7).
- Riesgo de desviarse de la especificación, mitigado con tests de
  propiedades (monotonía, rangos) y vectores numéricos publicados.

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

## ADR-009: kotlinx.serialization directa sobre el dominio + formato Content JSON v1

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** los cursos viajan empaquetados en assets como JSON (I1, todo
offline). Había dos caminos: DTOs intermedios + mapper, o serialización
directa sobre los modelos de dominio. Además, KGP 2.4 viene embebido en AGP 9
(sin plugin `kotlin-android`) y el plugin compiler de serialization debía
probarse con esa combinación.

**Decisión:**
- Plugin `kotlin("plugin.serialization")` + `libs.kotlinx.serialization.json`
  en `core-domain`; `core-content` expone `api` el artefacto JSON.
- Los modelos de dominio (`Course`, `Level`, `Unit`, `Lesson`, `Exercise`,
  `Language`, ids value class, `CourseSummary`, `MatchPair`) son
  `@Serializable` y se parsean **directamente** al dominio (sin capa DTO).
- Polimorfismo de `Exercise` por discriminador `"type"` con **nombres
  simples** del serializador (`@SerialName("MultipleChoice")`, `"Translate"`,
  `"MatchPairs"`, ...).
- Claves JSON de la pareja de idiomas: `from` / `to` vía `@SerialName` en
  `fromLanguage` / `toLanguage` (kotlinx usa el nombre de la propiedad por
  defecto y las contrapartes humanas del RFC eran `from`/`to`).
- `kotlin.Pair<String, String>` **no** se usa (serializa como objeto
  `{first, second}`, no como arreglo): `Exercise.MatchPair` data class con
  claves JSON `{left, right}`.
- Json estricto (reject unknown keys) en `CourseJson`; errores de parseo se
  traducen a `LinguaError.InvalidContent` y el validador estructural
  (`CourseValidator`) refuerza reglas que la serialización no cubre
  (correctIndex in-range, multiset de tokens de OrderWords, lados únicos de
  MatchPairs, no-solape en Translate).

**Consecuencias:**
- Menos código y una sola fuente de verdad (el dominio); la migración futura
  de formato implicará `@Deprecated` serial names o cambios de versión en
  manifest (Fase 3, ADR-012 documenta el JSON Schema formal).
- El JSON v1 queda definido por los serial names del dominio; `PackagedContent`
  es el test de contrato contra los cuatro cursos reales del repo.

---

## ADR-010: Voces TTS por idioma y fallback al sistema

**Estado:** Aprobado
**Fecha:** 2026-10-16

**Contexto:** los ejercicios de escucha (ListenChoose/ListenType) necesitan
voz. Piper (sherpa-onnx) no publica todavía una voz japonesa estable con
modelo descargable verificable; además, integrar el AAR de sherpa-onnx y
descargar las voces en CI (ADR-006) es trabajo grande que no debe bloquear
el audio entendible desde ya.

**Decisión:**
- La voz de Fase 2.1 es el **TTS del sistema** (`SystemTtsEngine` sobre
  `TextToSpeech`) con locales BCP-47 por idioma: es-ES, en-US, pt-BR,
  fr-FR, ja-JP (`core-audio`).
- Voces Piper elegidas para Fase 2.2 (en `models/manifest.json`, verificación
  SHA-256 antes de cargar): `en_US-amy-medium`, `pt_BR-faber-medium`,
  `fr_FR-siwis-medium`; para `ja` queda **fallback al sistema** hasta que
  exista voz publicada estable.
- El `TtsEngine` concreto se resuelve por DI (`AudioModule`); cambiar
  System→Piper no toca features ni dominio (Strategy, ADR-003).
- `isReadyFor` filtra por disponibilidad en el dispositivo: si no hay voz,
  el chip de audio se muestra inactivo y el ejercicio sigue usable (I3).

**Consecuencias:**
- Audio funciona sin descargar nada (la mayoría de dispositivos tienen
  voces offline del sistema); Piper sube calidad cuando llegue Fase 2.2.
- La calidad de voz depende del dispositivo — mitigación: Piper como
  default cuando exista manifest verificado.

---

## ADR-011: Autenticación Google Drive vía OAuth 2.0 + PKCE (Fase 4)

**Estado:** Aprobado
**Fecha:** 2026-10-17

**Contexto:** la sincronización (ADR-013) requiere subir/bajar el snapshot
desde un espacio privado del usuario. Se evaluó Firebase (no: app offline
sin servicios de Google Cloud, permission I3), GoogleSignIn legacy
(depends on Play Services SDK sign-in deprecado) y flux con navegador +
custom scheme identificado en OAuth 2.0 for Native Apps (RFC 8252).

**Decisión:**
- **Stack:** OAuth 2.0 Authorization Code + PKCE (RFC 7636) con OkHttp y
  redirect custom (`dev.linguago.oauth://callback`, `OAuthCallbackActivity`
  en app + intent-filter BROWSABLE). Sin AppAuth ni Play Services: flujo
  implementado end to end en `core-auth` (`OAuthEngine`, `OAuthSession`,
  `TokenStore`) — canje/refresh contra `oauth2.googleapis.com/token`.
- **Permisos:** solo `drive.appdata` (+ `openid profile`): una carpeta
  sandbox privada de la app en Drive — inaccessible incluso para el
  usuario en la UI de Drive; el `snapshot` de ADR-013 vive ahí.
- **Tokens** en DataStore privado de la app (`filesDir/datastore`), con
  copia en memoria (`TokenStore`) para consultas sincronas de `isLinked`.
- **Doble proveedor:** el flujo real (`google_drive`) requiere client id
  (gradle property `googleClientId`); sin él la UI muestra el estado
  "Sin configurar". Además, en builds debug existe el proveedor de
  laboratorio `dev_local` que respalda dentro del propio dispositivo para
  probar el flujo completo auth → sync sin credenciales — bind DI solo en
  debug (`app/src/debug`), interruptor `@Named("dev_cloud_enabled")`.
- UI: `feature-auth` nueva AuthScreen (carteleras por proveedor,
  vincular/desvincular); "Conectar cuenta" en SyncScreen (navega a auth).

**Consecuencias:**
- Para activar el respaldo REAL: crear un OAuth client (Web app o iOS con
  scheme custom; se documentará en README) y compilar con
  `-PgoogleClientId=...`; el resto de la app no cambia.
- El OneDrive queda **deferido** (ADR-015): el contrato CloudProvider ya
  lo soporta; solo falta MSAL + redirect msauth.
- El cifrado del token en reposo queda para ADR-014 (hoy:	aplicación UID-scope solo lectura vía ΔRead).

---

## ADR-012: JSON Schema formal y validación del contenido

**Estado:** Aprobado
**Fecha:** 2026-10-17

**Contexto:** el contenido vive como JSON (`content-*/…/course.json`) y se
decodifica con kotlinx-serialization. Sin un contrato formal, un contenido
malformado (campo faltante, variante de ejercicio nueva sin audioText, ids
inconsistentes) solo falla cuando la app lo parsea en runtime (I9: "contenido
inválido ⇒ error visible"), demasiado tarde.

**Decisión:**
- El contrato del contenido es `docs/schemas/course-v1.json`
  (draft-07): describe Course→Level→Unit→Lesson→Exercise con `oneOf`
  sobre las 10 variantes de ejercicio, `additionalProperties: false`,
  y `schemaVersion: 1` fijo en la raíz.
- Cada course.json lleva `"schemaVersion": 1`: futuro cambio incompatible
  ⇒ `schemaVersion` 2 y un codificador que soporte ambas (regla del RFC
  sobre versiones).
- La tarea `validateContent` (build raíz, classpath = json-schema-validator
  de networknt) valida los 4 course.json y **rompe el build** ante cualquier
  desvío. Corre en CI antes de los tests unitarios.

**Consecuencias:**
- El contenido imposible de parsear nunca llega a la app: el schema es el
  test de integración del contenido.
- La decodificación Kotlin sigue estando en core-content (el schema es el
  espejo del modelo `@Serializable`; drift posible pero revisado en cambios
  de contenido).

---

## ADR-013: Sincronización cloud — snapshot v1, merge monótono y NoopCloudProvider

**Estado:** Aprobado
**Fecha:** 2026-10-17

**Contexto:** Fase 3 requiere lograr "progreso del usuario por gamificación
visible fuera de la app". El proveedor definitivo (Google Drive appDataFolder
con OAuth) se decide en Fase 4 (ADR-011); ahora existe `CloudProvider`
(Strategy, ADR-003) pero sin flujo real ni formato de datos.

**Decisión:**
- **Formato:** snapshot JSON plano v1 (`SyncSnapshot`, schemaVersion=1,
  `exportedAtMillis`, streak y lista de lecciones). Sin cifrado: se sube al
  sandbox privado de la cuenta (appDataFolder/appfolder), que el usuario
  protege vía su sesión; cifrar el payload se decide en Fase 4.
- **Merge:** monótono por máximo de campo (`SnapshotMerge.mergedWith`):
  XP/racha = max(local, remote), lecciones por id con completed = OR;
  **nunca borra progreso** (I4). No requiere timestamps en Room.
- **Codec:** `SnapshotCodec` en core-sync: UTF-8 estricto
  (`ignoreUnknownKeys = false`), rechaza schemaVersion ≠ 1 y JSON inválido
  con `LinguaError.InvalidContent`.
- **Proveedor:** `NoopCloudProvider` bindeado por DI hasta Fase 4
  (id="none", `isLinked=false`, Devuelve `NotLinked`); SyncEngine orquesta
  snapshot→encode→push y fetch→decode→restore sobre el repositorio.
- UI: `SyncScreen` + `SyncViewModel` en feature-sync muestran proveedor y
  disparan backup/restore; sin cuenta vinculada el submit queda inhabilitado.

**Consecuencias:**
- La Fase 3 deja completamente listo el flujo sincronizable real solo
  cambia la implementación de `CloudProvider` (ADR-011).
- El merge monótono no reconcilia cuentas múltiples en usarse en paralelo;
  suficiente para Fase 3 (una cuenta, un dispositivo).

---

## ADR-014: Cifrado del snapshot y tokens — AES-GCM-256 con Keystore

**Estado:** Aprobado
**Fecha:** 2026-10-17

**Contexto:** ADR-013 deja el snapshot en claro dentro del sandbox cloud y
los tokens en el DataStore privado de la app. Riesgo: snapshot en text plano
es legible si alguna vez escapa del sandbox; tokens en claro son legibles con
root o backup del `filesDir`.

**Decisión:**
- **Algoritmo:** AES-GCM-256 (`AES/GCM/NoPadding`), IV de 12 bytes
  aleatorio por cifrado (prefijo al payload), tag GCM de 16 bytes al final.
  Formato: `IV || ciphertext || tag`.
- **Clave:** AndroidKeyStore, alias `linguago_crypto_key`, PKCS8
  (con generación aleatoria). Hardware-backed cuando el dispositivo lo
  soporte; software cuando no. Sin almacenar la clave en texto.
- **Punto único de cifrado:** `CryptoService` (core-common) con dos
  impls: `KeystoreCryptoService` (prod) y `NoOpCryptoService` (tests/dev
  fallback). El snapshot se cifra en `SyncEngine` (chokepoint de
  transporte); los tokens se cifran en `TokenStore`.
- **Fallo limpio:** si el descifrado falla (payload corrupto/ajeno), se
  devuelve `LinguaError.InvalidContent`/`NotFound` y la sesión se invalida,
**nunca** se acepta texto plano.
- **Migración:** tokens en claro preexistentes se detectan en el primer
  acceso y se re-cifran automáticamente.

**Consecuencias:**
- Un snapshot robado es inútil sin la clave del dispositivo; el respaldo ya
  no es "solo sandbox": es sandbox + cifrado.
- El_keystore genera fallos solo en dispositivos muy degradados; el fallback
  NoOp preserva el 100% de funcionalidad (I1–I3) a cambio de no cifrar,
  comportamiento que se documenta.

---

## ADR-015: OneDrive (appfolder vía MSAL) — diferido

**Estado:** Diferido
**Fecha:** 2026-10-17

**Contexto:** el RFC §6 contempla OneDrive (`appfolder`) como segundo
proveedor de respaldo. El contrato `CloudProvider` (ADR-013), el selector
(`DelegatingCloudProvider`) y el flujo de auth (ADR-011) ya soportan N
proveedores; falta la implementación concreta con MSAL Android y el registro
de la aplicación en Azure AD (credencial externa, igual que el client id de
Google).

**Decisión:** se difiere hasta que exista la aplicación registrada en Azure
y la fase de contenido esté cerrada. El número queda reservado y la
implementación seguirá el mismo patrón que `GoogleDriveCloudProvider`:
Strategy + registro del proveedor solo cuando esté configurado.

**Consecuencias:**
- No hay respaldo nativo para cuentas Microsoft todavía; Google Drive cubre
  el caso principal y la app sigue 100% funcional offline (I1/I3).
- El trabajo futuro es acotado: un módulo de cliente (MSAL), un
  `CloudProvider` nuevo y sus bindings; sin cambios en `SyncEngine`.

---
## ADR-016: Repaso espaciado FSRS-4.5 con vocabulario del contenido

**Estado:** Aprobado
**Fecha:** 2026-10-17

**Contexto:** el RFC promete repaso espaciado (FSRS) desde Fase 2, pero solo
existían los contratos (`WordProgress`, `GetNextReviewUseCase`) sin
persistencia, algoritmo ni UI. Sin repaso, el contenido completado se
olvida y el progreso por gamificación no se sostiene.

**Decisión:**
- **Algoritmo:** FSRS-4.5 puro en dominio (`FsrsScheduler`), con los 17
  parámetros por defecto, retención objetivo 90%, ratings Otra vez/Difícil/
  Bien/Fácil. "Otra vez" se vuelve a deber en 1 día; el resto usa intervalo
  derivado de stability. Testeable en JVM sin Android (I7).
- **Vocabulario:** se extrae de pares explícitos del contenido
  (`MatchPairs`, `KanaChoose`, `KanjiReading`) al completar una lección
  (`VocabularyExtractor`). No se toca el JSON de contenido ni el schema.
- **Persistencia:** nueva tabla Room `word_progress` (clave `word`+`courseId`,
  columnas stability/difficulty/reviewCount/lastReview/nextReview).
  DB v2 con migración 1→2 sin pérdida de progreso.
- **Repaso:** pantalla `feature-review` (N de M, mostrar traducción, 4
  ratings, cierre). Entrada desde Home solo cuando hay tarjetas vencidas
  (`Repasar ahora · N`). Sin XP por repaso en esta versión (evita inflar el
  total; se revisará con métricas reales).
- **Sync:** el word progress queda **fuera** del snapshot v1: el estado FSRS
  no es monótono y mezclarlo requiere reglas propias (futuro snapshot v2).

**Consecuencias:**
- Repaso offline completo y persistente; el merge monótono (ADR-013) no se
  ve afectado porque no se sincroniza.
- Cambiar los parámetros FSRS no invalida tarjetas: solo cambia la agenda
  futura (estado actual sigue siendo válido).

---

## ADR-017: Particionamiento por Product Flavors de idioma

**Estado:** Propuesto
**Fecha:** 2026-10-17

**Contexto:** el APK release monolítico de v0.2.0 pesa 508.36 MB (debug
528.28 MB) porque empaqueta los cuatro cursos con sus modelos TTS/STT
(`models/` = 510.55 MB: 232.26 MB de Piper y 278.27 MB de Vosk). Ese tamaño
dificulta la distribución en F-Droid y en GitHub Releases. La distribución
debe seguir siendo FOSS y sin Play Feature Delivery (vetado por I6).

**Decisión:** definir Product Flavors por idioma (`en`, `pt`, `fr`, `ja`) en
`:app`, con sourceSets que empaqueten únicamente `models/tts/<lang>` y
`models/stt/<lang>` del curso correspondiente. Cada APK se mide
individualmente. No se fija un límite duro por flavor: el total de los cuatro
flavors debe caber bajo 2 GB (límite de GitHub Releases), con holgura
esperada de ~700-800 MB total. Se monitoriza el peso pero no se bloquea el
release por diferencias de ±5 MB. La integridad SHA-256 de los modelos se
mantiene intacta (`ModelIntegrityTest`).

**Consecuencias:**
- Se reduce el APK de 508.36 MB a un rango estimado de 100–170 MB por flavor.
- Cambiar a un segundo curso desde Ajustes mantiene el texto al 100% (JSON
  incluido) y degrada el audio a `SystemTtsEngine` si el modelo no está en el
  flavor instalado (I3).
- Más variantes de build que validar en CI; el Configuration Cache de
  AGP 9.4.1 exige materializar los sourceSets sin capturar `fileTree`.

**Invariantes protegidos:** I1 (offline), I3 (degradación limpia), I6 (FOSS y
distribución sin Play Feature Delivery).

---

## ADR-018: Schema JSON v2 dual y Regla de No-Aplanamiento Pedagógico

**Estado:** Propuesto
**Fecha:** 2026-10-17

**Contexto:** el contrato v1 (`docs/schemas/course-v1.json`) solo describe
jerarquía y ejercicios A1/A2. Escalar `es-en` a B2 real exige metadatos
pedagógicos que hoy no existen (nivel CEFR, escenario, objetivos can-do,
gramática, banda de frecuencia, feedback de error estructurado) y nuevas
mecánicas. Reescribir los cuatro cursos a la vez sin revisores nativos
introduciría regresiones pedagógicas y rompería la paridad de `es-pt`,
`es-fr` y `es-ja`.

**Decisión:** crear `docs/schemas/course-v2.json` (draft-07) y mantener
coexistencia dual v1/v2. `validateContent` inspecciona `schemaVersion` y
enruta cada `course.json` a su schema. Solo `es-en` migra a v2; `es-pt`,
`es-fr` y `es-ja` permanecen en v1 con sus 17 lecciones A1/A2. Toda lección
v2 declarada `B1` o `B2` debe incluir obligatoriamente `cefrLevel`,
`scenario`, `canDo`, `grammarPoints[]`, `vocabFrequencyBand`,
`feedbackOnError` estructurado, al menos un `FreeSpeakingPrompt` y al menos
un modismo con nota cultural. El validador estructural falla si falta
cualquiera de estos elementos (Regla de No-Aplanamiento Pedagógico).

**Consecuencias:**
- El contenido B2 imposible de parsear o pedagógicamente incompleto rompe el
  build antes de llegar a la app (I9).
- Los modelos de `core-domain` usan valores por defecto seguros para v1
  (`cefrLevel = A1`, `locale = null`, `feedbackOnError = null`), de modo que
  el contenido existente sigue cargando sin cambios.
- El drift entre schema y modelos `@Serializable` se cubre con tests de
  contrato y con `PackagedContentTest`.

**Invariantes protegidos:** I1 (contenido empaquetado), I4 (ids históricos
preservados), I9 (validación estática en CI).

---

## ADR-019: Evaluación de producción oral libre B2 sin LLMs (heurística + autoevaluación)

**Estado:** Propuesto
**Fecha:** 2026-10-17

**Contexto:** `FreeSpeakingPrompt` requiere evaluar producción oral libre en
B2. Un juicio semántico y pragmático real exige un modelo de lenguaje, pero
los LLMs on-device (SLM cuantizados de ~1B) rompen el presupuesto de APK y de
RAM y agotan la batería, mientras que las APIs cloud violan I1/I2. Existe una
incompatibilidad real entre evaluación semántica abierta y el invariante
offline.

**Decisión:** aplicar el Permiso de Imposibilidad y declarar la limitación.
Opción A (descartada): embeber un SLM cuantizado vía JNI; rompe el
presupuesto de peso, provoca OOM en dispositivos de 3–4 GB y degrada la
batería. Opción B (elegida): `FreeSpeakingHeuristicGrader` en `core-domain`,
determinista y puro, que combina cuatro señales sobre la transcripción de
Vosk: longitud mínima de tokens, cobertura de `conceptClusters`, detección de
patrones de los `grammarPoints` exigidos y fluidez en WPM sobre la ventana de
voz activa. El resultado se complementa con una rúbrica obligatoria de
autoevaluación del estudiante frente a una respuesta modelo (texto y audio
Piper).

**Consecuencias:**
- Evaluación 100% offline, testeable en JVM y sin dependencias nuevas (I7).
- Trade-off asumido: puede aceptar una frase con keywords válidas pero
  incoherencia pragmática sutil (falso positivo estimado ~10–15%); se mitiga
  con la autoevaluación reflexiva y la respuesta canónica audible.
- El falso negativo por reconocimiento imperfecto de Vosk se acota con
  distancia de edición en la cobertura léxica y no bloquea el avance (I3).

**Invariantes protegidos:** I1 (offline), I2 (sin backend), I3 (sin
fricción), I7 (testabilidad).

---

## ADR-020: Inicialización asíncrona no bloqueante de motores nativos (Vosk/Piper)

**Estado:** Propuesto
**Fecha:** 2026-10-17

**Contexto:** `LessonViewModel.startLesson()` llama hoy a
`ttsEngine.isReadyFor()` y `sttEngine.isReadyFor()` de forma síncrona. Esas
funciones extraen árboles de assets a `filesDir`, calculan SHA-256 de hasta
94 MB e instancian `org.vosk.Model` / `OfflineTts` en el hilo llamante, lo
que produce jank y riesgo de ANR al abrir una lección. Además
`SttEngine.transcribe()` es una suspensión opaca de ~6 s que no expone
parciales ni energía acústica a la UI.

**Decisión:** prohibir I/O, hashing y carga nativa dentro de `isReadyFor()`.
`core-audio` expone un estado reactivo `StateFlow<AudioEngineState>`
(`Idle`, `Verifying`, `Extracting`, `Loading`, `Ready`, `Degraded`) y realiza
el warmup en `Dispatchers.IO` con `Mutex` por idioma y extracción atómica
(directorio temporal + `renameTo`). Se añade
`observeTranscription(language): Flow<SpeechCaptureUpdate>` que emite
`partialResult` y RMS cada ~50 ms, con cancelación cooperativa y cierre
garantizado de `AudioRecord` y `Recognizer` en `finally`.

**Consecuencias:**
- Entrar a `LessonScreen` deja de hacer lecturas de disco en el hilo
  principal (verificable con `StrictMode.detectDiskReads()`).
- El streaming habilita el visualizador de onda y el feedback en tiempo real
  de Fase 2 sin cambiar el contrato de dominio.
- Mayor complejidad de concurrencia (Mutex, ciclo de vida nativo); se cubre
  con tests de estrés en Fase 4.

**Invariantes protegidos:** I3 (sin fricción), I8 (UX y accesibilidad
responsivas).

---

## ADR-021: Migración Room v2→v3 con test instrumentado de regresión de estado

**Estado:** Propuesto
**Fecha:** 2026-10-17

**Contexto:** v2.0 necesita persistir métricas locales de producción oral y
registro de errores frecuentes. Cualquier cambio de schema en Room puede
destruir progreso si la migración es incorrecta. I4 exige que el progreso del
usuario sea intocable, también durante un upgrade de la app.

**Decisión:** subir `LinguagoDatabase` a `version = 3` con `MIGRATION_2_3`
explícita en `DataModule.kt` y el JSON de schema exportado en
`core-data/schemas/.../3.json` (commiteado). Ningún cambio de schema se
fusiona sin un test instrumentado bloqueante (`Migration2To3Test` con
`MigrationTestHelper`) que parte de una base v2 con el estado exacto de un
usuario v0.2.0 —8/17 lecciones completadas, 3 tarjetas FSRS en estado
`learning` y racha de 5 días— aplica las migraciones y verifica que el estado
persiste íntegro.

**Consecuencias:**
- El upgrade no puede perder progreso sin romper CI (I4).
- La migración es determinista y reproducible en emulador API 34/37.
- Prohibido `fallbackToDestructiveMigration`; cualquier error de migración
  debe ser visible y corregible, no silencioso.

**Invariantes protegidos:** I4 (progreso intocable), I7 (testabilidad).

---

## ADR-022: Cierre determinista de recursos nativos JNA/JNI bajo cancelación

**Estado:** Propuesto
**Fecha:** 2026-10-18

**Contexto:** `VoskSttEngine` mantiene `org.vosk.Model` (JNA) y
`PiperTtsEngine` mantiene `OfflineTts` (JNI sherpa-onnx) en mapas por idioma.
Las operaciones de captura y síntesis pueden cancelarse en mitad de una
llamada nativa (`AudioRecord.read()`, `acceptWaveForm()`, `generate()`), por
ejemplo al salir de una lección. Si otra corrutina libera el puntero mientras
la llamada nativa sigue en curso, `libvosk.so`/`libsherpa-onnx-jni.so` pueden
leer memoria liberada y provocar `SIGSEGV`. Los cierres aislados en `finally`
no bastan: hace falta que el cierre espere a la operación nativa y que una
liberación posterior no pueda colarse durante un warmup.

**Decisión:** serializar cada recurso nativo por idioma con
`NativeSerialLane<T>` (cola serializada, sin ref-counting). La lane conserva
un `Mutex` por recurso, aloja un único puntero y expone:

- `use {}`: retiene el lock durante toda la operación nativa; si la corrutina
  se cancela, el bloque termina y el lock se libera.
- `close()`: adquiere el lock en `NonCancellable`, espera a que no haya uso
  activo y libera el recurso; marca la lane como cerrada para rechazar
  instalaciones tardías y cerrarlas al instante.
- `install()`: intercambia el recurso bajo el lock y cierra el anterior; un
  `lifecycle: Mutex` por motor serializa `install()` con `release()`.

`SpeechCapture` crea `AudioRecord` y `Recognizer` dentro del `try` y cierra
ambos en `finally` con `runCatching` independientes, de modo que un fallo al
crear el recognizer no deja la fuente abierta y un fallo al liberar la fuente
no omite el cierre del recognizer. `PiperTtsEngine` ejecuta `generate()` y
`playBlocking()` bajo la misma lane, comprueba la cancelación antes y después
de la síntesis y libera `AudioTrack` con `runCatching`. `release()` en ambos
motores espera las operaciones activas, libera los recursos, resetea el
warmup (`WarmupStateRegistry.reset`) y permite recargar en el siguiente
`warmUp()`.

El contrato se verifica con `NativeAudioStressTest` (instrumentado, flavor
`en`/`full`): 10 ciclos de calentamiento para estabilizar las arenas nativas,
baseline tras GC, 50 ciclos de `startLesson → playAudio → transcribeSpoken`
cancelado a los 500 ms, TTS cancelado cada 5 ciclos a los 250 ms, tres rondas
de GC y delta de `Debug.getNativeHeapAllocatedSize()` menor que 15 MB. Al
final consulta `ActivityManager.getHistoricalProcessExitReasons` para exigir
cero `REASON_CRASH_NATIVE`.

**Consecuencias:**
- Un idioma ejecuta una síntesis/captura nativa a la vez; dos peticiones
  simultáneas se serializan. Es aceptable porque la UI reproduce un prompt
  cada vez y los ejercicios de voz son secuenciales.
- El cierre es determinista ante cancelación y no compite con
  `acceptWaveForm()`/`generate()`; las liberaciones se encapsulan para que un
  fallo en `AudioRecord.release()`/`AudioTrack.release()` no oculte el resto.
- La lane rechaza recursos instalados tras `close()` y evita fugas durante
  una carrera warmup/release; el recurso se recarga de forma perezosa.
- Medición local en emulador API 37 x86_64: 50 ciclos con delta de **95 952
  bytes (~0.09 MB)** y 0 crashes nativos. La validación en hardware ARM
  físico sigue siendo parte del checkpoint humano de Fase 4.
- Trade-off asumido: el baseline se toma tras el calentamiento para excluir
  la expansión inicial de las arenas de los runtimes nativos; el test sigue
  midiendo 50 ciclos completos después de ese baseline.

**Invariantes protegidos:** I1 (offline), I3 (sin fricción), I7 (testabilidad).

---

## ADR-023: Baseline Profiles y Macrobenchmark del flujo crítico

**Estado:** Propuesto
**Fecha:** 2026-10-18

**Contexto:** el arranque en frío y la navegación hasta la primera lección y
el repaso son la ruta crítica percibida por el usuario. Sin perfil, ART
compila el código en cada instalación y el coste se nota especialmente en
hardware modesto. El roadmap exige perfilar Cold Start → Home → apertura de
lección B2 → interacción `ClozeDragDrop` → pantalla de Repaso FSRS, medir el
efecto y mantener la app offline (I1) sin fricción (I3).

**Decisión:** se añade el módulo `:baselineprofile` (`com.android.test`,
self-instrumenting) que apunta a `:app` y solo instrumenta el flavor `en`.
`BaselineProfileGenerator` genera `startupProfile` y
`criticalJourneyProfile` con UiAutomator y selectores estables. La navegación
usa `tapClickableAncestor` para tolerar el fling de Compose que consume el
primer toque. `BenchmarkSeedActivity`, presente solo en
`enNonMinifiedRelease`, fija el curso `es-en` y siembra 20 tarjetas FSRS
vencidas para que el repaso sea determinista y sobreviva a las pasadas
sucesivas de `BaselineProfileRule`.

Con `saveInSrc = true`, AGP escribe ambos perfiles en
`app/src/enRelease/generated/baselineProfiles/`. El baseline se copia a
`app/src/main/baseline-prof.txt` y se versiona para que los cuatro flavors
de idioma lo reutilicen; el startup permanece en
`app/src/enRelease/generated/baselineProfiles/startup-prof.txt`.
`profileinstaller` queda como dependencia de `:app`. Los benchmarks
`StartupBenchmark` usan `StartupTimingMetric` y `FrameTimingMetric`,
`StartupMode.COLD` y 5 iteraciones, comparando
`CompilationMode.Partial(BaselineProfileMode.Require)` con
`CompilationMode.None()`. En emulador se suprime el error `EMULATOR` solo
para el smoke local; no se acepta como evidencia de rendimiento.

**Consecuencias:**
- Añade un módulo de test (`:baselineprofile`): el proyecto pasa de 21 a 22
  módulos Gradle; `build-logic` sigue como included build.
- Medición preliminar en emulador API 37 x86_64 (2026-10-18): TTID mediana
  de **560.60 ms sin perfil** y **493.65 ms con perfil** (−11.9 %). No alcanza
  el objetivo de ≥15 %, pero el emulador no representa ARM físico; la
  validación definitiva sigue en el checkpoint humano de Fase 4.
- En la ventana de scroll de Home se registraron 68 frames por pasada y un
  P95 de `frameDurationCpuMs` de 89.70 ms sin perfil frente a 80.04 ms con
  perfil. La ventana incluye el arranque COLD, así que solo vale como smoke.
- El baseline versionado pesa ~2.42 MB y el startup ~2.07 MB. Deben
  regenerarse cuando cambien de forma significativa la navegación o las
  pantallas del recorrido, y revisar el diff en el PR.
- Regeneración: `:app:generateEnBaselineProfile`. Macrobenchmark:
  `:baselineprofile:connectedEnBenchmarkReleaseAndroidTest`.
- `suppressErrors = "EMULATOR"` es exclusivamente local; CI y hardware real
  deben ejecutar sin esa supresión.

**Invariantes protegidos:** I3 (sin fricción), I7 (testabilidad).

---

## ADR-024: RELEASE GATE F4 — módulo QA de sesión instrumentada contra `enRelease`

**Fecha:** 2026-10-19 · **Estado:** Propuesto

**Contexto:** F4.3 ya valida los 4 flavors release con granularidad de
smoke manual; ROADMAP_V2.md (Fase 4) exige además un RELEASE GATE: una
sesión de uso real suficientemente larga (≥20 min) contra la variante
release minificada (R8) que exponga fugas de memoria, degradación térmica,
rachas de UI race y caminos STT/TTS degradados, con reporte objetivo y
auditable antes de declarar `v2.0` estable.

**Decisión:** se añade el módulo `:core:qa` (`com.android.test`,
self-instrumenting sobre `:app`, flavor `en`, buildType `release`
firmado con debug para pruebas). Allí vive
`ReleaseGateF4Test`: orquesta las 5 lecciones B2 de la unidad
«Negociación y decisiones» («Plazos y concesiones», «Presupuesto bajo
presión», «Quién decide qué», «Concesiones sin perder terreno», «Cerrar el
trato») leídas del `course.json` de `content-es-en`; si el presupuesto de
tiempo exige más minutos, las pasadas extra reabren lecciones ya completadas
y, como la app entra en modo repaso con contenido reordenado, se registran
como NOTE best-effort (no votan). También congela métricas de
thermal/RAM/nativeHeap/`fd` en baseline y en cada iteración. También
perturbaciones exploratorias por índice de ejercicio: rotación
(user_rotation vía shell, `setOrientationLeft` rompe el árbol
UiAutomator), `trim-memory RUNNING_CRITICAL`, `always_finish_activities`,
doble-tap en Continuar, cancelación de TTS/STT, notificación durante
recording, 5 min en background, fontScale 2× / modo oscuro / locale `en-US`.
El reporte JSON se escribe en el almacenamiento externo y se copia a
`/data/local/tmp/` porque AGP desinstala ambos APK al terminar (wipe de
`/Android/data`). Puede ejecutarse predeterminadamente en modo completo y
smoke con overrides:
- Smoke (~15 min): `:core:qa:connectedEnReleaseAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.linguago.releasegate.durationMinutes=1
  -Pandroid.testInstrumentationRunnerArguments.linguago.releasegate.minLessons=1`.
- Completo: sin overrides (20 min objetivo). Los veredictos no-PASS se
  muestran como lo que son: fallas por criterio objetivo.

**Limitaciones documentadas (importantes):**
- `fd_count` no es medible en una app release no-debuggable
  (`/proc/<pid>/fd` denegado): queda `null` y esa prueba no vota.
- En el emulador API 37 x86_64 no hay micrófono ni gestión térmica real:
  los caminos STT/FREE_SPEAKING degradan al SKIP oficial del panel (I3:
  nunca bloquear) y quedan documentados como verificación subjetiva
  humana sobre hardware físico.
- La RAM pico del proceso (~400-500 MB) incluye los modelos Piper/Vosk
  residentes; el gate reporta el peak junto con la traza por iteración
  para que el criterio de fuga se juzgue con la curva completa y no con
  un solo valor.
- Las verificaciones subjetivas (TalkBack, haptics, "sensación" de
  latencia del audio) se registran en `manual_checks` como pendientes de
  human reviewer: la automatización no inventa ese veredicto.

**Criterios del veredicto (v1):** PASS exige duración ≥ objetivo (salvo
smoke), ≥ `minLessons` completadas, 0 crashes/ANR, ningún escenario FAIL,
0 `race_suspects` y ninguna fuga sostenida. La RAM se juzga con el drift del
heap nativo por lección (≤ 30 MB/lección sobre la primera, con modelos ya
cargados) y un techo absoluto catastrófico de 600 MB; el desglose
java/native/graphics/private-other de cada iteración queda en el JSON.

**Alternativas consideradas:**
- Test dentro de `app/src/androidTest`: mezclado con el código de la app
  y metría el job de instrumentados; descartado por separación de capas
  (la QA de release vive en su propio módulo).
- Macrobenchmark (`BenchmarkRule`): describe un micro-recorrido, no
  permite el acumulador de wall-clock (20 min), perturbaciones del
 Ventana o intents IO externos; descartado para este propósito (se queda
  en `:baselineprofile` para rendimiento puro).
- Correrlo por subcomandos ad-hoc del build: no consistencia ni
  reporte determinable; descartado.

**Consecuencias:**
- El proyecto pasa de 22 a 23 módulos Gradle (commit y conteos
  actualizados en ARCHITECTURE.md / AGENTS.md / llms.txt).
- Los veredictos de cada corrida viven en
  `docs/qa/release-gate-f4-results.json` (artefacto reproducible: el
  test y el driver son deterministas frente a `course.json`).
- El módulo reutilizará el patrón de test self-instrumenting con flavor
  dim explícito ya establecido por `:baselineprofile` (build-logic se
  mantiene intacta).

**Invariantes protegidos:** I3 (sin degradar UX por artefactos de QA: el
gate usa los caminos oficiales de la app), I4 (no corrompe el progreso:
lleva seeds y restauración de estado), I7 (testabilidad: veredicto
objetivo y repetible).

---

## ADR-025: Vidas con penalización no bloqueante y XP diferido

**Estado:** Aprobado
**Fecha:** 2026-10-24

**Contexto:** el rediseño gamificado (ADR-026) introduce un coste del error
para incentivar la precisión. El invariante I3 prohibía "vidas" y esperas, y
además el XP se persistía por respuesta correcta
(`SubmitAnswerUseCaseImpl`) y se volvía a sumar al completar la lección
(`CompleteLessonUseCaseImpl`): eso hacía imposible "perder el XP de la clase"
y duplicaba el total acumulado.

**Decisión:**
- Vidas máximas **10**, arranque con 10 y **+1 cada 60 s**. Estado
  `HeartsState(hearts, max, lastRegenEpochSecond)` persistido en Room v4
  (tabla `hearts_progress`); al leer se aplica la regeneración por epoch
  (incluido el arranque en frío).
- Consumo: respuesta incorrecta en lección −1; `Otra vez` en repaso FSRS −1;
  el SKIP de voz nunca consume.
- A 0 vidas la lección se **cancela**: no se persiste `xpEarned`, no se llama
  a `CompleteLessonUseCase` (sin progreso, sin racha, sin `attempts`). El
  repaso FSRS nunca se bloquea: a 0 vidas continúa sin penalizar.
- **XP diferido:** `SubmitAnswerUseCase` solo califica; el XP se persiste una
  única vez en `CompleteLessonUseCase` (`markLessonCompleted` + `addXp` +
  racha). Se elimina el `addXp` por respuesta y con ello el doble conteo.
- Repetir una lección completada otorga **XP completo** y `attempts++`
  (decisión de producto).
- Sync: `SyncSnapshot` añade `heartsCurrent` y
  `heartsLastRegenEpochSecond` con defaults (10 / 0); el merge normaliza la
  regeneración en ambos lados y toma el **mínimo** (las vidas perdidas no
  resucitan al sincronizar). Los snapshots antiguos decodifican con 10 vidas.
- **I3 se reinterpreta:** "sin fricción" = sin login obligatorio, anuncios ni
  esperas permanentes. Las vidas devuelven el acceso en ≤10 min y nunca
  cierran el camino.

**Alternativas consideradas:** (a) no añadir vidas (rechazada: se quiere
coste del error); (b) vidas comprables o con backend (rechazada: viola I1/I2);
(c) bloqueo permanente a 0 vidas (rechazada: viola I3).

**Consecuencias:**
- Room v3→v4 con `MIGRATION_3_4` y test instrumentado `Migration3To4Test`.
- Nuevos contratos de dominio: `HeartsState`, `HeartsRules`,
  `ObserveHeartsUseCase`, `ConsumeHeartUseCase`, `RegisterAnswerUseCase`.
- `LessonViewModel` gana `hearts` y `lessonCancelled`; `ReviewViewModel`
  consume en `Otra vez`.
- El Release Gate F4 limita los fallos intencionales del driver (presupuesto
  por lección) para no cancelar lecciones durante la sesión.
- README y ARCHITECTURE actualizan el texto de I3.

**Invariantes protegidos:** I3 (redefinido), I4 (las vidas no son progreso;
el XP sigue siendo monótono), I8.

---

## ADR-026: Learning Path lineal y lenguaje visual flat-3D («Feather»)

**Estado:** Aprobado
**Fecha:** 2026-10-24

**Contexto:** Home muestra hoy tarjetas de unidad en una columna
(`UnitCard`), sin camino ni narrativa de progreso. Se quiere un único camino
lineal animado, con cofres por unidad, mascota y HUD de racha/vidas/XP,
manteniendo la identidad púrpura existente (RFC §7) y sin arte de terceros
(I5).

**Decisión:**
- **Camino lineal único:** se aplana `course.levels → units → lessons` en el
  orden del curso (que ya es la rampa de dificultad A1→B2). Tras cada unidad
  se inserta un nodo `Chest` independiente; la última lección sigue siendo
  rejugable.
- **Desbloqueo:** una lección sin completar está `Locked` si algún predecesor
  lineal no está completado; las completadas siempre son clicables
  (repetición con XP completo, ADR-025).
- **Identidad visual "Feather"** (nombre interno del sistema de diseño, sin
  dependencia externa): lenguaje flat-3D (borde inferior 4dp, sin sombras
  difusas), radios 20–24dp en componentes nuevos (12/16 existentes se
  conservan), púrpura actual + oro para cofres y rojo para corazones. Tokens
  aditivos en `core-ui/LinguaTokens.kt`.
- **Movimiento:** Compose nativo primero (`animate*AsState`, `spring`,
  `Canvas`, `AnimatedContent`); Rive/Lottie solo si el arte final lo exige,
  midiendo el APK (presupuesto 100–170 MB por flavor).
- **Mascota original** (colibrí "Lumi", placeholder vectorial en Fase 6 y
  arte final después).
- El camino se activa con `BuildConfig.LEARNING_PATH_V2` y mantiene
  `UnitCard` como fallback.
- **HUD:** racha, vidas y nivel visual derivado (`totalXp / 100 + 1`), sin
  persistir nivel.

**Alternativas consideradas:** (a) camino por unidad (rechazada: se quiere
uno solo); (b) cambiar la paleta a verde (rechazada: identidad original I5);
(c) empezar con Rive/Lottie (rechazada: jank y tamaño antes de validar el
diseño).

**Consecuencias:**
- Nuevos composables en `core-ui` (`PathNode`, `HeartsIndicator`,
  `LiquidXpBar`, `ConfettiBurst`, `ChestNode`).
- `HomeUiState` gana campos aditivos (`pathNodes`, `hearts`, `level`,
  `xpIntoLevel`); `onLessonClick` no cambia.
- `docs/DESIGN_SYSTEM.md` es la fuente de verdad de tokens y componentes.
- Regenerar baseline profile y macrobenchmark; actualizar el driver del gate
  F4.
- Sin arte de terceros.

**Invariantes protegidos:** I5, I8, I1.

---

## Registro de decisiones pendientes

| Tema | Se decide en | ADR previsto |
|---|---|---|
| Proveedor OneDrive (appfolder vía MSAL) | registrado en ADR-015 como diferido | - |
