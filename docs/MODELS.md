# Modelos de voz (TTS / STT)

> Fuente de la estrategia: ADR-003 (runtime), ADR-006 (distribución) y
> ADR-010 (voces por idioma). Estado verificado en la Fase 2 y re-validado
> durante las pruebas de emulador (TTS Piper suena; STT Vosk corre completo).

## Política

- **Nada se commitea**: los modelos viven fuera del repo; la caché de
  descargas es `models/.voice-downloads/` (ignorada por git).
- Verificación **SHA-256** obligatoria antes de cargar cualquier modelo
  (`core-audio.ModelIntegrity`); un hash inválido desactiva el modelo.
- **Degradación**: sin modelos, la app usa el TTS del sistema
  (`SystemTtsEngine`) y los ejercicios nunca se bloquean (I3).

## Voz TTS por idioma destino (ADR-010)

| Curso | Motor preferido | Voz Piper | SHA-256 | Verificación |
|---|---|---|---|---|
| ES→EN | Piper | `en_US-amy-medium` | en `models/manifest.json` | ✅ audio real en emulador (AudioTrack, sherpa-onnx) |
| ES→PT | Piper | `pt_BR-faber-medium` | en `models/manifest.json` | ⏳ pendiente de dispositivo físico |
| ES→FR | Piper | `fr_FR-siwis-medium` | en `models/manifest.json` | ⏳ pendiente de dispositivo físico |
| ES→JA | TTS del sistema | — (no existe voz Piper JA estable) | — | fallback por diseño (ADR-010) |

Los paquetes `vits-piper-<voz>.tar.bz2` (~25–60 MB por voz) incluyen
`<voz>.onnx` + `tokens.txt` + `espeak-ng-data/`; el runtime sherpa-onnx se
comparte entre las tres voces (~46 MB en el APK, ADR-003).

## STT (repaso de pronunciación, Fase 2.3)

| Curso | Motor | Modelo | Verificación |
|---|---|---|---|
| ES→EN | Vosk Small | `vosk-model-small-en-us-0.15` | ✅ flujo completo en emulador (silencio ⇒ texto vacío ⇒ avanza sin bloquear) |
| ES→PT | Vosk Small | `vosk-model-small-pt-0.3` | ⏳ pendiente de dispositivo físico (ojo: layout legacy del modelo PT) |
| ES→FR | Vosk Small | `vosk-model-small-fr-0.22` | ⏳ pendiente de dispositivo físico |
| ES→JA | Vosk Small | `vosk-model-small-ja-0.22` | ⏳ pendiente de dispositivo físico |

Los hashes viven **por archivo** en `models/manifest.json`
(`stt[].files[].sha256`) y se verifican antes de cargar. SpeakRepeat usa
`VoskSttEngine`; sin modelo verificado (o sin permiso de micrófono) el panel
queda inactivo y "No puedo hablar ahora" deja avanzar (I3). La norma del
grader es igualdad de frases normalizadas.

> Nota de empaquetado: Vosk usa **JNA**; R8 necesita las reglas de
> `app/proguard-rules.pro` (sin ellas el release falla con
> `UnsatisfiedLinkError: com.sun.jna.Pointer`).

## Descarga (tarea manual `fetchVoiceModels`)

```bash
./gradlew fetchVoiceModels
```

La tarea (no conectada al build ni al CI) para cada voz/modelo:

1. Descarga el paquete oficial (GitHub de sherpa-onnx para TTS; alphacephei
   para Vosk) con caché en `models/.voice-downloads/`.
2. Extrae en `models/tts/<voz>/` o `models/stt/<modelo>/` (una vez: si
   existe, se salta).
3. Calcula el **SHA-256** y lo graba en `models/manifest.json` (fuente de
   verdad de hashes, ADR-006); en Vosk, por cada archivo del modelo.
4. `models/` es assets de la app: al instalar, los motores copian el paquete
   a `filesDir`, verifican los hashes y cargan; sin voces (repo limpio/CI)
   degradar al sistema o a "no disponible" sin bloquear (I3).
