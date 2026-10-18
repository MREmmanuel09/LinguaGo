# Formato de contenido v2 — LinguaGo v2.0

> Guía de autoría del contrato [`docs/schemas/course-v2.json`](schemas/course-v2.json)
> (JSON Schema draft-07, ADR-018). El formato v1 sigue vigente para `es-pt`,
> `es-fr` y `es-ja`; su guía canónica es [`../CONTENT_FORMAT.md`](../CONTENT_FORMAT.md).
> Alcance de v2.0: **solo el curso `es-en`** (A1→B2 profundo).

## Reglas de oro de v2

1. **UTF-8 sin BOM.** Un BOM rompe el parseo de kotlinx-serialization.
2. **`schemaVersion: 2`** en la raíz. `validateContent` enruta cada curso a su
   schema (v1 o v2) según este campo (F0.3).
3. **Nada de claves extra.** Todos los objetos usan
   `additionalProperties: false`.
4. **Regla de No-Aplanamiento Pedagógico.** Una lección `B1` o `B2` debe
   declarar `scenario`, `canDo`, `grammarPoints[]`, `vocabFrequencyBand`,
   `culturalNotes[]`, al menos un `FreeSpeakingPrompt` y `feedbackOnError` en
   **todos** sus ejercicios. El schema lo exige; el validador estructural
   (F0.3) lo refuerza.
5. **Contenido original** bajo CC BY-SA 4.0 (I6).

## Campos nuevos a nivel de curso

| Campo | Tipo | Regla |
|---|---|---|
| `schemaVersion` | integer | Siempre `2`. |
| `locale` | string (opcional) | Variante dialectal objetivo: `en-US`, `en-GB`, `pt-BR`, `pt-PT`, `fr-FR`, `ja-JP`. Si se omite, el curso usa la variante por defecto del idioma. |

`from` sigue siendo `"es"`; `to` sigue siendo `en`, `pt`, `fr` o `ja`. La
variante concreta se expresa con `locale`, no cambiando `to`.

## Campos nuevos a nivel de lección

| Campo | Tipo | Cuándo es obligatorio | Significado |
|---|---|---|---|
| `cefrLevel` | `A1` \| `A2` \| `B1` \| `B2` | Recomendado siempre; obligatorio para invocar la regla B1+ | Nivel CEFR de la lección. Si se omite, se asume `A1` por retrocompatibilidad. |
| `scenario` | string | `cefrLevel` ∈ {`B1`,`B2`} | Situación comunicativa realista (reunión, negociación, consulta, debate…). |
| `canDo` | string | `cefrLevel` ∈ {`B1`,`B2`} | Descriptor de lo que el estudiante puede hacer al terminar, redactado como *can-do* CEFR. |
| `grammarPoints` | array de objetos | `cefrLevel` ∈ {`B1`,`B2`} | Focos gramaticales con `id`, `label` y `description?`. Los `id` se referencian desde `feedbackOnError.ruleRef` y `FreeSpeakingPrompt.grammarRuleRefs`. |
| `vocabFrequencyBand` | `1000-2000` \| `2000-3000` \| `3000-5000` \| `5000+` | `cefrLevel` ∈ {`B1`,`B2`} | Banda de frecuencia léxica dominante de la lección. |
| `culturalNotes` | array de objetos | `cefrLevel` ∈ {`B1`,`B2`} (mínimo 1) | Modismos o referencias culturales con `idiom`, `note` y `translation?`. |

### `feedbackOnError` (por ejercicio)

Objeto opcional en A1/A2 y **obligatorio en todos los ejercicios de una
lección B1/B2**:

```json
"feedbackOnError": {
  "type": "grammar",
  "explanation": "La condicional mixta combina presente con consecuencia futura.",
  "ruleRef": "gp-mixed-conditionals"
}
```

| Campo | Obligatorio | Regla |
|---|---|---|
| `type` | Sí | `grammar`, `vocabulary`, `spelling`, `pronunciation`, `word_order`, `register` o `comprehension`. |
| `explanation` | Sí | Explicación concreta del error cometido (no un mensaje genérico). |
| `ruleRef` | No | `id` de un `grammarPoints[]` de la misma lección. |

## Nuevas variantes de ejercicio

Además de las 10 variantes de v1 (que siguen siendo válidas sin campos
nuevos), v2 añade:

### `AudioOnlyPrompt` — audio puro sin texto visible

El estudiante escucha y elige; la transcripción **nunca** se muestra antes de
responder. `audioText` es el texto que el TTS pronuncia, no una pista visual.

| Campo | Obligatorio | Regla |
|---|---|---|
| `audioText` | Sí | Frase que sintetiza Piper. |
| `question` | No | Enunciado visible (p. ej. "What is the speaker offering?"). |
| `options` | Sí | ≥ 2 opciones. |
| `correctIndex` | Sí | Índice dentro de `options`. |
| `allowSlowPlayback` | No | Habilita reproducción a 0.75x. |

### `SemanticMatch` — emparejamiento semántico

Pares con matiz de registro; admite tarjetas multilínea para B2.

| Campo | Obligatorio | Regla |
|---|---|---|
| `pairs[].left` / `pairs[].right` | Sí | Textos únicos por lado. |
| `pairs[].register` | No | `neutral`, `formal`, `informal`, `literary` o `colloquial`. |
| `pairs[].nuance` | No | Matiz pragmático que explica la diferencia. |
| `instructions` | No | Enunciado visible. |

### `ClozeDragDrop` — huecos arrastrables

| Campo | Obligatorio | Regla |
|---|---|---|
| `passage` | Sí | Texto con huecos `{{id}}`; cada `id` debe existir en `blanks[]`. |
| `blanks[].id` | Sí | Identificador del hueco (mismo patrón que los ids). |
| `blanks[].answer` | Sí | Respuesta correcta. |
| `blanks[].options` | Sí | ≥ 2 opciones e incluye `answer` (lo verifica el validador estructural). |

### `FreeSpeakingPrompt` — producción oral libre B2 (sin LLM)

Evaluación 100% offline (ADR-019): heurística determinista sobre la
transcripción de Vosk (longitud, `conceptClusters`, patrones de
`grammarRuleRefs` y WPM) + autoevaluación guiada del estudiante. **Prohibido**
usar LLMs on-device o cloud.

| Campo | Obligatorio | Regla |
|---|---|---|
| `prompt` | Sí | Consigna oral. |
| `minTokens` | Sí | Longitud mínima de la transcripción. |
| `conceptClusters` | Sí | ≥ 1 cluster; cada cluster ≥ 1 sinónimo o palabra clave. |
| `grammarRuleRefs` | No | `id`s de `grammarPoints[]` que deben aparecer (patrones). |
| `timeLimitSeconds` | No | 10–300 s. |
| `minWpm` / `maxWpm` | No | Rango de fluidez esperado. |
| `modelAnswer` | Sí | Respuesta canónica para la autoevaluación. |
| `selfAssessmentCriteria` | Sí | ≥ 1 criterio de autoevaluación. |

## Retrocompatibilidad v1 → v2

- Las 10 variantes de v1 conservan sus campos obligatorios originales; los
  campos nuevos (`feedbackOnError`) son opcionales en A1/A2.
- Una lección sin `cefrLevel` es válida y se interpreta como `A1`; por tanto
  un ejercicio con forma v1 sigue siendo válido dentro de v2.
- Los cursos existentes (`es-pt`, `es-fr`, `es-ja`) no se migran en v2.0:
  siguen en `schemaVersion: 1` y se validan contra `course-v1.json`.
- El fixture de contrato vive en
  `content/content-es-en/src/main/assets/courses/es-en/fixtures/leccion-b2-minimal.json`
  y se usa en los tests de F0.3.

## Ejemplo de lección B1

```json
{
  "id": "es-en-lv-5-un-1-ls-1",
  "title": "Resolver un malentendido",
  "cefrLevel": "B1",
  "scenario": "Un compañero de trabajo cree que no le avisaste de un cambio de última hora.",
  "canDo": "Puedo aclarar un malentendido, disculparme y proponer una solución con una condicional.",
  "grammarPoints": [
    { "id": "gp-second-conditional", "label": "Second conditional", "description": "If + past simple, would + infinitive para situaciones hipotéticas." }
  ],
  "vocabFrequencyBand": "2000-3000",
  "culturalNotes": [
    { "idiom": "break the ice", "note": "Se usa para romper la tensión inicial en una conversación difícil.", "translation": "romper el hielo" }
  ],
  "exercises": [
    {
      "type": "SemanticMatch",
      "id": "es-en-b1-e1",
      "instructions": "Empareja cada disculpa con su efecto.",
      "pairs": [
        { "left": "I'm sorry for the confusion.", "right": "Asumir la responsabilidad", "register": "neutral", "nuance": "Disculpa directa y breve." },
        { "left": "I should have told you earlier.", "right": "Reconocer el error de omisión", "register": "formal", "nuance": "Admite la culpa sin excusas." }
      ],
      "feedbackOnError": {
        "type": "register",
        "explanation": "Ambas disculpas son válidas; la segunda es más formal y explícita."
      }
    },
    {
      "type": "FreeSpeakingPrompt",
      "id": "es-en-b1-e2",
      "prompt": "Aclara el malentendido con tu compañero y propón una solución.",
      "minTokens": 20,
      "conceptClusters": [
        ["sorry", "apologise", "apologize"],
        ["misunderstanding", "confusion"],
        ["if", "would", "could"]
      ],
      "grammarRuleRefs": ["gp-second-conditional"],
      "timeLimitSeconds": 45,
      "minWpm": 60,
      "maxWpm": 150,
      "modelAnswer": "I'm sorry for the confusion. If we reviewed the schedule together now, this wouldn't happen again. Could we do that?",
      "selfAssessmentCriteria": [
        "¿Me disculpé sin excusas?",
        "¿Propuse una solución concreta?",
        "¿Usé una condicional?"
      ],
      "feedbackOnError": {
        "type": "grammar",
        "explanation": "La respuesta modelo usa una condicional para reconocer el error y propone una acción inmediata.",
        "ruleRef": "gp-second-conditional"
      }
    }
  ]
}
```

## Ejemplo de lección B2

```json
{
  "id": "es-en-lv-9-un-1-ls-1",
  "title": "Plazos y concesiones",
  "cefrLevel": "B2",
  "scenario": "Una reunión con un cliente británico para renegociar un plazo de entrega sin romper la relación comercial.",
  "canDo": "Puedo negociar condiciones y justificar concesiones con matices de cortesía, hipótesis y reformulación.",
  "grammarPoints": [
    { "id": "gp-mixed-conditionals", "label": "Mixed conditionals", "description": "Condicionales mixtas para conectar pasado y presente." },
    { "id": "gp-hedging", "label": "Hedging", "description": "Atenuadores de cortesía: I'm afraid, it seems, perhaps." }
  ],
  "vocabFrequencyBand": "3000-5000",
  "culturalNotes": [
    { "idiom": "play it by ear", "note": "En el Reino Unido se usa para decidir sobre la marcha sin comprometerse de antemano.", "translation": "improvisar según las circunstancias" }
  ],
  "exercises": [
    {
      "type": "AudioOnlyPrompt",
      "id": "es-en-b2-e1",
      "audioText": "I'm afraid we can't bring the deadline forward, but we could phase the delivery.",
      "question": "What is the speaker offering?",
      "options": ["A shorter deadline", "A phased delivery", "A discount"],
      "correctIndex": 1,
      "allowSlowPlayback": true,
      "feedbackOnError": {
        "type": "comprehension",
        "explanation": "El hablante rechaza adelantar el plazo, pero ofrece entregar por fases.",
        "ruleRef": "gp-hedging"
      }
    },
    {
      "type": "ClozeDragDrop",
      "id": "es-en-b2-e2",
      "passage": "If we {{b1}} the scope now, we {{b2}} more time for testing.",
      "blanks": [
        { "id": "b1", "answer": "narrow", "options": ["narrow", "narrowed", "narrowing"] },
        { "id": "b2", "answer": "would have", "options": ["will have", "would have", "had"] }
      ],
      "feedbackOnError": {
        "type": "grammar",
        "explanation": "La condicional mixta combina presente con una consecuencia presente o futura.",
        "ruleRef": "gp-mixed-conditionals"
      }
    },
    {
      "type": "FreeSpeakingPrompt",
      "id": "es-en-b2-e3",
      "prompt": "Responde al cliente: no puedes adelantar la entrega, pero propón una alternativa con una condición.",
      "minTokens": 25,
      "conceptClusters": [
        ["deadline", "delivery date", "schedule"],
        ["phase", "split", "stages"],
        ["if", "provided that", "as long as"]
      ],
      "grammarRuleRefs": ["gp-mixed-conditionals", "gp-hedging"],
      "timeLimitSeconds": 60,
      "minWpm": 70,
      "maxWpm": 160,
      "modelAnswer": "I'm afraid we can't bring the deadline forward. However, if we split the delivery into two phases, we could have the first batch ready by Friday, provided that the final specifications are confirmed today.",
      "selfAssessmentCriteria": [
        "¿Rechacé el plazo con cortesía?",
        "¿Propuse una alternativa concreta?",
        "¿Usé una condicional y un atenuador?"
      ],
      "feedbackOnError": {
        "type": "grammar",
        "explanation": "La respuesta modelo combina un rechazo atenuado con una condicional mixta y una condición explícita.",
        "ruleRef": "gp-mixed-conditionals"
      }
    }
  ]
}
```

## Flujo de autoría v2

```bash
# 1. Edita/crea el JSON (UTF-8 sin BOM)
# 2. Valida el contrato (F0.3 enruta v1/v2 automáticamente)
./gradlew validateContent
# 3. Gate completo antes del PR
./gradlew ktlintCheck detekt validateContent test
```

Estado F0.1: `validateContent` aún valida solo `course-v1.json`; el routing
v1/v2 se implementa en F0.3. Hasta entonces, el fixture B2 se valida contra
`course-v2.json` en los tests de contrato.
