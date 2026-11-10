# Fase 4 — Voz, assets y perfil

**Fecha:** 2026-11-09
**Dispositivo:** `Medium_Phone_API_37.0` (emulador x86_64, API 37)
**Variantes:** `full` debug/release y releases `en/pt/fr/ja`

## Deduplicación de `espeak-ng-data`

| Antes | Después |
|---|---|
| 3 copias de 17,2 MB (una por voz Piper) | 1 copia compartida en `models/tts/espeak-ng-data` |
| Copia local dentro de cada voz | Voces sin `espeak-ng-data`; Gradle lo inyecta una vez |
| — | Solo diccionarios `en/pt/fr/es`; se retiran los demás |

- `fetchVoiceModels` deduplica y poda el árbol.
- `PiperTtsEngine` usa la copia local de cachés antiguas o el árbol compartido
  extraído una sola vez. Logcat confirmó:
  `data_dir=.../files/tts/espeak-ng-data`.
- TTS FR cargó y reprodujo tras borrar la caché, sin `UnsatisfiedLinkError`.

## Gestor de voces

| Paso | Resultado |
|---|---|
| Estado inicial | `142,7 MB extraídos` (FR Piper + FR Vosk de la caché anterior). |
| Liberar | Confirmación → `0 MB`; se cerraron los motores y se borraron `filesDir/tts` y `filesDir/stt`. |
| Reextracción | Review → Piper FR extrae su voz + árbol compartido: `61,3 MB` reportados. |
| Comparación | Antes de compartir, una sola voz FR ocupaba `77,4 MB`; ahora `61,3 MB`. |

La UI muestra el tamaño y el aviso «Espacio de voces liberado»; al volver a
Ajustes se refresca el tamaño real.

## Perfil CEFR A1–C2

- Se renderizan las 6 etapas del curso activo.
- ES→FR mostró `A1 3/10`, `A2 0/8`, `B1 0/20`, `B2 0/22`, `C1 0/24`, `C2 0/18`.
- Validado en claro, oscuro y `fontScale 2.0x`; las píldoras se distribuyen en
  2 filas de 3 y no se solapan.
- La precisión media, fluidez oral y calendario de F3 siguen visibles.

## Contenido B1–C2

Auditoría automática de los 4 cursos:

| Curso | Lecciones | B1–C2 | Estructura |
|---|---:|---:|---|
| ES→EN | 111 | 84 | 0 hallazgos |
| ES→FR | 102 | 84 | 0 hallazgos |
| ES→JA | 102 | 84 | 0 hallazgos |
| ES→PT | 102 | 84 | 0 hallazgos |

Comprobado: `scenario`, `canDo`, `grammarPoints`, `culturalNotes`,
`modelAnswer` y `conceptClusters` presentes; sin marcadores de inglés obvios
en FR/PT/JA. La revisión editorial **humana/nativa** de naturalidad B1–C2
queda desacoplada en commits `content:` para `v3.1` (ADR-051).

## Baseline Profile

- Regenerado con la UI de 3.0.0 en `Medium_Phone_API_37.0`.
- Baseline: `25.435 → 26.113` reglas (+3,37 %).
- Startup: `20.650 → 21.306` reglas (+3,62 %).

## Tamaños de release

| Flavor | arm64 | universal |
|---|---:|---:|
| full | 340,5 MB | 379,2 MB |
| en | 110,7 MB | 149,4 MB |
| fr | 111,6 MB | 150,3 MB |
| pt | 102,2 MB | 140,9 MB |
| ja | 62,4 MB | 101,1 MB |

El `full` arm64 bajó de 392,3 MB (F1) a 340,5 MB. Queda 0,5 MB por encima
del objetivo orientativo de 340 MB; se conservaron las tres voces Piper y los
cuatro modelos Vosk completos. Se comprimieron las librerías nativas y se
excluyeron `libsherpa-onnx-c-api/cxx-api` tras verificar que el JNI no las
carga.

## Calidad y verificación

- `ktlintCheck detekt validateContent test` en verde.
- `:app:assembleFullRelease`, `enRelease`, `ptRelease`, `frRelease` y
  `jaRelease` en verde con R8.
- `app-full-arm64-v8a-release.apk` verificado: árbol espeak compartido,
  `libonnxruntime.so` presente y librerías auxiliares ausentes.
- Hashes en `dist/v3.0.0/SHA256SUMS.txt`.

## Pendiente para `v3.1`

- Revisión lingüística nativa de B1–C2.
- Validación de STT en hardware ARM físico (PT/FR/JA) si se publica fuera del
  emulador.
