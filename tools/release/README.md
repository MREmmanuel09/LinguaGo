# Publicación del release estable

Estas herramientas cierran F1 y publican el release estable **3.2.0** cuando
la revisión nativa y la firma estén listas.

## Requisitos

1. **Revisión nativa completa**
   - `build/reports/native-review-plan.csv`: 840 lecciones con
     `review_status=approved`, `reviewer` informado y sin incidencias
     `blocker`/`major`.
   - `build/reports/native-review-b2-idioms.csv`: 40 modismos aprobados.
   - Guía: `docs/qa/fase1.5-revision-nativa.md`.

2. **Keystore de distribución** en `local.properties`:
   ```properties
   linguago.releaseStoreFile=C:\\ruta\\linguago-release.jks
   linguago.releaseStorePassword=...
   linguago.releaseKeyAlias=...
   linguago.releaseKeyPassword=...
   ```
   Sin keystore, el APK release se firma con la clave debug y el script aborta.

3. **GitHub CLI autenticado**
   ```powershell
   gh auth login
   ```

4. **Árbol de trabajo limpio** (sin cambios tracked; los directorios locales no
   versionados no bloquean).

## Comprobación previa

```powershell
python tools/content/prepare_native_review.py
python tools/release/check_native_review.py
```

El segundo comando debe terminar con:

```text
Revisión nativa completa: release desbloqueado.
```

## Registrar la revisión por lotes

Después de revisar un curso/nivel, marca el lote:

```powershell
python tools/release/mark_reviewed.py --course es-en --level A1 `
  --status approved --reviewer "Tu nombre" --include-idioms
```

Para una lección concreta:

```powershell
python tools/release/mark_reviewed.py --lesson-id es-en-a1-p0-u1-l01 `
  --status approved --reviewer "Tu nombre"
```

Si encuentras un problema:

```powershell
python tools/release/mark_reviewed.py --lesson-id es-en-a1-p0-u1-l01 `
  --status needs_change --reviewer "Tu nombre" --notes "Corregir '...'"
```

El release queda bloqueado mientras haya filas `pending` o `needs_change`.

## Revisión nativa de F2 (3.3.0)

F2 añade 599 lecciones A2. El kit y el validador son independientes del de F1:

```powershell
python tools/content/prepare_native_review_f2.py
python tools/release/mark_reviewed_f2.py --course es-en --unit 01 `
  --status approved --reviewer "Tu nombre"
python tools/release/check_native_review_f2.py
```

El CSV de F2 vive en `build/reports/native-review-f2-plan.csv` y cubre
ES→EN (143), ES→PT (152), ES→FR (152) y ES→JA (152). El release 3.3.0 queda
bloqueado mientras queden filas `pending` o `needs_change`.

## Keystore de distribución

Si no tienes keystore, créalo una sola vez:

```powershell
& "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -v `
  -keystore "D:\Proyectos\LinguaGo\linguago-release.jks" `
  -alias linguago -keyalg RSA -keysize 2048 -validity 10000
```

Añade a `local.properties` (no se commitea):

```properties
linguago.storeFile=D:\\Proyectos\\LinguaGo\\linguago-release.jks
linguago.storePassword=...
linguago.keyAlias=linguago
linguago.keyPassword=...
```

## Publicación

```powershell
powershell -ExecutionPolicy Bypass -File tools/release/publish_3_2_0.ps1
```

El script:

1. Comprueba que el árbol está limpio y `gh` autenticado.
2. Bloquea el release si la revisión nativa no está aprobada.
3. Actualiza `versionCode` y `versionName` (`3.2.0`, `16`).
4. Ejecuta el gate completo.
5. Construye `:app:assembleFullRelease`.
6. Verifica que la firma **no** sea `CN=Android Debug`.
7. Copia los APK a `dist/v3.2.0/` y genera `SHA256SUMS.txt` y `RELEASE.md`.
8. Commitea la versión, crea el tag `v3.2.0` y publica la release en GitHub.

## Dry run

Para validar sin commitear, taggear ni publicar:

```powershell
powershell -ExecutionPolicy Bypass -File tools/release/publish_3_2_0.ps1 -DryRun
```

## Rollback

Si falla después del tag:

1. Borra el tag local/remoto:
   ```powershell
   git tag -d v3.2.0
   git push origin :refs/tags/v3.2.0
   ```
2. Revierte el commit de release:
   ```powershell
   git revert <commit>
   ```
3. Corrige y repite.
