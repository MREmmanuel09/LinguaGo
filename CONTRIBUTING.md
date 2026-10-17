# Contribuir a LinguaGo

Gracias por tu interés. LinguaGo es un proyecto de código abierto offline-first:
toda decisión debe alinearse con los invariantes del [README](README.md) y con
la [arquitectura](ARCHITECTURE.md).

## Cómo reportar errores

1. Busca en los [issues](https://github.com/MREmmanuel09/LinguaGo/issues) si ya existe.
2. Si no, usa la plantilla de reporte (título claro, pasos, esperado vs actual,
   versión de la app, dispositivo/API).
3. Para **vulnerabilidades de seguridad**, no abras un issue: sigue
   [SECURITY.md](SECURITY.md).

## Cómo proponer cambios

1. **Fork**, luego **rama** con prefijo según el cambio:
   - `feat/<nombre>` — funcionalidad nueva
   - `fix/<nombre>` — corrección de bug
   - `docs/<nombre>` — documentación
   - `refactor/<nombre>` — refactor
   - `chore/<nombre>` — tareas de mantenimiento
   - `content/<nombre>` — contenido pedagógico
2. **Conventional Commits** obligatorio (en español):
   - `feat: agrega ejercicio de ordenar palabras`
   - `fix: corrige merge de racha en sync`
   - `content: amplía lecciones de «La familia»`
3. **Antes de abrir el PR, asegúrate:**
   - `./gradlew ktlintCheck detekt validateContent test` pasa local
   - `./gradlew :core:core-ui:connectedDebugAndroidTest` pasa si tocas UI
   - Agrega o actualiza tests si tocas lógica
   - Si la decisión es arquitectónica, añade un ADR a
     [docs/DECISIONS.md](docs/DECISIONS.md)

## Reglas de commits y módulos

- Nunca mezcles cambios de código con contenido pedagógico en el mismo commit.
- Las reglas de dependencias entre módulos están en
  [ARCHITECTURE.md](ARCHITECTURE.md):
  - `feature/*` solo depende de `core/*`
  - `core/*` nunca depende de `feature/*`
  - `content/*` solo contiene assets
- Las violaciones del sistema de dependencias serán rechazadas en revisión.

## Proponer contenido pedagógico

1. Lee [CONTENT_FORMAT.md](CONTENT_FORMAT.md) (contrato v1 del contenido).
2. El contenido vive en `content/*` como assets JSON puros (UTF-8 **sin BOM**).
3. Valida antes del PR con `./gradlew validateContent`.
4. Frases, historia y explicaciones deben ser 100 % originales — nada de
   material de terceros (ver [LICENSE-CONTENT](LICENSE-CONTENT)).

## Decisiones

Cada cambio que toca la arquitectura, el modelo de datos o los invariantes
necesita un ADR en [docs/DECISIONS.md](docs/DECISIONS.md) con el formato de
ADR-000:

```text
## ADR-XXX: Título corto
**Estado:** Aprobado / Reemplazado por ADR-YYY / Diferido / Rechazado
**Fecha:** YYYY-MM-DD
**Contexto:** ...
**Decisión:** ...
**Consecuencias:** ...
```

## Código de conducta

Este proyecto adopta el [Código de Conducta](CODE_OF_CONDUCT.md)
(Contributor Covenant 2.1). Al participar, se espera que lo respetes.
