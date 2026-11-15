# F2 — Auditoría de A2 (cuatro cursos)

**Fecha:** 2026-11-15
**Estado:** auditoría técnica completa; revisión nativa pendiente.
**Alcance:** F2.1 (ES→EN), F2.2 (ES→PT), F2.3 (ES→FR) y F2.4 (ES→JA).

## 1. Volumen

| Curso | A2 previo | A2 nuevo | A2 total | Ejercicios nuevos |
|---|---:|---:|---:|---:|
| ES→EN | 27 | 143 | **170** | 1.001 |
| ES→PT | 18 | 152 | **170** | 1.064 |
| ES→FR | 18 | 152 | **170** | 1.064 |
| ES→JA | 18 | 152 | **170** | 1.064 |
| **Total** | 81 | **599** | **680** | **4.193** |

- **F2.1:** 14 bloques de 10 lecciones + 1 unidad de cierre de 3 lecciones.
- **F2.2, F2.3 y F2.4:** 15 bloques de 10 lecciones + 1 unidad de cierre de 2
  lecciones.
- Cada lección tiene 7 ejercicios y hasta 2 focos gramaticales (A2).
- PT y JA reutilizan el currículo semántico A2; JA se segmenta con Janome para
  que `OrderWords` y `Translate` tengan tokens estables.
- Las versiones PT/JA se generaron con traducción asistida. La revisión nativa
  debe validar registro, naturalidad y segmentación antes del 3.3.0.

## 2. Resultado de la auditoría

| Comprobación | ES→EN | ES→PT | ES→FR | ES→JA |
|---|---:|---:|---:|---:|
| Lecciones nuevas | 143 | 152 | 152 | 152 |
| Ejercicios nuevos | 1.001 | 1.064 | 1.064 | 1.064 |
| Grupos de lecciones duplicadas | 0 | 0 | 0 | 0 |
| Títulos únicos | 143/143 | 152/152 | 152/152 | 152/152 |
| Ejercicios sin `feedbackOnError` | 0 | 0 | 0 | 0 |
| Ids de ejercicio duplicados | 0 | 0 | 0 | 0 |
| Frase más larga | 12 tokens | 12 tokens | 12 tokens | 12 tokens |
| Focos gramaticales | 1–2 | 1–2 | 1–2 | 1–2 |

Reparto de ejercicios por tipo (idéntico en los cuatro cursos, 7 por lección):

- `MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`, `ListenChoose`,
  `ListenType` y `SpeakRepeat`.

En JA, la frase más larga tiene 30 caracteres japoneses (sin espacios), además
del tope de 12 tokens espaciados.

## 3. Gate ejecutado

- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` →
  **BUILD SUCCESSFUL**.
- `contentScaleReport` → **1.856/4.080** lecciones.
- `PackagedContentTest` incluye `es_en_a2_completa_f2`,
  `es_pt_a2_completa_f2`, `es_fr_a2_completa_f2` y `es_ja_a2_completa_f2`.
- `ContentRampTest` mantiene el tope A2 de 12 tokens y limita la gramática a
  2 focos por lección.
- `python tools/content/audit_f2.py` → informe en `build/reports/f2-audit.json`.

## 4. Pendiente

1. **Revisión nativa humana de F2.1–F2.4.** No se ha repetido la aprobación
   masiva de ADR-054. El release 3.3.0 exige cerrar esta revisión; la revisión
   de JA es especialmente importante por la segmentación y el registro.
2. **Release 3.3.0.** No se abre hasta completar la puerta nativa y el gate de
   release.

## 5. Herramientas

- `tools/content/a2_factory.py`: patrones, construcción y validación por lote.
- `tools/content/a2_specs.py`: 15 bloques temáticos compartidos.
- `tools/content/a2_localized.py`: adaptación a PT/JA desde las traducciones.
- `tools/content/translate_a2.py`: traducción y segmentación JA con Janome.
- `tools/content/f2_1_a2_es_en.py`: inserción de F2.1.
- `tools/content/f2_2_a2_es_pt.py`: inserción de F2.2.
- `tools/content/f2_3_a2_es_fr.py`: inserción de F2.3.
- `tools/content/f2_4_a2_es_ja.py`: inserción de F2.4.
- `tools/content/audit_f2.py`: auditoría de duplicados, tokens y feedback.
