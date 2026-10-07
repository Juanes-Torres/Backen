<#
.SYNOPSIS
    Muestra los ambientes de KAIROS y sus variables de entorno, y arranca el backend en el ambiente elegido.

.EXAMPLE
    .\scripts\ambiente.ps1                              # desarrollo: muestra variables y arranca
    .\scripts\ambiente.ps1 -Ambiente produccion -SoloMostrar
    .\scripts\ambiente.ps1 -Ambiente produccion         # exige todas las variables antes de arrancar
#>
param(
    [ValidateSet('desarrollo', 'pre-produccion', 'produccion')]
    [string]$Ambiente = 'desarrollo',

    # Solo muestra la informacion, no arranca el backend
    [switch]$SoloMostrar
)

$raiz = Split-Path -Parent $PSScriptRoot
$recursos = Join-Path $raiz 'src\main\resources'

# Valores por defecto que usa application-desarrollo.properties
$porDefectoDesarrollo = @{
    DB_URL         = 'jdbc:postgresql://localhost:5432/kairos_db'
    DB_USERNAME    = 'kairos'
    DB_PASSWORD    = 'kairos123'
    JWT_SECRET     = 'cambia-esta-clave-secreta-de-32-caracteres-min'
    CORS_ORIGINS   = 'http://localhost:5173'
    ADMIN_EMAIL    = 'admin@kairos.com'
    ADMIN_PASSWORD = 'Admin123*'
    FRONTEND_URL   = 'http://localhost:5173'
}
$variables = 'DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET', 'CORS_ORIGINS', 'ADMIN_EMAIL', 'ADMIN_PASSWORD', 'FRONTEND_URL'

# Opcionales en todos los ambientes: correo de bienvenida (si no estan, el correo queda apagado)
$opcionalesCorreo = 'MAIL_ENABLED', 'MAIL_USERNAME', 'MAIL_PASSWORD'

function Ocultar([string]$nombre, [string]$valor) {
    if ($nombre -match 'PASSWORD|SECRET') { return '****' }
    return $valor
}

# ===== 1. Ambientes disponibles =====
Write-Host ''
Write-Host '=== Ambientes disponibles ===' -ForegroundColor Cyan
Get-ChildItem $recursos -Filter 'application-*.properties' | ForEach-Object {
    $nombre = $_.BaseName -replace '^application-', ''
    if ($nombre -eq $Ambiente) {
        Write-Host ("  > {0}  (seleccionado)" -f $nombre) -ForegroundColor Green
    } else {
        Write-Host ("    {0}" -f $nombre)
    }
}

# ===== 2. Variables de entorno del ambiente =====
Write-Host ''
Write-Host ("=== Variables de entorno para '{0}' ===" -f $Ambiente) -ForegroundColor Cyan

$faltantes = @()
$tabla = foreach ($v in $variables) {
    $valor = [Environment]::GetEnvironmentVariable($v)
    if ($valor) {
        $origen = 'variable de entorno'
        $mostrar = Ocultar $v $valor
    } elseif ($Ambiente -eq 'desarrollo') {
        $origen = 'valor por defecto (desarrollo)'
        $mostrar = Ocultar $v $porDefectoDesarrollo[$v]
    } else {
        $origen = 'FALTA'
        $mostrar = '-'
        $faltantes += $v
    }
    [PSCustomObject]@{ Variable = $v; Valor = $mostrar; Origen = $origen }
}
# SERVER_PORT es opcional en todos los ambientes (por defecto 8080)
$puerto = [Environment]::GetEnvironmentVariable('SERVER_PORT')
if ($puerto) {
    $tabla += [PSCustomObject]@{ Variable = 'SERVER_PORT'; Valor = $puerto; Origen = 'variable de entorno' }
} else {
    $puerto = '8080'
    $tabla += [PSCustomObject]@{ Variable = 'SERVER_PORT'; Valor = $puerto; Origen = 'valor por defecto (opcional)' }
}
foreach ($v in $opcionalesCorreo) {
    $valor = [Environment]::GetEnvironmentVariable($v)
    if ($valor) {
        $tabla += [PSCustomObject]@{ Variable = $v; Valor = (Ocultar $v $valor); Origen = 'variable de entorno' }
    } elseif ($Ambiente -eq 'desarrollo') {
        $tabla += [PSCustomObject]@{ Variable = $v; Valor = '-'; Origen = 'opcional (o en secrets.properties)' }
    } else {
        $tabla += [PSCustomObject]@{ Variable = $v; Valor = '-'; Origen = 'opcional (correo apagado)' }
    }
}
$tabla | Format-Table -AutoSize | Out-String | Write-Host

if ($faltantes.Count -gt 0) {
    Write-Host ("Faltan {0} variable(s) obligatoria(s) para '{1}': {2}" -f $faltantes.Count, $Ambiente, ($faltantes -join ', ')) -ForegroundColor Red
    Write-Host 'Configuralas en esta terminal antes de arrancar, por ejemplo:' -ForegroundColor Yellow
    Write-Host '    $env:JWT_SECRET = "una-clave-larga-de-al-menos-32-caracteres"' -ForegroundColor Yellow
    exit 1
}

if ($SoloMostrar) {
    Write-Host 'Todo listo. (Modo -SoloMostrar: no se arranca el backend.)' -ForegroundColor Green
    exit 0
}

# ===== 3. Arrancar el backend =====
Write-Host ("Arrancando el backend en '{0}' en http://localhost:{1} ... (Ctrl + C para detenerlo)" -f $Ambiente, $puerto) -ForegroundColor Green
Push-Location $raiz
try {
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=$Ambiente"
} finally {
    Pop-Location
}
