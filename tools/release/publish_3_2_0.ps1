param(
    [string]$Version = "3.2.0",
    [string]$VersionCode = "16",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $Root
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Android\Android Studio\jbr" }

Write-Host "== LinguaGo $Version =="

$status = git status --porcelain --untracked-files=no
if ($status) {
    throw "El árbol de trabajo no está limpio. Haz commit o stash antes de publicar."
}

gh auth status *> $null
if ($LASTEXITCODE -ne 0) {
    throw "gh no está autenticado. Ejecuta 'gh auth login' antes de publicar."
}

python tools/release/check_native_review.py
if ($LASTEXITCODE -ne 0) {
    throw "La revisión nativa no está completa."
}

$localProps = @{}
if (Test-Path "local.properties") {
    Get-Content "local.properties" | ForEach-Object {
        if ($_ -match "^\s*([^#=]+)=(.*)$") {
            $localProps[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
}
$requiredProps = @(
    "linguago.storeFile",
    "linguago.storePassword",
    "linguago.keyAlias",
    "linguago.keyPassword"
)
foreach ($key in $requiredProps) {
    if (-not $localProps.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($localProps[$key])) {
        throw "Falta $key en local.properties. Configura el keystore de distribución."
    }
}
if (-not (Test-Path $localProps["linguago.storeFile"])) {
    throw "No existe el keystore: $($localProps['linguago.storeFile'])"
}
$keystoreArgs = @(
    "-Plinguago.storeFile=$($localProps['linguago.storeFile'])",
    "-Plinguago.storePassword=$($localProps['linguago.storePassword'])",
    "-Plinguago.keyAlias=$($localProps['linguago.keyAlias'])",
    "-Plinguago.keyPassword=$($localProps['linguago.keyPassword'])"
)

python tools/release/bump_version.py $Version $VersionCode
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo actualizar la versión."
}

./gradlew ktlintCheck detekt validateContent test contentScaleReport --console=plain
if ($LASTEXITCODE -ne 0) {
    throw "El gate de calidad falló."
}

& ./gradlew @keystoreArgs :app:assembleFullRelease --console=plain
if ($LASTEXITCODE -ne 0) {
    throw "El build release falló."
}

$universal = "app/build/outputs/apk/full/release/app-full-universal-release.apk"
$arm64 = "app/build/outputs/apk/full/release/app-full-arm64-v8a-release.apk"
if (-not (Test-Path $universal) -or -not (Test-Path $arm64)) {
    throw "No se encontraron los APK universal/arm64."
}

$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA "Android\Sdk" }
$buildTools = Get-ChildItem (Join-Path $sdk "build-tools") -Directory | Sort-Object Name -Descending | Select-Object -First 1
$apksigner = Join-Path $buildTools.FullName "apksigner.bat"
$previousErrorAction = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$certs = & $apksigner verify --print-certs $universal 2>&1 | Out-String
$signingExit = $LASTEXITCODE
$ErrorActionPreference = $previousErrorAction
if ($signingExit -ne 0) {
    throw "No se pudo verificar la firma del APK."
}
if ($certs -match "CN=Android Debug") {
    throw "El APK está firmado con Android Debug. Configura el keystore de distribución antes de publicar."
}

$dist = Join-Path "dist" "v$Version"
New-Item -ItemType Directory -Force $dist | Out-Null
Copy-Item $universal (Join-Path $dist "LinguaGo-$Version-global.apk") -Force
Copy-Item $arm64 (Join-Path $dist "LinguaGo-$Version-arm64.apk") -Force

$hashGlobal = (Get-FileHash (Join-Path $dist "LinguaGo-$Version-global.apk") -Algorithm SHA256).Hash
$hashArm = (Get-FileHash (Join-Path $dist "LinguaGo-$Version-arm64.apk") -Algorithm SHA256).Hash
$sums = @(
    "$hashGlobal  LinguaGo-$Version-global.apk",
    "$hashArm  LinguaGo-$Version-arm64.apk"
) -join "`n"
$utf8 = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllText((Join-Path $dist "SHA256SUMS.txt"), $sums + "`n", $utf8)

$releaseNotes = @"
# LinguaGo $Version

**versionCode:** $VersionCode
**Fecha:** $((Get-Date).ToString("yyyy-MM-dd"))

Release estable de F0/F1: A1 completo en ES→EN, ES→PT, ES→FR y ES→JA.
Total: 1.257/4.080 lecciones.

## APKs

- ``LinguaGo-$Version-global.apk``: APK full universal.
- ``LinguaGo-$Version-arm64.apk``: APK full arm64.

## Integridad

- ``SHA256SUMS.txt`` contiene los hashes de ambos APK.
"@
[System.IO.File]::WriteAllText((Join-Path $dist "RELEASE.md"), $releaseNotes, $utf8)

$changelogPath = "CHANGELOG.md"
$changelog = Get-Content $changelogPath -Raw
if ($changelog -notmatch "## \[$([regex]::Escape($Version))\]") {
    $entry = "## [$Version] - $((Get-Date).ToString("yyyy-MM-dd"))`r`n`r`nEstable tras la revisión nativa de F0/F1. Mismo contenido que 3.2.0-rc1.`r`n`r`n"
    $changelog = $changelog -replace "(?m)^## \[", "$entry## ["
    [System.IO.File]::WriteAllText((Join-Path $Root $changelogPath), $changelog, $utf8)
}

if ($DryRun) {
    Write-Host "Dry run completado. No se ha commiteado, taggeado ni publicado."
    return
}

git add app/build.gradle.kts CHANGELOG.md
git commit -m "chore: publicar release $Version"
git tag -a "v$Version" -m "LinguaGo $Version"
git push origin main
git push origin "v$Version"

gh release create "v$Version" `
    (Join-Path $dist "LinguaGo-$Version-global.apk") `
    (Join-Path $dist "LinguaGo-$Version-arm64.apk") `
    --title "LinguaGo $Version" `
    --notes-file (Join-Path $dist "RELEASE.md") `
    --latest

Write-Host "Release $Version publicado."
