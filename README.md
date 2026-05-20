# MinimarketPOS — Sistema de Gestión de Inventario Local

> **Arquitectura Unitaria con Servidor de Datos Centralizado**  
> Kotlin (JVM) · RandomAccessFile · SQL Server

---

## Descripción

Sistema de punto de venta monolítico para cadena de minimarkets. Cada estación gestiona su inventario local usando archivos binarios de acceso aleatorio (56 bytes/registro) con un índice `HashMap` en RAM para búsquedas O(1). Al finalizar la jornada, los datos se sincronizan al servidor central SQL Server.

## Arquitectura

```
┌─────────────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│      Main.kt            │     │     Send.kt          │     │   Update.kt     │
│  (CRUD Inventario)      │────▶│  (Transferencia)     │────▶│ (Consolidación) │
│  RandomAccessFile       │     │  Files.copy()        │     │  JDBC + MERGE   │
│  + HashMap Index        │     │  Carpeta compartida  │     │  SQL Server     │
└─────────────────────────┘     └──────────────────────┘     └─────────────────┘
        │                              │                            │
        ▼                              ▼                            ▼
  data/articulos.dat          shared/DATOS/articulos.dat      MinimarketDB
```

## Estructura del Proyecto

```
├── build.gradle.kts              # Build system con Kotlin JVM
├── settings.gradle.kts
├── sql/
│   └── create_database.sql       # Script DDL para SQL Server
├── src/main/kotlin/minimarket/
│   ├── model/
│   │   └── Articulo.kt           # Data class (56 bytes fijos)
│   ├── persistence/
│   │   └── ArchivoArticulos.kt   # I/O + índice HashMap
│   ├── Main.kt                   # App CRUD interactiva
│   ├── Send.kt                   # Transferencia de archivos
│   └── Update.kt                 # Sincronización SQL Server
├── data/                          # Archivo binario (runtime)
└── shared/DATOS/                  # Simulación carpeta de red
```

## Requisitos

- **JDK 17+**
- **Microsoft SQL Server** (para el componente Update)
- **Gradle** (incluido via wrapper)

## Ejecución

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación principal (CRUD)
./gradlew runMain

# Transferir datos a carpeta compartida
./gradlew runSend

# Sincronizar con SQL Server
./gradlew runUpdate
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
| Seguridad      | Autenticación integrada Windows para JDBC                        |
| Integridad     | Eliminación lógica (ID = -1), sin corrupción de bloques         |