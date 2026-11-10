# F0 — Auditoría de las réplicas PT/FR/JA

**Fecha:** 2026-11-10
**Alcance:** 15 unidades piloto, 150 lecciones y 1.470 ejercicios de ES→PT, ES→FR y ES→JA.
**Método:** auditor automático (`tools/content/audit_f0_replicas.py`) + revisión lingüística asistida de títulos, modismos y metadatos. **No sustituye a una revisión nativa humana.**

## Resultado ejecutivo

La auditoría **no encuentra defectos estructurales bloqueantes**: todas las unidades están al final de su nivel, los IDs son válidos y únicos, los recuentos de ejercicios y focos gramaticales coinciden con el nivel, no falta `feedbackOnError` y los topes de frase se respetan con el tokenizador actual.

La reserva principal es pedagógica: **146 de las 150 lecciones replican ejercicios de lecciones históricas**. Las 4 restantes son lecciones A2 nuevas que sustituyen a las duplicadas. Las réplicas son unidades de repaso válidas, pero no contenido nuevo; deben sustituirse por contenido original en F1–F6.

## Cobertura auditada

| Curso | Unidades | Lecciones | Ejercicios | Lecciones con ejercicios duplicados |
|---|---:|---:|---:|---:|
| ES→PT | B1, B2, C1, C2 | 40 | 420 | 40/40 |
| ES→FR | A2, B1, B2, C1, C2 | 50 | 490 | 48/50 |
| ES→JA | A1, A2, B1, B2, C1, C2 | 60 | 560 | 58/60 |
| **Total** | **15** | **150** | **1.470** | **146/150** |

## Comprobaciones estructurales

- **Posición:** las 15 unidades son la última unidad de su nivel.
- **IDs:** lecciones `es-<curso>-<nivel>-p0-u1-lNN` y ejercicios `...-eNN`; sin colisiones.
- **Recuentos:** A1/A2 7 ejercicios; B1 9; B2 10; C1 11; C2 12.
- **Gramática:** 0 focos en A1/A2; 2 focos en B1–C2.
- **Feedback:** 0 ejercicios sin `feedbackOnError`.
- **Longitud:** ninguna frase supera el tope del nivel con el tokenizador del test (`longestSentenceTokens`).
- **Modismos B2:** 10 por curso, distintos entre sí y sin solapamiento con los 22 históricos de cada curso.
- **FreeSpeaking:** los modelos B1–C2 pasan el grader sin `MISSING_GRAMMAR` ni `LOW_COVERAGE`.
- **Ids históricos:** intactos; los tests de conservación y recuento pasan.

## Revisión lingüística asistida

### Modismos B2 nuevos

Se revisaron los 30 modismos introducidos en las réplicas B2:

- **PT:** dar um jeito, ficar de olho, quebrar o galho, pôr a mão na massa, dar com os burros n'água, estar com a corda no pescoço, fazer vista grossa, custar os olhos da cara, acertar na mosca, a cereja do bolo.
- **FR:** tirer les ficelles, mettre la main à la pâte, coûter les yeux de la tête, poser un lapin, tirer son épingle du jeu, avoir le bras long, faire la sourde oreille, casser les pieds, mettre les points sur les i, jeter l'éponge.
- **JA:** 猫の手も借りたい, 顔が広い, 耳が痛い, 口が堅い, 目を通す, 手を貸す, 足を運ぶ, 胸を張る, 水に流す, 油を売る.

**Veredicto:** las formas y las traducciones son correctas. No se detectaron calcos ni errores de registro graves.

### Correcciones aplicadas

- FR B2: «Matiser une critique» → **«Nuancer une critique»**.
- FR B2: «Rebattre avec des preuves» → **«Réfuter avec des preuves»**.
- **4 lecciones A2 nuevas:** «Au marché» y «À la gare» en FR; «レストランで» y «天気と季節» en JA. Sustituyen a las 4 lecciones duplicadas.
- **30 notas B2 contextualizadas:** cada modismo se presenta ahora dentro del tema de su lección.

Las correcciones de títulos se aplicaron tanto a la lección histórica como a la réplica.

### Observaciones lingüísticas

- **Modismos B2 contextualizados (resuelto):** los 30 modismos son correctos y sus notas ya conectan con el tema de la lección.
- **Lecciones A2 nuevas (resuelto):** FR A2 y JA A2 ya no contienen lecciones duplicadas dentro de la unidad.
- **Tokenización JA (media):** el A2 japonés no separa palabras con espacios; el tope de tokens mide 1 y el test de longitud queda neutralizado. Es una limitación preexistente del método, no de esta tanda. Recomendación: añadir una métrica por caracteres/morfermas para JA.

## Hallazgos

| Severidad | Hallazgo | Impacto | Recomendación |
|---|---|---|---|
| Alta | 146/150 lecciones replican ejercicios históricos | No aportan contenido nuevo; sirven para cerrar el piloto y validar la pipeline | Sustituir por contenido original en F1–F6 |
| Media | Tokenización JA sin espacios | El tope de longitud no protege el nivel | Métrica por caracteres/morfermas |
| Resuelto | 4 lecciones A2 duplicadas | Repetición dentro de la unidad | Sustituidas por contenido nuevo |
| Resuelto | 30 modismos B2 sin contexto temático | Nota cultural poco integrada | Notas contextualizadas |
| Resuelto | 2 títulos franceses incorrectos | Calidad lingüística | Corregidos en histórico y réplica |

## Limitaciones de la revisión

- Es una **revisión asistida por IA**, no una revisión nativa humana. No cubre juicios finos de naturalidad, registro o variedad dialectal.
- El contenido replicado hereda la calidad del material histórico; no se ha reeditado frase a frase.
- Los 30 modismos B2 se revisaron y contextualizaron, pero su validación final requiere criterio humano.

## Recomendaciones

1. Mantener las réplicas como **bloques de repaso** de F0, no como contenido definitivo.
2. Hacer una **revisión nativa humana** de los 30 modismos B2 y de las 4 lecciones A2 nuevas.
3. Incorporar una métrica de longitud por caracteres/morfermas para japonés.
4. Sustituir las réplicas por lecciones nuevas durante F1–F6.
5. Repetir esta auditoría al cerrar cada fase.

## Evidencia

- `python tools/content/audit_f0_replicas.py` → informe JSON en `build/reports/f0-replicas-audit.json`.
- `./gradlew validateContent` → válido en los cuatro cursos.
- `./gradlew :core:core-content:testDebugUnitTest --rerun-tasks` → 15 tests de contenido y 11 de rampa en verde.
- `./gradlew ktlintCheck detekt validateContent test contentScaleReport` → BUILD SUCCESSFUL; total 657/4.080.
