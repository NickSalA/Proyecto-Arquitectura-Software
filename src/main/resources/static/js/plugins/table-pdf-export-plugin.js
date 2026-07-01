(function () {
    "use strict";

    const exportButtonSelector = "[data-pdf-export]";
    const boundFlag = "pdfPluginBound";

    function init(root) {
        const scope = root || document;
        scope.querySelectorAll(exportButtonSelector).forEach(bindExportButton);
    }

    function bindExportButton(button) {
        if (button.dataset[boundFlag] === "true") {
            return;
        }

        button.dataset[boundFlag] = "true";
        button.addEventListener("click", function () {
            const tableSelector = button.getAttribute("data-table-target");
            const table = tableSelector ? document.querySelector(tableSelector) : null;
            const title = button.getAttribute("data-document-title") || document.title || "Reporte";

            if (!table) {
                window.alert("No se encontro la tabla para exportar.");
                return;
            }

            exportTable(table, title);
        });
    }

    function exportTable(table, title) {
        const printableTable = cloneTableForPdf(table);
        const printWindow = window.open("", "_blank", "width=980,height=720");

        if (!printWindow) {
            window.alert("No se pudo abrir la ventana de exportacion. Permita ventanas emergentes.");
            return false;
        }

        printWindow.opener = null;
        printWindow.document.open();
        printWindow.document.write(buildPrintDocument(title, printableTable));
        printWindow.document.close();

        window.setTimeout(function () {
            printWindow.focus();
            printWindow.print();
        }, 300);

        return true;
    }

    function cloneTableForPdf(table) {
        const clone = table.cloneNode(true);

        clone.removeAttribute("id");
        clone.querySelectorAll("[data-pdf-ignore]").forEach(function (element) {
            element.remove();
        });

        const exportedColumnCount = clone.querySelectorAll("thead th").length;
        clone.querySelectorAll("tbody tr").forEach(function (row) {
            if (row.cells.length === 1 && row.cells[0].hasAttribute("colspan")) {
                row.cells[0].colSpan = Math.max(exportedColumnCount, 1);
            }
        });

        return clone.outerHTML;
    }

    function buildPrintDocument(title, tableHtml) {
        const safeTitle = escapeHtml(title);
        const generatedAt = escapeHtml(new Date().toLocaleString("es-PE"));

        return "<!doctype html>" +
            "<html lang=\"es\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>" + safeTitle + "</title>" +
            "<style>" +
            "@page{size:A4;margin:18mm;}" +
            "*{box-sizing:border-box;}" +
            "body{margin:0;color:#172033;font-family:Arial,Helvetica,sans-serif;}" +
            "header{margin-bottom:22px;padding-bottom:14px;border-bottom:2px solid #dbe3ef;}" +
            "h1{margin:0;font-size:24px;letter-spacing:-.02em;}" +
            "p{margin:8px 0 0;color:#64748b;font-size:12px;}" +
            "table{width:100%;border-collapse:collapse;font-size:12px;}" +
            "th,td{padding:10px 9px;border:1px solid #dbe3ef;text-align:left;}" +
            "th{color:#172033;background:#eff6ff;text-transform:uppercase;font-size:10px;letter-spacing:.06em;}" +
            "tr:nth-child(even) td{background:#f8fafc;}" +
            ".empty{text-align:center;color:#64748b;}" +
            "footer{margin-top:18px;color:#64748b;font-size:11px;}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<header><h1>" + safeTitle + "</h1><p>Generado: " + generatedAt + "</p></header>" +
            tableHtml +
            "<footer>Plugin PDF independiente - MinimarketPOS</footer>" +
            "</body>" +
            "</html>";
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/\"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    window.MinimarketPdfTablePlugin = {
        init: init,
        exportTable: exportTable
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", function () {
            init(document);
        });
    } else {
        init(document);
    }
})();
