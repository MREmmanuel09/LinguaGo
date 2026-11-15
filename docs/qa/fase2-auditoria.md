# F2 — Auditoría de A2 (ES→EN y ES→FR)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F2.1 (A2 ES→EN) y F2.3 (A2 ES→FR).

## 1. Volumen

| Curso | A2 previo | A2 nuevo | A2 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 27 | 143 | **170** | 1.001 |
| ES→FR | 18 | 152 | **170** | 1.064 |
| **Total** | 45 | **295** | **340** | **2.065** |

- **F2.1:** 14 bloques de 10 lecciones + 1 unidad de cierre de 3 lecciones.
- **F2.3:** 15 bloques de 10 lecciones + 1 unidad de cierre de 2 lecciones.
- Cada lección tiene 7 ejercicios y hasta 2 focos gramaticales (A2).

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→FR |
|---|---:|---:|
| Lecciones nuevas | 143 | 152 |
| Ejercicios nuevos | 1.001 | 1.064 |
| Grupos de lecciones duplicadas | 0 | 0 |
| Títulos únicos | 143/143 | 152/152 |
| Ejercicios sin `feedbackOnError` | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 |
| Frase más larga | 12 tokens | 12 tokens |
| Focos gramaticales | 1–2 | 1–2 |

Reparto de ejercicios por tipo (idéntico en ambos cursos, 7 por lección):

- `MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`, `ListenChoose`,
  `ListenType` y `SpeakRepeat`.

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test` → **BUILD SUCCESSFUL**.
- `./gradlew contentScaleReport` → **1.552/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_a2_completa_f2` y
  `es_fr_a2_completa_f2`.
- `ContentRampTest` mantiene el tope A2 de 12 tokens y limita la gramática a
  2 focos por lección.
- `python tools/content/audit_f2.py` → informe en `build/reports/f2-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F2.1 y F2.3.** No se ha repetido la aprobación
   masiva de ADR-054. El release 3.3.0 exige cerrar esta revisión.
2. **F2.2 (A2 ES→PT) y F2.4 (A2 ES→JA).** A2 queda en 18/170 en ambos cursos.
3. **Release 3.3.0.** No se abre hasta completar los cuatro cursos y su puerta
   nativa.

## 5. Herramientas

- `tools/content/a2_factory.py`: patrones, construcción y validación por lote.
- `tools/content/a2_specs.py`: 15 bloques temáticos y cierre de FR.
- `tools/content/f2_1_a2_es_en.py`: inserción de F2.1.
- `tools/content/f2_3_a2_es_fr.py`: inserción de F2.3.
- `tools/content/audit_f2.py`: auditoría de duplicados, tokens y feedback.
