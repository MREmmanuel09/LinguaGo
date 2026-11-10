# F0.1 — Fábrica de contenido y piloto A1/A2/B1/B2/C1 ES→EN

**Fecha:** 2026-11-09
**Estado:** F0 en curso (50/240 lecciones del piloto)

## Entregado

- ADR-052 aprobado: 170 lecciones por nivel, 1.020 por idioma, 7 fases.
- `contentScaleReport` para medir el hueco por curso y nivel.
- Contrato operativo en `docs/CONTENT_FACTORY.md`.
- Bloques piloto A1, A2, B1, B2 y C1 ES→EN: **50 lecciones / 440 ejercicios**.

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

## Verificación

- `validateContent`: ES→EN **161 lecciones / 1.509 ejercicios**; schema v2 válido.
- `ContentRampTest`: A1/A2 con 7 ejercicios; B1 con 9; B2 con 10; C1 con 11, 2 focos y frases dentro de tope.
- `PackagedContentTest`: se conservan ids históricos; el piloto añade 50 lecciones y 440 ejercicios.
- `FreeSpeakingPrompt`: los modelos B1, B2 y C1 pasan el grader sin fallos de gramática ni cobertura.
- `contentScaleReport`: ES→EN 161/1.020; total 467/4.080.
- Smoke visual: el camino incluye las unidades «Piloto A1 · Vida diaria», «Piloto A2 · Autonomía», «Piloto B2 · Argumentación y matices» y «Piloto C1 · Precisión y argumentación».
- Gate `ktlintCheck detekt validateContent test` en verde.

## Pendiente de F0

- Bloque piloto C2 de ES→EN (10 lecciones).
- Réplica de los seis bloques en PT, FR y JA (180 lecciones).
- Revisión nativa del bloque piloto.
- Auditoría de cierre de F0 antes de abrir F1/A1 masivo.
