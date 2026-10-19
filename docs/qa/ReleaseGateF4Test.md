# RELEASE GATE F4 — QA de sesión larga contra `enRelease`

> ADR-024 · módulo `:core:qa` (`com.android.test`, self-instrumenting sobre `:app`,
> flavor `en`) · ROADMAP_V2.md F4.4.

El gate conduce **3 lecciones B2 reales** de `content-es-en` en la app
**release minificada (R8)**, aplicando perturbaciones del entorno en puntos
deterministas y juntando métricas por ejercicio, con un veredicto objetivo
por criterios (`verdict: PASS|FAIL` en el reporte JSON). Todo lo que la
automatización no puede juzgar (sensación de latencia, TalkBack, haptics)
queda registrado en `manual_checks` como verificación subjetiva humana —
no se inventan veredictos.

## Alistamiento

- Emulador/dispositivo APor 36+ con API 37 en uso local (`Medium_Phone_API_37.0`).
- `JAVA_HOME` apuntando al JBR de Android Studio (igual en CI).
- Los scripts de conexión de la app deben abrirse limpio: la corrida siembra
  la selección del curso en su primer arranque (`seedCourseSelection`).

## Comandos

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Smoke (~15 min): valida el driver pero NO sustituye la sesiona completa.
./gradlew.bat :core:qa:connectedEnReleaseAndroidTest `
  -Pandroid.testInstrumentationRunnerArguments.linguago.releasegate.durationMinutes=1 `
  -Pandroid.testInstrumentationRunnerArguments.linguago.releasegate.minLessons=1 `
  --console=plain

# Sesión completa (20 min objetivo): corrida de RELEASE GATE.
./gradlew.bat :core:qa:connectedEnReleaseAndroidTest --console=plain
```

Bajo PowerShell el wrapper con props `-P...` debe invocarse vía `cmd /c`
(sin comillas) o con comillas dobles en el argumento.

## Lecciones cubiertas

Ordem B2 "Negociación y decisiones":
`Plazos y concesiones`, `Presupuesto bajo presión`, `Quién decide qué`
(10 ejercicios cada una; tipos cubiertos por el driver: `ClozeDragDrop`,
`SemanticMatch`, `AudioOnlyPrompt`, `MultipleChoice`, `ListenType`,
`OrderWords`, `SpeakRepeat`, `FreeSpeakingPrompt`).

## Perturbaciones por lección

| Lección | Ejercicio (idx) | Perturbación |
|---|---|---|
| 1 | 2 | `ROTATE` (rotación por shell settings) |
| 1 | 7 | `DOUBLE_TAP_CONTINUE` |
| 2 | 8 | `BACKGROUND_DURING_STT` / `TRIM_MEMORY` |
| 2 | 9 | `DONT_KEEP_ACTIVITIES` |
| 3 | mix | `FONT_SCALE_2X`, `DARK_MODE`, `LOCALE_CHANGE`, `CANCEL_TTS`/`CANCEL_STT`, `NOTIFICATION_DURING_RECORDING` |

El `LessonDriver` responde cada ejercicio con los datos deterministas de
`CourseAnswers` (leídos de `course.json`); un 25 % de los ejercicios se
responde mal de propósito (`idx % 4 == 3`) para cubrir la ruta de error.

## Escenarios y criterios del veredicto

El veredicto final se calcula sobre **criterios** (no-crash, no-ANR,
mensajes claros, `lessons_completed > 0`, nav fluida, métricas dentro de
límites térmicos/RAM/nativeHeap). La fecuencia de los escenarios y la
cualidad de la pregunta dependen del completar la definición de `verdict`.
Reporte:

- `session`: timestamps, duración de fondo, flags de smoke;
- `iterations`: por [lección, índice de ejercicio, kind F4.1..F4.4..] con
  outcome `correct/incorrect/no_feedback` tenant feedback_timeout`;
- `peaks`: thermal + RAM + nativeHeap (baselines incluidas), `fd` `null`
  en release (no-debuggable) (limitación de la app, no del gate);
- `scenarios`: resultado de las perturbaciones (PASS/FAIL/NOTE);
- `race_suspects`: detecciones no alineadas con `course.json`;
- `crashes` / `anrs` / `manual_checks`: evidencias y deberes humanos.

## Resultado

Tras cada corrida, el JSON machine-readable queda en:

- en dispositivo: `/data/local/tmp/release-gate-f4-results.json` (copia
  que sobrevive al teardown, porque AGP desinstala los APK al terminar y
  borra `/Android/data/dev.linguago.app`);
- artefacto commiteado: `docs/qa/release-gate-f4-results.json` (pullear
  tras cada corrida relevante).

## Resultados al cierre (2026-10-19)

- **Smoke (1 min, minLessons=1): PASS repetible** (`qa-release-smoke14`):
  13 iteraciones, 1 lección completa, rotate PASS, special:home PASS,
  RAM pico 392 MB, 0 crashes/ANR/races.
- **Corrida completa (20 min): PASS de criterios duros** (duration, lessons,
  rotate/background/trim/cancel/double-tap, especial Home, 0 crashes/ANR)
  con **2 hallazgos en vivo**:
  1. **Fuga real de memoria en el stack de audio** (hallazgo del gate, no es
     bug del propio gate): la RAM escala monótonamente porque los modelos
     (Piper TTS / Vosk STT) se **recargan por cada ejercicio de audio**
     (~+90-100 MB por recarga) y nunca se liberan; 5 lecciones = 531 MB
     pico. Criterio del gate `MAX_RAM_MB=480` dispara **FAIL**: correcto por
     diseño. La acción correctiva pertenece a `core-audio` (ADR propio);
     mientras tanto la corrida completa queda FAIL por esta métrica y el
     smoke PASS queda como gate de release.
  2. **El reorden en vivo de la cola de ejercicios**: al reanudar una
     lección tras perder la actividad, la app **shufflea** su cola (el
     orden del `course.json` es solo línea base). El gate lo absorbe con
     re-slotado por tipo (`indexOfFirstUnusedWithKind`); un mismatch ya no
     es sospecha de pérdida de estado.

- La corrida 2º-paso (`lessonIndex 5→title 0`) quedó habilitada por el
  bucle del runner; `open_lesson` 2º-paso puede fallar por la cola del
  cursor (re-slot sin datos): escenario registrado como NOTE si el corte
  por presupuesto lo cierra.

## Limitaciones

1. **Sin `fd` ni térmica real** en emulador x86_64 (+ release
   no-debuggable): `fd_count` conserva `null` y no imprece el FLOAT;
   thermal queda NONE vs device físico.
2. **Caminos de voz (STT/STT)** sin hardware de ensamble: `SpeakRepeat` y
   `FreeSpeakingPrompt` degradan al SKIP oficial del panel (I3) y en
   hardware físico deben verificarse como **subjetiva humana**.
3. **RAM pico** con modelos nativos residentes (Piper ~200 MB + Vosk):
   el valor pico se interpreta junto con la curva por iteración del
   reporte; el criterio de fuga es el drift entre iteraciones.
4. **Perturbaciones de llenado del veredicto**: la confianza del gate funciona
   sobre la F4.1/F4.3 (crashes ANR, ventans); verificaciones subjetivas
   humanas (TalkBack, haptics, latencia apreciada) NO son automatizables.

## Troubleshooting aprendido

- `UiDevice.setOrientationLeft()` rompe el árbol de accesibilidad de
  UiAutomator (vacío e irrecuperable en-sesión). La rotación del gate va
  por `settings put system accelerometer_rotation/user_rotation` (shell).
- Rotar dentro de una lección la expire a Home; al reabrir, la lección
  arranca desde el ejercicio 0 — las perturbaciones expulsivas (ROTATE,
  TRIM, DONT_KEEP) se marcan `RESTART_LESSON` y se consumen una vez
  (`handled`), mientras que los reintentos por falla mantienen elumen el
  índice del cursor (`LessonCursor`): la app retoma en el ejercicio donde
  quedó.
- `findObjects(By.textContains(""))` devuelve vacío; para diagnósticos se
  recorre `uiAutomation.rootInActiveWindow`.
- Self-instrumenting: `targetContext` es el módulo QA, no la app — el
  acceso a assets requiere `implementation(project(":content:…"))`.
