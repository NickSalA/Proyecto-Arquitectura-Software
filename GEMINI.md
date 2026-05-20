# Documentación Técnica de Arquitectura: Fase 1

> **Proyecto:** Sistema de Ventas Distribuido (Minimarket)
> **Fase:** Arquitectura Unitaria con Servidor de Datos
> **Fecha de Entrega:** 20 de Mayo de 2026

---

## 1. Información General

| Campo | Descripción |
| :--- | :--- |
| **Nombre del Sistema** | Sistema de Gestión de Inventario Local (MinimarketPOS) |
| **Arquitectura** | Unitaria con Servidor de Datos Centralizado |
| **Lenguaje Base** | Kotlin (JVM) |
| **Motor de Base de Datos** | Microsoft SQL Server |

---

## 2. Introducción

El presente documento detalla el diseño e implementación del primer entregable del sistema corporativo para una cadena de minimarkets. El objetivo principal es proveer a cada punto de venta de una aplicación monolítica autónoma capaz de gestionar su propio inventario local durante la jornada laboral, mitigando la dependencia de una conexión constante a internet. Al finalizar el día, el sistema cuenta con servicios de sincronización para consolidar la información en un servidor centralizado.

---

## 3. Especificaciones y Atributos de Calidad

| Atributo | Métrica / Estrategia de Implementación |
| :--- | :--- |
| **Disponibilidad** | 99.9% a nivel de punto de venta. La caída del servidor central no afecta la operatividad local gracias al uso de archivos de acceso aleatorio. |
| **Rendimiento** | Operaciones CRUD en disco inferiores a 50ms mediante el uso de un índice de punteros en memoria RAM (`HashMap`). |
| **Seguridad** | Autenticación integrada de Windows para la conexión JDBC hacia SQL Server, evitando credenciales en código duro. |
| **Integridad** | Uso del enfoque de "Eliminación Lógica" (asignación del identificador `-1`) para evitar la corrupción de bloques de bytes en el disco físico. |

---

## 4. Arquitectura del Sistema

La solución se enmarca en una **Arquitectura Unitaria**, donde la capa de presentación, lógica de negocio y persistencia de datos conviven en la misma máquina física (el punto de venta).

El modelo se complementa con dos componentes de integración:

* **Capa Local (Aplicación):** Gestiona el mantenimiento mediante archivos binarios de acceso aleatorio con organización indexada.
* **Componente de Transferencia (`Send.EXE`):** Extrae el archivo local y lo replica en una carpeta compartida de red delegando la carga de E/S al sistema operativo.
* **Componente de Consolidación (`Update.EXE`):** Servicio backend que procesa el archivo de red y sincroniza las diferencias hacia el motor relacional mediante sentencias condicionales (`MERGE` / `IF EXISTS`).

---

## 5. Diseño de Datos y Persistencia

Para garantizar la lectura posicional directa sin sobrecargar la memoria (`seek()`), se diseñó un registro de longitud estrictamente fija de **56 bytes** por artículo.

| Campo | Tipo de Dato (Kotlin) | Tipo de Dato (SQL Server) | Tamaño en Disco |
| :--- | :--- | :--- | :--- |
| **ID** | `Int` | `INT (PK)` | 4 bytes |
| **Descripción** | `String` (20 caracteres fijos) | `VARCHAR(20)` | 40 bytes |
| **Precio** | `Double` | `DECIMAL(10,2)` | 8 bytes |
| **Stock** | `Int` | `INT` | 4 bytes |

---

## 6. Vista de Desarrollo y Convenciones

El código fuente sigue un paradigma imperativo clásico sobre la sintaxis de Kotlin para facilitar el mantenimiento y control de recursos de hardware.

* **Estructuras de Datos:** Se utilizan `data classes` para la definición inmutable de entidades.
* **Gestión de I/O:** Manejo explícito de la clase `java.io.RandomAccessFile` con bloques `try-finally` para asegurar el cierre de flujos de bytes y prevenir fugas de memoria.
* **Rutas Dinámicas:** Uso de `InetAddress.getLocalHost().hostName` para la resolución de nombres de red en tiempo de ejecución, eliminando dependencias de rutas estáticas forzadas.

---

## 7. Guía de Despliegue y Ejecución

La topología de directorios requerida para el funcionamiento del sistema exige la siguiente configuración en la máquina cliente e infraestructura de red:

1. Crear el directorio principal `C:\PROYECTO` en la estación de trabajo.
2. Generar las subcarpetas `C:\PROYECTO\APPLICATION` y `C:\PROYECTO\DATA`.
3. Compilar `Main.kt` y `Send.kt` y alojar los ejecutables resultantes en la carpeta `APPLICATION`.
4. Crear una carpeta llamada `DATOS` en el servidor (o escritorio local) y habilitar el uso compartido de red en Windows.
5. Ejecutar el script SQL proporcionado para instanciar la base de datos `MinimarketDB`.
6. Iniciar la aplicación cliente, registrar artículos y cerrar el programa.
7. Ejecutar el componente `Send.EXE` para transferir los datos a la carpeta compartida.
8. Ejecutar el componente `Update.EXE` en el servidor para reflejar los cambios en SQL Server.
