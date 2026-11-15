# F1.5 — Cierre del release estable 3.2.0

**Fecha:** 2026-11-15
**Estado:** bloqueado por revisión nativa y firma de distribución.
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

## 2. Bloqueos actuales

| Bloqueo | Estado | Cómo se resuelve |
|---|---|---|
| Revisión nativa F0/F1 | 0/840 lecciones, 0/40 modismos | Completar los CSV y firmar el acta |
| Keystore de distribución | No configurado | Añadir las 4 propiedades a `local.properties` |
| GitHub CLI | Sin autenticar | `gh auth login` |
| Árbol de trabajo | Limpio | Mantenerlo limpio antes del release |

## 3. Criterios de aceptación

1. `python tools/release/check_native_review.py` imprime
   «Revisión nativa completa: release desbloqueado.».
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
# rellenar los CSV con review_status=approved
python tools/release/check_native_review.py
powershell -ExecutionPolicy Bypass -File tools/release/publish_3_2_0.ps1
```

El script hace el bump a `3.2.0` (`versionCode 16`), el gate, el build release,
la verificación de firma, el commit, el tag y la publicación en GitHub.

## 5. Documentos relacionados

- `docs/qa/fase1.5-revision-nativa.md`
- `docs/qa/fase1.5-auditoria-f1.md`
- `docs/qa/fase1.5-release-candidate.md`
- `tools/release/README.md`

## 6. Decisión

El release **3.2.0 no se publica** hasta que los cuatro bloqueos estén
resueltos. El RC1 queda como artefacto de validación técnica y no debe
distribuirse.
