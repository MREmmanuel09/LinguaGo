# F6 — Auditoría de C2 (cuatro cursos)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F6.1 (ES→EN), F6.2 (ES→PT), F6.3 (ES→FR) y F6.4 (ES→JA).

## 1. Volumen

| Curso | C2 previo | C2 nuevo | C2 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 28 | 142 | **170** | 1.704 |
| ES→PT | 28 | 142 | **170** | 1.704 |
| ES→FR | 28 | 142 | **170** | 1.704 |
| ES→JA | 28 | 142 | **170** | 1.704 |
| **Total** | 112 | **568** | **680** | **6.816** |

- **Estructura:** 15 bloques por curso (14 de 10 lecciones + 1 de 2).
- **Cada lección:** 12 ejercicios, 2 focos gramaticales, `scenario`, `canDo`,
  banda `12000+`, nota cultural, microficha, glosario y feedback completo.
- **Producción oral:** un `FreeSpeakingPrompt` por lección.
- **Tipos:** `MultipleChoice`, `Translate`, tres `ClozeDragDrop`,
  `SemanticMatch`, `ListenChoose`, `AudioOnlyPrompt`, `ListenType`,
  `SpeakRepeat`, `MatchPairs` y `FreeSpeakingPrompt`.

F6 se construyó sobre la base semántica de F2, elevada con conectores,
ironía y matices C2. La revisión nativa debe validar el salto C1→C2.

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→PT | ES→FR | ES→JA |
|---|---:|---:|---:|---:|
| Unidades nuevas | 15 | 15 | 15 | 15 |
| Lecciones nuevas | 142 | 142 | 142 | 142 |
| Ejercicios nuevos | 1.704 | 1.704 | 1.704 | 1.704 |
| Grupos de lecciones duplicadas | 0 | 0 | 0 | 0 |
| Títulos únicos | 142/142 | 142/142 | 142/142 | 142/142 |
| Ejercicios sin `feedbackOnError` | 0 | 0 | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 | 0 | 0 |
| Focos gramaticales | 2/2 | 2/2 | 2/2 | 2/2 |
| Notas culturales | 142 | 142 | 142 | 142 |
| `FreeSpeakingPrompt` | 142 | 142 | 142 | 142 |
| Frase más larga | 20 tokens | 20 tokens | 22 tokens | 15 tokens |

En JA, la frase más larga tiene 59 caracteres japoneses (sin espacios).

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` →
  **BUILD SUCCESSFUL**.
- `contentScaleReport` → **4.080/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_c2_completa_f6` y
  `cursos_de_arranque_completan_c2_f6`.
- `ContentRampTest` exige 12 ejercicios, 2 focos gramaticales, un
  `FreeSpeakingPrompt` y un máximo de 48 tokens.
- `python tools/content/audit_f5_f6.py` → informe en
  `build/reports/f5-f6-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F6.1–F6.4.** No se ha repetido la aprobación
   masiva de ADR-054. Es importante validar el salto C1→C2, la ironía y el
   registro de PT/JA.
2. **Release 4.0.0.** Aparcado por decisión de producto (2026-11-15). No se ha
   cambiado la versión ni se ha publicado ningún artefacto.

## 5. Herramientas

- `tools/content/advanced_factory.py`: 11/12 ejercicios, clozes, SemanticMatch,
  AudioOnlyPrompt y FreeSpeakingPrompt.
- `tools/content/advanced_specs.py`: bloques C1/C2 y notas culturales.
- `tools/content/f5_f6_advanced.py`: inserción de C1 y C2.
- `tools/content/audit_f5_f6.py`: auditoría de duplicados, tokens y feedback.
