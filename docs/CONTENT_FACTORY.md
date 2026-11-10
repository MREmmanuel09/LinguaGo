# Fábrica de contenido — F0

> Documento operativo de la expansión a 1.020 lecciones por idioma
> (ADR-052). Define cómo se produce un bloque de 10 lecciones y qué debe
> cumplir antes de entrar en `course.json`.

## 1. Unidad de producción

- **Bloque editorial:** 10 lecciones.
- **Nivel:** 17 bloques (170 lecciones).
- **Curso:** 102 bloques.
- **Total del programa:** 408 bloques / 4.080 lecciones.

Un bloque no se mezcla con otro en el mismo commit `content:`. Cada bloque
tiene un objetivo comunicativo, dos focos gramaticales (B1+) y una nota
cultural por lección.

## 2. Plantilla de bloque

| Lección | Función | Evidencia de salida |
|---:|---|---|
| 1 | Consolidación del bloque anterior | Reciclaje explícito de 30–40 % |
| 2 | Entrada contextual | Escenario + audio |
| 3 | Léxico y colocaciones | Glosario de 6–14 términos |
| 4 | Foco gramatical 1 | `grammarPoints[0]` |
| 5 | Foco gramatical 2 | `grammarPoints[1]` |
| 6 | Interacción / rol | Diálogo o tarea de elección |
| 7 | Comprensión | `ListenChoose`, `ListenType` o `AudioOnlyPrompt` |
| 8 | Producción guiada | `SpeakRepeat` o `FreeSpeakingPrompt` |
| 9 | Integración | Ejercicio que combina foco 1 + foco 2 |
| 10 | Evaluación formativa | Bloque corto de repaso y producción |

## 3. Contrato de lección

Campos obligatorios:

- `id`, `title`, `cefrLevel`, `exercises`.
- B1+: `scenario`, `canDo`, `grammarPoints` (2), `vocabFrequencyBand`,
  `culturalNotes` (≥ 1), `microLesson`, `glossary`.
- `feedbackOnError` en todos los ejercicios de B1+.
- `feedbackOnSuccess` recomendado en los focos gramaticales.
- Una `FreeSpeakingPrompt` en B1/B2/C1 y dos en C2.
- Tokens máximos: A1 8 · A2 12 · B1 24 · B2 30 · C1 38 · C2 48.

Cantidad de ejercicios por nivel:

| Nivel | Ejercicios |
|---|---:|
| A1 | 7 |
| A2 | 7 |
| B1 | 9 |
| B2 | 10 |
| C1 | 11 |
| C2 | 12 |

## 4. Ids

- Lección nueva: `es-<idioma>-<nivel>-u<bloque>-l<lección>`.
- Ejercicio nuevo: `<lesson-id>-e<nn>`.
- No se recicla ningún id histórico.
- Los ids deben ser únicos en todo el curso, no solo en la lección.

Los bloques de la ola B usan el mismo esqueleto semántico que la ola A, pero
con ids propios por idioma y contenido adaptado, no traducción literal.

## 5. Flujo de autoría

1. **Matriz:** completar la fila del bloque (objetivo, canDo, gramática,
   léxico, cultura, reciclaje).
2. **Borrador ES→EN:** escribir las 10 lecciones y sus ejercicios.
3. **Autochequeo:** `validateContent`, `ContentRampTest`, duplicados,
   `modelAnswer`, tokens y UTF-8 sin BOM.
4. **Revisión nativa EN:** 100 % de micro-clases, audio y oral; 20 % del resto.
5. **Adaptación PT/FR/JA:** misma función comunicativa, contenido natural por
   idioma.
6. **Revisión nativa por idioma:** mismo muestreo.
7. **Commit `content:` por bloque** y actualización de conteos si aplica.

## 6. Puertas de calidad

### Puerta de lote

- Esquema v2 válido.
- 10 lecciones, 7–12 ejercicios por nivel.
- Sin ids duplicados ni reciclados.
- Todos los `FreeSpeakingPrompt` con `modelAnswer` que pasa el grader.
- Glosario, micro-clase, cultura y feedback completos.

### Puerta de ola

- Revisión nativa de la sección 5.
- Audio/TTS sin frases por encima del tope.
- Sin duplicados semánticos entre lecciones del mismo nivel.
- Smoke en emulador de las lecciones del bloque.

### Puerta de release

- `ktlintCheck detekt validateContent test` en verde.
- Crecimiento del APK comprimido dentro de presupuesto.
- Scroll del camino y perfil CEFR comprobados.
- Sin incidencias nativas abiertas del nivel.

## 7. Orden de F0

1. Aprobar ADR-052.
2. Añadir `contentScaleReport` y congelar los cupos en un informe.
3. Escribir el bloque piloto **A1 ES→EN**.
4. Repetir el piloto en A2–C2 de ES→EN.
5. Adaptar los seis bloques a PT/FR/JA.
6. Auditoría de F0 y decisión de continuar a F1.

## 8. Comando de seguimiento

```powershell
./gradlew contentScaleReport
```

Muestra lecciones actuales, objetivo (170 por nivel) y hueco restante por
curso y nivel. No bloquea el build durante F0; a partir de F1 se usa como
semáforo de avance.
