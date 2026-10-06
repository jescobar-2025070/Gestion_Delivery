# Script de prueba e2e para simular peticiones reales a los microservicios.
# ADVERTENCIA: Asegurate de tener los 4 microservicios levantados (ej. desde tu IDE)
# antes de correr este script.

$ErrorActionPreference = "Continue"

$AUTH_URL = "http://localhost:8081/api/v1"
$CATALOG_URL = "http://localhost:8082/api/v1"
$ORDER_URL = "http://localhost:8083/api/v1"
$DELIVERY_URL = "http://localhost:8084/api/v1"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [string]$Body,
        [string]$Token,
        [string]$ContentType = "application/json",
        [string]$Accept = "application/json"
    )
    $headers = @{ Accept = $Accept }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Body $Body -ContentType $ContentType -Headers $headers
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  ERROR HTTP $statusCode : $responseBody" -ForegroundColor Red
        return $null
    }
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "    Iniciando pruebas de API E2E          " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. LOGIN
Write-Host "`n[1] Autenticando usuarios..." -ForegroundColor Yellow

$adminRes = Invoke-Api -Method Post -Uri "$AUTH_URL/auth/login" -Body '{"email":"admin@fastorder.com","password":"admin123"}'
$ADMIN_TOKEN = $adminRes.token

$clienteRes = Invoke-Api -Method Post -Uri "$AUTH_URL/auth/login" -Body '{"email":"maria@fastorder.com","password":"admin123"}'
$CLIENTE_TOKEN = $clienteRes.token

$repartidorRes = Invoke-Api -Method Post -Uri "$AUTH_URL/auth/login" -Body '{"email":"juan@fastorder.com","password":"admin123"}'
$REPARTIDOR_TOKEN = $repartidorRes.token

if (-not $ADMIN_TOKEN) {
    Write-Host "Error: Fallo la autenticacion." -ForegroundColor Red
    exit 1
}
Write-Host "  Tokens obtenidos exitosamente." -ForegroundColor Green

# 2. CATALOGO (ADMIN)
Write-Host "`n[2] ADMIN crea un comercio..." -ForegroundColor Yellow
$comercioRes = Invoke-Api -Method Post -Uri "$CATALOG_URL/comercios" -Token $ADMIN_TOKEN -Body '{"nombre":"Pizza Nostra","categoria":"RESTAURANTE","direccion":"Avenida Siempre Viva 123","abierto":true}'
$comercioRes | ConvertTo-Json | Write-Host
$COMERCIO_ID = $comercioRes.id

Write-Host "`n[3] ADMIN crea un producto en el comercio $COMERCIO_ID..." -ForegroundColor Yellow
$productoRes = Invoke-Api -Method Post -Uri "$CATALOG_URL/comercios/$COMERCIO_ID/productos" -Token $ADMIN_TOKEN -Body '{"nombre":"Pizza Familiar","precio":80.00,"stock":50,"disponible":true}'
$productoRes | ConvertTo-Json | Write-Host
$PRODUCTO_ID = $productoRes.id

# 3. CLIENTE EXPLORA Y COMPRA
Write-Host "`n[4] CLIENTE consulta catalogo de comercios..." -ForegroundColor Yellow
$comercios = Invoke-Api -Method Get -Uri "$CATALOG_URL/comercios" -Token $CLIENTE_TOKEN
$comercios | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n[5] CLIENTE realiza un pedido del producto $PRODUCTO_ID..." -ForegroundColor Yellow
$pedidoBody = '{"productos":[{"productoId":' + $PRODUCTO_ID + ',"cantidad":2}]}'
$pedidoRes = Invoke-Api -Method Post -Uri "$ORDER_URL/pedidos" -Token $CLIENTE_TOKEN -Body $pedidoBody
$pedidoRes | ConvertTo-Json -Depth 5 | Write-Host
$PEDIDO_ID = $pedidoRes.id

# 4. DELIVERY - REPARTIDOR
Write-Host "`n[6] REPARTIDOR consulta pedidos disponibles..." -ForegroundColor Yellow
$disponibles = Invoke-Api -Method Get -Uri "$DELIVERY_URL/pedidos/disponibles" -Token $REPARTIDOR_TOKEN
$disponibles | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n[7] REPARTIDOR toma el pedido $PEDIDO_ID (EN_PREPARACION)..." -ForegroundColor Yellow
$resPrep = Invoke-Api -Method Patch -Uri "$DELIVERY_URL/pedidos/$PEDIDO_ID/estado" -Token $REPARTIDOR_TOKEN -Body '{"estado":"EN_PREPARACION"}'
$resPrep | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n[8] REPARTIDOR actualiza estado a EN_CAMINO..." -ForegroundColor Yellow
$resCamino = Invoke-Api -Method Patch -Uri "$DELIVERY_URL/pedidos/$PEDIDO_ID/estado" -Token $REPARTIDOR_TOKEN -Body '{"estado":"EN_CAMINO"}'
$resCamino | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n[9] CLIENTE revisa sus pedidos..." -ForegroundColor Yellow
$misPedidos = Invoke-Api -Method Get -Uri "$ORDER_URL/pedidos/mis-pedidos" -Token $CLIENTE_TOKEN
$misPedidos | ConvertTo-Json -Depth 5 | Write-Host

# 5. AUDITORIA
Write-Host "`n[10] ADMIN revisa logs de auditoria..." -ForegroundColor Yellow
$auditoria = Invoke-Api -Method Get -Uri "$AUTH_URL/auditoria" -Token $ADMIN_TOKEN
$auditoria | ConvertTo-Json -Depth 5 | Write-Host

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "          Pruebas API Finalizadas           " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
