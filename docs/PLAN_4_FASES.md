# Plan de 4 fases — cierre 2.5.0 y 3.0.0

> Plan operativo derivado de `docs/PLAN_PROFESIONALIZACION_APP.md`.
> Cada fase es acumulativa y termina con el gate Gradle en verde.

## F1 — Cierre y verificación de `v2.5.0`

**Alcance**
1. Validación funcional y visual en emulador (`Medium_Phone_API_37.0`):
   onboarding, camino, lección, repaso, progreso, ajustes, sync/auth,
   rotación, `fontScale = 2.0x`, claro/oscuro y TTS/STT en EN/PT/FR/JA.
2. Regenerar `baseline-prof.txt` con `:app:generateEnBaselineProfile`.
3. Macrobenchmark y smoke test de arranque, scroll, rotación y respaldo.
4. Empaquetar `full/en/pt/fr/ja` release, medir tamaños y generar
   `SHA256SUMS.txt`; preparar tag y nota `v2.5.0`.

**Criterio de salida**
- `ktlintCheck detekt validateContent test` y compilación instrumentada verdes.
- Evaluación en emulador documentada.
- Baseline Profile actualizado.
- Artefactos y hashes generados.

## F2 — Caminos, navegación y economía FSRS (`v3.0.0`)

**Estado:** completada y validada en emulador (2026-11-09).

**Alcance**
1. Barra inferior de 4 destinos (`Inicio · Repaso · Progreso · Ajustes`) con
   badge de tarjetas vencidas.
2. `CourseSwitcherSheet` en Home y Ajustes, cambio de curso en 2 toques.
3. Títulos bajo los nodos del camino y botón flotante «Ir a mi lección».
4. Cofres de unidad con `+20 XP` única vez y resumen de unidad.
5. Repaso FSRS: `+1 ❤` cada 5 tarjetas superadas, audio 1x/0.75x y
   `culturalNote` en el reverso.

**Criterio de salida**
- Navegación y cambio de curso sin perder la pila.
- Bono de cofre no duplicable (test de repositorio/DAOs).
- Recuperación de vidas cubierta por test de `ReviewViewModel`.

## F3 — Persistencia, respaldo y sincronización (`v3.0.0`)

**Estado:** completada y validada en emulador (2026-11-09).

**Alcance**
1. Room `v4 → v5`: métricas de voz, precisión, `culturalNote` y
   `daily_activity`, con `MIGRATION_4_5` y `Migration4To5Test`.
2. `SyncSnapshot v3` con `words` y merge monótono por tarjeta.
3. Exportación/importación local `.json` vía SAF sin cuenta.
4. Unificación de `SyncScreen` + `AuthScreen` con `LinguaTopBar`.
5. Conexión de métricas reales de fluidez oral en perfil.

**Criterio de salida**
- Migración instrumentada sobre base v4 poblada sin pérdida (I4).
- Backup/restore restaura lecciones, racha, vidas y tarjetas FSRS.
- `SnapshotMergeTest` y `SyncEngineTest` verdes.

## F4 — Voz, assets y perfil (`v3.0.0` / `v3.1`)

**Estado:** completada la ingeniería (2026-11-09); revisión editorial humana
de B1–C2 diferida a `v3.1`.

**Alcance**
1. Deduplicar `espeak-ng-data` en un árbol compartido de `models/tts`.
2. Gestor de almacenamiento de voces en Ajustes (`clearExtractedCache`).
3. Perfil con desglose CEFR A1–C2, precisión media y calendario de 28 días.
4. Revisión editorial nativa B1–C2 en commits `content:` separados.

**Criterio de salida**
- APK `full` arm64 ≤ 340 MB y verificación de integridad de modelos.
- Perfil renderiza las 6 etapas en claro/oscuro y `2.0x`.
- Cero regresiones de contenido y modelos.

**Resultado (2026-11-09):** las 6 etapas CEFR y el gestor de voces están
validados; `full` arm64 quedó en `340,5 MB` (0,5 MB sobre el objetivo
orientativo) conservando las tres voces Piper y los cuatro modelos Vosk. La
revisión editorial humana de B1–C2 queda para `v3.1`.
