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

## Registro de decisiones pendientes

| Tema | Se decide en | ADR previsto |
|---|---|---|
| Proveedor OneDrive (appfolder vía MSAL) | registrado en ADR-015 como diferido | - |
