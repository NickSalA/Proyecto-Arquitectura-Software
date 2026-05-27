#!/bin/bash
# ============================================================
# Script de compilación para MinimarketPOS
# Compila los componentes usando kotlinc directamente
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src/main/kotlin"
OUT_DIR="$PROJECT_DIR/application"
DATA_DIR="$PROJECT_DIR/data"

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "╔══════════════════════════════════════════════════╗"
echo "║     MinimarketPOS - Script de Compilación       ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# Crear directorios de salida
mkdir -p "$OUT_DIR"
mkdir -p "$DATA_DIR"

# Verificar que kotlinc está disponible
if ! command -v kotlinc &> /dev/null; then
    echo -e "${RED}✗ ERROR: kotlinc no encontrado en PATH${NC}"
    echo "  Instale Kotlin: snap install kotlin"
    exit 1
fi

echo -e "${YELLOW}Compilando con $(kotlinc -version 2>&1)${NC}"
echo ""

# ----------------------------------------------------------
# 1. Compilar todos los archivos fuente en un solo JAR
# ----------------------------------------------------------
echo -e "${YELLOW}[1/3] Compilando aplicación principal (Main)...${NC}"
kotlinc \
    "$SRC_DIR/minimarket/data/model/Articulo.kt" \
    "$SRC_DIR/minimarket/data/persistence/ArchivoArticulos.kt" \
    "$SRC_DIR/minimarket/application/Main.kt" \
    -include-runtime \
    -d "$OUT_DIR/Main.jar" \
    2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✓ Main.jar compilado exitosamente${NC}"
else
    echo -e "${RED}   ✗ Error compilando Main.jar${NC}"
    exit 1
fi

# ----------------------------------------------------------
# 2. Compilar componente Send
# ----------------------------------------------------------
echo -e "${YELLOW}[2/3] Compilando componente Send...${NC}"
kotlinc \
    "$SRC_DIR/minimarket/data/model/Articulo.kt" \
    "$SRC_DIR/minimarket/application/AppConfig.kt" \
    "$SRC_DIR/minimarket/application/Send.kt" \
    -include-runtime \
    -d "$OUT_DIR/Send.jar" \
    2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✓ Send.jar compilado exitosamente${NC}"
else
    echo -e "${RED}   ✗ Error compilando Send.jar${NC}"
    exit 1
fi

# ----------------------------------------------------------
# 3. Compilar componente Update
# ----------------------------------------------------------
echo -e "${YELLOW}[3/3] Compilando componente Update...${NC}"

# Verificar si el driver JDBC de SQL Server está disponible
JDBC_JAR="$PROJECT_DIR/lib/mssql-jdbc.jar"
if [ -f "$JDBC_JAR" ]; then
    CLASSPATH_OPT="-classpath $JDBC_JAR"
    echo "   Driver JDBC encontrado: $JDBC_JAR"
else
    CLASSPATH_OPT=""
    echo -e "${YELLOW}   ⚠ Driver JDBC no encontrado en lib/mssql-jdbc.jar${NC}"
    echo "   Update.jar compilará sin driver JDBC."
    echo "   Descargue el driver de: https://learn.microsoft.com/sql/connect/jdbc/"
fi

kotlinc \
    "$SRC_DIR/minimarket/data/model/Articulo.kt" \
    "$SRC_DIR/minimarket/application/AppConfig.kt" \
    "$SRC_DIR/minimarket/application/Update.kt" \
    $CLASSPATH_OPT \
    -include-runtime \
    -d "$OUT_DIR/Update.jar" \
    2>&1

if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✓ Update.jar compilado exitosamente${NC}"
else
    echo -e "${RED}   ✗ Error compilando Update.jar${NC}"
    exit 1
fi

# ----------------------------------------------------------
# Resumen
# ----------------------------------------------------------
echo ""
echo "══════════════════════════════════════════════════"
echo -e "${GREEN}  Compilación completada exitosamente${NC}"
echo "══════════════════════════════════════════════════"
echo ""
echo "  Archivos generados en $OUT_DIR/:"
ls -lh "$OUT_DIR"/*.jar 2>/dev/null | awk '{print "    " $NF " (" $5 ")"}'
echo ""
echo "  Ejecución:"
echo "    kotlin -jar application/Main.jar     # App CRUD"
echo "    kotlin -jar application/Send.jar     # Transferencia"
echo "    kotlin -jar application/Update.jar   # Consolidación SQL"
echo ""
