# Documento de Arquitectura - Entregable 3

## 1. Nombre del Proyecto/Sistema

MinimarketPOS - Sistema Web de Gestion de Inventario.

## 2. Institucion o Empresa

Proyecto academico para la asignatura Diseño e Implementacion de soluciones TI basadas en patrones de arquitectura de software.

## 3. Ficha Tecnica del Documento

| Campo | Valor |
|-------|-------|
| Version | 3.0 |
| Fecha | 2026-06-15 |
| Elaborado por | Equipo del proyecto |
| Cambio | Implementacion de arquitectura MVC con N capas, FTP, Mirror y DataWarehouse |

## 4. Introduccion

### 4.1 Descripcion del Sistema

MinimarketPOS permite registrar, actualizar, eliminar y consultar articulos de inventario. El sistema usa una aplicacion Web MVC como punto de entrada, SQL Server como base operacional, un servidor FTP para intercambio de archivos, una base Mirror para replica intermedia y un DataWarehouse para analisis historico.

### 4.2 Objetivo

Implementar una solucion distribuida basada en MVC y N capas, incorporando transferencia FTP, replicacion Mirror y explotacion analitica mediante DataWarehouse.

### 4.3 Alcance

El alcance incluye CRUD web de articulos, exportacion FTP, sincronizacion Mirror, carga ETL al DataWarehouse y visualizacion OLAP tipo CrossTab.

### 4.4 Restricciones

La infraestructura se simula localmente con Docker. SQL Server, FTP, Mirror y DataWarehouse pueden residir en el mismo equipo durante la demostracion, pero se documentan como capas separadas.

### 4.5 Usuarios del Sistema

Los usuarios principales son operadores de inventario, administradores del minimarket y usuarios de consulta analitica.

## 5. Especificaciones y Requisitos

### 5.1 Especificaciones Tecnicas

| Elemento | Tecnologia |
|----------|------------|
| Lenguaje | Kotlin JVM |
| Framework MVC | Spring Boot MVC |
| Vista | Thymeleaf, HTML, CSS |
| Base de datos | Microsoft SQL Server 2022 |
| Acceso a datos | JDBC |
| FTP | Apache Commons Net + vsftpd Docker |
| Contenedores | Docker Compose |
| DataWarehouse | Modelo estrella en SQL Server |

### 5.2 Requisitos Funcionales

- Registrar articulos.
- Consultar inventario activo.
- Actualizar articulos.
- Eliminar articulos.
- Exportar datos operacionales hacia FTP.
- Descargar archivo desde FTP y sincronizar Mirror.
- Generar DataWarehouse desde Mirror.
- Crear y visualizar consulta CrossTab.

### 5.3 Atributos y Metricas

| Atributo | Implementacion |
|----------|----------------|
| Disponibilidad | Servicios Docker independientes para SQL Server y FTP |
| Seguridad | Sentencias parametrizadas JDBC y credenciales por variables de entorno |
| Rendimiento | Indices en tablas operacionales, Mirror y DataWarehouse |
| Robustez | ETL y Mirror con control transaccional |
| Escalabilidad | Separacion por capas y servicios ejecutables independientes |
| Portabilidad | Docker Compose y JDK 17 |
| Confiabilidad | Mirror desacopla la carga analitica de la base operacional |
| Mantenibilidad | Paquetes separados por responsabilidad |
| Compatibilidad | SQL Server 2022 y JVM 17 |
| Reproducibilidad | Scripts SQL idempotentes y comandos documentados |
| Usabilidad | Interfaz Web MVC accesible desde navegador |

## 6. Arquitectura del Sistema

### 6.1 Descripcion de la Arquitectura

La solucion implementa arquitectura Web MVC con N capas. La vista Thymeleaf recibe peticiones del navegador, el controlador MVC coordina la operacion, el servicio valida reglas de negocio, el repositorio ejecuta SQL parametrizado y la base operacional persiste los datos. Posteriormente, un proceso de exportacion publica la data en FTP, otro proceso actualiza Mirror y el ETL carga el DataWarehouse.

```text
Vista Web -> Controller -> Service -> Repository -> MinimarketDB
MinimarketDB -> ExportarFTP -> FTP Docker -> ActualizarMirror -> MinimarketMirror
MinimarketMirror -> GenerarDatawareHouse -> MinimarketDW -> OLAP
```

### 6.2 Tecnologias Utilizadas

Kotlin JVM, Spring Boot MVC, Thymeleaf, SQL Server, JDBC, Apache Commons Net, Docker Compose y Gradle.

### 6.3 Estructura y Componentes Funcionales

| Capa | Paquete/Ruta | Funcion |
|------|--------------|---------|
| Vista | `templates/articulos/index.html` | Interfaz de usuario web |
| Controlador | `minimarket.web` | Recibe solicitudes HTTP |
| Servicio | `minimarket.service` | Valida reglas de negocio |
| Datos | `minimarket.data.persistence` | Ejecuta operaciones JDBC |
| FTP | `minimarket.ftp` | Publica CSV en servidor FTP |
| Mirror | `minimarket.mirror` | Sincroniza replica desde FTP |
| ETL | `minimarket.etl` | Carga DataWarehouse |
| OLAP | `minimarket.olap` | Crea y consulta CrossTab |

### 6.4 Capa de Datos

La capa operacional usa `MinimarketDB.Articulos`. La capa Mirror usa `MinimarketMirror.ArticulosMirror` con columna `Activo` y `FechaSincronizacion`. La capa analitica usa `MinimarketDW` con `Dim_Articulo`, `Dim_Tiempo` y `Fact_Inventario`.

## 7. Vista de Desarrollo

### 7.1 Lenguaje de Programacion Usado

Kotlin sobre JVM 17.

### 7.2 Framework

Spring Boot MVC para la aplicacion web.

### 7.3 Librerias

- `spring-boot-starter-web`
- `spring-boot-starter-thymeleaf`
- `mssql-jdbc`
- `commons-net`

### 7.4 Convenciones de Desarrollo

Los paquetes separan responsabilidades por capa. Las operaciones SQL usan `PreparedStatement`. Los procesos de integracion se ejecutan como tareas Gradle independientes.

## 8. Capa de Despliegue

La infraestructura se levanta con Docker Compose. SQL Server usa el puerto `1433`. FTP usa el puerto `21` y el rango pasivo `21100-21110`. La aplicacion Web MVC se ejecuta en `http://localhost:8080/articulos`.

## 9. Integracion y Referencias Externas

El sistema se integra con SQL Server mediante JDBC y con FTP mediante Apache Commons Net. El FTP Docker simula un servidor externo de intercambio de archivos.

## 10. Capa de Seguridad

Las credenciales se configuran mediante variables de entorno. El acceso SQL usa sentencias parametrizadas. El servidor FTP queda aislado en Docker para la demostracion local.

## 11. Conclusiones

El Entregable 3 transforma el sistema en una solucion distribuida con arquitectura MVC y N capas. La incorporacion de FTP y Mirror desacopla la base operacional del DataWarehouse, mejora la trazabilidad del flujo de datos y permite demostrar integracion entre aplicacion, transferencia, replica y analitica.
