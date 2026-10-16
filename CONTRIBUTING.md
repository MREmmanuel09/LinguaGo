# Contribuir a LinguaGo

Gracias por tu interés. LinguaGo es un proyecto de código abierto Offline-first:
toda decisión debe alinearse con los invariantes del `README.md`.

## Cómo reportar errores

1. Busca en the [issues](https://github.com/MREmmanuel09/LinguaGo/issues) si ya existe.
2. Si no, crea uno nuevo con:
   - Título claro.
   - Pasos para reproducir.
   - Comportamiento esperado vs actual.
   - Versión de la app, dispositivo y versión de Android.

## Cómo proponer cambios

1. **Fork**, luego **rama** con prefijo según el cambio:
   - `feat/<nombre>` — funcionalidad nueva
   - `fix/<nombre>` — corrección de bug
   - `docs/<nombre>` — documentación
   - `refactor/<nombre>` — refactor
   - `chore/<nombre>` — tareas de mantenimiento
   - `content/<nombre>` — cambios de contenido pedagógico
2. **Conventional Commits** obligatorio:
   - `feat: agrega ejercicio de ordenar palabras`
   - `fix: corrige merge de racha en sync`
   - `refactor(comments): simplifica fsrs kernel`
3. **Antes de abrir el PR, asegúrate:**
   - `./gradlew build` pasa local
   - `./gradlew ktlintCheck detekt` sin issues
   - Agrega o actualiza tests si tocas lógica
   - Si tu decisión es arquitectónica, añade un ADR a `docs/DECISIONS.md`

## Estructura de commits y módulos

- nunca mezclar** cambios de código con contenido pedagógico en el mismo commit.
- Las reglas de dependencias entre módulos están en `ARCHITECTURE.md`:
  - `feature/*` solo depende de `core/*`
  - `core/*` nunca depende de `feature/*`
  - `content/*` solo contiene assets
- Violaciones del sistema de dependencia serán rechazadas en revisión.

## Proponer contenido pedagógico

1. Lee `CONTENT_FORMAT.md` (a definir en la Fase 3).
2. Los contenidos viven en `content/*` como assets JSON puros.
3. Comunicaciones, historia o explicaciones deben ser 100% originales —
   compares ninguna copia de material de terceros.

## Decisiones

Cada cambio que toca la arquitectura, el modelo de datos, o viola/modifica un
invariante necesita un ADR (Architecture Decision Record) en `docs/DECISIONS.md`
con el formato:

```
### ADR-XXX: Título corto
Estado: Aprobado / Reemplazado por ADR-YYY / Rechazado
Contexto: ...
Decisión: ...
Consecuencias: ...
```

## Código de conducta

Sé gentil, se cívico y usa buen juicio. Los moderadores reservan el derecho de
cuestionar contribuciones que dañen el espíritu del proyecto.
