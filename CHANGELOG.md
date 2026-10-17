# Changelog

Todos los cambios notables de LinguaGo se documentan aquí.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado sigue [SemVer](https://semver.org/lang/es/).
Pre-1.0: los cambios incompatibles pueden ocurrir en `0.y.0`.

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
