# MinimarketPOS - Entregable 3

> Modelo de Arquitectura MVC con N Capas: Aplicacion, Datos, FTP, Mirror y DataWarehouse.

## Descripcion

MinimarketPOS es un sistema de gestion de inventario para una cadena de minimarkets. En este entregable la aplicacion se expone como una solucion Web MVC, mantiene sus datos operacionales en SQL Server, publica una copia por FTP, sincroniza una base Mirror y alimenta un DataWarehouse para consultas OLAP.

## Arquitectura

```text
Cliente Web
   -> Vista Thymeleaf / HTML / CSS
   -> Controller MVC
   -> Service
   -> Repository JDBC
   -> MinimarketDB
   -> ExportarFTP
   -> Servidor FTP Docker
   -> ActualizarMirror
   -> MinimarketMirror
   -> GenerarDatawareHouse
   -> MinimarketDW
   -> CreateCrossTab / ViewCrossTab
```

## Componentes

| Componente | Ruta | Responsabilidad |
|------------|------|-----------------|
| Web MVC | `src/main/kotlin/minimarket/web` | Controladores Spring MVC y arranque web |
| Vistas | `src/main/resources/templates` | Interfaz HTML Thymeleaf |
| Estilos | `src/main/resources/static/css` | Presentacion de la aplicacion web |
| Service | `src/main/kotlin/minimarket/service` | Validaciones y reglas de negocio |
| Repository | `src/main/kotlin/minimarket/data/persistence` | Llamadas JDBC a procedimientos almacenados SQL Server |
| FTP | `src/main/kotlin/minimarket/ftp/ExportarFTP.kt` | Exporta datos a `articulos.csv` en FTP |
| Mirror | `src/main/kotlin/minimarket/mirror/ActualizarMirror.kt` | Descarga CSV y sincroniza `MinimarketMirror` |
| ETL | `src/main/kotlin/minimarket/etl` | Carga `MinimarketDW` desde Mirror |
| OLAP | `src/main/kotlin/minimarket/olap` | Crea y visualiza CrossTab |

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

### 4. Exportar datos al FTP

```bash
./gradlew runExportarFTP
```

Este comando lee `MinimarketDB.Articulos`, genera `articulos.csv` y lo sube al servidor FTP Docker.

### 5. Actualizar Mirror desde FTP

```bash
./gradlew runActualizarMirror
```

Este comando descarga `/articulos.csv` desde FTP y sincroniza `MinimarketMirror.ArticulosMirror`.

### 6. Generar DataWarehouse

```bash
./gradlew runGenerarDatawareHouse
```

El ETL toma como origen los articulos activos de `MinimarketMirror`.

### 7. Crear y visualizar CrossTab

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

El documento tecnico del Entregable 3 esta en:

```text
docs/entregable3-arquitectura.md
```
