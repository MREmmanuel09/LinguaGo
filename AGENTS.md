# AGENTS.md — Guía para agentes de código

> Referencia operativa para agentes IA (opencode, Cursor, Copilot Workspace…)
> y contribuidores automatizados que trabajen en **LinguaGo**. Léela antes de
> tocar código. La documentación manda sobre este archivo: si hay conflicto,
> prevalecen `ARCHITECTURE.md` y `docs/DECISIONS.md`.

## Qué es este repositorio

App Android nativa **offline-first** de aprendizaje de idiomas (ES→EN/PT/FR/JA)
con 21 módulos Gradle, Clean Architecture, TTS/STT on-device, repaso espaciado
**FSRS** y respaldo opcional **cifrado** a Google Drive. Código MIT, contenido
CC BY-SA 4.0. Invariantes I1–I9 en el README: **no se rompen**.

Estructura rápida: `app/` (composition root) · `core/*` (dominio, datos, ui,
content, audio, auth, sync, common) · `feature/*` (pantallas) · `content/*`
(cursos como assets) · `build-logic/` (convention plugins) · `docs/`.

## Comandos esenciales (Windows PowerShell)

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"   # JBR de Android Studio

./gradlew ktlintCheck detekt validateContent test   # gate completo (= CI job 1)
./gradlew ktlintFormat                              # autofix de estilo (llamada separada)
./gradlew :app:assembleDebug                        # APK debug (assets con models/, pesado)
./gradlew :app:assembleRelease                      # valida R8 (firma debug si no hay keystore)
./gradlew :core:core-ui:connectedDebugAndroidTest   # tests instrumentados (emulador/dispositivo)
./gradlew validateContent                           # JSON Schema v1 de los 4 cursos
```

Emulador local: AVD `Medium_Phone_API_37.0`. En CI los instrumentados corren
en un emulador **API 34**.

## Mapa: dónde tocar según la tarea

| Quiero… | Módulo | Notas |
|---|---|---|
| Regla de negocio, grader, FSRS | `core-domain` | Kotlin puro, sin imports de Android ni Compose |
| Nueva pantalla / flujo | `feature/<nombre>` | Plugin `linguago.android.feature`; registrar ruta en `app/.../Routes.kt` + `LinguaApp.kt` |
| Persistencia (Room/DataStore) | `core-data` | Cambios de schema: subir versión + `Migration` + JSON en `core-data/schemas` (se commitea) |
| Componente visual | `core-ui` | Tokens en `LinguaTokens`; identidad visual = ADR |
| Contenido/lecciones | `content/content-*` | Ver `CONTENT_FORMAT.md`; validar con `validateContent` |
| Audio (TTS/STT) | `core-audio` | Contratos `TtsEngine`/`SttEngine`; modelos por SHA-256 |
| OAuth/Drive | `core-auth` | PKCE sin secretos; tokens cifrados con Keystore |
| Sync/snapshot | `core-sync` | `CloudProvider` (Strategy); merge **monótono** (I4) |
| Build/plugins/calidad | `build-logic` | Convention plugins; no dupliques DSL en módulos |

## Convenciones obligatorias

- **Conventional Commits** en español: `feat:`, `fix:`, `docs:`, `content:`,
  `chore:`, `style:`. Nunca mezclar código con contenido pedagógico.
- **ADR obligatorio** en `docs/DECISIONS.md` para decisiones de arquitectura,
  datos, seguridad o que toquen invariantes (formato en ADR-000).
- `explicitApi()` en librerías: visibilidad explícita (`public`) + KDoc en la
  API pública. Mantén ese estilo; no añadas comentarios decorativos.
- **Contenido: UTF-8 SIN BOM.** Un BOM rompe el parseo.
- Sin secretos en el repo: keystore y `googleClientId` van por gradle
  properties (`local.properties`), nunca commiteados.

## Trampas conocidas (aprendidas a golpes)

1. **Config cache**: no captures `fileTree(...)` dentro de tareas; materializa
   a `List<File>` (ver tarea `validateContent` en `build.gradle.kts`).
2. Clases presentes solo en un classpath auxiliar (p. ej. el validador JSON):
   usa el `buildscript { dependencies { classpath(...) } }`; resolver por
   reflexión con `URLClassLoader` mataba el daemon de Gradle.
3. **ktlint**: `ktlintFormat` primero y `build` después, en llamadas
   separadas. Nombres de test con `:` o `..` no compilan; mantén el formato.
4. **Room**: todo cambio de schema exige versión + `Migration` explícita +
   registrar el JSON de `schemas/`. Nunca `fallbackToDestructiveMigration`.
5. **R8**: si tocas audio (JNA/Vosk/sherpa-onnx) o serialización, revisa los
   keeps de `app/proguard-rules.pro` y valida con `assembleRelease` + smoke en
   dispositivo/emulador.
6. **Espresso ≥ 3.7.0** (las versiones viejas fallan con
   `InputManager.getInstance` en API 36+).
7. **Vosk usa JNA**: no elimines ni renombres `org.vosk.**` ni
   `com.sun.jna.**` (lección aprendida: `UnsatisfiedLinkError` en release).
8. `ConcurrentHashMap.getOrPut { null }` lanza **NPE** (`putIfAbsent(key,
   null)`): usa get/put explícito y nunca insertes `null`.
9. **PowerShell (Windows)**: evita `Set-Content -Encoding UTF8` (BOM). Usa
   `[System.IO.File]::WriteAllText(path, text, [System.Text.UTF8Encoding]::new($false))`.
   Los caracteres extraños que muestra la consola suelen ser un falso
   positivo del codepage: verifica **códigos** antes de "reparar" texto.
10. Los tests instrumentados necesitan dispositivo/emulador; no los corras en
    el job de calidad (ya están en el job "Instrumented" con API 34).

## Flujo de trabajo recomendado

1. Lee `ARCHITECTURE.md` y el ADR relacionado; si la decisión es nueva,
   escribe el ADR antes del código.
2. Cambio mínimo y enfocado; tests unitarios para lógica nueva.
3. Gate local: `ktlintFormat` → `ktlintCheck detekt validateContent test`;
   `assembleDebug`/`assembleRelease` si toca app; `connectedDebugAndroidTest`
   si toca UI.
4. Commit Conventional (en español) y PR usando la plantilla.
5. Deja que CI (2 jobs: calidad+release, instrumented API 34) valide.

## Qué NO hacer

- No rompas los invariantes I1–I9 (offline-first, sin fricción, progreso
  intocable, contenido validado…).
- No crees dependencias `feature→feature` ni `core→feature`.
- No añadas red en rutas críticas de aprendizaje; la nube es solo respaldo.
- No commitees modelos de voz ni credenciales.
- No cambies la identidad visual (`core-ui/tokens`) sin ADR.
