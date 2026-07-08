#!/bin/bash
# ============================================================
# Script de compilacion manual (alternativa a Gradle)
# Compila cliente Swing, ETL y consultas OLAP en JARs ejecutables.
# NOTA: La mayoria de las funcionalidades requieren Gradle:
#   ./gradlew runWeb        # Aplicacion Web MVC + REST + SOAP
#   ./gradlew runMain       # Cliente Swing
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src/main/kotlin"
OUT_DIR="$PROJECT_DIR/application"
LIB_DIR="$PROJECT_DIR/lib"
MSSQL_VERSION="13.2.1.jre11"
JDBC_JAR="$LIB_DIR/mssql-jdbc-$MSSQL_VERSION.jar"
JDBC_URL="https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/$MSSQL_VERSION/mssql-jdbc-$MSSQL_VERSION.jar"

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "╔══════════════════════════════════════════════════╗"
echo "║     MinimarketPOS - Script de Compilacion       ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

mkdir -p "$OUT_DIR"
mkdir -p "$LIB_DIR"

if ! command -v kotlinc &> /dev/null; then
    echo -e "${RED}✗ ERROR: kotlinc no encontrado en PATH${NC}"
    echo "  Instale Kotlin o use Gradle con ./gradlew build."
    exit 1
fi

if ! command -v jar &> /dev/null; then
    echo -e "${RED}✗ ERROR: herramienta jar no encontrada en PATH${NC}"
    echo "  Instale un JDK para poder empaquetar JARs ejecutables."
    exit 1
fi

if [ ! -f "$JDBC_JAR" ]; then
    echo -e "${YELLOW}Driver JDBC no encontrado. Descargando mssql-jdbc $MSSQL_VERSION...${NC}"
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}✗ ERROR: curl no encontrado para descargar el driver JDBC${NC}"
        exit 1
    fi
    curl -L --fail -o "$JDBC_JAR" "$JDBC_URL"
fi

echo -e "${YELLOW}Compilando con $(kotlinc -version 2>&1)${NC}"
echo "Driver JDBC: $JDBC_JAR"
echo ""

embed_jdbc_driver() {
    local jar_file="$1"
    local temp_dir
    temp_dir="$(mktemp -d)"

    # Se incrusta mssql-jdbc dentro del JAR para ejecutar sin configurar classpath externo.
    (
        cd "$temp_dir"
        jar xf "$jar_file"
        cp META-INF/MANIFEST.MF APP-MANIFEST.MF
        jar xf "$JDBC_JAR"
        rm -f META-INF/*.SF META-INF/*.RSA META-INF/*.DSA
        jar cfm "$jar_file" APP-MANIFEST.MF .
    )

    rm -rf "$temp_dir"
}

compile_jar() {
    local step="$1"
    local jar_name="$2"
    local description="$3"
    shift 3

    echo -e "${YELLOW}[$step/4] Compilando $description...${NC}"
    if kotlinc "$@" -include-runtime -d "$OUT_DIR/$jar_name.jar" 2>&1; then
        embed_jdbc_driver "$OUT_DIR/$jar_name.jar"
        echo -e "${GREEN}   ✓ $jar_name.jar compilado exitosamente${NC}"
    else
        echo -e "${RED}   ✗ Error compilando $jar_name.jar${NC}"
        exit 1
    fi
    echo ""
}

compile_jar 1 "Main" "aplicacion principal (Main)" \
    "$SRC_DIR/minimarket/data/model/Articulo.kt" \
    "$SRC_DIR/minimarket/config/AppConfig.kt" \
    "$SRC_DIR/minimarket/data/repository/RepositorioArticulosSQL.kt" \
    "$SRC_DIR/minimarket/client/Main.kt"

compile_jar 2 "GenerarDatawareHouse" "proceso ETL (GenerarDatawareHouse)" \
    "$SRC_DIR/minimarket/data/model/Articulo.kt" \
    "$SRC_DIR/minimarket/config/AppConfig.kt" \
    "$SRC_DIR/minimarket/dw/GenerarDatawareHouse.kt"

compile_jar 3 "CreateCrossTab" "creacion de vista OLAP (CreateCrossTab)" \
    "$SRC_DIR/minimarket/config/AppConfig.kt" \
    "$SRC_DIR/minimarket/dw/CreateCrossTab.kt"

compile_jar 4 "ViewCrossTab" "visualizacion OLAP (ViewCrossTab)" \
    "$SRC_DIR/minimarket/config/AppConfig.kt" \
    "$SRC_DIR/minimarket/dw/ViewCrossTab.kt"

echo "══════════════════════════════════════════════════"
echo -e "${GREEN}  Compilacion completada exitosamente${NC}"
echo "══════════════════════════════════════════════════"
echo ""
echo "  Archivos generados en $OUT_DIR/:"
for jar_file in "$OUT_DIR"/*.jar; do
    if [ -f "$jar_file" ]; then
        echo "    $jar_file"
    fi
done
echo ""
echo "  Ejecucion:"
echo "    java -jar application/Main.jar"
echo "    java -jar application/GenerarDatawareHouse.jar"
echo "    java -jar application/CreateCrossTab.jar"
echo "    java -jar application/ViewCrossTab.jar"
echo ""
echo "  Ejecucion apuntando a otro servidor SQL Server:"
echo "    MacOS/Linux: DB_HOST=192.168.1.50 java -jar application/Main.jar"
echo "    Windows CMD: set DB_HOST=192.168.1.50 && java -jar application\\Main.jar"
echo "    Windows PowerShell: \$env:DB_HOST=\"192.168.1.50\"; java -jar application\\Main.jar"
echo ""
echo "  Variables disponibles: DB_HOST, DB_PORT, DB_USER, DB_PASSWORD"
echo "  Valores por defecto: MATHIPC, 1433, sa, DreamTeam_26"
echo ""
