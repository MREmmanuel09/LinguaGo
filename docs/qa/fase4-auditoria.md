# F4 — Auditoría de B2 (cuatro cursos)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F4.1 (ES→EN), F4.2 (ES→PT), F4.3 (ES→FR) y F4.4 (ES→JA).

## 1. Volumen

| Curso | B2 previo | B2 nuevo | B2 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 32 | 138 | **170** | 1.380 |
| ES→PT | 32 | 138 | **170** | 1.380 |
| ES→FR | 32 | 138 | **170** | 1.380 |
| ES→JA | 32 | 138 | **170** | 1.380 |
| **Total** | 128 | **552** | **680** | **5.520** |

- **Estructura:** 14 bloques por curso (13 de 10 lecciones + 1 de 8).
- **Cada lección:** 10 ejercicios, 2 focos gramaticales, `scenario`, `canDo`,
  banda `3000-5000`, nota cultural, microficha, glosario y feedback completo.
- **Producción oral:** un `FreeSpeakingPrompt` por lección.
- **Tipos:** `MultipleChoice`, `OrderWords`, `ClozeDragDrop`, dos
  `SemanticMatch`, dos `AudioOnlyPrompt`, `ListenType`, `SpeakRepeat` y
  `FreeSpeakingPrompt`.
- **Modismos:** 138 modismos nuevos y únicos en ES→EN (170 con los históricos).

F4 se construyó sobre la base semántica de F2, elevada con conectores y
matices B2. La revisión nativa debe validar el salto de nivel y el registro.

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→PT | ES→FR | ES→JA |
|---|---:|---:|---:|---:|
| Unidades nuevas | 14 | 14 | 14 | 14 |
| Lecciones nuevas | 138 | 138 | 138 | 138 |
| Ejercicios nuevos | 1.380 | 1.380 | 1.380 | 1.380 |
| Grupos de lecciones duplicadas | 0 | 0 | 0 | 0 |
| Títulos únicos | 138/138 | 138/138 | 138/138 | 138/138 |
| Ejercicios sin `feedbackOnError` | 0 | 0 | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 | 0 | 0 |
| Focos gramaticales | 2/2 | 2/2 | 2/2 | 2/2 |
| Notas culturales | 138 | 138 | 138 | 138 |
| `FreeSpeakingPrompt` | 138 | 138 | 138 | 138 |
| Frase más larga | 20 tokens | 18 tokens | 20 tokens | 12 tokens |

En JA, la frase más larga tiene 49 caracteres japoneses (sin espacios).

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` →
  **BUILD SUCCESSFUL**.
- `contentScaleReport` → **2.968/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_b2_completa_f4` y
  `cursos_de_arranque_completan_b2_f4`.
- `ContentRampTest` exige 170 lecciones B2, 10 ejercicios, 2 focos
  gramaticales, un `FreeSpeakingPrompt` y un máximo de 30 tokens.
- `python tools/content/audit_f4.py` → informe en `build/reports/f4-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F4.1–F4.4.** No se ha repetido la aprobación
   masiva de ADR-054. Es importante validar el salto B1→B2, la naturalidad de
   los conectores y el registro de PT/JA.
2. **Release 3.5.0.** Aparcado por decisión de producto (2026-11-15). No se ha
   cambiado la versión ni se ha publicado ningún artefacto.

## 5. Herramientas

- `tools/content/b2_factory.py`: 10 ejercicios, SemanticMatch, AudioOnlyPrompt,
  cloze y FreeSpeakingPrompt.
- `tools/content/b2_specs.py`: 14 bloques B2 y 138 modismos únicos.
- `tools/content/f4_1_b2_es_en.py`: inserción de F4.1.
- `tools/content/f4_2_b2_es_pt.py`: inserción de F4.2.
- `tools/content/f4_3_b2_es_fr.py`: inserción de F4.3.
- `tools/content/f4_4_b2_es_ja.py`: inserción de F4.4.
- `tools/content/audit_f4.py`: auditoría de duplicados, tokens y feedback.
