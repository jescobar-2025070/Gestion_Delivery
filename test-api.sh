#!/bin/bash
# Script de prueba e2e para simular peticiones reales a los microservicios.
# ADVERTENCIA: Asegurate de tener los 4 microservicios levantados (ej. desde tu IDE) 
# antes de correr este script.

set -e



# URLs base (asumiendo que corren en localhost con sus puertos por defecto)
AUTH_URL="http://localhost:8081/api/v1"
CATALOG_URL="http://localhost:8082/api/v1"
ORDER_URL="http://localhost:8083/api/v1"
DELIVERY_URL="http://localhost:8084/api/v1"

echo "=========================================="
echo "    Iniciando pruebas de API E2E          "
echo "=========================================="

# 1. LOGIN
echo -e "\n[1] Autenticando usuarios..."

ADMIN_TOKEN=$(curl -s -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fastorder.com","password":"password123"}' | jq -r '.token')

CLIENTE_TOKEN=$(curl -s -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@fastorder.com","password":"password123"}' | jq -r '.token')

REPARTIDOR_TOKEN=$(curl -s -X POST "$AUTH_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@fastorder.com","password":"password123"}' | jq -r '.token')

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
    echo "Error: Fallo la autenticacion. Revisa que los servicios esten corriendo y la base de datos poblada."
    exit 1
fi
echo "Tokens obtenidos exitosamente."


# 2. CATALOGO Y COMERCIOS (ADMIN)
echo -e "\n[2] ADMIN crea un comercio..."
COMERCIO_RES=$(curl -s -X POST "$CATALOG_URL/comercios" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Pizza Nostra","categoria":"RESTAURANTE","direccion":"Avenida Siempre Viva 123","abierto":true}')
echo $COMERCIO_RES | jq .

COMERCIO_ID=$(echo $COMERCIO_RES | jq -r '.id')

echo -e "\n[3] ADMIN crea un producto en el comercio $COMERCIO_ID..."
PRODUCTO_RES=$(curl -s -X POST "$CATALOG_URL/comercios/$COMERCIO_ID/productos" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Pizza Familiar","precio":80.00,"stock":50,"disponible":true}')
echo $PRODUCTO_RES | jq .

PRODUCTO_ID=$(echo $PRODUCTO_RES | jq -r '.id')


# 3. CLIENTE EXPLORA Y COMPRA
echo -e "\n[4] CLIENTE consulta catálogo de comercios..."
curl -s -X GET "$CATALOG_URL/comercios" \
  -H "Authorization: Bearer $CLIENTE_TOKEN" | jq .

echo -e "\n[5] CLIENTE realiza un pedido del producto $PRODUCTO_ID..."
PEDIDO_RES=$(curl -s -X POST "$ORDER_URL/pedidos" \
  -H "Authorization: Bearer $CLIENTE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"productos\":[{\"productoId\":$PRODUCTO_ID,\"cantidad\":2}]}")
echo $PEDIDO_RES | jq .

PEDIDO_ID=$(echo $PEDIDO_RES | jq -r '.id')


# 4. DELIVERY - REPARTIDOR
echo -e "\n[6] REPARTIDOR consulta pedidos disponibles..."
curl -s -X GET "$DELIVERY_URL/pedidos/disponibles" \
  -H "Authorization: Bearer $REPARTIDOR_TOKEN" | jq .

echo -e "\n[7] REPARTIDOR toma el pedido $PEDIDO_ID (Cambia a EN_PREPARACION)..."
curl -s -X PATCH "$DELIVERY_URL/pedidos/$PEDIDO_ID/estado" \
  -H "Authorization: Bearer $REPARTIDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"EN_PREPARACION"}' | jq .

echo -e "\n[8] REPARTIDOR actualiza estado a EN_CAMINO..."
curl -s -X PATCH "$DELIVERY_URL/pedidos/$PEDIDO_ID/estado" \
  -H "Authorization: Bearer $REPARTIDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"EN_CAMINO"}' | jq .

echo -e "\n[9] CLIENTE revisa sus pedidos..."
curl -s -X GET "$ORDER_URL/pedidos/mis-pedidos" \
  -H "Authorization: Bearer $CLIENTE_TOKEN" | jq .


# 5. AUDITORIA
echo -e "\n[10] ADMIN revisa logs de auditoria del sistema..."
curl -s -X GET "$AUTH_URL/auditoria?entidad=COMERCIO" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .


echo -e "\n=========================================="
echo "          Pruebas API Finalizadas           "
echo "=========================================="
