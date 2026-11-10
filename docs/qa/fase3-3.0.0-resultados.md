# Fase 3 — Persistencia, respaldo y sincronización

**Fecha:** 2026-11-09
**Dispositivo:** `Medium_Phone_API_37.0` (emulador x86_64, API 37)
**Variantes:** `full` debug y release universal

## Migración Room v4 → v5

- Instalación nueva sobre una base v4 real: se conservaron el curso activo
  (ES→FR), la racha de 1 día, el XP y las 10 vidas.
- `Migration4To5Test` pasa sobre una base v4 poblada: racha, lecciones,
  tarjetas FSRS y vidas intactas; columnas nuevas con defaults; semilla de
  `daily_activity` con el último día de racha.
- `ProgressRepositoryV5Test` (5 tests instrumentados) verifica métricas de voz,
  actividad diaria, snapshot v3 y restauración monótona.

## Respaldo local por SAF

| Paso | Resultado |
|---|---|
| Exportar | `linguago-backup-2026-11-09.json` (2,1 KB en la primera prueba; 3,0 KB tras una lección nueva). |
| Contenido | `schemaVersion: 3`, racha, XP, vidas, lecciones con precisión y 9 tarjetas FSRS. |
| Importar | «Copia local importada y fusionada»; el progreso se mantiene. |
| Archivo inválido | `{not-json}` → «El archivo no es un respaldo válido de LinguaGo». |
| Release con R8 | Exportación posterior a `assembleFullRelease` verificada con el mismo JSON v3. |

## Respaldo cloud de laboratorio

- Vinculación instantánea de `dev_local` en debug.
- Archivo generado: `files/dev-cloud-snapshot.json` de 2.135 bytes para un
  JSON plano de 2.107 bytes (IV + tag GCM), confirmando cifrado de transporte.
- «Restaurar desde la nube» devuelve «Progreso restaurado y fusionado» sin
  pérdida de datos.

## Perfil con métricas reales

- Lección completada con 3/7 aciertos → la tarjeta Precisión media muestra
  `50 %`.
- Calendario de 28 días con «1 día con práctica» tras la actividad del día.
- Tarjetas de fluidez oral y tasa de superación muestran su estado vacío
  cuando aún no hay intentos; la escritura y agregación están cubiertas por
  `ProgressRepositoryV5Test` y `ProfileStatsTest`.
- Corrección de concordancia: «1 día con práctica» (antes «1 días»).

## Accesibilidad y tema

- `SyncScreen` y `Respaldo y datos` con `LinguaTopBar` y botón Volver.
- Modo oscuro correcto en perfil, calendario y respaldo.
- `fontScale 2.0x`: títulos, botones y tarjetas se expanden sin solaparse; la
  barra inferior pasa a iconos.
- Rotación a horizontal sin crash y conservando la pantalla.

## Tests y build

- `ktlintCheck detekt validateContent test` en verde.
- `:core:core-data:connectedDebugAndroidTest`: 9 tests, 0 fallos
  (`Migration2To3Test`, `Migration3To4Test`, `Migration4To5Test` y
  `ProgressRepositoryV5Test`).
- `:app:assembleFullRelease` en verde con R8 y smoke de exportación.

## Observaciones para F4

- La validación visual de métricas orales con micrófono real queda sujeta a la
  captura del emulador; la persistencia y el agregado están automatizados.
- El botón de Google Drive aparece deshabilitado cuando no hay `googleClientId`;
  el laboratorio local permite probar el flujo cloud en debug.
