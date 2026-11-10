# F0.1 — Fábrica de contenido y piloto A1 ES→EN

**Fecha:** 2026-11-09
**Estado:** F0 en curso (10/240 lecciones del piloto)

## Entregado

- ADR-052 aprobado: 170 lecciones por nivel, 1.020 por idioma, 7 fases.
- `contentScaleReport` para medir el hueco por curso y nivel.
- Contrato operativo en `docs/CONTENT_FACTORY.md`.
- Primer bloque piloto A1 ES→EN: **10 lecciones / 70 ejercicios**.

## Contenido del bloque

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

Cada lección tiene 7 ejercicios, micro-clase, glosario, `canDo`, audio,
`ListenChoose`, `ListenType`, `SpeakRepeat` y feedback de error/acierto.

## Verificación

- `validateContent`: ES→EN **121 lecciones / 1.139 ejercicios**; schema v2 válido.
- `ContentRampTest`: A1 con 7 ejercicios, sin gramática declarada y frases ≤8 tokens.
- `PackagedContentTest`: se conservan ids históricos; el piloto añade 10 lecciones y 70 ejercicios.
- `contentScaleReport`: ES→EN 121/1.020; total 427/4.080.
- Smoke visual: el camino muestra «Piloto A1 · Vida diaria» con «0 de 10 lecciones».
- Gate `ktlintCheck detekt validateContent test` en verde.

## Pendiente de F0

- Bloques piloto A2, B1, B2, C1 y C2 de ES→EN (50 lecciones).
- Réplica de los seis bloques en PT, FR y JA (180 lecciones).
- Revisión nativa del bloque piloto.
- Auditoría de cierre de F0 antes de abrir F1/A1 masivo.
