# Genera el set de SFX originales de LinguaGo (ADR-027).
# Uso: powershell -NoProfile -ExecutionPolicy Bypass -File tools\generate_sfx.ps1
# Salida: core/core-ui/src/main/res/raw/sfx_*.wav (PCM mono 16-bit, 44.1 kHz).

$ErrorActionPreference = "Stop"

$sampleRate = 44100
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
        [double]$Release = 0.04
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
        } elseif ($Wave -eq "soft") {
            $sample = [Math]::Sin($phase) + 0.22 * [Math]::Sin(2.0 * $phase)
        } else {
            $sample = [Math]::Sin($phase)
        }
        $envelope = Get-Envelope -Index $i -Count $count -AttackSamples $attackSamples -ReleaseSamples $releaseSamples
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

$tap = New-Sound 0.035
Add-Tone -Buffer $tap -StartSeconds 0 -DurationSeconds 0.026 -Frequency 1450 -Amplitude 0.16 -Attack 0.001 -Release 0.020
Write-Wav -Buffer $tap -Name "sfx_tap"

$select = New-Sound 0.075
Add-Tone -Buffer $select -StartSeconds 0 -DurationSeconds 0.040 -Frequency 880 -Amplitude 0.15 -Attack 0.002 -Release 0.030
Add-Tone -Buffer $select -StartSeconds 0.020 -DurationSeconds 0.050 -Frequency 1318.51 -Amplitude 0.10 -Attack 0.002 -Release 0.035
Write-Wav -Buffer $select -Name "sfx_select"

$deselect = New-Sound 0.065
Add-Tone -Buffer $deselect -StartSeconds 0 -DurationSeconds 0.035 -Frequency 659.25 -Amplitude 0.12 -Attack 0.002 -Release 0.025
Add-Tone -Buffer $deselect -StartSeconds 0.016 -DurationSeconds 0.044 -Frequency 440 -Amplitude 0.10 -Attack 0.002 -Release 0.030
Write-Wav -Buffer $deselect -Name "sfx_deselect"

$snap = New-Sound 0.090
Add-Noise -Buffer $snap -StartSeconds 0 -DurationSeconds 0.032 -Amplitude 0.18 -Seed 41
Add-Tone -Buffer $snap -StartSeconds 0 -DurationSeconds 0.055 -Frequency 220 -Amplitude 0.22 -Wave "triangle" -Attack 0.001 -Release 0.040
Add-Tone -Buffer $snap -StartSeconds 0.012 -DurationSeconds 0.040 -Frequency 330 -Amplitude 0.10 -Attack 0.001 -Release 0.030
Write-Wav -Buffer $snap -Name "sfx_snap"

$correct = New-Sound 0.340
Add-Tone -Buffer $correct -StartSeconds 0.000 -DurationSeconds 0.090 -Frequency 523.25 -Amplitude 0.15 -Wave "soft" -Attack 0.004 -Release 0.070
Add-Tone -Buffer $correct -StartSeconds 0.060 -DurationSeconds 0.090 -Frequency 659.25 -Amplitude 0.14 -Wave "soft" -Attack 0.004 -Release 0.070
Add-Tone -Buffer $correct -StartSeconds 0.120 -DurationSeconds 0.100 -Frequency 783.99 -Amplitude 0.14 -Wave "soft" -Attack 0.004 -Release 0.080
Add-Tone -Buffer $correct -StartSeconds 0.190 -DurationSeconds 0.120 -Frequency 1046.50 -Amplitude 0.08 -Wave "sine" -Attack 0.004 -Release 0.100
Write-Wav -Buffer $correct -Name "sfx_correct"

$incorrect = New-Sound 0.280
Add-Tone -Buffer $incorrect -StartSeconds 0.000 -DurationSeconds 0.120 -Frequency 246.94 -Amplitude 0.12 -Wave "triangle" -Attack 0.004 -Release 0.090
Add-Tone -Buffer $incorrect -StartSeconds 0.080 -DurationSeconds 0.160 -Frequency 220.00 -Amplitude 0.11 -Wave "triangle" -Attack 0.004 -Release 0.120
Add-Tone -Buffer $incorrect -StartSeconds 0.080 -DurationSeconds 0.150 -Frequency 110.00 -Amplitude 0.05 -Wave "sine" -Attack 0.004 -Release 0.110
Write-Wav -Buffer $incorrect -Name "sfx_incorrect"

$complete = New-Sound 0.640
Add-Tone -Buffer $complete -StartSeconds 0.000 -DurationSeconds 0.100 -Frequency 523.25 -Amplitude 0.13 -Wave "soft" -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.070 -DurationSeconds 0.100 -Frequency 659.25 -Amplitude 0.13 -Wave "soft" -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.140 -DurationSeconds 0.100 -Frequency 783.99 -Amplitude 0.13 -Wave "soft" -Attack 0.004 -Release 0.080
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.180 -Frequency 1046.50 -Amplitude 0.12 -Wave "soft" -Attack 0.004 -Release 0.140
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.260 -Frequency 523.25 -Amplitude 0.06 -Wave "sine" -Attack 0.020 -Release 0.220
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.260 -Frequency 659.25 -Amplitude 0.05 -Wave "sine" -Attack 0.020 -Release 0.220
Add-Tone -Buffer $complete -StartSeconds 0.210 -DurationSeconds 0.260 -Frequency 783.99 -Amplitude 0.05 -Wave "sine" -Attack 0.020 -Release 0.220
Write-Wav -Buffer $complete -Name "sfx_complete"
