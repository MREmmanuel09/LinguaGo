---
name: Cambio
about: Propón un cambio (feat/fix/docs/content/chore)
title: ''
labels: ''
assignees: ''
---

## Qué cambia

Breve descripción del cambio y su motivación.

## Módulos afectados

- [ ] `app`
- [ ] `core/*`
- [ ] `feature/*`
- [ ] `content/*`
- [ ] `build-logic`
- [ ] documentación

## Checklist

- [ ] `./gradlew ktlintCheck detekt validateContent test` pasa local
- [ ] `./gradlew assembleRelease` compila (cuando el cambio toca código de la app)
- [ ] `./gradlew :core:core-ui:connectedDebugAndroidTest` pasa (cuando toca UI/design system)
- [ ] Tests unitarios agregados/actualizados si toca lógica
- [ ] Contenido validado contra el schema si toca `content/*` (`validateContent`)
- [ ] Reglas de dependencias respetadas ([ARCHITECTURE.md](../ARCHITECTURE.md))
- [ ] ADR agregado a `docs/DECISIONS.md` si es una decisión arquitectónica
- [ ] `CHANGELOG.md` actualizado si el cambio es visible al usuario
- [ ] Conventional Commits (`feat:`, `fix:`, `docs:`, `content:`, ...)
