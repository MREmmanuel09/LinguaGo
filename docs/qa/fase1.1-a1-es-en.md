# F1.1 — A1 completo de ES→EN

**Fecha:** 2026-11-10
**Estado:** borrador generado; pendiente de revisión nativa y del gate de F0.

## 1. Alcance

| Concepto | Valor |
|---|---:|
| Bloques nuevos | 15 |
| Lecciones nuevas | 150 |
| Ejercicios nuevos | 1.050 |
| A1 ES→EN después de F1.1 | 170/170 |
| ES→EN total después de F1.1 | 321/1.020 |

La matriz cubre 15 bloques temáticos:

1. Primeros pasos
2. Mi día
3. Personas
4. Comida
5. Casa y ciudad
6. Salud
7. Escuela y trabajo
8. Compras
9. Viajes
10. Tiempo libre
11. Naturaleza
12. Tecnología
13. Cultura
14. Repaso y proyectos
15. Consolidación A1

Cada bloque tiene 10 lecciones y cada lección 7 ejercicios:
`MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`, `ListenChoose`,
`ListenType` y `SpeakRepeat`.

## 2. Contrato respetado

- `cefrLevel: A1`.
- `vocabFrequencyBand: 1000-2000`.
- 0 focos gramaticales declarados.
- Frases de hasta 8 tokens.
- `feedbackOnError` en el 100 % de los ejercicios.
- IDs nuevos `es-en-a1-uNN-lNN` y `...-eNN`; ids históricos intactos.
- Sin `FreeSpeakingPrompt` ni `AudioOnlyPrompt` en A1.

## 3. Verificación

- `validateContent`: ES→EN **321 lecciones / 2.679 ejercicios**; schema v2 válido.
- `ContentRampTest`: 11 tests, 0 fallos.
- `PackagedContentTest`: 16 tests, 0 fallos, incluido `es_en_a1_completa_f1`.
- Gate `ktlintCheck detekt validateContent test contentScaleReport`: BUILD SUCCESSFUL.
- `contentScaleReport`: **807/4.080** lecciones.
- Smoke: «A1 · Mi día» visible en el camino con «0 de 10 lecciones».

## 4. Pendiente

- Revisión nativa humana de F1.1 (y de F0, que sigue siendo la puerta de F1).
- Ola B: A1 ES→PT (F1.2), ES→FR (F1.3) y ES→JA (F1.4).
- Auditoría y release 3.2.0 (F1.5).
- Sustituir las réplicas de F0 por contenido original en las fases siguientes.

## 5. Nota de calidad

F1.1 usa una fábrica de patrones A1 con vocabulario temático. Es contenido
nuevo, pero **no se considera cerrado** hasta pasar la revisión nativa y el
smoke de todas las unidades. No debe publicarse como release 3.2.0 todavía.
