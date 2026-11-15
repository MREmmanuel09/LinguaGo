# F1.5 — Auditoría de F1

**Fecha:** 2026-11-15
**Alcance:** 60 unidades y 600 lecciones A1 nuevas (150 por curso), 4.200 ejercicios.
**Método:** auditor automático (`tools/content/audit_f1.py`) + revisión lingüística asistida + gate completo.

## Resultado ejecutivo

F1 está **estructuralmente cerrada**: los cuatro cursos tienen A1 170/170, sin lecciones duplicadas, con 7 ejercicios por lección y todos los contratos de A1 respetados. La revisión nativa humana sigue pendiente y es el bloqueo principal para el release estable.

## Cobertura

| Curso | Unidades F1 | Lecciones | Ejercicios | Duplicados | Tipos |
|---|---:|---:|---:|---:|---|
| ES→EN | 15 | 150 | 1.050 | 0 | 150 de cada tipo |
| ES→PT | 15 | 150 | 1.050 | 0 | 150 de cada tipo |
| ES→FR | 15 | 150 | 1.050 | 0 | 150 de cada tipo |
| ES→JA | 15 | 150 | 1.050 | 0 | 150 de cada tipo |

Cada lección usa `MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`,
`ListenChoose`, `ListenType` y `SpeakRepeat`.

## Comprobaciones

- **Estructura:** 15 bloques por curso, 10 lecciones por bloque.
- **Contrato A1:** 7 ejercicios, 0 focos gramaticales, banda `1000-2000`.
- **Longitud:** máximo 8 tokens por frase (EN 8, PT 7, FR 7, JA 7).
- **Feedback:** 0 ejercicios sin `feedbackOnError`.
- **Ids:** nuevos `es-<curso>-a1-uNN-lNN`; históricos intactos.
- **Títulos:** 150 únicos por curso.
- **Duplicados:** 0 grupos duplicados tras sustituir los 5 pares detectados en cada curso.
- **Japonés:** máximo 14 caracteres por frase; tokenización por espacios.
- **FreeSpeaking:** no aplica en A1.

## Correcciones aplicadas

La primera pasada detectó 5 pares de lecciones duplicadas por curso (10 lecciones
por curso). Se sustituyeron por contenido nuevo:

- Aeropuerto/terminal/sala de espera en lugar de repetir direcciones.
- Voleibol/bádminton/rugby en lugar de repetir fútbol/tenis/baloncesto.
- Cine/café/excursión en lugar de repetir las invitaciones.
- Dudas/preguntas/ideas en lugar de repetir problemas.
- Examen/nota/resultado en lugar de repetir la evaluación.

Tras la corrección, `audit_f1.py` reporta **0 duplicados** en los cuatro cursos.

## Hallazgos

| Severidad | Hallazgo | Impacto | Recomendación |
|---|---|---|---|
| Alta | Revisión nativa humana pendiente | Bloquea el release estable | Completar el gate de ADR-053 |
| Media | Contenido generado con patrones | Repetición de estructuras y vocabulario controlado | Revisión nativa y variación en F2–F6 |
| Media | Tokenización JA por espacios | El tope de tokens no mide morfemas reales | Métrica por caracteres/morfermas |
| Baja | Algunas frases japonesas simplificadas | Verbos sin objeto para respetar el tope | Revisar naturalidad en la pasada nativa |

## Verificación

- `python tools/content/audit_f1.py` → `build/reports/f1-audit.json`.
- `validateContent`: 1.257 lecciones / 10.527 ejercicios; schema v2 válido.
- `PackagedContentTest`: 19 tests, 0 fallos.
- `ContentRampTest`: 11 tests, 0 fallos.
- `contentScaleReport`: **1.257/4.080**.
- Gate `ktlintCheck detekt validateContent test contentScaleReport`: BUILD SUCCESSFUL.

## Conclusión

F1 queda **auditada técnicamente**. El release estable 3.2.0 no debe publicarse
hasta completar la revisión nativa humana y firmar el APK con el keystore de
distribución.

Kit de revisión nativa: `docs/qa/fase1.5-revision-nativa.md`.
CSV: `build/reports/native-review-plan.csv` (840 lecciones) y
`build/reports/native-review-b2-idioms.csv` (40 modismos).
