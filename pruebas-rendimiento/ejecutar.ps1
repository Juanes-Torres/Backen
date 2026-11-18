# =========================================================
# KAIRÓS - ejecutar las pruebas de rendimiento con Apache JMeter
#
#   Carga  (usuarios normales, con pausas, 5 minutos):
#     .\pruebas-rendimiento\ejecutar.ps1 -Tipo carga
#   Estrés (sin pausas, subiendo usuarios por escalones hasta que se rompa):
#     .\pruebas-rendimiento\ejecutar.ps1 -Tipo estres
#   Con otros valores:
#     .\pruebas-rendimiento\ejecutar.ps1 -Tipo carga -Usuarios 100 -Duracion 600
#
# Necesita: el backend corriendo (docker compose up -d) y JMeter (ver docs/pruebas-rendimiento.md).
# La clave del usuario sale de la variable KAIROS_CLAVE o, si no existe, de ADMIN_PASSWORD en .env.
# Nunca se muestra en pantalla.
# Resultados: pruebas-rendimiento\resultados\<fecha>-<tipo>-<usuarios>u\reporte\index.html
# =========================================================
param(
    [ValidateSet("carga", "estres")] [string] $Tipo = "carga",
    [int[]]  $Usuarios,                  # carga: 50  | estrés: 50,100,200,400,800 (un escalón por valor)
    [int]    $Duracion,                  # segundos por prueba (carga: 300 | estrés: 60 por escalón)
    [int]    $Pausa = -1,                # ms entre peticiones de un usuario (carga: 1000 | estrés: 0)
    [string] $Servidor = "localhost",
    [int]    $Puerto = 8080,
    [string] $Email = "admin@kairos.com",
    [string] $JMeter = $(if ($env:JMETER_HOME) { $env:JMETER_HOME } else { "$env:USERPROFILE\herramientas\apache-jmeter-5.6.3" })
)
$ErrorActionPreference = "Stop"
$carpeta = $PSScriptRoot
$raiz = Split-Path $carpeta -Parent

# ----- Valores por defecto de cada tipo de prueba -----
if ($Tipo -eq "carga") {
    if (-not $Usuarios) { $Usuarios = @(50) }
    if (-not $Duracion) { $Duracion = 300 }
    if ($Pausa -lt 0) { $Pausa = 1000 }
    $rampa = 60
} else {
    if (-not $Usuarios) { $Usuarios = @(50, 100, 200, 400, 800) }
    if (-not $Duracion) { $Duracion = 60 }
    if ($Pausa -lt 0) { $Pausa = 0 }
    $rampa = 10
}

# ----- Revisiones antes de empezar -----
$jmeterBat = Join-Path $JMeter "bin\jmeter.bat"
if (-not (Test-Path $jmeterBat)) { throw "No encuentro JMeter en $JMeter. Instálalo o pon la ruta en JMETER_HOME." }

if (-not $env:KAIROS_CLAVE) {
    $archivoEnv = Join-Path $raiz ".env"
    if (Test-Path $archivoEnv) {
        $linea = Get-Content $archivoEnv | Where-Object { $_ -match '^ADMIN_PASSWORD=' } | Select-Object -First 1
        if ($linea) { $env:KAIROS_CLAVE = $linea.Substring("ADMIN_PASSWORD=".Length).Trim() }
    }
}
if (-not $env:KAIROS_CLAVE) { throw "Falta la clave: define KAIROS_CLAVE o ADMIN_PASSWORD en .env" }

try {
    Invoke-WebRequest "http://${Servidor}:${Puerto}/api/categorias" -UseBasicParsing -TimeoutSec 10 | Out-Null
} catch {
    throw "El backend no responde en http://${Servidor}:${Puerto}. Arráncalo con: docker compose up -d"
}

# JMeter con más memoria: muchos usuarios simultáneos
$env:HEAP = "-Xms1g -Xmx2g"

$resumen = @()
foreach ($n in $Usuarios) {
    $nombre = "{0}-{1}-{2}u" -f (Get-Date -Format "yyyyMMdd-HHmmss"), $Tipo, $n
    $salida = Join-Path $carpeta "resultados\$nombre"
    New-Item -ItemType Directory -Force $salida | Out-Null
    Write-Host ""
    Write-Host "=== $Tipo con $n usuarios, $Duracion s, pausa $Pausa ms ===" -ForegroundColor Cyan

    # Mide CPU y memoria del contenedor del backend cada 5 s mientras corre la prueba
    $medicion = Start-Job -ArgumentList (Join-Path $salida "recursos.csv") -ScriptBlock {
        param($archivo)
        "hora,cpu,memoria" | Out-File $archivo -Encoding utf8
        while ($true) {
            $s = docker stats --no-stream --format "{{.CPUPerc}},{{.MemUsage}}" kairos-backend 2>$null
            if ($s) { "$(Get-Date -Format HH:mm:ss),$s" | Out-File $archivo -Append -Encoding utf8 }
            Start-Sleep 5
        }
    }

    # La clave va en un archivo temporal (no en la línea de comandos ni en el plan) y se borra al terminar
    $archivoClave = Join-Path $env:TEMP ("kairos-jmeter-{0}.properties" -f [guid]::NewGuid())
    "clave=$($env:KAIROS_CLAVE -replace '\\', '\\')" | Out-File $archivoClave -Encoding ascii
    try {
        & $jmeterBat -n -t (Join-Path $carpeta "kairos.jmx") `
            -l (Join-Path $salida "resultados.jtl") -j (Join-Path $salida "jmeter.log") `
            -e -o (Join-Path $salida "reporte") -q $archivoClave `
            "-Jhost=$Servidor" "-Jpuerto=$Puerto" "-Jusuarios=$n" "-Jrampa=$rampa" `
            "-Jduracion=$Duracion" "-Jpausa=$Pausa" "-Jemail=$Email" | Out-Null
    } finally {
        Remove-Item $archivoClave -Force -ErrorAction SilentlyContinue
        Stop-Job $medicion; Remove-Job $medicion
    }

    # Lo más importante del reporte: la fila "Total" de statistics.json
    $estadisticas = Join-Path $salida "reporte\statistics.json"
    if (-not (Test-Path $estadisticas)) { Write-Warning "JMeter no generó el reporte: revisa $salida\jmeter.log"; continue }
    $t = (Get-Content $estadisticas -Raw | ConvertFrom-Json).Total
    $cpu = Import-Csv (Join-Path $salida "recursos.csv") | ForEach-Object { [double]($_.cpu -replace '%', '') }
    $fila = [pscustomobject]@{
        Usuarios          = $n
        Peticiones        = $t.sampleCount
        "Pet/s"           = [math]::Round($t.throughput, 1)
        "Promedio ms"     = [math]::Round($t.meanResTime)
        "p90 ms"          = [math]::Round($t.pct1ResTime)
        "p95 ms"          = [math]::Round($t.pct2ResTime)
        "p99 ms"          = [math]::Round($t.pct3ResTime)
        "Max ms"          = [math]::Round($t.maxResTime)
        "Errores %"       = [math]::Round($t.errorPct, 2)
        "CPU max backend" = if ($cpu) { "{0:N0} %" -f ($cpu | Measure-Object -Maximum).Maximum } else { "-" }
        Reporte           = "resultados\$nombre\reporte\index.html"
    }
    $resumen += $fila
    $fila | Format-List | Out-String | Write-Host

    # Entre escalones de estrés, un respiro para que el backend se recupere
    if ($Tipo -eq "estres" -and $n -ne $Usuarios[-1]) { Start-Sleep 15 }
}

Write-Host "=== Resumen ($Tipo) ===" -ForegroundColor Green
$resumen | Format-Table Usuarios, Peticiones, "Pet/s", "Promedio ms", "p95 ms", "p99 ms", "Max ms", "Errores %", "CPU max backend" -AutoSize | Out-String | Write-Host
$resumen | Export-Csv (Join-Path $carpeta ("resultados\resumen-{0}-{1}.csv" -f $Tipo, (Get-Date -Format "yyyyMMdd-HHmm"))) -NoTypeInformation -Encoding utf8
