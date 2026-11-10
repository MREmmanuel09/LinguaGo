# Acta de cierre técnico de F0

**Fecha:** 2026-11-10
**Estado:** F0 cerrada técnicamente (240/240 lecciones piloto)
**Decisión:** ADR-053

## 1. Entregado

| Curso | Lecciones totales | Lecciones piloto | Ejercicios totales |
|---|---:|---:|---:|
| ES→EN | 171 | 60 | 1.629 |
| ES→PT | 162 | 60 | 1.566 |
| ES→FR | 162 | 60 | 1.566 |
| ES→JA | 162 | 60 | 1.566 |
| **Total** | **657** | **240** | **6.327** |

- Se conservan todos los ids históricos.
- Los 240 pilotos cubren A1–C2 en los cuatro cursos.
- Los pilotos acumulan **2.240 ejercicios**.

## 2. Verificación

- `validateContent`: válido en los cuatro cursos.
- `ContentRampTest`: 11 tests, 0 fallos.
- `PackagedContentTest`: 15 tests, 0 fallos.
- `FreeSpeakingPrompt`: todos los modelos B1–C2 pasan el grader.
- `contentScaleReport`: 657/4.080 lecciones.
- Gate `ktlintCheck detekt validateContent test contentScaleReport`: BUILD SUCCESSFUL.
- Smoke visual: unidades piloto visibles en ES→EN, ES→PT y ES→FR.

## 3. Auditoría de réplicas

Informe completo: `docs/qa/fase0-auditoria-replicas.md`.

Resumen:

- Sin defectos estructurales bloqueantes.
- **146/150 réplicas** reutilizan ejercicios históricos; las 4 restantes son
  lecciones A2 nuevas.
- 30 modismos B2 revisados y contextualizados.
- 2 títulos franceses corregidos en histórico y réplica.
- Limitación pendiente: tokenización JA por espacios.

## 4. Riesgos aceptados

1. **Réplicas de repaso:** las 146 lecciones replicadas no son contenido nuevo.
   Se aceptan como cierre técnico de F0 y se sustituirán en F1–F6.
2. **Revisión nativa pendiente:** la revisión asistida por IA no sustituye la
   puerta nativa de ADR-052.
3. **Longitud JA:** el tokenizador por espacios no mide bien el japonés; se
   añadirá una métrica por caracteres/morfermas en F1.

## 5. Puerta de entrada a F1

No se abre F1 hasta:

1. Completar la revisión nativa humana de las réplicas conforme a ADR-052
   (100 % de micro-clases, producción oral y audio; 20 % estratificado del
   resto).
2. Registrar el acta de revisión nativa con los cambios aplicados.
3. Añadir la métrica de longitud JA.
4. Confirmar que las réplicas se sustituirán por contenido original en F1–F6.

## 6. Conclusión

F0 queda **cerrada técnicamente**: la pipeline de contenido, los contratos de
schema, la rampa CEFR, los tests y la auditoría están en verde. El cierre
definitivo de calidad queda condicionado a la revisión nativa humana descrita
en ADR-053.
