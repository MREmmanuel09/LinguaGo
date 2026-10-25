# Changelog

Todos los cambios notables de LinguaGo se documentan aquí.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y el versionado sigue [SemVer](https://semver.org/lang/es/).
Pre-1.0: los cambios incompatibles pueden ocurrir en `0.y.0`.

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
