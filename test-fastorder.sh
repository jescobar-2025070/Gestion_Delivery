#!/bin/bash
# Script para ejecutar todas las pruebas del proyecto Gestion_Delivery
# Finaliza con error si alguna prueba falla

set -e
set -o pipefail

echo "==========================================================="
echo "   Iniciando bateria de pruebas - Gestion_Delivery         "
echo "==========================================================="

echo "Ejecutando pruebas del proyecto..."
mvn clean test

echo "==========================================================="
echo "        TODAS LAS PRUEBAS PASARON EXITOSAMENTE             "
echo "==========================================================="
