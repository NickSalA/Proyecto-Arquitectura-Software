# Sistema de Gestion de Inventario

> Arquitectura MVC con N Capas: Aplicacion Web, API REST, WebService SOAP, FTP, Mirror, DataWarehouse y Plugins independientes (PDF + Seguridad).

## Descripcion

Sistema de gestion de inventario para minimarket. La aplicacion se expone como solucion Web MVC con API REST y WebService SOAP, mantiene sus datos operacionales en SQL Server, publica una copia por FTP, sincroniza una base Mirror, alimenta un DataWarehouse para consultas OLAP e incorpora plugins independientes (exportacion PDF y monitoreo de actividad del operador).

## Arquitectura

```text
Cliente Web (Thymeleaf)
   -> Vista HTML / CSS / JS
   -> Plugins JS (PDF Export + Security Activity)
   -> Controller MVC / API REST
   -> Service
   -> Repository JDBC
   -> MinimarketDB
   -> ExportarFTP -> FTP Docker -> ActualizarMirror -> MinimarketMirror
                                                         |
                                                         v
                                                    GenerarDatawareHouse
                                                         |
                                                         v
                                                    MinimarketDW (Estrella)
                                                         |
                                                         v
                                                    CreateCrossTab / ViewCrossTab (OLAP)

SOAP Client (SoapUI / curl)
   -> /ws/* (MessageDispatcherServlet)
   -> SoapArticleEndpoint
   -> ArticuloService (mismo que el REST y MVC)
   -> Repository JDBC -> MinimarketDB
```

## Componentes

| Componente | Ruta | Responsabilidad |
|------------|------|-----------------|
| Web MVC | `minimarket/web/controller` | Controladores Spring MVC |
| API REST | `minimarket/api/controller` | Endpoints REST JSON `/api/articulos` |
| WebService SOAP | `minimarket/plugin/webservice/soap` | Endpoint SOAP en `/ws/*` con WSDL generado |
| Vistas | `templates/articulos/index.html` | Interfaz HTML Thymeleaf |
| Plugin PDF | `static/js/plugins/table-pdf-export-plugin.js` | Exporta tablas HTML a PDF desde el navegador |
| Plugin Seguridad | `static/js/plugins/security-activity-plugin.js` + `minimarket/plugin/seguridad` | Monitorea actividad del operador y bloquea por inactividad |
| Service | `minimarket/service` | Validaciones y reglas de negocio |
| Repository | `minimarket/data/repository` | Llamadas JDBC a procedimientos almacenados SQL Server |
| FTP | `minimarket/ftp/ExportarFTP.kt` | Exporta datos a `articulos.csv` en FTP |
| Mirror | `minimarket/mirror/ActualizarMirror.kt` | Descarga CSV y sincroniza `MinimarketMirror` |
| ETL | `minimarket/dw/GenerarDatawareHouse.kt` | Carga `MinimarketDW` desde Mirror |
| OLAP | `minimarket/dw/CreateCrossTab.kt` + `ViewCrossTab.kt` | Crea y visualiza CrossTab |

## Requisitos

- JDK 17+
- Docker y Docker Compose
- Gradle o wrapper disponible

Este repositorio incluye una distribucion local en `.gradle-local/gradle-8.10.2`. Si `./gradlew` falla por falta de `gradle/wrapper/gradle-wrapper.jar`, usar:

```bash
JAVA_HOME="$PWD/.jdk-local/jdk-17.0.19+10" PATH="$PWD/.jdk-local/jdk-17.0.19+10/bin:$PATH" .gradle-local/gradle-8.10.2/bin/gradle build
```

## Infraestructura Docker

El archivo `docker-compose.yml` levanta:

- `minimarket_sqlserver`: SQL Server 2022 en el puerto `1433`.
- `minimarket_ftp`: servidor FTP en el puerto `21` con rango pasivo `21100-21110`.

Credenciales por defecto:

```text
SQL Server:
user: sa
password: DreamTeam_26

FTP:
host: localhost
user: minimarket
password: minimarket123
archivo remoto: /articulos.csv
```

## Ejecucion

### 1. Levantar infraestructura

```bash
docker compose up -d
```

### 2. Crear bases de datos

```bash
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -i /sql/create_database.sql
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -i /sql/create_mirror.sql
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -i /sql/create_datawarehouse.sql
```

Si la imagen tiene `sqlcmd` en la ruta antigua, usar `/opt/mssql-tools/bin/sqlcmd`.

El script `sql/create_database.sql` tambien crea los procedimientos almacenados usados por el CRUD:

```text
dbo.sp_Articulo_Listar
dbo.sp_Articulo_Buscar
dbo.sp_Articulo_Insertar
dbo.sp_Articulo_Actualizar
dbo.sp_Articulo_Eliminar
dbo.sp_Articulo_Existe
dbo.sp_Articulo_Cantidad
```

### 3. Ejecutar aplicacion Web MVC

```bash
./gradlew runWeb
```

Comando alternativo si el wrapper no esta disponible:

```bash
JAVA_HOME="$PWD/.jdk-local/jdk-17.0.19+10" PATH="$PWD/.jdk-local/jdk-17.0.19+10/bin:$PATH" .gradle-local/gradle-8.10.2/bin/gradle runWeb
```

Abrir:

```text
http://localhost:8080/articulos
```

La vista de articulos incluye:
- Boton `Exportar PDF`, conectado al plugin `table-pdf-export-plugin.js`.
- Monitoreo de actividad con bloqueo automatico por inactividad (plugin `security-activity-plugin.js`).
- El operador se configura en `config.properties` (`plugin.security.operator`).

### 4. API REST

La API REST esta disponible en `http://localhost:8080/api/articulos`:

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| `GET` | `/api/articulos` | Listar todos los articulos |
| `GET` | `/api/articulos/{id}` | Buscar articulo por ID |
| `POST` | `/api/articulos` | Crear articulo |
| `PUT` | `/api/articulos/{id}` | Actualizar articulo |
| `DELETE` | `/api/articulos/{id}` | Eliminar articulo |

### 5. WebService SOAP

El WebService SOAP esta disponible en `http://localhost:8080/ws`:

| Recurso | URL |
|---------|-----|
| WSDL | `http://localhost:8080/ws/articles.wsdl` |
| Endpoint SOAP | `http://localhost:8080/ws` (POST con XML SOAP) |

Operaciones: `GetAllArticles`, `GetArticleById`, `CreateArticle`, `UpdateArticle`, `DeleteArticle`.

### 6. Pruebas

#### Con SoapUI
```text
File -> Import Project -> minimarket-rest-soapui-project.xml   # Pruebas REST
```

#### Con scripts bash
```bash
./test-api.sh       # 12 pruebas REST + reporte HTML
./test-api-soap.sh  # 10 pruebas SOAP + reporte HTML
```

### 7. Exportar datos al FTP

```bash
./gradlew runExportarFTP
```

Este comando lee `MinimarketDB.Articulos`, genera `articulos.csv` y lo sube al servidor FTP Docker.

### 8. Actualizar Mirror desde FTP

```bash
./gradlew runActualizarMirror
```

Este comando descarga `/articulos.csv` desde FTP y sincroniza `MinimarketMirror.ArticulosMirror`.

### 9. Generar DataWarehouse

```bash
./gradlew runGenerarDatawareHouse
```

El ETL toma como origen los articulos activos de `MinimarketMirror`.

### 10. Crear y visualizar CrossTab

```bash
./gradlew runCreateCrossTab
./gradlew runViewCrossTab
```

## Variables de entorno

| Variable | Valor por defecto |
|----------|-------------------|
| `DB_HOST` | `localhost` en tareas Gradle |
| `DB_PORT` | `1433` |
| `DB_USER` | `sa` |
| `DB_PASSWORD` | `DreamTeam_26` |
| `FTP_HOST` | `localhost` |
| `FTP_PORT` | `21` |
| `FTP_USER` | `minimarket` |
| `FTP_PASSWORD` | `minimarket123` |
| `FTP_REMOTE_FILE` | `/articulos.csv` |

## Scripts SQL

| Script | Base | Uso |
|--------|------|-----|
| `sql/create_database.sql` | `MinimarketDB` | Base operacional y procedimientos almacenados CRUD |
| `sql/create_mirror.sql` | `MinimarketMirror` | Base espejo alimentada por FTP |
| `sql/create_datawarehouse.sql` | `MinimarketDW` | Modelo estrella para analitica |

## Documentacion

| Documento | Contenido |
|-----------|-----------|
| `docs/entregable3-arquitectura.md` | Arquitectura MVC con N capas, FTP, Mirror, DW, OLAP |
| `docs/entregable4-plugin-pdf.md` | Plugin PDF Export (frontend JS) |
| `docs/entregable5-plugin-seguridad.md` | Plugin Seguridad y Monitoreo de Actividad |
| `docs/entregable6-plugins-webservice-soapui.md` | Plugins, WebService REST y SOAP, pruebas SoapUI |
