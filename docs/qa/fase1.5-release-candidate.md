# Release candidate 3.2.0-rc1

**Fecha:** 2026-11-15
**Versión:** `3.2.0-rc1` (`versionCode 15`)
**Estado:** build técnico correcto; **no distribuible**.

## 1. Contenido incluido

- F0 cerrada técnicamente: 240 lecciones piloto.
- F1 completa: 600 lecciones A1 nuevas (150 por curso).
- A1 170/170 en ES→EN, ES→PT, ES→FR y ES→JA.
- Total del programa: **1.257/4.080 lecciones** y **10.527 ejercicios**.

## 2. Build

- `:app:assembleFullRelease` → **BUILD SUCCESSFUL** (R8).
- `:app:assembleFullDebug` → **BUILD SUCCESSFUL**.
- Gate `ktlintCheck detekt validateContent test contentScaleReport` → verde.

## 3. Artefactos

| APK | Tamaño | SHA-256 |
|---|---:|---|
| `app-full-universal-release.apk` | 398.119.014 B | `2DB784AF5DB7F80463D36BABE5E52B44F64D158DCB0AC6D5AEF5E36D6DB39262` |
| `app-full-arm64-v8a-release.apk` | 357.549.794 B | `1DAA8DE40CF4CDE6861934ABF55284D4BD897342593BD0614D568DB7BDF20E3E` |

Ruta: `app/build/outputs/apk/full/release/`.

## 4. Bloqueos de publicación

1. **Firma:** el APK release está firmado con el certificado **Android Debug**
   (`CN=Android Debug`) porque no hay keystore de distribución configurado.
   **No debe distribuirse.**
2. **Revisión nativa:** ADR-053 exige completar la revisión nativa de F0/F1
   antes del release estable.
3. **GitHub:** `gh` no está autenticado en este entorno; no se puede publicar
   la release ni subir el APK.

## 5. Para publicar el estable 3.2.0

1. Completar la revisión nativa humana de F0/F1 y registrar el acta.
2. Configurar `releaseStoreFile`, `releaseStorePassword`, `releaseKeyAlias` y
   `releaseKeyPassword` en `local.properties` o variables de entorno.
3. Reconstruir `:app:assembleFullRelease` y verificar la firma con
   `apksigner verify --print-certs`.
4. Autenticar `gh` y publicar la release con el APK firmado.
5. Etiquetar `v3.2.0` y actualizar el CHANGELOG.

## 6. Conclusión

El contenido y el build de F1 están listos. El release **3.2.0-rc1** es un
artefacto de validación técnica; el estable 3.2.0 queda bloqueado por la
revisión nativa y la firma de distribución.
