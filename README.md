# MinimarketPOS — Sistema de Gestion de Inventario Local

> **Arquitectura Unitaria con Servidor de Datos Centralizado**  
> Kotlin (JVM) · RandomAccessFile · SQL Server

---

## Descripción

Sistema de punto de venta monolitico para cadena de minimarkets. Cada estacion gestiona su inventario local usando archivos binarios de acceso aleatorio (56 bytes/registro) con un indice `HashMap` en RAM para busquedas O(1). Al finalizar la jornada, los datos se transfieren a una carpeta compartida y luego se consolidan en SQL Server.

Este repositorio corresponde al **1er entregable: Modelo de Arquitectura Unitaria con Servidor de Datos**.

## Arquitectura

```
┌─────────────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│      Main.kt            │     │     Send.kt          │     │   Update.kt     │
│  (CRUD Inventario)      │────▶│  (Transferencia)     │────▶│ (Consolidación) │
│  RandomAccessFile       │     │  Files.copy()        │     │  JDBC SQL       │
│  + HashMap Index        │     │  Carpeta compartida  │     │  SQL Server     │
└─────────────────────────┘     └──────────────────────┘     └─────────────────┘
        │                              │                            │
        ▼                              ▼                            ▼
  data/articulos.dat    \\MATHIPC\Users\User\Desktop\DATOS\articulos.dat    MinimarketDB
```

La carpeta compartida configurada para la transferencia es:

```text
\\MATHIPC\Users\User\Desktop\DATOS
```

## Estructura del Proyecto

```
├── build.gradle.kts              # Build system con Kotlin JVM
├── settings.gradle.kts
├── sql/
│   └── create_database.sql       # Script DDL para SQL Server
├── src/main/kotlin/minimarket/
│   ├── application/
│   │   ├── AppConfig.kt          # Configuracion compartida de rutas y JDBC
│   │   ├── Main.kt               # App CRUD interactiva
│   │   ├── Send.kt               # Transferencia de archivos
│   │   └── Update.kt             # Sincronizacion SQL Server
│   └── data/
│       ├── model/Articulo.kt     # Data class (56 bytes fijos)
│       └── persistence/ArchivoArticulos.kt
└── data/                          # Archivo binario local: articulos.dat
```

## Requisitos

- **JDK 17+**
- **Docker Desktop** (para levantar SQL Server)
- **Gradle** (incluido via wrapper)

Si Windows usa Java 8 por defecto, configurar `JAVA_HOME` hacia un JDK 17 o superior antes de compilar. Por ejemplo, usando el JDK incluido en IntelliJ:

```powershell
$env:JAVA_HOME="C:\Users\User\AppData\Local\Programs\IntelliJ IDEA Ultimate\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Configuracion

El componente `Update` se conecta al SQL Server levantado por Docker usando:

```text
jdbc:sqlserver://localhost:1433;databaseName=MinimarketDB;user=sa;password=DreamTeam_26;trustServerCertificate=true
```

La ruta local de datos de la aplicacion es:

```text
data/articulos.dat
```

La ruta compartida por defecto, validada en Windows, es:

```text
\\MATHIPC\Users\User\Desktop\DATOS\articulos.dat
```

Si se necesita ejecutar en otra maquina o sistema operativo, se puede cambiar la ruta compartida con la variable de entorno `SHARED_DATA_PATH` sin modificar el codigo.

En Windows PowerShell:

```powershell
$env:SHARED_DATA_PATH="\\MATHIPC\Users\User\Desktop\DATOS\articulos.dat"
```

En Linux Mint:

```bash
export SHARED_DATA_PATH="/mnt/datos/articulos.dat"
```

## Ejecución

### 1. Levantar SQL Server

```powershell
docker compose up -d
```

### 2. Crear la base de datos

Ejecutar el script `sql/create_database.sql` contra el contenedor SQL Server:

```powershell
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -i /sql/create_database.sql
```

Si la imagen tiene `sqlcmd` en la ruta antigua, usar:

```powershell
docker exec -it minimarket_sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -i /sql/create_database.sql
```

### 3. Compilar el proyecto

```powershell
.\gradlew.bat build
```

### 4. Ejecutar la aplicacion principal

Permite registrar, consultar, modificar, eliminar y listar articulos en el archivo local `data/articulos.dat`.

```powershell
.\gradlew.bat runMain
```

### 5. Transferir datos con Send

`Send` copia `data/articulos.dat` hacia la carpeta compartida configurada. En Windows, por defecto usa `\\MATHIPC\Users\User\Desktop\DATOS`.

```powershell
.\gradlew.bat runSend
```

### 6. Consolidar datos con Update

`Update` lee el archivo desde la carpeta compartida y sincroniza la tabla `Articulos` en `MinimarketDB`. Inserta nuevos articulos, actualiza los existentes y elimina de SQL Server los registros que ya no esten activos en el archivo local.

```powershell
.\gradlew.bat runUpdate
```

## Flujo de validacion del primer entregable

1. Levantar SQL Server con Docker.
2. Crear la base `MinimarketDB` y la tabla `Articulos`.
3. Ejecutar `runMain` y registrar articulos.
4. Ejecutar `runSend` para transferir el archivo local a la carpeta compartida.
5. Ejecutar `runUpdate` para consolidar los datos en SQL Server, incluyendo eliminaciones logicas realizadas localmente.
6. Consultar la tabla `Articulos` para verificar que los registros fueron insertados o actualizados.

## Ejecucion En Linux Mint

En Linux Mint se puede ejecutar el mismo proyecto, pero la carpeta compartida de Windows debe montarse como una ruta local usando SMB/CIFS. La ruta UNC de Windows `\\MATHIPC\Users\User\Desktop\DATOS` se representa como `//MATHIPC/Users/User/Desktop/DATOS` al montarla.

### 1. Instalar dependencias

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk docker.io docker-compose-plugin cifs-utils
```

### 2. Montar la carpeta compartida

```bash
sudo mkdir -p /mnt/datos
sudo mount -t cifs //MATHIPC/Users/User/Desktop/DATOS /mnt/datos -o username=TU_USUARIO,password=TU_PASSWORD,vers=3.0
```

Si la carpeta compartida no tiene credenciales, probar:

```bash
sudo mount -t cifs //MATHIPC/Users/User/Desktop/DATOS /mnt/datos -o guest,vers=3.0
```

Verificar que el montaje funciona:

```bash
ls -la /mnt/datos
```

### 3. Configurar la ruta para Send y Update

```bash
export SHARED_DATA_PATH="/mnt/datos/articulos.dat"
```

### 4. Levantar SQL Server y crear la base


```bash
docker compose up -d
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -i /sql/create_database.sql
```

### 5. Ejecutar el flujo

```bash
./gradlew build
./gradlew runMain
./gradlew runSend
./gradlew runUpdate
```

### 6. Consultar SQL Server

```bash
docker exec -it minimarket_sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "DreamTeam_26" -C -d MinimarketDB -Q "SELECT * FROM Articulos"
```

## Diseño de Registro (56 bytes)

| Campo       | Tipo Kotlin | Tamaño |
|-------------|-------------|--------|
| ID          | `Int`       | 4 B    |
| Descripción | `String`    | 40 B   |
| Precio      | `Double`    | 8 B    |
| Stock       | `Int`       | 4 B    |

## Atributos de Calidad

| Atributo       | Implementación                                                   |
|----------------|------------------------------------------------------------------|
| Disponibilidad | 99.9% local — servidor central no afecta operatividad           |
| Rendimiento    | Seek directo O(1) via HashMap en RAM                             |
| Seguridad      | Conexion JDBC controlada hacia SQL Server local en Docker        |
| Integridad     | Eliminación lógica (ID = -1), sin corrupción de bloques         |
