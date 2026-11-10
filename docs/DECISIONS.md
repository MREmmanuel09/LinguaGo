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
| 027 | Feedback multisensorial (sonido, háptica y ajustes) | Aprobado |
| 028 | Micro-clases de gramática y feedback de acierto | Aprobado |
| 029 | Tableros de respuesta horizontales y emparejamiento puzzle | Aprobado |
| 030 | Reintentos pedagógicos, vidas y fairness de XP | Aprobado |
| 031 | Profundidad táctil 3D y sonido cálido v2 («Feather 2.0», Fase 1) | Aprobado |
| 032 | Lumi expresiva, banners de unidad por etapa y camino vivo (Fase 2) | Aprobado |
| 033 | Lección viva: panel de feedback, combos, ondas y fichas animadas (Fase 3) | Aprobado |
| 034 | Flashcards 3D y calificación semántica en el repaso (Fase 4) | Aprobado |
| 035 | Bento de progreso, onboarding ilustrado y transiciones (Fase 5) | Aprobado |
| 036 | Ajustes pedagógicos de la lección: brief temático, validación explícita, emparejamiento bidireccional y corrección final | Aprobado |
| 037 | Accesibilidad AA: tintas sobre color y HUD de vidas numérico | Aprobado |
| 038 | Guía de origen opcional en OrderWords | Aprobado |
| 039 | Tolerancia a diacríticos en la evaluación | Aprobado |
| 040 | Glosario por lección, long-press de palabras y etapa del camino por CEFR | Aprobado |
| 041 | Migración de los cursos de arranque pt/fr/ja a schema v2 | Aprobado |
| 042 | Expansión progresiva a C1/C2 con rampa verificada y protocolo de lotes | Aprobado |
| 043 | Captura de voz tolerante y evaluación oral sin falsos negativos | Aprobado |
| 044 | Tipografía 100 % Nunito y tokens de contenedor (V-04, V-08) | Aprobado |
| 045 | Paleta extendida, bisel oscuro y `PathStage.onAccent` (V-03, V-09) | Aprobado |
| 046 | Grader multilingüe y dirección de tarjetas FSRS (D-01, D-04) | Aprobado |
| 047 | Avance neutro al omitir un ejercicio de voz (B-02, I3) | Aprobado |
| 048 | Navegación de 4 destinos, cambio rápido de curso, cofres y recuperación activa de vidas | Aprobado |
| 049 | Persistencia de sesión en `SavedStateHandle` (B-08) | Aprobado |
| 050 | Room v5, `SyncSnapshot` v3 y respaldo local por SAF | Aprobado |
| 051 | `espeak-ng-data` compartido, gestor de voces y desglose CEFR | Aprobado |

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

**Estado:** Aprobado
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

**Estado:** Aprobado
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

**Actualización (2026-11-02):** los tres cursos de arranque se migraron a v2
mediante ADR-041 conservando sus ids; después los tres llegaron a **60
lecciones (A1–B2 completo)/526 ejercicios**. Los tests fijan ese contrato. La
revisión lingüística nativa de B1–B2 sigue pendiente.

**Invariantes protegidos:** I1 (contenido empaquetado), I4 (ids históricos
preservados), I9 (validación estática en CI).

---

## ADR-019: Evaluación de producción oral libre B2 sin LLMs (heurística + autoevaluación)

**Estado:** Aprobado
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

**Estado:** Aprobado
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

**Estado:** Aprobado
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

**Estado:** Aprobado
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

**Estado:** Aprobado
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

**Fecha:** 2026-10-19 · **Estado:** Aprobado

**Contexto:** F4.3 ya valida los 4 flavors release con granularidad de
smoke manual; ROADMAP_V2.md (Fase 4) exige además un RELEASE GATE: una
sesión de uso real suficientemente larga (≥20 min) contra la variante
release minificada (R8) que exponga fugas de memoria, degradación térmica,
rachas de UI race y caminos STT/TTS degradados, con reporte objetivo y
auditable antes de declarar `v2.0` estable.

**Decisión:** se añade el módulo `:core:qa` (`com.android.test`,
self-instrumenting sobre `:app`, flavor `en`, buildType `release`
firmado con debug para pruebas). Allí vive
`ReleaseGateF4Test`: orquesta las 5 primeras lecciones del Camino (A1:
«Saludos», «Presentarse», «La familia», «Comida y bebida», «Colores y
números») leídas del `course.json` de `content-es-en`. El camino lineal
(ADR-026) bloquea B2 hasta completar el recorrido, así que el gate conduce
las primeras lecciones disponibles; siguen siendo contenido real validado.
Si el presupuesto de tiempo exige más minutos, las pasadas extra reabren
lecciones ya completadas y, como la app entra en modo repaso con contenido
reordenado, se registran como NOTE best-effort (no votan). También congela
métricas de thermal/RAM/nativeHeap/`fd` en baseline y en cada iteración.
Incluye perturbaciones exploratorias por índice de ejercicio: rotación
(user_rotation vía shell, `setOrientationLeft` rompe el árbol
UiAutomator), `trim-memory RUNNING_CRITICAL`, `always_finish_activities`,
doble-tap en Continuar, cancelación de TTS/STT, notificación durante
recording, 5 min en background, fontScale 2× / modo oscuro / locale `en-US`.
El driver navega los nodos del camino por `contentDescription` y acota los
fallos intencionales (2 por lección, y solo con vidas > 3) para no cancelar
la lección por la penalización de ADR-025.
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

## ADR-027: Feedback multisensorial (sonido, háptica y ajustes)

**Estado:** Aprobado
**Fecha:** 2026-10-25

**Contexto:** la app solo emite audio de voz (TTS/STT) y háptica muy limitada
en la lección (`confirm`, `tick`, `error`). No hay efectos de sonido de
interfaz, no existe ajuste para desactivarlos y los botones/opciones no tienen
respuesta sensorial consistente. Se quiere reforzar cada interacción sin
depender de red, sin arte de terceros (I5) y sin afectar a la accesibilidad
(I8).

**Decisión:**
- **SFX originales de LinguaGo**, sintetizados en el repo y guardados como
  WAV PCM mono en `core-ui/src/main/res/raw`. No se descargan packs CC0 ni se
  usan samples de terceros: el sonido es parte de la identidad (I5). Un script
  en `tools/` permite regenerarlos.
- Set mínimo de eventos: `TAP`, `SELECT`, `DESELECT`, `SNAP`, `CORRECT`,
  `INCORRECT`, `COMPLETE`. Duración total del set &lt; 150 KB y volumen bajo
  (~0,45) para no tapar el TTS.
- Reproducción con `SoundPool` desde `core-ui`, con `AudioAttributes`
  `USAGE_ASSISTANCE_SONIFICATION` y un único `SoundPool` liberado al salir de
  composición. Sin nuevas dependencias.
- **Háptica rica** con `VibrationEffect` (minSdk 26): pulso de selección,
  encaje, acierto, error y completado. Se respeta el ajuste del sistema
  (`HAPTIC_FEEDBACK_ENABLED`) y el ajuste propio de la app. `VIBRATE` es un
  permiso normal y se declara en el manifest de la app.
- **Contrato único de feedback:** `InteractionFeedback.play(cue)` combina
  sonido y háptica; `LocalInteractionFeedback` lo distribuye y
  `Modifier.feedbackClickable(cue)` lo aplica en componentes compartidos. Los
  componentes no conocen `SoundPool` ni `Vibrator`.
- **Ajustes persistentes** en DataStore: `soundEffectsEnabled` y
  `hapticsEnabled`, ambos `true` por defecto. El composition root
  (`MainActivity`/`LinguaApp`) provee `LocalFeedbackSettings`.
- No se emite SFX por cada tecla del dictado; solo por eventos significativos.
  El teclado puede usar tick háptico opcional.
- Las animaciones de confirmación respetan `rememberAnimationsEnabled()`; el
  sonido y la háptica son refuerzos, nunca la única señal de estado.

**Alternativas consideradas:** (a) usar `ToneGenerator` para todo (rechazada:
sonidos pobres y difíciles de distinguir); (b) usar `playSoundEffect` del
sistema para todo (rechazada: no permite identidad propia ni eventos como
“encaje”); (c) packs de sonidos CC0 (rechazada: I5 exige arte original);
(d) háptica solo con `performHapticFeedback` (rechazada: no permite patrones).

**Consecuencias:**
- Nuevo paquete `core-ui/feedback` y assets `sfx_*.wav`.
- `SettingsRepository` y `SettingsScreen` ganan dos preferencias.
- `LinguaHaptics` amplía su API y conserva el fallback por si el dispositivo
  no tiene vibrador o no soporta amplitudes.
- Los componentes compartidos (`OptionCard`, `PrimaryButton3D`, `PathNode`,
  tableros y celdas de audio) emiten feedback sin cambios en cada pantalla.

**Invariantes protegidos:** I1 (todo local), I5 (sonido original), I8
(accesibilidad y ajustes).

---

## ADR-028: Micro-clases de gramática y feedback de acierto

**Estado:** Aprobado
**Fecha:** 2026-10-25

**Contexto:** el contenido ya declara `scenario`, `canDo`, `grammarPoints`
(con `description`) y `culturalNotes`, pero la UI no los muestra antes de la
lección. Tampoco existe una explicación cuando el alumno acierta:
`feedbackOnError` solo se usa al fallar. El alumno necesita una preparación
corta de **gramática y reglas** que le permita afrontar los ejercicios, y una
confirmación que refuerce el porqué de la respuesta correcta.

**Decisión:**
- La **micro-clase de materia** es una explicación breve de gramática/reglas
  y ejemplos, previa a los ejercicios. No es un ejercicio, no se califica, no
  consume vidas ni XP y siempre se puede saltar o reabrir.
- La **cápsula de mecánica** (cómo se responde, qué significa cada sonido,
  cómo se reintenta) es UI genérica por tipo de ejercicio y no forma parte del
  contenido pedagógico.
- Se añade un campo **opcional** `microLesson` a la lección v2:
  ```json
  "microLesson": {
    "title": "Condicionales mixtos",
    "goals": ["...", "..."],
    "sections": [
      {
        "heading": "Regla",
        "body": "...",
        "examples": [
          {"source": "...", "target": "...", "note": "..."}
        ]
      }
    ]
  }
  ```
  Es aditivo y opcional: no cambia `schemaVersion` ni rompe cursos v1. Si no
  existe, la UI construye un fallback con `grammarPoints[].description`,
  `scenario`, `canDo` y `culturalNotes`.
- Se añade un campo **opcional** `feedbackOnSuccess` a cada ejercicio v2:
  ```json
  "feedbackOnSuccess": {
    "explanation": "Por qué la respuesta es correcta.",
    "ruleRef": "gp-mixed-conditionals"
  }
  ```
  El grader lo devuelve solo en acierto; en error se mantiene
  `feedbackOnError`. Si falta, la UI muestra la respuesta correcta y, cuando
  exista, la descripción de la regla referenciada.
- **Regla editorial:** los ejemplos de la micro-clase no deben copiar los
  enunciados de los ejercicios; explican la regla, no adelantan respuestas.
- La micro-clase se muestra antes del primer ejercicio, con opción de saltar;
  se reabre desde el botón “Guía” de la cabecera. No bloquea el avance (I3).
- Todo el contenido sigue empaquetado y validado offline (`validateContent`).

**Alternativas consideradas:** (a) pantalla de teoría separada por lección
(rechazada: añade navegación y fricción); (b) generar explicaciones de acierto
solo con plantillas (rechazada como único mecanismo: útil como fallback, pero
no reemplaza la explicación pedagógica); (c) reutilizar `feedbackOnError` en
aciertos (rechazada: confunde el diagnóstico del error con el refuerzo).

**Consecuencias:**
- `Lesson` gana `microLesson`; `Exercise` gana `feedbackOnSuccess`; ambos con
  default `null`.
- `CourseValidator`, JSON Schema v2 y `docs/CONTENT_FORMAT.md` se actualizan
  en la fase de implementación.
- La autoría de micro-clases y explicaciones de acierto se hace por lección;
  el fallback garantiza que ninguna lección quede sin preparación.
- `GradingResult` gana `successExplanation`/`matchedRuleId` y la UI puede
  mostrar “por qué” sin romper el flujo de error.

**Invariantes protegidos:** I1, I3, I8.

---

## ADR-029: Tableros de respuesta horizontales y emparejamiento puzzle

**Estado:** Aprobado
**Fecha:** 2026-10-25

**Contexto:** `OrderWords` apila la frase elegida en una columna vertical y el
banco de palabras en un `Row` que se desborda; `MatchPairs` muestra dos listas
verticales con selección, sin una mecánica manipulativa. Se quiere una
experiencia más natural: frase horizontal y parejas que encajan como piezas
de rompecabezas, sin pistas de color ni de forma.

**Decisión:**
- **OrderWords:** la respuesta se construye en una tira horizontal con scroll
  y auto-scroll al último token; el banco usa `FlowRow` para envolver sin
  desbordar. Los tokens se identifican por índice/ID, no por texto, para
  soportar palabras repetidas. `Translate` conserva su tablero vertical.
- **MatchPairs:** tablero de piezas con una forma genérica de pestaña/ranura;
  todas las piezas comparten color y formato para no revelar la pareja. La
  pieza izquierda tiene ranura y la derecha pestaña; se puede tocar o
  arrastrar. Al comprobar, las parejas incorrectas se separan y las correctas
  quedan encajadas. No se cambia el `ExerciseGrader`.
- **SemanticMatch:** reutiliza la mecánica con una variante de texto largo y
  badges de registro/matiz; nunca usa color por pareja.
- **Accesibilidad:** emparejar por toque es siempre suficiente; cada pieza
  anuncia lado, texto y estado. Objetivos ≥ 48 dp. Reduced motion elimina
  rebotes y sacudidas.
- Sin dependencias nuevas: Compose nativo (`Canvas`, `Path`, `Animatable`,
  `pointerInput`) y arte propio (I5).

**Alternativas consideradas:** (a) mantener listas y solo cambiar colores
(rechazada: no resuelve el diseño); (b) validar cada encaje como correcto sin
“Comprobar” (rechazada: rompe la semántica de vidas/XP y el grader);
(c) usar una librería de drag-and-drop (rechazada: dependencia innecesaria).

**Consecuencias:**
- Nuevos composables `OrderWordsBoard` y `PuzzleMatchBoard`; sustituyen al
  tablero vertical anterior para `OrderWords` y `MatchPairs`.
- Tests de UI para añadir/quitar, encajar/separar y TalkBack.
- El gate F4 actualiza la detección del tablero por su etiqueta fija.

**Invariantes protegidos:** I5, I8, I3.

---

## ADR-030: Reintentos pedagógicos, vidas y fairness de XP

**Estado:** Aprobado
**Fecha:** 2026-10-25

**Contexto:** hoy no se puede reintentar un audio, un dictado o una frase
después de fallar. `submitAnswer` tampoco bloquea reenvíos tras el feedback,
lo que permite duplicar XP en algunos tipos. Se quiere una corrección real:
escuchar de nuevo, corregir y volver a intentar sin castigar dos veces.

**Decisión:**
- La FSM de lección gana un estado de reintento:
  `Answering → Graded → Retry → Answering`. El feedback deja de ser terminal
  para los tipos reintentables.
- Solo se reintentan los tipos de escucha/producción: `ListenChoose`,
  `AudioOnlyPrompt`, `ListenType`, `SpeakRepeat` y `FreeSpeakingPrompt`.
  En reconocimiento (opción, ordenar, emparejar, cloze) se revela la respuesta
  y se explica; no hay reintento.
- **Vidas:** el primer fallo consume 1 vida; los reintentos no consumen más.
  El SKIP de voz sigue sin consumir.
- **XP:** se otorga como máximo una vez por ejercicio. Un acierto tras
  reintento puede otorgar el XP del ejercicio, pero nunca duplicarlo.
- **Respuesta correcta:** en tipos reintentables no se revela la transcripción
  al primer fallo; se ofrece “Reintentar” y, si el alumno se rinde,
  “Ver respuesta”. En tipos no reintentables se revela al fallar.
- El estado de intentos (`retryCount`) es de sesión; no se persiste en Room
  en esta fase.
- Repetir audio y reproducción lenta (0.75x) están disponibles sin coste.

**Alternativas consideradas:** (a) reintentos infinitos con penalización por
cada intento (rechazada: castiga la corrección); (b) no revelar nunca la
respuesta (rechazada: bloquea el aprendizaje); (c) persistir intentos en Room
(se deja para una fase posterior con migración).

**Consecuencias:**
- `LessonViewModel` gana guardas de envío, contador de intentos y control de
  XP/vidas por ejercicio.
- `TypePanel` deja de guardar el texto en estado local.
- Nuevos botones de reintento/reproducción en `BottomActions`.
- Tests de FSM para no duplicar XP ni vidas y para el flujo de reintento.

**Invariantes protegidos:** I3, I4, I8.

---

## ADR-031: Profundidad táctil 3D y sonido cálido v2 («Feather 2.0», Fase 1)

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** el lenguaje "Feather" (ADR-026) definió cuerpo + borde inferior,
pero la implementación se quedó a medias: `PrimaryButton3D` solo escala al
presionar y `OptionCard` es un borde plano sin volumen. Los SFX de ADR-027 son
senoidales muy secos y faltan eventos para cofre, giro de tarjeta y pérdida de
vida. El resultado se percibe plano y poco táctil. Se quiere una base física
consistente y un set de sonido más cálido sin dependencias externas ni arte de
terceros (I5) y sin romper accesibilidad (I8).

**Decisión:**
- **Física de hundimiento real:** todo control flat-3D (botones, tarjetas de
  opción, nodos, chips) desplaza su cuerpo `PRESS_DEPTH_DP = 4 dp` sobre el
  borde inferior al presionarse y vuelve con `LinguaMotion.SpringBouncy`; la
  escala ya no es la única señal de presión.
- **Bisel de luz superior:** un degradado blanco con `HIGHLIGHT_ALPHA = 0.18`
  en el tercio superior del cuerpo da volumen sin sombras difusas ni coste de
  render. Ambos valores son tokens aditivos en `LinguaTokens.kt`.
- **`TokenChip` compartido:** los tokens de `OrderWords` dejan de ser un
  componente privado de `feature-lesson` y pasan a `core-ui` con la misma
  física 3D, semántica `Role.Button` + `selected` y cues `SELECT`/`DESELECT`.
- **SFX v2 originales:** el generador `tools/generate_sfx.ps1` incorpora
  síntesis con armónicos y envolventes percusivas (timbre de madera/marimba y
  acordes suaves) en lugar de senos puros. Se sintetizan a **22,05 kHz** mono
  16-bit (suficiente para sonidos de UI) para que el set completo, con cuatro
  cues nuevos, siga bajo el presupuesto de 150 KB de ADR-027. Sigue siendo
  arte original sintetizado en el repo (I5), sin samples externos.
- **Nuevos cues:** `STREAK_COMBO` (racha de aciertos), `CARD_FLIP` (revelar
  tarjeta de repaso), `HEART_LOST` (pérdida de vida) y `CHEST_OPEN`
  (desbloqueo de cofre). Cada uno tiene sonido, patrón háptico y fallback
  `performHapticFeedback`.
- **Conexión inmediata:** `ChestRewardSheet` emite `CHEST_OPEN` al mostrarse,
  `ReviewScreen` emite `CARD_FLIP` al revelar y `LessonScreen` emite
  `HEART_LOST` cuando bajan las vidas. `STREAK_COMBO` queda disponible para
  la coreografía de combos de la Fase 3.
- **Accesibilidad y rendimiento:** con `rememberAnimationsEnabled() == false`
  el hundimiento se aplica en estado final (sin rebote); el sonido y la
  háptica siguen siendo refuerzos, nunca la única señal. Sin librerías nuevas.

**Alternativas consideradas:** (a) escalar en vez de hundir (rechazada: se
percibe plano y no usa el borde inferior); (b) sombras difusas y `elevation`
(rechazada: coste de render y rompe el lenguaje flat-3D); (c) packs de SFX
externos (rechazada: I5 exige identidad propia); (d) dejar `TokenPill` privado
(rechazada: duplica física y estilos entre pantallas).

**Consecuencias:**
- `LinguaTokens` gana `PRESS_DEPTH_DP` y `HIGHLIGHT_ALPHA`; `LinguaMotion`
  reutiliza sus springs existentes.
- `PrimaryButton3D`, `OptionCard`, `PathNode` y el nuevo `TokenChip` comparten
  la misma física de hundimiento y bisel.
- Se regeneran los WAV de `core-ui/res/raw` y se añaden cuatro archivos.
- `FeedbackCue`, `UiSoundPlayer` y `LinguaHaptics` se amplían; los tests de
  háptica cubren los nuevos cues.
- No cambia el contrato de ninguna pantalla ni el estado de presentación.

**Invariantes protegidos:** I1 (todo local), I5 (sonido y arte originales), I8
(accesibilidad, reduced motion y ajustes de sonido/háptica).

---

## ADR-032: Lumi expresiva, banners de unidad por etapa y camino vivo (Fase 2)

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** Lumi existe como un vector monocromo teñido de un solo color y
solo flota verticalmente; no reacciona al contexto. Las cabeceras de unidad
del camino son texto plano sin jerarquía visual ni progreso integrado, y los
conectores entre nodos son trazos estáticos. La app sigue sintiéndose plana en
la pantalla principal. Se quiere una mascota con identidad y emociones, un
camino con etapas legibles y movimiento con propósito, sin arte de terceros
(I5) ni coste de rendimiento.

**Decisión:**
- **Lumi multicapa original:** el vector `ic_lumi.xml` se rediseña con capas
  de color propias (cuerpo púrpura, pecho claro, ala turquesa, pico ámbar y
  ojo oscuro con brillo). Se pinta con `Image` sin `tint`; el parámetro `tint`
  desaparece de `LumiMascot`.
- **`LumiMood`:** `IDLE` (flote suave), `HAPPY` (rebote + destellos),
  `ENCOURAGING` (balanceo empático) y `LISTENING` (pulso atento). Cada mood
  define duración y transformaciones; con `rememberAnimationsEnabled() ==
  false` se muestra el estado final sin movimiento (I8). Lumi sigue siendo
  decorativa para TalkBack (`contentDescription = null`).
- **`PathStage` (A1→B2):** enum en `core-ui` que mapea la etapa a tokens
  existentes, sin hex nuevos: A1→`ACCENT`, A2→`INFO`, B1→`PRIMARY`,
  B2→`WARNING`. La etapa se asigna por **índice del nivel** en la rampa del
  curso (0→A1, 1→A2, 2→B1, 3→B2), no por el nombre editorial del nivel; no se
  persiste.
- **`UnitHeader` como banner 3D:** cuerpo con borde inferior, bisel de luz,
  insignia de etapa con su color, título, contador y barra de progreso
  integrada. Mantiene semántica de heading y el texto "N de M lecciones".
- **Camino vivo:** el conector del tramo activo dibuja un punto que recorre la
  curva (`PathMeasure`); sin animaciones del sistema el punto no se muestra.
  El color del tramo completado se conserva.
- **Sin dependencias nuevas:** Compose nativo (`Canvas`, `PathMeasure`,
  `graphicsLayer`). Sin Rive/Lottie.

**Alternativas consideradas:** (a) seguir tiñendo Lumi de un color (rechazada:
pierde identidad y expresividad); (b) mapear la etapa por el nombre del nivel
(rechazada: los nombres son editoriales, no etiquetas CEFR); (c) usar
`LinearProgressIndicator` de Material dentro del banner (rechazada: añade
semántica anidada y rompe el lenguaje 3D); (d) animar el trazo completo del
conector (rechazada: más caro y visualmente ruidoso que un punto).

**Consecuencias:**
- `PathItem.Header` gana `stage: PathStage = PathStage.A1` (aditivo).
- `LumiMascot` gana `mood`; `RewardSheet` usa `HAPPY` y el camino usa `IDLE`
  o `ENCOURAGING` según el nodo activo.
- `UnitHeader` gana `stage`; las llamadas existentes siguen compilando por el
  valor por defecto.
- Tests del adaptador cubren la asignación de etapas; la galería visual puede
  capturar los cuatro moods.

**Invariantes protegidos:** I1, I5 (arte original), I8 (reduced motion y
TalkBack).

---

## ADR-033: Lección viva — panel de feedback, combos, ondas y fichas animadas (Fase 3)

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** el feedback de la lección aparece como un banner plano que empuja
el contenido y comparte el espacio con botones sueltos; no hay refuerzo de
racha de aciertos, los paneles de audio no comunican visualmente que se está
reproduciendo o escuchando, y las fichas de los tableros se mueven sin
transiciones. La lección se siente estática pese a tener ya sonido y háptica.

**Decisión:**
- **Panel de feedback deslizante:** al calificar, el bloque de feedback
  (banner + reintento/ver respuesta + Continuar) entra desde abajo con
  `slideInVertically + fadeIn` sobre un `LinguaCard3D`, con Lumi `HAPPY` en
  acierto y `ENCOURAGING` en error. Con reduced motion solo hay fundido o
  estado final, sin desplazamiento.
- **Combo de aciertos:** `LessonUiState.combo` cuenta aciertos consecutivos
  (se reinicia al fallar) y vive solo en la sesión; no se persiste. A partir
  de 2 se muestra una insignia animada con llama y el cue `STREAK_COMBO`
  sustituye al `CORRECT` (nunca se solapan). Es refuerzo, no la única señal.
- **Ondas de audio reactivas:** nuevo `VoiceWaveIndicator(active)` en
  `core-ui` (barras con fase animada; estado bajo y estático si no está
  activo). `LessonUiState.speaking` se enciende mientras el `TtsEngine.speak`
  suspendido está en curso, de modo que las ondas reflejan reproducción real;
  en `SpeakRepeat` reflejan la escucha del micrófono y en `FreeSpeakingPanel`
  la energía RMS real se complementa con un pulso suave mientras graba.
- **Fichas animadas:** las fichas elegidas de `OrderWords` usan
  `animateItem()` en la `LazyRow`; las piezas de `ClozeDragDropBoard` y
  `PuzzleMatchBoard` escalan suavemente mientras se arrastran (elevación
  visual sin sombras difusas).
- **Sin dependencias nuevas** y sin cambios de contrato del grader ni del
  contenido.

**Alternativas consideradas:** (a) mantener el banner plano y solo añadir
sonido (rechazada: no resuelve la percepción estática); (b) animar ondas con
amplitud simulada durante el TTS (rechazada: sería deshonesto; se usa el
estado real de `speak`); (c) persistir el combo (rechazada: es refuerzo de
sesión, no progreso); (d) usar librerías de animación (rechazada: Compose
nativo basta).

**Consecuencias:**
- `LessonUiState` gana `combo` y `speaking` (aditivos, con default).
- `AudioOnlyPromptPanel` gana `speaking: Boolean = false`; `AudioPanel` y
  `SpeechPanel` internos lo consumen.
- Nuevo `VoiceWaveIndicator` público en `core-ui`.
- El panel de feedback cambia de composición, no de mensajes ni de
  accesibilidad (live region se conserva en `FeedbackBanner`).
- Tests unitarios cubren el combo (incremento y reinicio).

**Invariantes protegidos:** I1 (todo local), I3 (nada bloquea), I8 (reduced
motion, TalkBack y ajustes de sonido/háptica).

---

## ADR-034: Flashcards 3D y calificación semántica en el repaso (Fase 4)

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** el repaso muestra la palabra, un botón para revelar y cuatro
botones idénticos apilados. No hay sensación de tarjeta física, no se puede
escuchar la pronunciación y la calificación FSRS no comunica su semántica
(reintentar, difícil, bien, fácil). Se quiere una interacción de flashcard
real sin dependencias nuevas ni romper I3/I8.

**Decisión:**
- **`FlipCard3D` en `core-ui`:** tarjeta con cuerpo flat-3D (borde inferior y
  bisel) que gira en `rotationY` al tocarla y muestra la cara frontal
  (palabra) o trasera (traducción). Usa `cameraDistance` para perspectiva,
  `tween(520)` y, con reduced motion, cambio instantáneo sin giro. La tarjeta
  es un botón accesible (`Role.Button`, `stateDescription` y
  `onClickLabel`) y conserva el texto real para TalkBack.
- **Pronunciación:** `ReviewViewModel` carga el curso para conocer el idioma
  meta e inyecta `TtsEngine`; `speakTranslation()` reproduce la traducción y
  expone `speaking` (para `VoiceWaveIndicator`) y `speechAvailable` (si el
  motor está listo). Sin voz, el botón queda deshabilitado y el repaso sigue
  (I1, I3).
- **Botonera semántica:** una fila de cuatro botones 3D compactos con color
  por intención: `AGAIN` coral, `HARD` ámbar (texto oscuro por contraste),
  `GOOD` turquesa y `EASY` púrpura. `PrimaryButton3D` gana parámetros
  opcionales de color de cuerpo/borde/contenido y `compact` (altura mínima
  48 dp, tipografía `labelLarge`), sin cambiar su comportamiento por defecto.
- **Orden de la pantalla:** cabecera y progreso, tarjeta 3D, botón
  “Mostrar traducción” antes de revelar; tras revelar, pronunciación y fila
  de calificación. El cue `CARD_FLIP` sigue acompañando el revelado.
- **Sin dependencias nuevas.**

**Alternativas consideradas:** (a) mantener la lista de botones (rechazada:
no comunica intención y se siente plana); (b) usar `AnimatedContent` con
escala en vez de giro 3D (rechazada: se pidió una flashcard física);
(c) pronunciar la palabra de origen (rechazada: el objetivo es el idioma
meta); (d) crear un botón nuevo solo para el repaso (rechazada: se extiende
`PrimaryButton3D` de forma aditiva).

**Consecuencias:**
- Nuevo `FlipCard3D` público en `core-ui`.
- `PrimaryButton3D` gana `bodyColor`, `edgeColor`, `contentColor` y `compact`
  con valores por defecto compatibles.
- `ReviewUiState` gana `speaking` y `speechAvailable`; `ReviewViewModel`
  inyecta `CourseRepository` y `TtsEngine`.
- `ReviewScreen` reorganiza su contenido; el contrato FSRS y las vidas no
  cambian.

**Invariantes protegidos:** I1 (voz local), I3 (nunca bloquea), I8
(TalkBack, contraste y reduced motion).

---

## ADR-035: Bento de progreso, onboarding ilustrado y transiciones (Fase 5)

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** el perfil es una lista de cajas grises apiladas, el onboarding
usa tarjetas planas sin identidad por idioma y la navegación cambia de
pantalla sin transición. Son las últimas pantallas que rompen el lenguaje
flat-3D del resto de la app.

**Decisión:**
- **Perfil Bento:** tarjeta héroe con anillo de dominio animado (lecciones
  completadas / totales), Lumi y nivel visual derivado del XP; cuadrícula 2×2
  de tarjetas 3D (XP, lecciones, tarjetas, repaso pendiente) y tarjeta de
  racha a ancho completo con mejor marca. Todo usa `LinguaCard3D` y tokens
  existentes; sin nuevas dependencias.
- **Iconos originales nuevos:** `ic_star` (XP) e `ic_cards` (repaso) en
  `core-ui`, registrados en `LinguaIcons` (I5). No se usan packs externos.
- **Onboarding ilustrado:** Lumi `HAPPY` en la bienvenida, insignia de idioma
  con color por destino (`EN`→`INFO`, `PT`→`ACCENT`, `FR`→`PRIMARY`,
  `JA`→`WARNING`) y un panel de confirmación animado al seleccionar el curso.
  La selección sigue siendo `OptionCard` (RadioButton) para conservar I8.
- **Nivel y dominio:** `ProfileStats` centraliza `levelFor`/`xpIntoLevelFor`
  (100 XP por nivel, mismo criterio que el camino) con test unitario; no se
  persiste nada nuevo.
- **Transiciones de navegación:** `NavHost` usa fundido entre pestañas y
  deslizamiento lateral para Lección y Repaso, con `pop` inverso. Con
  reduced motion las duraciones son 0 (cambio instantáneo).
- **Sin cambios de contrato** en repositorios, rutas ni contenido.

**Alternativas consideradas:** (a) mantener la lista de tarjetas del perfil
(rechazada: no jerarquiza la información); (b) crear arte raster por idioma
(rechazada: peso de APK e I5; basta insignia tipográfica original);
(c) transición global única (rechazada: las pestañas no deben deslizarse como
una lección); (d) animar el anillo con `infiniteRepeatable` (rechazada: debe
reflejar el valor real, no decorar).

**Consecuencias:**
- `ProfileUiState` gana `level` y `xpIntoLevel` (aditivos).
- Nuevo `ProfileStats` con test; nuevos `ic_star`/`ic_cards`.
- `ProfileScreen` y `OnboardingScreen` se reorganizan; `LinguaApp` añade
  transiciones.
- Sin nuevas dependencias ni cambios de esquema.

**Invariantes protegidos:** I1, I5 (arte original), I8 (reduced motion,
TalkBack y contraste).

---

## ADR-036: Ajustes pedagógicos de la lección

**Estado:** Aprobado
**Fecha:** 2026-10-31

**Contexto:** la micro-clase previa (“Antes de empezar”) incluye una sección
de mecánicas de interfaz que compite con la explicación del tema; los
ejercicios de opción validan al tocar una palabra, sin dar tiempo a elegir;
`Translate` construye la frase en vertical; el emparejamiento solo acepta
empezar por la columna izquierda; y la lección termina sin una medición
porcentual ni una oportunidad de corregir los fallos. Se quiere una
experiencia más clara y formativa sin romper I1/I3/I8.

**Decisión:**
- **Brief temático:** se elimina `mechanics` del brief y de su UI. “Antes de
  empezar” explica gramática, tema, objetivos y ejemplos. Si no hay
  `microLesson`, se usa `scenario`, `canDo` y `grammarPoints`; el último
  recurso es una sección temática con el título y nivel, nunca instrucciones
  de juego. (Sustituye parcialmente ADR-028.)
- **Glosario y tooltip:** se deriva un glosario de la lección desde
  `VocabularyExtractor` (MatchPairs, notas culturales) y
  `microLesson.examples`, con clave en el idioma meta en minúsculas. Al
  mantener pulsada una ficha se muestra su traducción en un popup **encima**
  de ella; se oculta al soltar y no interfiere con la selección. Sin entrada
  de glosario no se muestra popup. La cobertura total requerirá un campo
  opcional `glossary` en el contenido (fase posterior).
- **Validación explícita:** en ejercicios de opción
  (`MultipleChoice`, `ListenChoose`, `AudioOnlyPrompt`, `KanaChoose`,
  `KanjiReading`, `ParticleFill`) tocar solo selecciona; el grader se invoca
  con “Siguiente”. Tokens, cloze y parejas conservan “Comprobar”.
- **Formar frases horizontal:** `TokenBoard` (Translate) usa una tira
  horizontal con scroll y `animateItem`, igual que `OrderWords`.
- **Emparejamiento bidireccional:** nuevo `selectedRight`; se puede empezar
  por cualquiera de las dos columnas. El arrastre y la separación se
  conservan; se ignoran piezas ya emparejadas.
- **Medición final:** se cuenta el **primer intento** de cada ejercicio
  (acierto/error). La nota es `aciertos / total` en porcentaje entero y se
  muestra en `RewardSheet` junto a aciertos y errores. Los reintentos y la
  corrección no modifican la nota.
- **Corrección antes de terminar:** si hay fallos, al llegar al final se
  muestra una hoja con el número de errores y dos acciones: “Repetir errores”
  (principal) y “Ver resultados” (secundaria, respeta I3). La repetición usa
  solo los ejercicios fallados, sin XP ni vidas, y al terminar muestra la
  tabla final. (Complementa ADR-030.)

**Alternativas consideradas:** (a) mantener las mecánicas en el brief
(rechazada: el alumno necesita el tema, no instrucciones); (b) inventar
traducciones o empaquetar un diccionario externo (rechazada: I5 y calidad);
(c) validar al seleccionar (rechazada: impide cambiar de opinión); (d) nota
sobre el resultado tras corregir (rechazada: la corrección es formativa);
(e) bloqueo obligatorio de la ventana de corrección (rechazada: I3).

**Consecuencias:**
- `LessonBriefUi`/`LessonBriefBuilder` pierden `mechanics`.
- `LessonUiState` gana `glossary`, `selectedRight`, contadores de score,
  `mistakes`, `showMistakeReview` y `correctingMistakes`.
- `TokenChip` gana `translation` y popup de long-press; los tableros de
  parejas ganan `selectedRight`.
- `RewardSheet` gana tabla opcional de resultados; nuevo `MistakeReviewSheet`
  y `LessonScoring` con test.
- Sin cambios de esquema ni dependencias nuevas.

**Invariantes protegidos:** I1 (offline), I3 (nada bloquea), I8 (TalkBack,
reduced motion y contraste).

---

## ADR-037: Accesibilidad AA: tintas sobre color y HUD de vidas numérico

**Estado:** Aprobado
**Fecha:** 2026-11-01

**Contexto:** la revisión manual en emulador y la revisión de video de la
app (2026-11-01) confirman que los estados con color pincelan texto con el
color crudo sobre fondos tintados del mismo tono: en modo claro,
`Accent`/`Destructive` sobre su tinte 12% rinde ~2.3:1 y ~2.8:1; en modo
oscuro el estado seleccionado (`primary` sobre tinte 6%) rinde ~3.1:1. Todo
por debajo del mínimo WCAG AA 4.5:1 que exige I8. Además, la fila de 10
corazones de `HeartsIndicator` ocupa ~218dp de cabecera en lección, home y
repaso, saturando la zona superior, y el track de `LiquidXpBar` apenas se
distingue del fondo en oscuro.

**Decisión:**
1. **Tintas sobre color**: nuevos tokens `PRIMARY_INK`, `ACCENT_INK` y
   `DESTRUCTIVE_INK` (más variantes oscuras) en `LinguaTokens`, resueltos por
   tema en `linguaExtendedColors()` siguiendo el patrón de los corazones
   (ADR-026). Los colores crudos siguen siendo los fondos tintados, bordes e
   iconografía grande; la tinta solo colorea texto e iconos de estado en
   `OptionCard`, `PuzzleMatchBoard`, `SemanticMatchBoard`, `FeedbackBanner` y
   los enlaces `primary` de home, repaso, sincronización y cabecera de
   lección. Un test JVM verifica el ratio WCAG ≥ 4.5:1 de cada tinta contra
   su tinte calculado por composición alfa, todo desde los hex canónicos.
2. **HUD de vidas numérico**: `HeartsCounter` (píldora `❤ 8/10` con pulso
   animado al cambiar) sustituye a `HeartsIndicator` como único representante
   de vidas en lección, home y repaso. La semántica TalkBack mantiene
   “N de max vidas” y el latido por pérdida de vida de ADR-026/ADR-027.
3. **Límites del progreso**: `LiquidXpBar` dibuja un borde de 1dp alrededor
   del track para que los extremos de la barra sean visibles en ambos temas.
4. `PrimaryButton3D` deshabilitado baja el alpha de cuerpo/borde de 0.45 a
   0.35 para que se distinga de un botón activo.

**Alternativas consideradas:** (a) relleno sólido con texto blanco estilo
Duolingo (rechazada: exigiría oscurecer `Accent`/`Destructive` y rompe
§7.1/ADR-031); (b) sobrescribir `colorScheme.primary` en oscuro (rechazada:
rompe los rellenos sólidos con `onPrimary` blanco, p. ej. `TokenChip`
seleccionado); (c) mantener los 10 corazones con tamaño menor (rechazada: la
fila sigue consumiendo ancho de cabecera sin aportar información legible a
distancia).

**Consecuencias:**
- Los hex existentes no cambian (la identidad se preserva); solo se añaden
  tintas, con test de identidad y de contraste en `core-ui`.
- `HeartsIndicator` se elimina: su único representante pasa a ser
  `HeartsCounter`; se actualizan previews, `DesignGalleryScreenshotTest` y
  `PathDesignAccessibilityTest`.
- El estilo Feather se conserva: fondos tintados al 6–12% + texto en tinta
  oscura (claro) o clara (oscuro).
- Sin cambios de esquema, de contenido ni de dependencias.

**Invariantes protegidos:** I5 (identidad original), I8 (contraste AA y
TalkBack), I7 (la lógica de vidas no cambia).

---

## ADR-038: Guía de origen opcional en OrderWords

**Estado:** Aprobado
**Fecha:** 2026-11-02

**Contexto:** `OrderWords` presenta solo los tokens objetivo y la
instrucción genérica "Toca las palabras de abajo". Sin frase de referencia ni
audio, el alumno no puede saber qué oración reconstruir: la revisión manual
en emulador y el video de la lección B2 "Correos que no se malinterpretan"
confirman la adivinanza ciega tanto en A1 como en B2.

**Decisión:** añadir `sourcePhrase` **opcional** a `Exercise.OrderWords` y a
`course-v1.json` (retrocompatible: ausente = ejercicio sin guía).
`OrderWordsBoard` muestra la frase de origen sobre la zona de respuesta y, si
el ejercicio declara `audioText`, ofrece el reproductor con la frase hablada
(título neutro para no revelar el texto). El validador rechaza
`sourcePhrase` en blanco si se declara. Los 39 ejercicios `OrderWords`
existentes (35 es-en, 2 es-fr, 2 es-pt) reciben su frase guía en un cambio
de contenido separado del código.

**Alternativas consideradas:** (a) derivar la guía de los ejemplos de la
micro-clase o del glosario (rechazada: solo 0/35 frases coinciden y el
glosario es palabra a palabra); (b) traducir con un modelo generativo
(rechazada: I1/I5, la app no usa LLM).

**Consecuencias:** campo opcional nuevo en dominio, esquema, validador y
`CONTENT_FORMAT.md`; contenido actualizado sin romper ejercicios previos;
sin cambios de datos persistidos ni dependencias nuevas.

**Invariantes protegidos:** I1 (offline), I5 (contenido propio), I7
(validador y grader testeables).

---

## ADR-039: Tolerancia a diacríticos en la evaluación

**Estado:** Aprobado
**Fecha:** 2026-11-02

**Contexto:** la evaluación normaliza minúsculas, puntuación y espacios, pero
no los diacríticos: en el dictado francés "Bonne journee" falla y cuesta una
vida frente a "Bonne journée" (prueba manual en emulador, 2026-11-01). En
teclados sin distribución francesa escribir é/è exige pulsación larga, así
que la exigencia penaliza el medio y no la competencia.

**Decisión:** `ExerciseGrader.normalize()` descompone en NFD y elimina los
signos combinantes **latinos** (U+0300–U+036F) antes de comparar. El rango es
deliberadamente estrecho para no tocar los signos de sonoridad del japonés
(U+3099/U+309A), que distinguen が/か y deben seguir contando. Aplica a
Translate, OrderWords, ListenType y SpeakRepeat (todos usan `normalize`).

**Alternativas consideradas:** (a) mantener estricto con aviso "falta
tilde" (rechazada por el usuario en la decisión de diseño); (b) usar
`\p{Mn}` genérico (rechazada: rompería kana con dakuten).

**Consecuencias:** el dictado y las frases aceptan texto sin tildes ni
diéresis; se añade test de regresión que fija el comportamiento japonés sin
cambios; sin cambios de contenido ni de esquema.

**Invariantes protegidos:** I7 (grader puro y testeable), I3 (sin fricción
injustificada).

---

## ADR-040: Glosario por lección, long-press de palabras y etapa del camino por CEFR

**Estado:** Aprobado
**Fecha:** 2026-11-02

**Contexto:** el long-press de traducción existía solo en las fichas de
tokens y en las opciones, con **una** traducción por palabra y un glosario
derivado únicamente de los ejemplos de la micro-clase y del vocabulario
revisable (pares de `MatchPairs` e idiomas). En los enunciados, la frase
origen y los pasajes no había forma de consultar una palabra. Además, el
camino asignaba la etapa CEFR por el **índice** del nivel del curso
(`PathStage.entries[levelIndex]`): con los niveles A1/B1/B2 actuales, el
nivel B1 se mostraba como “A2” y el B2 como “B1”, reforzando la sensación de
salto de dificultad.

**Decisión:**
1. `glossary` **opcional** por lección en schema v2: lista de entradas
   `{term, translations[]}` con una o varias traducciones. Se fusiona con los
   ejemplos de la micro-clase y el vocabulario revisable
   (`LessonGlossary.fromLesson`, dominio puro). Para traducciones de una sola
   palabra se añade el reverso (español→inglés), de modo que el long-press
   también responde en enunciados y frases origen.
2. Nuevo componente `WordGlossaryText` (core-ui): divide el texto en
   palabras, detecta la pulsada con `TextLayoutResult` y ancla la burbuja
   sobre ella (`TranslationTooltip` con término + lista). Se usa en
   enunciados, frase origen de Translate, guía de OrderWords y respuesta
   modelo de producción oral. Las fichas y opciones pasan a
   `translations: List<String>?`.
3. Accesibilidad: acción “Ver traducciones” para TalkBack que abre la lista
   de palabras traducibles del texto (I8).
4. `PathStage` se deriva del `cefrLevel` de la primera lección del nivel
   (ADR-032 dejaba de depender del índice); el nivel mixto A1/A2 conserva la
   etapa de su primera lección.

**Alternativas consideradas:** (a) un diccionario bilingüe embebido
(rechazada por ahora: licencia de terceros e I5, tamaño y atribución);
(b) derivar palabras de los ejemplos con alineación automática (rechazada:
traducciones erróneas en frases largas); (c) mantener la etapa por índice
(rechazada: etiqueta niveles equivocados).

**Consecuencias:** modelo, esquema v2, validador y `docs/CONTENT_FORMAT.md`
ganan el campo; `LessonUiState.glossary` pasa a `Map<String, List<String>>`;
fichas y opciones cambian de `translation: String?` a
`translations: List<String>?`; las 6 lecciones A1 de es-en estrenan glosario
curado (el resto llega por tandas). El progreso guardado no cambia: solo se
tocan ids de lección, no de ejercicio.

**Actualización (cierre de UX):** el long-press también cubre el pasaje de
`ClozeDragDrop` y la respuesta canónica del `FeedbackBanner`. `Mi rutina` se
movió del nivel A1 al A2 conservando su id (progreso intacto), así que la
rampa queda A1 5 / A2 17 / B1 20 / B2 22 = 64 lecciones y 554 ejercicios.
`ContentRampTest` añade la comprobación de que cada nivel agrupa un solo
CEFR, para que no vuelva a colarse una lección A2 en A1.

**Actualización posterior (A1):** se añadió la unidad `es-en-a1-un-3`
“Vida cotidiana y entorno” con 5 lecciones y 35 ejercicios, así que A1 pasa
a 10 lecciones y el curso a **69 lecciones y 589 ejercicios**.

**Invariantes protegidos:** I1 (offline), I5 (contenido propio), I7
(glosario y lookup puros y testeables), I8 (acción TalkBack).

---

## ADR-041: Migración de los cursos de arranque pt/fr/ja a schema v2

**Estado:** Aprobado
**Fecha:** 2026-11-02
**Reemplaza parcialmente:** ADR-018 (paridad v1 de los cursos de arranque).

**Contexto:** `es-pt`, `es-fr` y `es-ja` quedaron como cursos de arranque en
v1: 2 lecciones y 12 ejercicios cada uno, sin `glossary`, `cefrLevel` ni
`feedbackOnError`. La UI compartida ya ofrecía long-press de traducciones y
feedback estructurado, pero esos cursos no podían aprovecharlos. Mantener v1
también obligaba a un test de congelación que impedía mejorar su contenido.

**Decisión:** migrar los tres cursos a `schemaVersion: 2` conservando el 100 %
de los ids de lección y de ejercicio. Cada lección declara `cefrLevel: A1`,
`vocabFrequencyBand: 1000-2000`, `canDo`, `microLesson` y un `glossary` curado;
todos los ejercicios reciben `feedbackOnError` específico. La migración
mantiene el alcance de entonces: **2 lecciones y 12 ejercicios por curso**,
sin inventar niveles ni añadir ejercicios nuevos en ese paso. `locale` queda en `pt-BR`, `fr-FR` y `ja-JP`.
El test `cursos_de_arranque_migran_a_v2_sin_cambiar_ids` sustituye al de
congelación v1 y verifica ids, conteos, glosario, micro-clase y feedback.

**Alternativas consideradas:** (a) dejar v1 y limitarse a corregir la
documentación (rechazada: la experiencia de long-press y feedback seguía
siendo desigual); (b) reescribir ahora los tres cursos completos A1–B2
(rechazada: sin revisión nativa y fuera del alcance); (c) migrar a v2 y
ampliar por fases (elegida).

**Consecuencias:** los tres cursos ganan glosario y feedback sin cambiar su
alcance; `validateContent` los enruta a v2; el test de congelación v1 se
reemplaza; la expansión A1/A2/B1/B2 se completó después (ver actualización),
aunque la revisión lingüística nativa sigue pendiente. La migración no toca
ids, así que el progreso guardado se conserva.

**Actualización posterior:** `es-pt`, `es-fr` y `es-ja` se ampliaron a **60
lecciones y 526 ejercicios** cada uno (A1–B2 completo). Los ids históricos y
la migración a v2 se mantienen.

**Invariantes protegidos:** I1 (offline), I4 (ids y progreso), I5 (contenido
propio), I8 (acción TalkBack), I9 (validación en CI).

---

## ADR-042: Expansión progresiva a C1/C2 con rampa verificada y protocolo de lotes

**Estado:** Aprobado (MREmmanuel09, 2026-11-08)
**Fecha:** 2026-11-08
**Plan de referencia:** [docs/PLAN_EXPANSION_C1_C2.md](PLAN_EXPANSION_C1_C2.md)

**Contexto:** el contenido 2.0.0 cubre A1–B2 en los cuatro cursos (249
lecciones / 2.167 ejercicios). `CefrLevel` y `VocabFrequencyBand` del dominio
no admiten C1/C2, el schema v2 no los declara y la rampa está verificada por
tests solo hasta B2. La app debe crecer manteniendo y escalando la dificultad
de forma progresiva en los cuatro idiomas.

**Decisión:**
1. Se extiende `CefrLevel` con `C1`, `C2` y `VocabFrequencyBand` con
   `8000-12000` y `12000+`. Cambio estrictamente aditivo: sin
   `schemaVersion: 3` ni migraciones; los cursos existentes siguen válidos
   porque no usan los valores nuevos.
2. El validador y el schema exigen a C1/C2 los mismos metadatos pedagógicos
   que a B1/B2 (`scenario`, `canDo`, 2 `grammarPoints`, banda léxica,
   ≥1 `culturalNote` y `feedbackOnError` en todos los ejercicios).
3. Escalones de dificultad por nivel, verificados con `ContentRampTest`
   extendido: 11 ejercicios por lección en C1 y 12 en C2, topes de 38 y 48
   tokens por frase y ≥1 (C1) / 2 (C2) `FreeSpeakingPrompt` con
   `grammarRuleRefs`. La rampa global `A1→A2→B1→B2→C1→C2` no retrocede.
4. Cada nivel entra primero por `es-en` como piloto y luego se replica en
   `es-fr`, `es-pt` y `es-ja` manteniendo el esqueleto de unidades; un
   release por nivel (2.1.0 – 2.4.0 según el plan).
5. Los ids históricos no se tocan y los nuevos siguen el patrón del curso
   sin reciclar identificadores ya existentes: el progreso guardado depende
   de la identidad de ejercicios y lecciones.
6. Protocolo por lote (una unidad del curso): redacción → rampa → gate local
   (`ktlintCheck detekt validateContent test`) → humo manual en emulador →
   commit `content:`. Nada de acumular deudas de contenido.

**Alternativas consideradas:** (a) `schemaVersion: 3` con migración
(rechazada: rompe retrocompatibilidad sin ganancia); (b) añadir niveles solo
a `es-en` (rechazada: desiguala los cuatro cursos); (c) sustituir lecciones
de B2 para liberar presupuesto (rechazada: viola el no-retroceso y el
progreso guardado).

**Consecuencias:** +168 lecciones y +1.920 ejercicios al cerrar el plan (417
lecciones / 4.087 ejercicios totales). El camino de aprendizaje gana dos
etapas (C1, C2) en `PathItems`. Peso estimado: < 2 MB de JSON por APK. La
revisión lingüística nativa se reordena por prioridad y sigue siendo el
mayor riesgo abierto; el gate mecánico evita regresiones de dificultad.

**Invariantes protegidos:** I1 (offline), I4 (progreso e ids), I5
(contenido propio CC BY-SA), I9 (contenido validado en CI).

---

## ADR-043: Captura de voz tolerante y evaluación oral sin falsos negativos

**Estado:** Aprobado
**Fecha:** 2026-11-08
**Se aplica en:** release 2.0.1 (hotfix de voz)

**Contexto:** con pronunciación correcta, la producción oral libre fallaba
por dos causas ajenas al estudiante. Primera la captura: el endpoint de
Vosk corta en pausas breves (~0,3 s) y el tope de captura era 6 s, de modo
que la transcripción llegaba truncada y de ahí `TOO_SHORT`/`LOW_COVERAGE`
("no registra bien"). Segunda el grader: `FreeSpeakingHeuristicGrader` no
normalizaba diacríticos (el grader de escritura sí, ADR-039), la tolerancia
difusa empezaba en 5 caracteres, la cadencia WPM se juzgaba sin margen y una
autoevaluación < 0,5 bloqueaba el paso aunque todas las señales objetivas
fueran buenas ("no lo pasa como bueno").

**Decisión:**
1. `SpeechCapture` (ADR-020) gana tres constantes de fábrica:
   `DEFAULT_MIN_CAPTURE_NANOS` = 1,5 s — un endpoint con texto antes de ese
   umbral es prematuro: su enunciado se concatena y la captura continúa —,
   `DEFAULT_ENDPOINT_GRACE_NANOS` = 1,2 s — tras un enunciado válido se
   sigue escuchando; si llega otro se concatena y la gracia se reinicia; si
   expira en silencio, la captura termina — y `DEFAULT_MAX_CAPTURE_NANOS` =
   10 s (antes 6 s). El texto acumulado se compone con `merge`, que deduplica
   por `endsWith` la repetición que Vosk reintroduce en `finalResult`. Los
   parámetros nuevos van al final de los constructores: usos y tests
   posicionales siguen compilando.
2. `FreeSpeakingHeuristicGrader` (ADR-019): `tokenize` normaliza NFD y
   filtra las marcas `[\u0300-\u036F]`, igual que la evaluación escrita
   (ADR-039); la rama japonesa no se normaliza (sus dakuten son contenido,
   no acentos, y el conteo por caracteres del test depende de ello). La
   tolerancia difusa (distancia ≤ 2) aplica desde 4 caracteres para
   absorber la fonética en palabras cortas. La cadencia admite un margen del
   ±20 %: es válida entre `minWpm/1.2` y `maxWpm*1.2`. La autoevaluación pasa
   a ser informativa: se reporta en `FreeSpeakingFeedback.advisories` y nunca
   bloquea `passes`; el enum conserva `LOW_SELF_ASSESSMENT` por
   compatibilidad.
3. El catálogo de hedging se amplía con formulaciones frecuentes (*i guess*,
   *i wonder*, *i suppose*, *it might*, *kind of*, *sort of* y
   *to be honest*), para que la gramática declarada no dependa de una sola
   muletilla.
4. La UI no cambia: `FreeSpeakingPanel` y `LessonViewModel` ya leen el
   desglose como lo tienen; `advisories` no se presenta como fallo.

**Alternativas consideradas:** (a) subir el umbral de silencio de Vosk
(rechazada: depende del modelo/versión en producción y no es testeable en
JVM); (b) descartar el enunciado prematuro sin concatenarlo (rechazada: una
respuesta breve y válida se perdería); (c) un LLM en dispositivos que avale
el audio (rechazada: ADR-019 descarta el modelo en dispositivo por peso, RAM
y batería, además de I1); (d) depender solo del `finalResult` de Vosk
(rechazada: cuando el stream termina sin endpoint llega vacío y se pierden
los parciales ya emitidos).

**Consecuencias:** la captura sobrevive pausas de hasta ~1,2 s entre frases
y enunciados cortos al inicio; pasan transcripciones correctas con tildes
omitidas por Vosk, WPM desplazado por el micrófono o autoevaluación dura.
Siguen fallando la longitud mínima real, la cobertura insuficiente y la
gramática exigida no detectada. Coste: hasta 1,2 s adicionales de grabación
cuando la voz termina en silencio, imperceptible para el usuario. El
contrato de los tests de captura (recuento de chunks) se conserva; los
tests de endpoint dejan la gracia en 0 para reproducir el corte antiguo de
2.0.0 y se añaden los casos prematuro/gracia/concatenación.

**Invariantes protegidos:** I1 (STT on-device), I3 (sin fricción), I4
(nada elimina progreso), I7 (evaluación determinista testeable en JVM).

---

## Registro de decisiones pendientes

| Tema | Se decide en | ADR previsto |
|---|---|---|
| Proveedor OneDrive (appfolder vía MSAL) | registrado en ADR-015 como diferido | - |

## ADR-044: Tipografía 100 % Nunito y tokens de contenedor (V-04, V-08)

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 2.5.0 (versionCode 13)

**Contexto:** `LinguaTypography` definía solo 7 de los 15 slots de Material 3;
los 8 restantes caían a Roboto. `OptionCard` pintaba el tinte de estado sobre
la superficie, dejando el texto con ratio 1,15:1 al seleccionar.

**Decisión:** completar los 15 slots con Nunito (pesos 400–800) y añadir
`SECONDARY/TERTIARY/ERROR_CONTAINER` en `LinguaTokens`/`LinguaColors`. En
`OptionCard`, pintar primero `colors.surface` opaco y el tinte después.

**Consecuencias:** tipografía de identidad homogénea (I5) y contraste AA en
opciones seleccionadas (I8). Test `LinguaTypographyNunitoTest`.

---

## ADR-045: Paleta extendida, bisel oscuro y `PathStage.onAccent` (V-03, V-09)

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 2.5.0 (versionCode 13)

**Contexto:** en modo oscuro el bisel superior dejaba una neblina gris y el
texto activo de `LinguaBottomBar` era verde mientras el icono era púrpura.
`PathStage.onAccent` declaraba tinta oscura solo para B2/C2.

**Decisión:** `topHighlight()` se vuelve `@Composable` y atenúa el alpha en
superficies oscuras (`DARK_HIGHLIGHT_ALPHA`). `PathStage.onAccent` usa tinta
oscura para A1/A2/B2/C2 y blanca para B1/C1. `LinguaBottomBar` unifica
icono y etiqueta en `primaryInk`. Se añaden `WARNING_INK`/`INFO_INK`.

**Consecuencias:** contraste AA verificado por `LinguaContrastTest`.

---

## ADR-046: Grader multilingüe y dirección de tarjetas FSRS (D-01, D-04)

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 2.5.0 (versionCode 13)

**Contexto:** `FreeSpeakingHeuristicGrader.patternFor` solo reconocía
etiquetas en inglés, por lo que FR/PT/JA marcaban `MISSING_GRAMMAR` siempre.
`VocabularyExtractor` dejaba la dirección invertida en `culturalNotes` y en
`MatchPairs` japoneses.

**Decisión:** `evaluate` recibe `language` y el catálogo se resuelve por
familia gramatical × idioma meta (EN/FR/PT/JA). `VocabularyExtractor.fromLesson`
normaliza a frente ES → reverso meta. `ReviewViewModel` pronuncia el término
en escritura japonesa cuando el curso es japonés.

**Consecuencias:** `FreeSpeakingHeuristicGrader` y el contrato de contenido
verifican familias gramaticales en EN/FR/PT/JA y que `modelAnswer` no
marque `MISSING_GRAMMAR` ni `LOW_COVERAGE`. Los prompts con gramática no
ejemplificada se resuelven con revisiones nativas de contenido separadas.

---

## ADR-047: Avance neutro al omitir un ejercicio de voz (B-02, I3)

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 2.5.0 (versionCode 13)

**Contexto:** `skipSpeech`/`skipFreeSpeaking` calificaban la respuesta vacía
como incorrecta: rompían el combo, sumaban un error y añadían el ejercicio a
`mistakes`.

**Decisión:** `submitAnswer` acepta `skipped`. Cuando es `true`, el combo se
conserva, los contadores no se mueven, el ejercicio no entra en `mistakes` y
el feedback es un `InfoBanner` informativo. La lógica pura vive en
`LessonAnswerRules`.

**Consecuencias:** saltar voz avanza sin castigo. Test `LessonAnswerRulesTest`.

---

## ADR-048: Navegación de 4 destinos, cambio rápido de curso, cofres y recuperación activa de vidas

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 3.0.0 (versionCode 14)

**Contexto:** el rediseño Feather 3.0 necesita acceso en un toque al repaso,
cambio de idioma sin volver al onboarding y recompensas de práctica que no
dependan solo del temporizador de vidas.

**Decisión:**
1. `LinguaBottomBar` pasa a 4 destinos (`Inicio · Repaso · Progreso · Ajustes`).
   El badge de Repaso observa las tarjetas vencidas del curso activo mediante
   `AppShellViewModel`. El destino inicial se guarda con `rememberSaveable`
   para no reconstruir el grafo al cambiar de curso.
2. `CourseSwitcherSheet` (core-ui) se abre desde Home y Ajustes; escribe
   `selectedCourseId` sin borrar el curso anterior ni pasar por onboarding.
3. El camino muestra el título bajo cada nodo, un FAB «Ir a mi lección» y
   conserva el auto-scroll al nodo activo.
4. El cofre de una unidad completada otorga `XP_PER_CHEST` (+20 XP) una sola
   vez. Los ids reclamados se guardan en DataStore junto a las preferencias;
   Room v5 los migrará a datos de progreso en F3.
5. En Repaso, cada 5 tarjetas superadas se recupera 1 vida
   (`HeartsRules.recoverOne`). «Otra vez» sigue consumiendo una vida. La regla
   pura vive en `ReviewHeartRules`.
6. El reverso de la tarjeta muestra la `culturalNote` derivada del contenido y
   ofrece audio a 1x/0.75x. La calificación usa cuadrícula 2×2 con
   `fontScale >= 1.5`.

**Consecuencias:** navegación de 4 pestañas, cambio de curso en 2 toques,
recompensa de cofre no duplicable y recuperación de vidas por práctica. Los
tests cubren la regla de recuperación y las reglas de vidas; la validación
visual se documenta en `docs/qa/fase2-3.0.0-resultados.md`.

---

## ADR-049: Persistencia de sesión en `SavedStateHandle` (B-08)

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 2.5.0 (versionCode 13)

**Contexto:** la rotación o la muerte del proceso expulsaban al usuario del
ejercicio actual.

**Decisión:** `LessonViewModel` guarda `index`, `xpEarned`, `correctCount`,
`incorrectCount` y los ids de `mistakes` en `SavedStateHandle`, restaurándolos
en `startLesson`. `MainActivity` espera la primera lectura de
`SettingsRepository` antes de montar el `NavHost` y sincroniza las barras del
sistema con el tema.

**Consecuencias:** rotación y *process death* retoman el ejercicio exacto.

---

## ADR-050: Room v5, `SyncSnapshot` v3 y respaldo local por SAF

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 3.0.0 (versionCode 14)

**Contexto:** el respaldo v2 no incluía el mazo FSRS (D-07), las columnas de
voz de Room v3 nunca se escribían, `daily_activity` no existía y los usuarios
sin Google Drive no tenían forma de exportar su progreso. Además, cuenta y
respaldo vivían en dos pantallas separadas.

**Decisión:**

1. `LinguagoDatabase` sube a **v5** con `MIGRATION_4_5` explícita y schema
   exportado. Añade `culturalNote` a `word_progress`,
   `speakingPassedAttempts`/`bestScorePercent`/`lastScorePercent` a
   `lesson_progress` y la tabla `daily_activity`. Ninguna fila existente se
   borra ni se trunca; la actividad histórica se siembra con el último día de
   racha. `Migration4To5Test` lo verifica sobre una base v4 poblada.
2. Cada lección completada actualiza `lastScorePercent`/`bestScorePercent`;
   cada respuesta oral libre registra intentos, aprobados, mejor WPM y última
   puntuación. Las revisiones FSRS y las lecciones incrementan
   `daily_activity` sin tocar la racha ni el XP total más de una vez.
3. `SyncSnapshot` sube a **v3** con `words: List<WordEntry>`. El merge conserva,
   por clave `(word, courseId)`, la entrada con más repasos y, en empate, la de
   `lastReviewEpochMillis` más reciente; los errores y la nota cultural se
   conservan con máximo/relleno. `SnapshotCodec` sigue aceptando v1, v2 y v3.
4. `SyncScreen` unifica cuenta y respaldo: estado legible del proveedor,
   vincular/desvincular Google Drive o laboratorio, copia cifrada en la nube y
   exportación/importación `.json` vía SAF sin cuenta. La importación local usa
   el mismo merge monótono que la nube (I4). La ruta `auth` deja de exponerse
   en la app; el módulo `feature-auth` permanece como API reutilizable.
5. El perfil muestra métricas reales del curso activo: mejor fluidez oral,
   precisión media, tasa de superación oral y calendario local de 28 días.

**Consecuencias:** restaurar un respaldo recupera racha, XP, vidas, lecciones y
tarjetas FSRS; el respaldo local funciona 100% sin Google; las métricas de voz
y actividad dejan de ser columnas muertas. La nube sigue siendo opcional (I1,
I3) y el merge nunca retrocede progreso (I4).

---

## ADR-051: `espeak-ng-data` compartido, gestor de voces y desglose CEFR

**Estado:** Aprobado
**Fecha:** 2026-11-09
**Se aplica en:** release 3.0.0 (versionCode 14)

**Contexto:** cada paquete Piper incluía una copia idéntica de
`espeak-ng-data` (~17,2 MB), triplicando ese árbol en `models/tts` y en el APK
`full`. Además, `filesDir` acumulaba voces y modelos extraídos sin forma de
consultarlos o liberarlos, y el perfil solo mostraba un conteo global de
lecciones.

**Decisión:**

1. `models/tts/espeak-ng-data` es el único árbol compartido. La tarea
   `fetchVoiceModels` lo extrae de la primera voz y elimina las copias
   locales; el empaquetado Gradle excluye `espeak-ng-data/**` de cada voz y
   lo inyecta una sola vez en `tts/espeak-ng-data`.
2. `PiperTtsEngine` usa la copia local de una caché antigua si existe; en
   instalaciones nuevas extrae el árbol compartido una sola vez con
   `AtomicInstall` y lo reutiliza entre voces. Las cachés previas siguen
   funcionando sin migración destructiva.
3. `VoiceStorageManager` (core-audio) consulta el espacio de
   `filesDir/tts` y `filesDir/stt`, cierra los motores nativos y borra las
   copias extraídas. Ajustes muestra el tamaño y pide confirmación antes de
   liberar; la próxima reproducción vuelve a extraer los assets sin red (I1).
4. `ProfileStats` calcula el avance real por etapa CEFR A1–C2 del curso
   activo y el perfil lo renderiza en seis píldoras, incluidas las etapas sin
   contenido o sin empezar.

**Consecuencias:** el árbol de modelos y el APK dejan de llevar dos copias
redundantes (~34 MB antes de compresión); el usuario puede recuperar espacio
sin perder progreso ni contenido; el perfil refleja la rampa CEFR real del
curso activo. La revisión editorial humana de B1–C2 permanece desacoplada en
commits `content:` para `v3.1`.

---
