# F0/F1 — Kit de revisión nativa

**Fecha:** 2026-11-15
**Estado:** plan preparado; revisión humana pendiente.
**Decisión:** ADR-052, ADR-053 y ADR-054.

> El release 3.2.0 se publica con **aprobación técnica** (ADR-054). Esta
> revisión nativa sigue pendiente y se usará para corregir el contenido en un
> parche 3.2.x si aparecen incidencias.

## 1. Alcance

| Bloque | Lecciones | Contenido |
|---|---:|---|
| F0 | 240 | A1–C2 ES→EN + réplicas PT/FR/JA |
| F1 | 600 | A1 ES→EN/PT/FR/JA |
| **Total** | **840** | 40 modismos B2 del piloto |

Archivos de trabajo:

- `build/reports/native-review-plan.csv`: 840 lecciones con flags de revisión.
- `build/reports/native-review-b2-idioms.csv`: 40 modismos B2.

Generación reproducible:

```powershell
python tools/content/prepare_native_review.py
```

## 2. Muestreo obligatorio

Según ADR-052:

1. **100 % de micro-clases.**
2. **100 % de audio** (`ListenChoose`, `ListenType`, `SpeakRepeat`, `AudioOnlyPrompt`).
3. **100 % de producción oral** (`FreeSpeakingPrompt`) en B1–C2.
4. **20 % estratificado del resto**, marcado con `stratified_sample=yes`.

El CSV ya marca cada lección con:

- `micro_lesson_required`
- `audio_required` y `audio_exercises`
- `free_speaking_required`
- `stratified_sample`

## 3. Cómo registrar la revisión

En `native-review-plan.csv`:

- `review_status`: `pending` / `approved` / `needs_change`.
- `reviewer`: nombre o alias.
- `notes`: problema, ejercicio concreto y corrección propuesta.

En `native-review-b2-idioms.csv`:

- `review_status`: `approved` / `needs_change`.
- `notes`: forma correcta, registro o traducción.

Para incidencias, usa este formato:

```text
lesson_id / exercise_id — severidad — problema — corrección propuesta
```

Severidades: `blocker`, `major`, `minor`, `suggestion`.

## 4. Checklist por idioma

### ES→EN

- Artículos, preposiciones y contracciones.
- Tiempos verbales y concordancia.
- Naturalidad de las frases de audio.
- Ortografía británica/americana coherente (`colour`/`color`).

### ES→PT

- Portugués brasileño (`você`, `celular`, `ônibus`, `café da manhã`).
- Contracciones (`no`, `na`, `do`, `da`).
- Concordancia y conjugación.
- Naturalidad de `Eu gosto de...` y `Eu preciso de...`.

### ES→FR

- Elisión y contracciones (`au`, `du`, `à l'`, `d'un`).
- Partitivos (`du pain`, `de la musique`, `de l'eau`).
- Género, número y acentos.
- Naturalidad de `J'aime...` y `J'ai besoin de...`.

### ES→JA

- Partículas (`は`, `が`, `を`, `に`, `で`, `へ`).
- Cortesía `です`/`ます`.
- Contadores (`一冊`, `二冊`, `三冊`).
- Separación por morfemas y longitud.
- Kanji/kana adecuados al nivel A1.

## 5. Checklist por nivel

### A1

- 7 ejercicios.
- 0 focos gramaticales declarados.
- Máximo 8 tokens.
- Sin `FreeSpeakingPrompt` ni `AudioOnlyPrompt`.

### B1–C2

- 9/10/11/12 ejercicios.
- 2 focos gramaticales.
- `FreeSpeakingPrompt` y `feedbackOnError`.
- Nota cultural y micro-clase.
- Frases dentro del tope del nivel.

## 6. Incidencias conocidas

- **F0:** 146/150 réplicas reutilizan ejercicios históricos; se aceptan como
  repaso provisional.
- **F1:** contenido generado con patrones; puede sonar repetitivo.
- **JA:** la tokenización por espacios no mide morfemas reales; revisar la
  longitud por caracteres/morfermas.
- **F1 JA:** algunos verbos se simplificaron para respetar el tope de 8 tokens.

## 7. Cierre

La revisión nativa se considera cerrada cuando:

1. Las 840 lecciones tengan `review_status` distinto de `pending`.
2. Los 40 modismos B2 estén aprobados o corregidos.
3. No queden incidencias `blocker` o `major`.
4. Se haya ejecutado de nuevo el gate completo.
5. Se firme el acta y se publique el release estable con el keystore de
   distribución.

## 8. Firma

| Rol | Nombre | Fecha | Resultado |
|---|---|---|---|
| Revisor nativo EN |  |  |  |
| Revisor nativo PT |  |  |  |
| Revisor nativo FR |  |  |  |
| Revisor nativo JA |  |  |  |
| Responsable de release |  |  |  |
