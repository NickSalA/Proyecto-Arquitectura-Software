#!/bin/bash
# ===========================================
# Test Suite para WebService SOAP de Margen
# Uso: bash test-api-soap.sh
# Requisitos: curl, python3 (para formatear)
# ===========================================
BASE="http://localhost:8080/ws"
PASS=0
FAIL=0
REPORT="test-api-soap-report.html"

declare -a TEST_NAMES=()
declare -a TEST_RESULTS=()
declare -a TEST_DETAILS=()

echo ""
echo "╔══════════════════════════════════════╗"
echo "║  MinimarketPOS - SOAP Margen Suite   ║"
echo "╚══════════════════════════════════════╝"
echo ""

soap_call_margen() {
    local desc="$1" body="$2" expected="$3"
    local payload
    payload=$(cat <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:mar="http://minimarket.plugin/soap/margen">
   <soapenv:Header/>
   <soapenv:Body>
      $body
   </soapenv:Body>
</soapenv:Envelope>
EOF
)
    local response
    response=$(curl -s -X POST "$BASE" \
        -H "Content-Type: text/xml;charset=UTF-8" \
        -H "SOAPAction: \"\"" \
        -d "$payload" 2>/dev/null)
    if echo "$response" | grep -q "$expected"; then
        echo "  PASS | $desc"
        ((PASS++))
        TEST_NAMES+=("$desc")
        TEST_RESULTS+=("PASS")
        TEST_DETAILS+=("Esperado: $expected | OK")
    else
        echo "  FAIL | $desc"
        echo "        Esperado: $expected"
        echo "        Obtenido: $(echo "$response" | head -c 300)"
        ((FAIL++))
        TEST_NAMES+=("$desc")
        TEST_RESULTS+=("FAIL")
        TEST_DETAILS+=("Esperado: $expected | Obtenido: $(echo "$response" | sed 's/</\&lt;/g' | head -c 200)")
    fi
}

soap_call_margen "Calcular margen (compra 10, venta 15, 50 unid)" \
    '<mar:CalcularMargenRequest>
       <mar:precioCompra>10.0</mar:precioCompra>
       <mar:precioVenta>15.0</mar:precioVenta>
       <mar:cantidad>50</mar:cantidad>
    </mar:CalcularMargenRequest>' \
    'CalcularMargenResponse'

soap_call_margen "Margen: ganancia unitaria 5.0" \
    '<mar:CalcularMargenRequest>
       <mar:precioCompra>10.0</mar:precioCompra>
       <mar:precioVenta>15.0</mar:precioVenta>
       <mar:cantidad>1</mar:cantidad>
    </mar:CalcularMargenRequest>' \
    '5.0'

soap_call_margen "Margen: ganancia total 250" \
    '<mar:CalcularMargenRequest>
       <mar:precioCompra>10.0</mar:precioCompra>
       <mar:precioVenta>15.0</mar:precioVenta>
       <mar:cantidad>50</mar:cantidad>
    </mar:CalcularMargenRequest>' \
    '250.0'

soap_call_margen "Margen: precio venta 0 (sin margen)" \
    '<mar:CalcularMargenRequest>
       <mar:precioCompra>10.0</mar:precioCompra>
       <mar:precioVenta>0.0</mar:precioVenta>
       <mar:cantidad>1</mar:cantidad>
    </mar:CalcularMargenRequest>' \
    'CalcularMargenResponse'

soap_call_margen "Margen: precio compra 0 (margen infinito)" \
    '<mar:CalcularMargenRequest>
       <mar:precioCompra>0.0</mar:precioCompra>
       <mar:precioVenta>20.0</mar:precioVenta>
       <mar:cantidad>5</mar:cantidad>
    </mar:CalcularMargenRequest>' \
    'CalcularMargenResponse'

echo ""
echo "╔══════════════════════════════════════╗"
echo "║  Resultados: $PASS passed, $FAIL failed                ║"
echo "╚══════════════════════════════════════╝"

TOTAL=$((PASS + FAIL))
STATUS_ICON="✅"
STATUS_COLOR="#22c55e"
STATUS_TEXT="Todos los tests pasaron"
if [ "$FAIL" -gt 0 ]; then
    STATUS_ICON="❌"
    STATUS_COLOR="#ef4444"
    STATUS_TEXT="$FAIL test(s) fallaron"
fi

ROWS=""
for i in "${!TEST_NAMES[@]}"; do
    ROW_CLASS="pass-row"
    BADGE="✅"
    BADGE_BG="#22c55e"
    if [ "${TEST_RESULTS[$i]}" = "FAIL" ]; then
        ROW_CLASS="fail-row"
        BADGE="❌"
        BADGE_BG="#ef4444"
    fi
    ESCAPED_DETAIL=$(echo "${TEST_DETAILS[$i]}" | sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g')
    ROWS+="<tr class=\"$ROW_CLASS\">
        <td><span class=\"badge\" style=\"background:$BADGE_BG\">$BADGE</span></td>
        <td>${TEST_NAMES[$i]}</td>
        <td><code>$ESCAPED_DETAIL</code></td>
    </tr>"
done

cat > "$REPORT" <<HTML
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MinimarketPOS - Reporte de Tests SOAP</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #0f172a; color: #e2e8f0; padding: 2rem; line-height: 1.6;
        }
        .container { max-width: 900px; margin: 0 auto; }
        h1 { font-size: 1.8rem; color: #f8fafc; margin-bottom: 0.25rem; }
        .subtitle { color: #94a3b8; margin-bottom: 2rem; font-size: 0.9rem; }
        .summary {
            background: #1e293b; border-radius: 12px; padding: 1.5rem 2rem;
            margin-bottom: 2rem; display: flex; align-items: center; gap: 1.5rem;
            border: 1px solid #334155;
        }
        .summary-icon { font-size: 2.5rem; }
        .summary-info h2 { font-size: 1.2rem; color: #f1f5f9; }
        .summary-info p { color: #94a3b8; font-size: 0.9rem; }
        .stats { display: flex; gap: 1.5rem; margin-left: auto; }
        .stat { text-align: center; }
        .stat-value { font-size: 1.8rem; font-weight: 700; }
        .stat-label { font-size: 0.75rem; text-transform: uppercase; color: #64748b; letter-spacing: 0.05em; }
        .stat-pass .stat-value { color: #22c55e; }
        .stat-fail .stat-value { color: #ef4444; }
        .stat-total .stat-value { color: #f8fafc; }
        table {
            width: 100%; border-collapse: collapse; background: #1e293b;
            border-radius: 12px; overflow: hidden; border: 1px solid #334155;
        }
        th {
            background: #334155; padding: 0.75rem 1rem; text-align: left;
            font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: #94a3b8;
        }
        td { padding: 0.75rem 1rem; border-top: 1px solid #334155; font-size: 0.9rem; }
        .fail-row { background: rgba(239, 68, 68, 0.08); }
        .badge { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 50%; font-size: 0.85rem; }
        code { color: #facc15; font-size: 0.8rem; word-break: break-all; }
        .footer { text-align: center; margin-top: 2rem; color: #64748b; font-size: 0.8rem; }
    </style>
</head>
<body>
    <div class="container">
        <h1>MinimarketPOS - WebService SOAP</h1>
        <p class="subtitle">Reporte de pruebas SOAP &mdash; $(date '+%Y-%m-%d %H:%M')</p>

        <div class="summary">
            <div class="summary-icon">$STATUS_ICON</div>
            <div class="summary-info">
                <h2>$STATUS_TEXT</h2>
                <p>$TOTAL pruebas ejecutadas</p>
            </div>
            <div class="stats">
                <div class="stat stat-pass">
                    <div class="stat-value">$PASS</div>
                    <div class="stat-label">Passed</div>
                </div>
                <div class="stat stat-fail">
                    <div class="stat-value">$FAIL</div>
                    <div class="stat-label">Failed</div>
                </div>
                <div class="stat stat-total">
                    <div class="stat-value">$TOTAL</div>
                    <div class="stat-label">Total</div>
                </div>
            </div>
        </div>

        <table>
            <thead>
                <tr><th>Estado</th><th>Prueba</th><th>Detalle</th></tr>
            </thead>
            <tbody>
                $ROWS
            </tbody>
        </table>

        <div class="footer">
            MinimarketPOS &mdash; Proyecto Arquitectura de Software
        </div>
    </div>
</body>
</html>
HTML

echo ""
echo "📄 Reporte HTML generado: $REPORT"
echo "   Abrelo en el navegador: file://$(pwd)/$REPORT"
