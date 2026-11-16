# Avisos de terceros y atribuciones

LinguaGo es software propietario (ver [`LICENSE`](./LICENSE)), pero incluye
componentes y recursos de terceros que conservan sus propias licencias. Esta
lista resume las atribuciones obligatorias para la distribución comercial.

> Revisa esta lista antes de cada publicación: las dependencias cambian con
> `gradle/libs.versions.toml`, los recursos de voz con `models/voices.lock.json`
> y `models/manifest.json`.

## Bibliotecas incluidas en la app

| Componente | Uso | Licencia |
|---|---|---|
| AndroidX (Core, Activity, Lifecycle, Navigation, DataStore, WorkManager, Room, ProfileInstaller, Benchmark, Test) | plataforma, persistencia, navegación y pruebas | Apache-2.0 |
| Jetpack Compose, Compose Material 3, Material Icons Extended | interfaz y sistema de diseño | Apache-2.0 |
| Kotlin (stdlib, runtime, plugins) y kotlinx.coroutines | lenguaje y concurrencia | Apache-2.0 |
| kotlinx.serialization | serialización JSON | Apache-2.0 |
| Dagger Hilt | inyección de dependencias | Apache-2.0 |
| OkHttp / Okio | cliente HTTP (respaldo en Drive) | Apache-2.0 |
| sherpa-onnx 2.0.0 (k2-fsa) | síntesis de voz Piper offline en el dispositivo (sin espeak-ng embebido) | Apache-2.0 |
| Vosk Android (alphacephei) | reconocimiento de voz offline | Apache-2.0 |
| JNA (usado por Vosk) | acceso nativo | LGPL-2.1 o Apache-2.0 (doble licencia) |
| Kaldi (modelos Vosk) | modelos de reconocimiento | Apache-2.0 |
| Gradle, Android Gradle Plugin, KSP, ktlint, detekt | build y calidad (no se distribuyen en el APK) | Apache-2.0 |
| JUnit 4 | pruebas (no se distribuye en el APK) | EPL-1.0 |

## Voces TTS empaquetadas en el APK (3.5.0, ADR-057)

Las voces Piper se incluyen en los APK de distribución junto con fonemas
precalculados (`phonemes.bin`), lexicón y metadatos; **no se descargan en
tiempo de ejecución**. Todas las voces seleccionadas permiten uso comercial
según la lista de Piper/COMMERCIAL_USE.

| Voz | Idioma | Dataset / licencia del dataset | Atribución requerida |
|---|---|---|---|
| `en_US-ljspeech-medium` | en-US | LJ Speech Dataset, **dominio público** | LJ Speech Dataset (public domain). Voz entrenada por Bryce Beattie. |
| `fr_FR-siwis-medium` | fr-FR | SIWIS French Speech Synthesis Database, **CC BY 4.0** | "The SIWIS French Speech Synthesis Database" (Yamagishi, Honnet, Garner y Lazaridis, 2017; University of Edinburgh; DOI 10.7488/ds/1705), CC BY 4.0. Modelo base Hi-Fi TTS (voz de Cori Samuel, reader 92), CC BY 4.0. |
| `pt_BR-faber-medium` | pt-BR | Faber voice dataset (OHF-Voice), **CC0 1.0** | Faber voice dataset (CC0). Modelo base Hi-Fi TTS (voz de Cori Samuel, reader 92), CC BY 4.0. |

Textos de licencia incluidos en `app/src/main/assets/licenses/`: `PUBLIC-DOMAIN.txt`,
`CC-BY-4.0.txt`, `CC0-1.0.txt`, `Apache-2.0.txt`. Cada voz incluye además
`ATTRIBUTION.txt` dentro de su paquete en `models/tts/<voz>/` (empaquetado como asset).

## Modelos STT empaquetados (según variante)

| Recurso | Uso | Licencia |
|---|---|---|
| Modelos **Vosk Small** (en/pt/fr/ja, según flavor) | reconocimiento de voz | Apache-2.0 |

## Herramientas de build (no distribuidas)

| Componente | Uso | Licencia / nota |
|---|---|---|
| espeak-ng 1.52.0 (vía `espeakng-loader`) | **solo en tiempo de build** para precalcular fonemas IPA; nunca se incluye en el APK ni en assets | GPL-3.0 en origen, pero no forma parte del artefacto distribuido. Los fonemas generados se empaquetan como datos. |
| Piper phonemizer / utilidades de preparación propias | generación de `phonemes.bin`, `lexicon.txt`, `tokens.txt` | código del proyecto (propietario) |

## Fuentes tipográficas

| Recurso | Uso | Licencia |
|---|---|---|
| Tipografía **Nunito** (si se incluye en la app) | interfaz | SIL Open Font License 1.1 |

## Obligaciones para la distribución comercial

- Conservar avisos de copyright y atribuciones de cada componente y voz.
- Incluir los textos de Apache-2.0, MIT (si aplica), CC0-1.0, CC BY 4.0,
  LGPL-2.1, EPL-1.0 y OFL 1.1 según corresponda (ver `app/src/main/assets/licenses/`).
- Cumplir la atribución de SIWIS (CC BY 4.0) y del modelo base Hi-Fi TTS
  donde aplique, incluso en materiales de distribución.
- No incluir componentes GPL/AGPL en el APK o binarios distribuidos.
- Mantener sincronizados **Ajustes → Licencias y créditos**, este archivo y los
  assets de licencias.

## Referencias

- Apache-2.0: https://www.apache.org/licenses/LICENSE-2.0
- CC BY 4.0: https://creativecommons.org/licenses/by/4.0/
- CC0-1.0: https://creativecommons.org/publicdomain/zero/1.0/
- LGPL-2.1: https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
- EPL-1.0: https://www.eclipse.org/legal/epl-1.0/
- SIL OFL 1.1: https://openfontlicense.org/
- Piper commercial use: https://github.com/OHF-Voice/piper1-gpl/blob/main/docs/COMMERCIAL_USE.md
