# Documento de Arquitectura - Entregable 4

## 1. Nombre del Componente

Plugin independiente de exportacion de tablas a PDF para MinimarketPOS.

## 2. Objetivo

Agregar una funcionalidad opcional que permita exportar la tabla de inventario activo a un documento PDF desde la interfaz web, sin modificar la logica principal del CRUD, los controladores, los servicios ni la capa de datos.

## 3. Alcance

El alcance del plugin incluye:

- Detectar botones configurados con `data-pdf-export`.
- Obtener la tabla HTML indicada por `data-table-target`.
- Excluir columnas o celdas marcadas con `data-pdf-ignore`.
- Construir una vista imprimible de la tabla.
- Abrir el dialogo del navegador para guardar o imprimir como PDF.

No incluye generacion de PDF desde el backend, persistencia de archivos ni cambios en SQL Server.

## 4. Ubicacion de Archivos

| Archivo | Responsabilidad |
|---------|-----------------|
| `src/main/resources/static/js/plugins/table-pdf-export-plugin.js` | Plugin JavaScript independiente para exportar tablas |
| `src/main/resources/templates/articulos/index.html` | Vista host que declara la tabla y el boton de exportacion |
| `src/main/resources/static/css/app.css` | Estilos visuales del boton de exportacion |

## 5. Arquitectura Propuesta

```text
Cliente Web
   -> Vista Thymeleaf / HTML
   -> Tabla de inventario renderizada
   -> Boton Exportar PDF
   -> Plugin table-pdf-export-plugin.js
   -> Ventana imprimible del navegador
   -> Guardar como PDF
```

El plugin funciona como modulo opcional de la interfaz. La aplicacion principal renderiza la tabla normalmente y el plugin solo se activa cuando encuentra un boton con el atributo `data-pdf-export`.

## 6. Contrato de Integracion

La vista se comunica con el plugin mediante atributos HTML, no mediante llamadas al backend.

| Atributo | Uso |
|----------|-----|
| `data-pdf-export` | Marca el boton que dispara la exportacion |
| `data-table-target="#inventory-table"` | Indica que tabla se debe exportar |
| `data-document-title="Inventario activo - MinimarketPOS"` | Define el titulo del documento generado |
| `data-pdf-ignore` | Excluye columnas o celdas que no deben aparecer en el PDF |

Ejemplo aplicado:

```html
<button type="button" data-pdf-export data-table-target="#inventory-table">
    Exportar PDF
</button>

<table id="inventory-table">
    ...
</table>
```

## 7. Independencia del Plugin

El plugin cumple la condicion de independencia porque:

- Esta en una carpeta separada: `static/js/plugins`.
- No modifica datos del sistema.
- No depende de controladores Spring MVC.
- No depende de servicios Kotlin.
- No depende de repositorios JDBC ni SQL Server.
- Si el archivo JavaScript se elimina o no se carga, el CRUD de articulos sigue funcionando.
- La funcionalidad PDF solo existe cuando el usuario presiona el boton de exportacion.

## 8. Flujo de Ejecucion

```text
1. El usuario abre /articulos.
2. Thymeleaf renderiza la tabla con los articulos activos.
3. El navegador carga table-pdf-export-plugin.js.
4. El plugin busca botones con data-pdf-export.
5. El usuario presiona Exportar PDF.
6. El plugin clona la tabla configurada.
7. El plugin elimina celdas marcadas con data-pdf-ignore.
8. El plugin crea una ventana imprimible.
9. El navegador abre el dialogo para imprimir o guardar como PDF.
```

## 9. Relacion con el Patron Microkernel / Plugins

La vista web actua como nucleo o host de la aplicacion. El plugin es una extension conectada por un contrato simple basado en atributos HTML. El nucleo no conoce los detalles internos de la exportacion y el plugin no conoce la logica interna del sistema.

```text
Nucleo: CRUD Web MVC de articulos
Extension: Plugin PDF de tabla
Contrato: data-pdf-export + data-table-target + data-pdf-ignore
```

## 10. Verificacion

Para comprobar la implementacion:

```bash
./gradlew build
./gradlew runWeb
```

Luego abrir:

```text
http://localhost:8080/articulos
```

Pruebas manuales:

- El CRUD debe seguir registrando, editando y eliminando articulos.
- El boton `Exportar PDF` debe abrir una vista imprimible.
- El documento no debe incluir la columna `Acciones`.
- Si se comenta la carga del script del plugin, la pagina debe seguir funcionando sin exportacion PDF.
