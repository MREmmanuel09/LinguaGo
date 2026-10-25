# RELEASE GATE F4 — QA de sesión larga contra `enRelease`

> ADR-024 · módulo `:core:qa` (`com.android.test`, self-instrumenting sobre `:app`,
> flavor `en`) · ROADMAP_V2.md F4.4.

El gate conduce **las 5 primeras lecciones del Camino (A1)** de
`content-es-en` en la app **release minificada (R8)**, aplicando
perturbaciones del entorno en puntos deterministas y juntando métricas por
ejercicio, con un veredicto objetivo por criterios (`verdict: PASS|FAIL` en
el reporte JSON). El camino lineal (ADR-026) bloquea B2 hasta completar el
recorrido, por eso el gate conduce las primeras lecciones disponibles. Todo
lo que la automatización no puede juzgar (sensación de latencia, TalkBack,
haptics) queda registrado en `manual_checks` como verificación subjetiva
humana — no se inventan veredictos.

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

Las 5 primeras del Camino (A1): `Saludos`, `Presentarse`, `La familia`,
`Comida y bebida`, `Colores y números` (7 ejercicios cada una; tipos
cubiertos: `MultipleChoice`, `Translate`/`OrderWords` vía tokens,
`MatchPairs`, `ListenChoose`, `ListenType`, `SpeakRepeat`).

### Navegación con el camino (ADR-026)

El driver localiza cada nodo por `contentDescription`
(`Lección: <título>.<estado>`), desplaza el `LazyColumn` en ambos sentidos y
solo toca nodos habilitados. Si el nodo está bloqueado, el intento falla y el
gate lo reporta.

### Vidas (ADR-025)

El driver falla a propósito para ejercitar el banner de error, con
**presupuesto de 2 fallos por lección** y solo si las vidas visibles son
> 3. Así la sesión no cancela lecciones por la penalización y conserva la
cobertura del feedback de error.

## Perturbaciones por lección

| Lección | Ejercicio (idx) | Perturbación |
|---|---|---|
| 1 | 2 | `ROTATE` (rotación por shell settings) |
| 2 | 6 | `BACKGROUND_DURING_STT` |
| 3 | 3 | `DOUBLE_TAP_CONTINUE` |
| 4 | 6 | `NOTIFICATION_DURING_RECORDING` |
| 5 | 0 | `TRIM_MEMORY` |
| 6 (pasada extra) | 6 | `CANCEL_STT` |
| 7 (pasada extra) | 4 | `CANCEL_TTS` |
| 8-10 | - | `FONT_SCALE_2X`, `DARK_MODE`, `LOCALE_CHANGE` |

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

## Resultados al cierre (2026-10-25)

- **Smoke (1 min, minLessons=1): PASS**: 10 iteraciones, 1 lección
  completa, rotate PASS, special:home PASS, 0 crashes/ANR/races, RAM pico
  277 MB.
- **Corrida completa (20 min): PASS (`failed=[]`)** — artefacto
  `docs/qa/release-gate-f4-results.json`: 5 lecciones completadas, 38
  iteraciones, 20.8 min de foreground, 0 crashes/ANR/races, RAM pico
  318 MB. Escenarios en verde: rotate, background durante STT, doble-tap,
  notificación durante grabación, trim-memory, background 5 min y Home
  final. Cubre `MultipleChoice`, `Translate`/`OrderWords`,
  `MatchPairs`/`SemanticMatch`, `ListenChoose`, `ListenType` y
  `SpeakRepeat` de las 5 primeras lecciones del Camino (A1).
- **Camino lineal (ADR-026)**: el driver localiza los nodos por
  `contentDescription`, desplaza el `LazyColumn` en ambos sentidos y
  respeta los nodos bloqueados. El camino bloquea B2 hasta completar el
  recorrido, por eso el gate conduce A1.
- **Vidas (ADR-025)**: presupuesto de 2 fallos por lección y solo con
  vidas > 3; los `SpeakRepeat` sin perturbación usan el SKIP oficial (no
  consumen vida). La sesión no canceló ninguna lección.
- **Criterios de memoria**: el pico absoluto (~320 MB) incluye los modelos
  Piper/Vosk residentes; el veredicto usa el **drift nativo por lección**
  (≤ 30 MB/lección sobre la primera) y un techo catastrófico de 600 MB.
  El desglose java/native/graphics/private-other de cada iteración queda en
  el JSON para diagnosticar.
- **Pasadas extra**: cuando el presupuesto pide más minutos que lecciones
  tiene el plan, el runner intenta reabrir lecciones ya completadas en
  frío. La app entra en **modo repaso** con contenido reordenado y el gate
  no puede puntuarlo con los datos del curso: esos intentos se registran
  como NOTE (best-effort) y no votan.


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
