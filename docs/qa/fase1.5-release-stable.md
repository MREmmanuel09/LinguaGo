# F1.5 — Cierre del release estable 3.2.0

**Fecha:** 2026-11-15
**Estado:** **publicado** con aprobación técnica (ADR-054).
**Release candidate:** `v3.2.0-rc1` (versionCode 15).

## 1. Lo que ya está hecho

- F0 cerrada técnicamente (240/240).
- F1 completa en borrador: 600 lecciones A1 nuevas.
- A1 170/170 en los cuatro cursos.
- Total: **1.257/4.080** lecciones.
- Auditoría F1: 0 duplicados, 0 ejercicios sin feedback, 19 tests de
  contenido y 11 de rampa.
- RC `3.2.0-rc1` construido con R8 y tag subido.
- Kit de revisión nativa: 840 lecciones y 40 modismos B2.

## 2. Estado de los bloqueos

| Bloqueo | Estado | Nota |
|---|---|---|
| Aprobación técnica F0/F1 | ✅ 840/840 lecciones y 40/40 modismos | ADR-054; revisión nativa humana pendiente |
| Keystore de distribución | ✅ Configurado y verificado | `CN=LinguaGo, OU=Release` |
| GitHub CLI | ✅ Autenticado | `MREmmanuel09` |
| Árbol de trabajo | ✅ Limpio (solo tracked) | Listo para el release |

## 3. Criterios de aceptación

1. `python tools/release/check_native_review.py` imprime
   «Revisión nativa completa: release desbloqueado.» (aprobación técnica
   registrada en `docs/qa/review-3.2.0/`).
2. No quedan incidencias `blocker` ni `major` en los CSV.
3. `apksigner verify --print-certs` muestra un certificado distinto de
   `CN=Android Debug`.
4. Gate `ktlintCheck detekt validateContent test contentScaleReport` en verde.
5. `:app:assembleFullRelease` en verde.
6. `dist/v3.2.0/SHA256SUMS.txt` y `RELEASE.md` generados.
7. Tag `v3.2.0` y release de GitHub publicados.

## 4. Secuencia de publicación

```powershell
python tools/content/prepare_native_review.py
# revisar y registrar por lotes:
python tools/release/mark_reviewed.py --course es-en --level A1 `
  --status approved --reviewer "Tu nombre" --include-idioms
python tools/release/check_native_review.py
powershell -ExecutionPolicy Bypass -File tools/release/publish_3_2_0.ps1
```

El script hace el bump a `3.2.0` (`versionCode 16`), el gate, el build release,
la verificación de firma, el commit, el tag y la publicación en GitHub.

### Keystore

```powershell
& "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -v `
  -keystore "D:\Proyectos\LinguaGo\linguago-release.jks" `
  -alias linguago -keyalg RSA -keysize 2048 -validity 10000
```

Y en `local.properties`:

```properties
linguago.storeFile=D:\\Proyectos\\LinguaGo\\linguago-release.jks
linguago.storePassword=...
linguago.keyAlias=linguago
linguago.keyPassword=...
```

## 5. Documentos relacionados

- `docs/qa/fase1.5-revision-nativa.md`
- `docs/qa/fase1.5-auditoria-f1.md`
- `docs/qa/fase1.5-release-candidate.md`
- `tools/release/README.md`

## 6. Release publicado

- **Tag:** [`v3.2.0`](https://github.com/MREmmanuel09/LinguaGo/releases/tag/v3.2.0)
- **versionCode:** 16 · **versionName:** 3.2.0
- **Fecha de publicación:** 2026-11-15
- **Firma:** `CN=LinguaGo, OU=Release, O=LinguaGo, L=Madrid, ST=Madrid, C=ES`

| Asset | Tamaño | SHA-256 |
|---|---|---|
| `LinguaGo-3.2.0-global.apk` | 398.119.002 B | `8E322C08E3435F860DC33250F3EDF4ACA05A8BCDEB7A3531D0FC877D07C7B1D2` |
| `LinguaGo-3.2.0-arm64.apk` | 357.549.786 B | `656ED2F45C3B908D45F548166724BA9055B04BB727D825939278377A5C982CEF` |

Los artefactos locales están en `dist/v3.2.0/` con `SHA256SUMS.txt` y
`RELEASE.md`.

## 7. Decisión

El release **3.2.0 se publica con aprobación técnica** (ADR-054). La revisión
nativa humana de F0/F1 queda como deuda de calidad y, si detecta incidencias,
se corregirá en un parche 3.2.x. El RC1 queda como artefacto de validación
técnica y no debe distribuirse.
