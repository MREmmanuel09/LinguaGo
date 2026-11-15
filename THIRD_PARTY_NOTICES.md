# Avisos de terceros y atribuciones

LinguaGo es software propietario (ver [`LICENSE`](./LICENSE)), pero incluye
componentes de terceros que conservan sus propias licencias. Esta lista
resume las atribuciones obligatorias; los textos completos de cada licencia
deben acompañar a la distribución (app, APK o paquete de código fuente).

> Revisa esta lista antes de cada publicación: las dependencias cambian con
> cada versión de `gradle/libs.versions.toml` y `models/manifest.json`.

## Bibliotecas de terceros

| Componente | Uso | Licencia |
|---|---|---|
| AndroidX (Core, Activity, Lifecycle, Navigation, DataStore, WorkManager, Room, ProfileInstaller, Benchmark, Test) | plataforma, persistencia, navegación y pruebas | Apache-2.0 |
| Jetpack Compose, Compose Material 3, Material Icons Extended | interfaz y sistema de diseño | Apache-2.0 |
| Kotlin (stdlib, compilador, plugins) y kotlinx.coroutines | lenguaje, concurrencia y corrutinas | Apache-2.0 |
| kotlinx.serialization | serialización JSON | Apache-2.0 |
| Dagger Hilt | inyección de dependencias | Apache-2.0 |
| OkHttp / Okio | cliente HTTP (respaldo en Drive) | Apache-2.0 |
| sherpa-onnx (k2-fsa) | síntesis de voz (TTS) en el dispositivo | Apache-2.0 |
| Vosk Android (alphacephei) | reconocimiento de voz (STT) offline | Apache-2.0 |
| JNA (usado por Vosk) | acceso nativo | LGPL-2.1 o Apache-2.0 (doble licencia) |
| Kaldi (modelos Vosk) | modelos de reconocimiento | Apache-2.0 |
| Gradle, Android Gradle Plugin, KSP, ktlint, detekt | build y calidad | Apache-2.0 |
| JUnit 4 | pruebas unitarias | EPL-1.0 |

## Recursos y modelos

| Recurso | Uso | Licencia |
|---|---|---|
| Tipografía **Nunito** | interfaz | SIL Open Font License 1.1 |
| Voces **Piper** (espeak-ng-data) | síntesis de voz | MIT / CC0 / Apache-2.0 según voz |
| Modelos **Vosk Small** | reconocimiento de voz | Apache-2.0 |

Los modelos de voz **no se redistribuyen dentro del APK**: se descargan bajo
demanda y se verifican por SHA-256 contra `models/manifest.json` (ADR-006,
invariante I7). Quien distribuya los APK debe incluir los avisos de licencia
de los modelos que distribuya.

## Obligaciones para la distribución comercial

- Conservar los títulos y avisos de copyright de cada componente.
- Incluir el texto de Apache-2.0, MIT, CC0, LGPL-2.1, EPL-1.0 y OFL 1.1 según
  corresponda.
- No aplicar licencias copyleft fuertes (GPL/AGPL) a los componentes propios.
- La app muestra un resumen en **Ajustes → Licencias y créditos**; mantenlo
  sincronizado con este archivo.

## Referencias

- Apache-2.0: https://www.apache.org/licenses/LICENSE-2.0
- MIT: https://opensource.org/license/mit
- CC0-1.0: https://creativecommons.org/publicdomain/zero/1.0/
- LGPL-2.1: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
- EPL-1.0: https://www.eclipse.org/legal/epl-1.0/
- SIL OFL 1.1: https://openfontlicense.org/
