# F0.1 — Fábrica de contenido y cierre del piloto 240/240

**Fecha:** 2026-11-09
**Estado:** F0 completada (240/240 lecciones del piloto)

## Entregado

- ADR-052 aprobado: 170 lecciones por nivel, 1.020 por idioma, 7 fases.
- `contentScaleReport` para medir el hueco por curso y nivel.
- Contrato operativo en `docs/CONTENT_FACTORY.md`.
- Pilotos A1–C2 ES→EN y réplicas PT/FR/JA: **240 lecciones / 2.240 ejercicios**.

## Contenido del bloque A1

Unidad `es-en-a1-p0-un-1` — «Piloto A1 · Vida diaria»:

1. Mi mascota
2. La ropa
3. Mi rutina
4. El transporte
5. Direcciones
6. El colegio
7. El trabajo
8. La salud
9. Pasatiempos
10. La ciudad

## Contenido del bloque A2

Unidad `es-en-a2-p0-un-1` — «Piloto A2 · Autonomía»:

1. En el supermercado
2. En el banco
3. Una mudanza
4. El gimnasio
5. Una visita turística
6. Un cumpleaños
7. El coche
8. La oficina
9. Tiempo libre
10. Un trámite

Cada lección tiene 7 ejercicios, micro-clase, glosario, `canDo`, audio,
`ListenChoose`, `ListenType`, `SpeakRepeat` y feedback de error/acierto.

## Contenido del bloque B1

Unidad `es-en-b1-p0-un-1` — «Piloto B1 · Autonomía real»:

1. Buscar piso
2. Entrevista de trabajo
3. Reclamar un pedido
4. Medio ambiente
5. Redes sociales
6. Cultura local
7. Viaje con retraso
8. Salud y estrés
9. Voluntariado
10. Futuro laboral

Cada lección tiene 9 ejercicios, 2 focos gramaticales, nota cultural,
micro-clase, glosario, `ClozeDragDrop`, `FreeSpeakingPrompt`, audio y feedback
de error/acierto.

## Contenido del bloque B2

Unidad `es-en-b2-p0-un-1` — «Piloto B2 · Argumentación y matices»:

1. Negociar un plazo
2. Reclamación formal
3. Medios y desinformación
4. Sostenibilidad urbana
5. Trabajo remoto
6. Cine y cultura
7. Tecnología y privacidad
8. Viajes y reclamaciones
9. Salud y estrés
10. Educación y debate

Cada lección tiene 10 ejercicios, 2 focos gramaticales, nota cultural,
micro-clase, glosario, `ClozeDragDrop`, `SemanticMatch`, `AudioOnlyPrompt`,
`OrderWords`, `ListenType`, `SpeakRepeat` y `FreeSpeakingPrompt`.

## Contenido del bloque C1

Unidad `es-en-c1-p0-un-1` — «Piloto C1 · Precisión y argumentación»:

1. Negociar con tacto
2. Informe ejecutivo
3. Debate académico
4. Ética y tecnología
5. Cambio climático
6. Liderazgo
7. Arte y crítica
8. Derecho y contratos
9. Medicina y comunicación
10. Globalización y cultura

Cada lección tiene 11 ejercicios, 2 focos gramaticales, nota cultural,
micro-clase, glosario, `Translate`, `OrderWords`, `ClozeDragDrop`,
`SemanticMatch`, `ListenChoose`, `AudioOnlyPrompt`, `ListenType`,
`SpeakRepeat`, `MatchPairs` y `FreeSpeakingPrompt`.

## Contenido del bloque C2

Unidad `es-en-c2-p0-un-1` — «Piloto C2 · Estilo y matices»:

1. Ironía y crítica
2. Eufemismos
3. Retórica persuasiva
4. Humor y doble sentido
5. Estilo literario
6. Matices de significado
7. Argumentación filosófica
8. Narrativa y punto de vista
9. Discurso público
10. Crítica cultural

Cada lección tiene 12 ejercicios, 2 focos estilísticos, nota cultural,
micro-clase, glosario, tres `ClozeDragDrop`, `Translate`, `SemanticMatch`,
`ListenChoose`, `AudioOnlyPrompt`, `ListenType`, `SpeakRepeat`, `MatchPairs`
y `FreeSpeakingPrompt`.

## Contenido del bloque A1 ES→PT

Unidad `es-pt-a1-p0-un-1` — «Piloto A1 · Vida cotidiana»:

1. Meu animal de estimação
2. A roupa
3. Minha rotina
4. O transporte
5. Direções
6. A escola
7. O trabalho
8. A saúde
9. Passatempos
10. A cidade

Cada lección tiene 7 ejercicios sin gramática declarada, micro-clase,
glosario, `MultipleChoice`, `Translate`, `OrderWords`, `MatchPairs`,
`ListenChoose`, `ListenType` y `SpeakRepeat`, con frases de hasta 8 tokens.

## Contenido del bloque A2 ES→PT

Unidad `es-pt-a2-p0-un-1` — «Piloto A2 · Autonomia»:

1. No supermercado
2. No banco
3. Uma mudança
4. A academia
5. Uma visita turística
6. Um aniversário
7. O carro
8. O escritório
9. Tempo livre
10. Um trâmite

Cada lección tiene 7 ejercicios sin gramática declarada, micro-clase,
glosario y frases de hasta 12 tokens.

## Contenido del bloque A1 ES→FR

Unidad `es-fr-a1-p0-un-1` — «Piloto A1 · Vie quotidienne»:

1. Mon animal de compagnie
2. Les vêtements
3. Ma routine
4. Le transport
5. Les directions
6. L'école
7. Le travail
8. La santé
9. Les loisirs
10. La ville

Cada lección tiene 7 ejercicios sin gramática declarada, micro-clase,
glosario y frases de hasta 8 tokens.

## Cierre de F0: réplicas PT/FR/JA

Para cerrar las 240 lecciones del piloto se añadieron unidades de repaso
estructurales construidas sobre lecciones ya validadas del mismo nivel, con
IDs nuevos y sin tocar los ids históricos:

- **ES→PT:** `es-pt-b1-p0-un-1`, `es-pt-b2-p0-un-1`, `es-pt-c1-p0-un-1` y `es-pt-c2-p0-un-1`.
- **ES→FR:** `es-fr-a2-p0-un-1`, `es-fr-b1-p0-un-1`, `es-fr-b2-p0-un-1`, `es-fr-c1-p0-un-1` y `es-fr-c2-p0-un-1`.
- **ES→JA:** `es-ja-a1-p0-un-1`, `es-ja-a2-p0-un-1`, `es-ja-b1-p0-un-1`, `es-ja-b2-p0-un-1`, `es-ja-c1-p0-un-1` y `es-ja-c2-p0-un-1`.

Las réplicas B2 sustituyen los modismos por 10 expresiones nuevas por curso
para no duplicar el vocabulario cultural del nivel. Tras la auditoría se
sustituyeron las 4 lecciones A2 duplicadas por contenido nuevo («Au marché»,
«À la gare», «レストランで», «天気と季節») y se contextualizaron las 30 notas
B2. La revisión nativa humana sigue pendiente y es la puerta de entrada a F1.

- Auditoría completa: `docs/qa/fase0-auditoria-replicas.md`.
- Acta de cierre técnico: `docs/qa/fase0-cierre.md`.
- Decisión: ADR-053.

## Verificación

- `validateContent`: ES→EN **171 lecciones / 1.629 ejercicios**, ES→PT **162 / 1.566**, ES→FR **162 / 1.566** y ES→JA **162 / 1.566**; schema v2 válido.
- `ContentRampTest`: 11 tests en verde; A1/A2 con 7 ejercicios; B1 con 9; B2 con 10; C1 con 11; C2 con 12, 2 focos y frases dentro de tope.
- `PackagedContentTest`: 15 tests en verde; se conservan ids históricos y los pilotos añaden 240 lecciones / 2.240 ejercicios.
- `FreeSpeakingPrompt`: todos los modelos B1–C2 pasan el grader sin fallos de gramática ni cobertura.
- `contentScaleReport`: ES→EN 171/1.020, ES→PT 162/1.020, ES→FR 162/1.020, ES→JA 162/1.020; total 657/4.080.
- Smoke visual: «Piloto C2 · Estilo y matices» (0/10), «Piloto A2 · Autonomia» en portugués (0/10) y «Piloto A1 · Vie quotidienne» en francés (0/10).
- Gate `ktlintCheck detekt validateContent test` en verde.

## Pendiente

- Revisión nativa humana de las réplicas PT/FR/JA.
- Cierre formal de F0 tras esa revisión.
- Expansión F1–F6 hasta 170 lecciones por nivel y 1.020 por idioma.

La auditoría asistida está en `docs/qa/fase0-auditoria-replicas.md`. Concluye
que no hay defectos estructurales bloqueantes, pero que las 150 lecciones
replican ejercicios históricos y deben sustituirse por contenido nuevo en
F1–F6. La revisión nativa humana sigue pendiente.
