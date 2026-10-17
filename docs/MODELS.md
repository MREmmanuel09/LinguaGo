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

## STT (repaso de pronunciación, Fase 2)

| Idioma | Motor | Modelo | Estado |
|---|---|---|---|
| en/pt/fr | Vosk Small | `vosk-model-small-*` por idioma | pendiente |

SpeakRepeat queda deshabilitado mientras no exista su modelo; el contenido
Fase 1 no incluye ejercicios de voz.

## Descarga (herramienta planificada, ADR-006)

```bash
# esqueleto previsto (Fase 3): gradle/model-fetch descarga AAR + modelos,
# verifica SHA-256 y llena models/manifest.json.
models/
├── manifest.json          # fuente de verdad de hashes
└── tts|stt/<archivo>
```

Hasta que la herramienta exista, `manifest.json` sirve de esquema: los
campos `sha256` vacíos significan *no verificado* y el engine los ignora.
