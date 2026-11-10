# F1.2 — A1 completo de ES→PT

**Fecha:** 2026-11-10
**Estado:** borrador generado; pendiente de revisión nativa y del gate de F0.

## 1. Alcance

| Concepto | Valor |
|---|---:|
| Bloques nuevos | 15 |
| Lecciones nuevas | 150 |
| Ejercicios nuevos | 1.050 |
| A1 ES→PT después de F1.2 | 170/170 |
| ES→PT total después de F1.2 | 312/1.020 |

La matriz replica los 15 bloques de F1.1, adaptados al portugués brasileño:

1. Primeiros passos
2. Meu dia
3. Pessoas
4. Comida
5. Casa e cidade
6. Saúde
7. Escola e trabalho
8. Compras
9. Viagens
10. Tempo livre
11. Natureza
12. Tecnologia
13. Cultura
14. Revisão e projetos
15. Consolidação A1

Cada bloque tiene 10 lecciones y cada lección 7 ejercicios:
`MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`, `ListenChoose`,
`ListenType` y `SpeakRepeat`.

## 2. Contrato respetado

- `cefrLevel: A1`.
- `vocabFrequencyBand: 1000-2000`.
- 0 focos gramaticales declarados.
- Frases de hasta 8 tokens.
- `feedbackOnError` en el 100 % de los ejercicios.
- IDs nuevos `es-pt-a1-uNN-lNN` y `...-eNN`; ids históricos intactos.
- Sin `FreeSpeakingPrompt` ni `AudioOnlyPrompt` en A1.

## 3. Verificación

- `validateContent`: ES→PT **312 lecciones / 2.616 ejercicios**; schema v2 válido.
- `ContentRampTest`: 11 tests, 0 fallos.
- `PackagedContentTest`: 17 tests, 0 fallos, incluido `es_pt_a1_completa_f1`.
- Gate `ktlintCheck detekt validateContent test contentScaleReport`: BUILD SUCCESSFUL.
- `contentScaleReport`: **957/4.080** lecciones.
- Smoke: «A1 · Comida» visible en el camino con «0 de 10 lecciones».

## 4. Pendiente

- Revisión nativa humana de F1.2 (y de F0, que sigue siendo la puerta de F1).
- Ola B: A1 ES→FR (F1.3) y ES→JA (F1.4).
- Auditoría y release 3.2.0 (F1.5).
- Sustituir las réplicas de F0 por contenido original en las fases siguientes.

## 5. Nota de calidad

F1.2 usa la misma fábrica de patrones A1 que F1.1, con vocabulario y gramática
adaptados al portugués brasileño. Es contenido nuevo, pero **no se considera
cerrado** hasta pasar la revisión nativa y el smoke de todas las unidades. No
debe publicarse como release 3.2.0 todavía.
