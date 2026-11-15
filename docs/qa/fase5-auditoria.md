# F5 — Auditoría de C1 (cuatro cursos)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F5.1 (ES→EN), F5.2 (ES→PT), F5.3 (ES→FR) y F5.4 (ES→JA).

## 1. Volumen

| Curso | C1 previo | C1 nuevo | C1 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 34 | 136 | **170** | 1.496 |
| ES→PT | 34 | 136 | **170** | 1.496 |
| ES→FR | 34 | 136 | **170** | 1.496 |
| ES→JA | 34 | 136 | **170** | 1.496 |
| **Total** | 136 | **544** | **680** | **5.984** |

- **Estructura:** 14 bloques por curso (13 de 10 lecciones + 1 de 6).
- **Cada lección:** 11 ejercicios, 2 focos gramaticales, `scenario`, `canDo`,
  banda `8000-12000`, nota cultural, microficha, glosario y feedback completo.
- **Producción oral:** un `FreeSpeakingPrompt` por lección.
- **Tipos:** `MultipleChoice`, `OrderWords`, `Translate`, `ClozeDragDrop`,
  `SemanticMatch`, `ListenChoose`, `AudioOnlyPrompt`, `ListenType`,
  `SpeakRepeat`, `MatchPairs` y `FreeSpeakingPrompt`.

F5 se construyó sobre la base semántica de F2, elevada con conectores y
matices C1. La revisión nativa debe validar el salto B2→C1 y el registro.

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→PT | ES→FR | ES→JA |
|---|---:|---:|---:|---:|
| Unidades nuevas | 14 | 14 | 14 | 14 |
| Lecciones nuevas | 136 | 136 | 136 | 136 |
| Ejercicios nuevos | 1.496 | 1.496 | 1.496 | 1.496 |
| Grupos de lecciones duplicadas | 0 | 0 | 0 | 0 |
| Títulos únicos | 136/136 | 136/136 | 136/136 | 136/136 |
| Ejercicios sin `feedbackOnError` | 0 | 0 | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 | 0 | 0 |
| Focos gramaticales | 2/2 | 2/2 | 2/2 | 2/2 |
| Notas culturales | 136 | 136 | 136 | 136 |
| `FreeSpeakingPrompt` | 136 | 136 | 136 | 136 |
| Frase más larga | 21 tokens | 19 tokens | 20 tokens | 13 tokens |

En JA, la frase más larga tiene 51 caracteres japoneses (sin espacios).

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` →
  **BUILD SUCCESSFUL**.
- `contentScaleReport` → **4.080/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_c1_completa_f5` y
  `cursos_de_arranque_completan_c1_f5`.
- `ContentRampTest` exige 11 ejercicios, 2 focos gramaticales, un
  `FreeSpeakingPrompt` y un máximo de 38 tokens.
- `python tools/content/audit_f5_f6.py` → informe en
  `build/reports/f5-f6-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F5.1–F5.4.** No se ha repetido la aprobación
   masiva de ADR-054. Es importante validar el salto B2→C1, la naturalidad de
   los conectores y el registro de PT/JA.
2. **Release 3.6.0.** Aparcado por decisión de producto (2026-11-15). No se ha
   cambiado la versión ni se ha publicado ningún artefacto.

## 5. Herramientas

- `tools/content/advanced_factory.py`: 11/12 ejercicios, clozes, SemanticMatch,
  AudioOnlyPrompt y FreeSpeakingPrompt.
- `tools/content/advanced_specs.py`: bloques C1/C2 y notas culturales.
- `tools/content/f5_f6_advanced.py`: inserción de C1 y C2.
- `tools/content/audit_f5_f6.py`: auditoría de duplicados, tokens y feedback.
