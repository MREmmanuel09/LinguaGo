# F1.3 — A1 completo de ES→FR

**Fecha:** 2026-11-10
**Estado:** borrador generado; pendiente de revisión nativa y del gate de F0.

## 1. Alcance

| Concepto | Valor |
|---|---:|
| Bloques nuevos | 15 |
| Lecciones nuevas | 150 |
| Ejercicios nuevos | 1.050 |
| A1 ES→FR después de F1.3 | 170/170 |
| ES→FR total después de F1.3 | 312/1.020 |

La matriz replica los 15 bloques de F1.1/F1.2, adaptados al francés:

1. Premiers pas
2. Ma journée
3. Personnes
4. Nourriture
5. Maison et ville
6. Santé
7. École et travail
8. Achats
9. Voyages
10. Temps libre
11. Nature
12. Technologie
13. Culture
14. Révision et projets
15. Consolidation A1

Cada bloque tiene 10 lecciones y cada lección 7 ejercicios:
`MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`, `ListenChoose`,
`ListenType` y `SpeakRepeat`.

## 2. Contrato respetado

- `cefrLevel: A1`.
- `vocabFrequencyBand: 1000-2000`.
- 0 focos gramaticales declarados.
- Frases de hasta 8 tokens.
- `feedbackOnError` en el 100 % de los ejercicios.
- IDs nuevos `es-fr-a1-uNN-lNN` y `...-eNN`; ids históricos intactos.
- Sin `FreeSpeakingPrompt` ni `AudioOnlyPrompt` en A1.

## 3. Verificación

- `validateContent`: ES→FR **312 lecciones / 2.616 ejercicios**; schema v2 válido.
- `ContentRampTest`: 11 tests, 0 fallos.
- `PackagedContentTest`: 18 tests, 0 fallos, incluido `es_fr_a1_completa_f1`.
- Gate `ktlintCheck detekt validateContent test contentScaleReport`: BUILD SUCCESSFUL.
- `contentScaleReport`: **1.107/4.080** lecciones.
- Smoke: «A1 · Personnes» visible en el camino con «0 de 10 lecciones».

## 4. Pendiente

- Revisión nativa humana de F1.3 (y de F0, que sigue siendo la puerta de F1).
- Ola B: A1 ES→JA (F1.4).
- Auditoría y release 3.2.0 (F1.5).
- Sustituir las réplicas de F0 por contenido original en las fases siguientes.

## 5. Nota de calidad

F1.3 usa una capa francesa sobre la matriz de F1.1, con contracciones
(`au`, `du`, `à l'`) y frases positivas para evitar negaciones complejas en A1.
Es contenido nuevo, pero **no se considera cerrado** hasta pasar la revisión
nativa y el smoke de todas las unidades. No debe publicarse como release
3.2.0 todavía.
