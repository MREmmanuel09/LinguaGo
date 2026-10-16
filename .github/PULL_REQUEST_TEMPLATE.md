---
name: Cambio
about: Propón un cambio (feat/fix/docs/content)
title: ''
labels: ''
assignees: ''
---

## Qué cambia

Breve descripción del cambio y su motivación.

## Módulos afectados

- [ ] `core/*`
- [ ] `feature/*`
- [ ] `content/*`
- [ ] `build-logic`

## Checklist

- [ ] `./gradlew build` pasa local
- [ ] `./gradlew ktlintCheck detekt` sin issues
- [ ] Tests agregados/actualizados si toca lógica
- [ ] Cumple las reglas de dependencias (`ARCHITECTURE.md`)
- [ ] ADR creado en `docs/DECISIONS.md` si toco arquitectura
- [ ] Conventional Commits (`feat:`, `fix:`, `docs:`, ...)
