# Modelos de voz (TTS / STT)

> Fuente de la estrategia: ADR-003 (runtime), ADR-006 (distribución) y
> ADR-010 (voces por idioma).

## Política

- **Nada se commitea**: los modelos viven fuera del repo (repo < 100 MB,
  RFC §5.2).
- Verificación **SHA-256** obligatoria antes de cargar cualquier modelo
  (`core-audio.ModelIntegrity`); un hash inválido desactiva el modelo.
- **Degradación**: sin modelos, la app usa el TTS del sistema
  (`SystemTtsEngine`) y los ejercicios nunca se bloquean (invariante I3/I7).

## Voz TTS por idioma destino (ADR-010)

| Curso | Motor preferido | Voz Piper | Estado |
|---|---|---|---|
| ES→EN | Piper | `en_US-amy-medium` | pendiente de descarga |
| ES→PT | Piper | `pt_BR-faber-medium` | pendiente de descarga |
| ES→FR | Piper | `fr_FR-siwis-medium` | pendiente de descarga |

Justificación de tamaño: variantes *medium/int8* (~25–60 MB por voz) como
balance calidad/peso; el runtime sherpa-onnx se comparte entre las tres
voces (ADR-003: ~46 MB en el APK).

**Japonés (ES→JA)**: no existe una voz Piper japonesa estable publicada;
el ADR-010 define fallback al TTS del sistema para `ja` hasta que aparezca
una voz verificable. El runtime STT de Fase 2 tampoco cubre `ja` en la
elección inicial.

## STT (repaso de pronunciación, Fase 2.3)

| Curso | Motor | Modelo | Estado |
|---|---|---|---|
| ES→EN | Vosk Small | `vosk-model-small-en-us-0.15` (~40 MB) | descargable vía `fetchVoiceModels` |
| ES→PT | Vosk Small | `vosk-model-small-pt-0.3` | descargable vía `fetchVoiceModels` |
| ES→FR | Vosk Small | `vosk-model-small-fr-0.22` | descargable vía `fetchVoiceModels` |
| ES→JA | Vosk Small | `vosk-model-small-ja-0.22` | descargable vía `fetchVoiceModels` |

SpeakRepeat usa `VoskSttEngine`: el modelo se verifica **por archivo** en
`models/manifest.json` (`stt[].files[].sha256`) antes de cargar; sin
modelo verificado (o sin permiso de micrófono) el panel queda inactivo y
"No puedo hablar ahora" deja avanzar sin bloquear (invariante I3). La
norma del grader sigue siendo igualdad de frases normalizadas.

## Descarga (tarea manual `fetchVoiceModels`)

Los paquetes vienen del tag `tts-models` de sherpa-onnx: cada
`vits-piper-<voz>.tar.bz2` incluye `<voz>.onnx` + `tokens.txt` +
`espeak-ng-data/` (mismo esquema que Piper, listo para el runtime).

```bash
./gradlew fetchVoiceModels
```

La tarea (no está connected al build ni al CI) para cada voz:

1. Descarga `https://github.com/k2-fsa/sherpa-onnx/releases/download/
   tts-models/vits-piper-<voz>.tar.bz2` (cache en `models/downloads/`).
2. Extrae en `models/tts/<voz>/` (una vez: si existe se salta).
3. Calcula el **SHA-256** del `<voz>.onnx` y lo graba en
   `models/manifest.json` (fuente de verdad de hashes, ADR-006).
4. `models/` es assets del app: al instalar, `PiperTtsEngine` copia el
   paquete a filesDir, verifica el SHA-256 y carga el runtime; sin
   voces (repo limpio / CI) degrada al TTS del sistema, I3.
