# Formato de contenido v1 — LinguaGo

> Contrato de autoría del contenido pedagógico. La fuente de verdad formal es
> [`docs/schemas/course-v1.json`](docs/schemas/course-v1.json) (JSON Schema
> draft-07, ADR-012/ADR-009); este documento explica cómo escribir contenido
> que lo cumpla.

## Reglas de oro

1. **UTF-8 sin BOM.** Un BOM (los 3 bytes `EF BB BF` al inicio) rompe el
   parseo de kotlinx-serialization. Los editores deben guardar "UTF-8" y no
   "UTF-8 con BOM" (en PowerShell, `[System.Text.UTF8Encoding]::new($false)`).
2. **Nada de claves extra.** Todos los objetos usan
   `additionalProperties: false`: una clave desconocida es un error.
3. **`validateContent` antes de cada PR.** `./gradlew validateContent` valida
   los 4 cursos contra el schema; el CI lo corre antes de los tests.
4. **Contenido original.** Nada copiado de terceros (invariante I5);
   licencia CC BY-SA 4.0.

## Estructura en el repositorio

```
content/content-es-<idioma>/src/main/assets/courses/<courseId>/
  manifest.json   → tarjeta ligera (se muestra en onboarding/ajustes)
  course.json     → curso completo
```

`<courseId>` es `es-en`, `es-pt`, `es-fr` o `es-ja`. El `id` dentro de los
JSON debe coincidir exactamente con el nombre de la carpeta (el loader lo
verifica). Los assets se empaquetan en el APK: el contenido es offline por
diseño (I1).

## `manifest.json`

```json
{
  "id": "es-en",
  "from": "es",
  "to": "en",
  "title": "Inglés esencial"
}
```

| Campo | Regla |
|---|---|
| `id` | `^[a-z]{2}-[a-z]{2}$` y debe igualar la carpeta |
| `from` | siempre `"es"` (v1) |
| `to` | `"en"` \| `"pt"` \| `"fr"` \| `"ja"` |
| `title` | nombre visible del curso (1+ caracteres) |

## `course.json`

```json
{
  "schemaVersion": 1,
  "id": "es-en",
  "from": "es",
  "to": "en",
  "levels": [
    {
      "id": "es-en-lv-1",
      "name": "Nivel 1",
      "units": [
        {
          "id": "es-en-lv-1-un-1",
          "title": "Primeros pasos",
          "lessons": [
            {
              "id": "es-en-lv-1-un-1-ls-1",
              "title": "Saludos",
              "exercises": [ /* ver abajo */ ]
            }
          ]
        }
      ]
    }
  ]
}
```

- `schemaVersion` es **siempre `1`** en este formato. Un cambio incompatible
  implica `2` + codec que soporte ambas versiones (ADR-012).
- Jerarquía: `levels[].units[].lessons[].exercises[]`; cada lista exige al
  menos 1 elemento.
- **Convención de ids** (`^[a-z0-9][a-z0-9-]*$`): `es-en-lv-1-un-1-ls-2`,
  `es-en-e7` (ejercicios). Mantén jerarquía legible: prefijo del curso +
  nivel/unidad/lección + sufijo del ejercicio.

## Ejercicios (10 tipos)

Campos comunes: `type` (discriminador, `@SerialName`) e `id`. `audioText` es
opcional en la mayoría; **obligatorio (string)** en los de escucha; si se
define, es la frase que el TTS pronuncia.

### MultipleChoice — elegir una opción

```json
{ "type": "MultipleChoice", "id": "es-en-e1",
  "question": "¿Cómo se dice «hola»?",
  "options": ["Hello", "Bye", "Thanks"], "correctIndex": 0 }
```

### Translate — ensamblar la traducción con tokens

```json
{ "type": "Translate", "id": "es-en-e2",
  "sourcePhrase": "Buenos días",
  "correctTokens": ["Good", "morning"],
  "distractorTokens": ["night"] }
```

### MatchPairs — emparejar columnas

```json
{ "type": "MatchPairs", "id": "es-en-e3",
  "pairs": [
    { "left": "adiós", "right": "Goodbye" },
    { "left": "gracias", "right": "Thank you" },
    { "left": "por favor", "right": "Please" }
  ] }
```

### OrderWords — ordenar la frase

```json
{ "type": "OrderWords", "id": "es-en-e4",
  "tokens": ["How", "are", "you"],
  "answerTokens": ["How", "are", "you"] }
```

### ListenChoose — escuchar y elegir (audioText obligatorio)

```json
{ "type": "ListenChoose", "id": "es-en-e5",
  "audioText": "Good evening",
  "options": ["Good morning", "Good evening", "Good night"],
  "correctIndex": 1 }
```

### ListenType — dictado (audioText obligatorio)

```json
{ "type": "ListenType", "id": "es-en-e6",
  "transcription": "Good evening",
  "audioText": "Good evening" }
```

### SpeakRepeat — repetir en voz alta (STT)

```json
{ "type": "SpeakRepeat", "id": "es-en-e7",
  "targetPhrase": "Nice to meet you" }
```

### KanaChoose — kana → romaji

```json
{ "type": "KanaChoose", "id": "es-ja-e1",
  "kana": "あ",
  "romajiOptions": ["u", "a", "e"],
  "correctRomaji": "a" }
```

### KanjiReading — kanji → lectura

```json
{ "type": "KanjiReading", "id": "es-ja-e7",
  "kanji": "山",
  "readingOptions": ["やま", "かわ", "そら"],
  "correctReading": "やま" }
```

### ParticleFill — completar la partícula

```json
{ "type": "ParticleFill", "id": "es-ja-e9",
  "sentenceTemplate": "わたし _ がくせいです",
  "particles": ["は", "を", "に"],
  "correctParticle": "は" }
```

## Cómo se califica (resumen)

`ExerciseGrader` (core-domain, Kotlin puro) mapea cada tipo a una respuesta:

| Respuesta del usuario | Tipos | Regla |
|---|---|---|
| `OptionPicked(index)` | MultipleChoice, ListenChoose, KanaChoose, KanjiReading, ParticleFill | índice == correcto |
| `TokenSequence(tokens)` | Translate, OrderWords | secuencia normalizada == esperada |
| `MatchedPairs(map)` | MatchPairs | todas las parejas correctas |
| `TypedText(text)` | ListenType | igualdad normalizada (trim/mayúsculas/puntuación) |
| `SpokenText(transcript)` | SpeakRepeat | igualdad normalizada de frases |

Fallar **no bloquea**: siempre se muestra feedback y se puede continuar (I3).

## Vocabulario para el repaso FSRS (ADR-016)

Al completar una lección, `VocabularyExtractor` crea tarjetas de repaso con
los **pares explícitos** del contenido:

- `MatchPairs` → `left ↔ right`
- `KanaChoose` → `kana ↔ correctRomaji`
- `KanjiReading` → `kanji ↔ correctReading`

Consecuencias de autoría:

- Un par `left/right` debe ser una pareja real palabra↔significado (las
  frases completas no entran al repaso).
- Los pares se deduplican por `left` (sin distinguir mayúsculas) dentro del
  curso: no repitas la misma palabra con otra traducción en otra lección.
- Tras completar una lección, el repaso aparece solo cuando hay tarjetas
  vencidas ("Repasar ahora · N" en Home).

## Flujo de autoría

```bash
# 1. Edita/crea el JSON del curso (UTF-8 sin BOM)
# 2. Valida contra el schema
./gradlew validateContent
# 3. Corre el gate completo antes del PR
./gradlew ktlintCheck detekt validateContent test
```

Errores comunes y su causa:

| Síntoma | Causa típica |
|---|---|
| `Unexpected character (code 65279)` o fallo de parseo | **BOM** al inicio del archivo |
| `INVALID … additional properties` | clave desconocida (¿typo? ¿nivel extra?) |
| `INVALID … required` | falta un campo obligatorio (p. ej. `audioText` en Listen*) |
| `InvalidContent` en runtime | ids duplicados o `id` de curso ≠ carpeta |
| Audio no suena y "Voz no disponible" | idioma sin voz Piper instalada (fallback del sistema) |

Para artículos nuevos de idioma (p. ej. ES→DE) además del curso habría que
crear el módulo `content-es-de`, incluirlo en `settings.gradle.kts`, añadir
el idioma al enum del dominio y registrar la dependencia en `app`.
