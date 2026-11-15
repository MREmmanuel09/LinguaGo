# F3 — Auditoría de B1 (cuatro cursos)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F3.1 (ES→EN), F3.2 (ES→PT), F3.3 (ES→FR) y F3.4 (ES→JA).

## 1. Volumen

| Curso | B1 previo | B1 nuevo | B1 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 30 | 140 | **170** | 1.260 |
| ES→PT | 30 | 140 | **170** | 1.260 |
| ES→FR | 30 | 140 | **170** | 1.260 |
| ES→JA | 30 | 140 | **170** | 1.260 |
| **Total** | 120 | **560** | **680** | **5.040** |

- **Estructura:** 14 bloques de 10 lecciones por curso.
- **Cada lección:** 9 ejercicios, 2 focos gramaticales, `scenario`, `canDo`,
  banda `2000-3000`, nota cultural, microficha, glosario y feedback completo.
- **Producción oral:** un `FreeSpeakingPrompt` por lección.
- **Tipos:** `MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`,
  `ListenChoose`, `ListenType`, `ClozeDragDrop`, `FreeSpeakingPrompt` y
  `SpeakRepeat`.

F3 se construyó sobre la base semántica de F2, elevada con conectores y
tareas B1. La revisión nativa debe validar el salto de nivel, el registro y la
naturalidad de las 560 lecciones.

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→PT | ES→FR | ES→JA |
|---|---:|---:|---:|---:|
| Unidades nuevas | 14 | 14 | 14 | 14 |
| Lecciones nuevas | 140 | 140 | 140 | 140 |
| Ejercicios nuevos | 1.260 | 1.260 | 1.260 | 1.260 |
| Grupos de lecciones duplicadas | 0 | 0 | 0 | 0 |
| Títulos únicos | 140/140 | 140/140 | 140/140 | 140/140 |
| Ejercicios sin `feedbackOnError` | 0 | 0 | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 | 0 | 0 |
| Focos gramaticales | 2/2 | 2/2 | 2/2 | 2/2 |
| Notas culturales | 140 | 140 | 140 | 140 |
| `FreeSpeakingPrompt` | 140 | 140 | 140 | 140 |
| Frase más larga | 18 tokens | 16 tokens | 17 tokens | 12 tokens |

En JA, la frase más larga tiene 42 caracteres japoneses (sin espacios).

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` →
  **BUILD SUCCESSFUL**.
- `contentScaleReport` → **2.416/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_b1_completa_f3` y
  `cursos_de_arranque_completan_b1_f3`.
- `ContentRampTest` exige 170 lecciones B1, 9 ejercicios, 2 focos
  gramaticales, un `FreeSpeakingPrompt` y un máximo de 24 tokens.
- `python tools/content/audit_f3.py` → informe en `build/reports/f3-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F3.1–F3.4.** No se ha repetido la aprobación
   masiva de ADR-054. Es especialmente importante validar el salto A2→B1 y el
   registro de PT/JA.
2. **Release 3.4.0.** Aparcado por decisión de producto (2026-11-15). No se ha
   cambiado la versión ni se ha publicado ningún artefacto.

## 5. Herramientas

- `tools/content/b1_factory.py`: 9 ejercicios, cloze, FreeSpeakingPrompt y
  validación por lote.
- `tools/content/b1_specs.py`: 14 bloques B1 y notas culturales.
- `tools/content/f3_1_b1_es_en.py`: inserción de F3.1.
- `tools/content/f3_2_b1_es_pt.py`: inserción de F3.2.
- `tools/content/f3_3_b1_es_fr.py`: inserción de F3.3.
- `tools/content/f3_4_b1_es_ja.py`: inserción de F3.4.
- `tools/content/audit_f3.py`: auditoría de duplicados, tokens y feedback.
