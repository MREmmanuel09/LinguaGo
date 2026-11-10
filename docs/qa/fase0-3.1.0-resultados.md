# F0.1 — Fábrica de contenido y piloto A1/A2/B1/B2 ES→EN

**Fecha:** 2026-11-09
**Estado:** F0 en curso (40/240 lecciones del piloto)

## Entregado

- ADR-052 aprobado: 170 lecciones por nivel, 1.020 por idioma, 7 fases.
- `contentScaleReport` para medir el hueco por curso y nivel.
- Contrato operativo en `docs/CONTENT_FACTORY.md`.
- Bloques piloto A1, A2, B1 y B2 ES→EN: **40 lecciones / 330 ejercicios**.

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

## Verificación

- `validateContent`: ES→EN **151 lecciones / 1.399 ejercicios**; schema v2 válido.
- `ContentRampTest`: A1/A2 con 7 ejercicios; B1 con 9; B2 con 10, 2 focos y frases dentro de tope.
- `PackagedContentTest`: se conservan ids históricos; el piloto añade 40 lecciones y 330 ejercicios.
- `FreeSpeakingPrompt`: los modelos B1 y B2 pasan el grader sin fallos de gramática ni cobertura.
- `contentScaleReport`: ES→EN 151/1.020; total 457/4.080.
- Smoke visual: el camino muestra «Piloto A1 · Vida diaria», «Piloto A2 · Autonomía» y «Piloto B2 · Argumentación y matices» con 0/10.
- Gate `ktlintCheck detekt validateContent test` en verde.

## Pendiente de F0

- Bloques piloto C1 y C2 de ES→EN (20 lecciones).
- Réplica de los seis bloques en PT, FR y JA (180 lecciones).
- Revisión nativa del bloque piloto.
- Auditoría de cierre de F0 antes de abrir F1/A1 masivo.
