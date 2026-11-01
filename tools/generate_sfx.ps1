# Genera el set de SFX originales de LinguaGo (ADR-027, ADR-031).
# Uso: powershell -NoProfile -ExecutionPolicy Bypass -File tools\generate_sfx.ps1
# Salida: core/core-ui/src/main/res/raw/sfx_*.wav (PCM mono 16-bit, 22.05 kHz).
#
# Síntesis v2: armónicos (parciales) y envolventes percusivas para un timbre
# cálido tipo madera/marimba. Sin samples de terceros (I5).

$ErrorActionPreference = "Stop"

$sampleRate = 22050
$outDir = Join-Path $PSScriptRoot "..\core\core-ui\src\main\res\raw"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function New-Sound {
    param([double]$Seconds)
    return New-Object "double[]" ([int]($Seconds * $sampleRate))
}

function Get-Envelope {
    param([int]$Index, [int]$Count, [int]$AttackSamples, [int]$ReleaseSamples)
    if ($Index -lt $AttackSamples) {
        return $Index / [double]$AttackSamples
    }
    if ($Index -gt ($Count - $ReleaseSamples)) {
        return [Math]::Max(0.0, ($Count - $Index) / [double]$ReleaseSamples)
    }
    return 1.0
}

function Add-Tone {
    param(
        [double[]]$Buffer,
        [double]$StartSeconds,
        [double]$DurationSeconds,
        [double]$Frequency,
        [double]$Amplitude,
        [string]$Wave = "sine",
        [double]$Attack = 0.004,
        [double]$Release = 0.04,
        [double[]]$Partials = @(1.0),
        [double]$Decay = 0.0
    )

    $start = [int]($StartSeconds * $sampleRate)
    $count = [int]($DurationSeconds * $sampleRate)
    $attackSamples = [Math]::Max(1, [int]($Attack * $sampleRate))
    $releaseSamples = [Math]::Max(1, [int]($Release * $sampleRate))

    for ($i = 0; $i -lt $count; $i++) {
        $index = $start + $i
        if ($index -lt 0 -or $index -ge $Buffer.Length) {
            continue
        }
        $t = $i / [double]$sampleRate
        $phase = 2.0 * [Math]::PI * $Frequency * $t
        $sample = 0.0
        if ($Wave -eq "triangle") {
            $sample = (2.0 / [Math]::PI) * [Math]::Asin([Math]::Sin($phase))
        } else {
            for ($h = 0; $h -lt $Partials.Length; $h++) {
                $sample += $Partials[$h] * [Math]::Sin(($h + 1) * $phase)
            }
        }
        $envelope = Get-Envelope -Index $i -Count $count -AttackSamples $attackSamples -ReleaseSamples $releaseSamples
        if ($Decay -gt 0.0) {
            $envelope *= [Math]::Exp(-$Decay * $t)
        }
        $Buffer[$index] += $sample * $envelope * $Amplitude
    }
}

function Add-Noise {
    param(
        [double[]]$Buffer,
        [double]$StartSeconds,
        [double]$DurationSeconds,
        [double]$Amplitude,
        [int]$Seed = 17
    )

    $start = [int]($StartSeconds * $sampleRate)
    $count = [int]($DurationSeconds * $sampleRate)
    $random = [System.Random]::new($Seed)
    $attackSamples = [Math]::Max(1, [int](0.001 * $sampleRate))
    $releaseSamples = [Math]::Max(1, [int](0.03 * $sampleRate))

    for ($i = 0; $i -lt $count; $i++) {
        $index = $start + $i
        if ($index -lt 0 -or $index -ge $Buffer.Length) {
            continue
        }
        $envelope = Get-Envelope -Index $i -Count $count -AttackSamples $attackSamples -ReleaseSamples $releaseSamples
        $noise = ($random.NextDouble() * 2.0) - 1.0
        $Buffer[$index] += $noise * $envelope * $Amplitude
    }
}

function Write-Wav {
    param([double[]]$Buffer, [string]$Name)

    $peak = 0.0
    foreach ($sample in $Buffer) {
        $absolute = [Math]::Abs($sample)
        if ($absolute -gt $peak) {
            $peak = $absolute
        }
    }
    $scale = 1.0
    if ($peak -gt 0.85) {
        $scale = 0.85 / $peak
    }

    $path = Join-Path $outDir "$Name.wav"
    $stream = [System.IO.File]::Create($path)
    $writer = [System.IO.BinaryWriter]::new($stream)
    $dataSize = [int]($Buffer.Length * 2)

    try {
        $writer.Write([Text.Encoding]::ASCII.GetBytes("RIFF"))
        $writer.Write([int](36 + $dataSize))
        $writer.Write([Text.Encoding]::ASCII.GetBytes("WAVE"))
        $writer.Write([Text.Encoding]::ASCII.GetBytes("fmt "))
        $writer.Write([int]16)
        $writer.Write([int16]1)
        $writer.Write([int16]1)
        $writer.Write([int]$sampleRate)
        $writer.Write([int]($sampleRate * 2))
        $writer.Write([int16]2)
        $writer.Write([int16]16)
        $writer.Write([Text.Encoding]::ASCII.GetBytes("data"))
        $writer.Write([int]$dataSize)

        foreach ($sample in $Buffer) {
            $clamped = [Math]::Max(-1.0, [Math]::Min(1.0, $sample * $scale))
            $value = [int][Math]::Round($clamped * 32767.0)
            $writer.Write([int16]$value)
        }
    } finally {
        $writer.Dispose()
        $stream.Dispose()
    }

    Write-Output "wrote $path"
}

# Timbre de madera/marimba: fundamental + parciales 2 y 3 con caída rápida.
$wood = @(1.0, 0.42, 0.20)
# Timbre suave para acordes: fundamental y quinta con caída lenta.
$warm = @(1.0, 0.30, 0.12)

$tap = New-Sound 0.045
Add-Tone -Buffer $tap -StartSeconds 0 -DurationSeconds 0.034 -Frequency 1500 -Amplitude 0.20 -Partials $wood -Decay 55 -Attack 0.001 -Release 0.020
Write-Wav -Buffer $tap -Name "sfx_tap"

$select = New-Sound 0.085
Add-Tone -Buffer $select -StartSeconds 0 -DurationSeconds 0.050 -Frequency 880 -Amplitude 0.16 -Partials $wood -Decay 32 -Attack 0.002 -Release 0.030
Add-Tone -Buffer $select -StartSeconds 0.022 -DurationSeconds 0.058 -Frequency 1318.51 -Amplitude 0.12 -Partials $wood -Decay 26 -Attack 0.002 -Release 0.035
Write-Wav -Buffer $select -Name "sfx_select"

$deselect = New-Sound 0.075
Add-Tone -Buffer $deselect -StartSeconds 0 -DurationSeconds 0.042 -Frequency 659.25 -Amplitude 0.14 -Partials $wood -Decay 34 -Attack 0.002 -Release 0.025
Add-Tone -Buffer $deselect -StartSeconds 0.018 -DurationSeconds 0.050 -Frequency 440 -Amplitude 0.11 -Partials $wood -Decay 30 -Attack 0.002 -Release 0.030
Write-Wav -Buffer $deselect -Name "sfx_deselect"

$snap = New-Sound 0.095
Add-Noise -Buffer $snap -StartSeconds 0 -DurationSeconds 0.030 -Amplitude 0.16 -Seed 41
Add-Tone -Buffer $snap -StartSeconds 0 -DurationSeconds 0.062 -Frequency 220 -Amplitude 0.24 -Wave "triangle" -Decay 22 -Attack 0.001 -Release 0.040
Add-Tone -Buffer $snap -StartSeconds 0.012 -DurationSeconds 0.045 -Frequency 330 -Amplitude 0.11 -Partials $wood -Decay 30 -Attack 0.001 -Release 0.030
Write-Wav -Buffer $snap -Name "sfx_snap"

$correct = New-Sound 0.360
Add-Tone -Buffer $correct -StartSeconds 0.000 -DurationSeconds 0.100 -Frequency 523.25 -Amplitude 0.17 -Partials $wood -Decay 16 -Attack 0.004 -Release 0.070
Add-Tone -Buffer $correct -StartSeconds 0.060 -DurationSeconds 0.100 -Frequency 659.25 -Amplitude 0.16 -Partials $wood -Decay 15 -Attack 0.004 -Release 0.070
Add-Tone -Buffer $correct -StartSeconds 0.120 -DurationSeconds 0.110 -Frequency 783.99 -Amplitude 0.16 -Partials $wood -Decay 14 -Attack 0.004 -Release 0.080
Add-Tone -Buffer $correct -StartSeconds 0.190 -DurationSeconds 0.130 -Frequency 1046.50 -Amplitude 0.10 -Partials $warm -Decay 10 -Attack 0.004 -Release 0.100
Write-Wav -Buffer $correct -Name "sfx_correct"

$incorrect = New-Sound 0.300
Add-Tone -Buffer $incorrect -StartSeconds 0.000 -DurationSeconds 0.130 -Frequency 246.94 -Amplitude 0.14 -Wave "triangle" -Decay 6 -Attack 0.004 -Release 0.090
Add-Tone -Buffer $incorrect -StartSeconds 0.080 -DurationSeconds 0.170 -Frequency 220.00 -Amplitude 0.12 -Wave "triangle" -Decay 6 -Attack 0.004 -Release 0.120
Add-Tone -Buffer $incorrect -StartSeconds 0.080 -DurationSeconds 0.160 -Frequency 110.00 -Amplitude 0.06 -Partials $warm -Decay 8 -Attack 0.004 -Release 0.110
Write-Wav -Buffer $incorrect -Name "sfx_incorrect"

$complete = New-Sound 0.660
Add-Tone -Buffer $complete -StartSeconds 0.000 -DurationSeconds 0.110 -Frequency 523.25 -Amplitude 0.15 -Partials $wood -Decay 12 -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.070 -DurationSeconds 0.110 -Frequency 659.25 -Amplitude 0.15 -Partials $wood -Decay 12 -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.140 -DurationSeconds 0.110 -Frequency 783.99 -Amplitude 0.15 -Partials $wood -Decay 11 -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.190 -Frequency 1046.50 -Amplitude 0.14 -Partials $wood -Decay 8 -Attack 0.004 -Release 0.140
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.300 -Frequency 523.25 -Amplitude 0.07 -Partials $warm -Decay 5 -Attack 0.020 -Release 0.220
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.300 -Frequency 659.25 -Amplitude 0.06 -Partials $warm -Decay 5 -Attack 0.020 -Release 0.220
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.300 -Frequency 783.99 -Amplitude 0.06 -Partials $warm -Decay 5 -Attack 0.020 -Release 0.220
Write-Wav -Buffer $complete -Name "sfx_complete"

# Racha de aciertos: dos notas ascendentes y brillantes (ADR-031).
$combo = New-Sound 0.240
Add-Tone -Buffer $combo -StartSeconds 0.000 -DurationSeconds 0.080 -Frequency 659.25 -Amplitude 0.16 -Partials $wood -Decay 20 -Attack 0.002 -Release 0.050
Add-Tone -Buffer $combo -StartSeconds 0.070 -DurationSeconds 0.090 -Frequency 880.00 -Amplitude 0.16 -Partials $wood -Decay 18 -Attack 0.002 -Release 0.060
Add-Tone -Buffer $combo -StartSeconds 0.140 -DurationSeconds 0.090 -Frequency 1108.73 -Amplitude 0.13 -Partials $wood -Decay 16 -Attack 0.002 -Release 0.060
Write-Wav -Buffer $combo -Name "sfx_combo"

# Giro de tarjeta: roce suave de papel (ruido corto + tono apagado).
$flip = New-Sound 0.130
Add-Noise -Buffer $flip -StartSeconds 0 -DurationSeconds 0.055 -Amplitude 0.12 -Seed 73
Add-Tone -Buffer $flip -StartSeconds 0.010 -DurationSeconds 0.070 -Frequency 720 -Amplitude 0.10 -Partials $warm -Decay 30 -Attack 0.002 -Release 0.050
Write-Wav -Buffer $flip -Name "sfx_flip"

# Pérdida de vida: dos notas graves y cortas, sin dureza.
$heart = New-Sound 0.300
Add-Tone -Buffer $heart -StartSeconds 0.000 -DurationSeconds 0.130 -Frequency 220.00 -Amplitude 0.15 -Wave "triangle" -Decay 7 -Attack 0.004 -Release 0.080
Add-Tone -Buffer $heart -StartSeconds 0.090 -DurationSeconds 0.170 -Frequency 174.61 -Amplitude 0.13 -Wave "triangle" -Decay 6 -Attack 0.004 -Release 0.110
Add-Tone -Buffer $heart -StartSeconds 0.090 -DurationSeconds 0.160 -Frequency 110.00 -Amplitude 0.06 -Partials $warm -Decay 7 -Attack 0.004 -Release 0.100
Write-Wav -Buffer $heart -Name "sfx_heart_lost"

# Apertura de cofre: fanfarria breve con brillo (ADR-031).
$chest = New-Sound 0.700
Add-Tone -Buffer $chest -StartSeconds 0.000 -DurationSeconds 0.110 -Frequency 523.25 -Amplitude 0.15 -Partials $wood -Decay 11 -Attack 0.003 -Release 0.080
Add-Tone -Buffer $chest -StartSeconds 0.080 -DurationSeconds 0.110 -Frequency 659.25 -Amplitude 0.15 -Partials $wood -Decay 11 -Attack 0.003 -Release 0.080
Add-Tone -Buffer $chest -StartSeconds 0.160 -DurationSeconds 0.120 -Frequency 783.99 -Amplitude 0.15 -Partials $wood -Decay 10 -Attack 0.003 -Release 0.090
Add-Tone -Buffer $chest -StartSeconds 0.240 -DurationSeconds 0.220 -Frequency 1046.50 -Amplitude 0.16 -Partials $wood -Decay 7 -Attack 0.003 -Release 0.160
Add-Tone -Buffer $chest -StartSeconds 0.240 -DurationSeconds 0.300 -Frequency 1567.98 -Amplitude 0.06 -Partials $warm -Decay 6 -Attack 0.010 -Release 0.220
Add-Tone -Buffer $chest -StartSeconds 0.240 -DurationSeconds 0.300 -Frequency 2093.00 -Amplitude 0.04 -Partials $warm -Decay 6 -Attack 0.010 -Release 0.220
Add-Noise -Buffer $chest -StartSeconds 0.240 -DurationSeconds 0.120 -Amplitude 0.04 -Seed 97
Write-Wav -Buffer $chest -Name "sfx_chest_open"
