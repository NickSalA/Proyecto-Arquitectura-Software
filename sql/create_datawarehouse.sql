-- ============================================================
-- Script DDL: MinimarketDW (Datawarehouse)
-- Motor: Microsoft SQL Server
-- Sistema: MinimarketPOS - Entregable 3
-- Modelo: Estrella (Star Schema)
-- ============================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MinimarketDW')
BEGIN
    CREATE DATABASE MinimarketDW;
END
GO

USE MinimarketDW;
GO

-- ============================================================
-- Dimensión: Dim_Articulo
-- Almacena información descriptiva de cada artículo.
-- ArticuloKey: clave subrogada auto-incrementada.
-- ArticuloID: clave de negocio (ID original del artículo).
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Dim_Articulo')
BEGIN
    CREATE TABLE Dim_Articulo (
        ArticuloKey   INT IDENTITY(1,1) PRIMARY KEY,
        ArticuloID    INT NOT NULL,
        Descripcion   VARCHAR(50) NOT NULL,
        Precio        DECIMAL(10,2) NOT NULL
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Dim_Articulo_ArticuloID')
BEGIN
    CREATE UNIQUE INDEX IX_Dim_Articulo_ArticuloID
    ON Dim_Articulo(ArticuloID);
END
GO

-- ============================================================
-- Dimensión: Dim_Tiempo
-- Almacena fechas únicas para análisis histórico.
-- Cada ejecución del ETL genera una nueva entrada.
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Dim_Tiempo')
BEGIN
    CREATE TABLE Dim_Tiempo (
        TiempoKey  INT IDENTITY(1,1) PRIMARY KEY,
        Fecha      DATE NOT NULL,
        Anio       INT NOT NULL,
        Mes        INT NOT NULL,
        Dia        INT NOT NULL
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Dim_Tiempo_Fecha')
BEGIN
    CREATE UNIQUE INDEX IX_Dim_Tiempo_Fecha
    ON Dim_Tiempo(Fecha);
END
GO

-- ============================================================
-- Tabla de Hechos: Fact_Inventario
-- Cruza dimensiones y almacena métricas de inventario.
-- Stock y PrecioActual son métricas volátiles que
-- se actualizan en cada ejecución ETL.
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Fact_Inventario')
BEGIN
    CREATE TABLE Fact_Inventario (
        TiempoKey      INT NOT NULL,
        ArticuloKey    INT NOT NULL,
        Stock          INT NOT NULL,
        PrecioActual   DECIMAL(10,2) NOT NULL,
        CONSTRAINT FK_Fact_Inventario_Tiempo
            FOREIGN KEY (TiempoKey) REFERENCES Dim_Tiempo(TiempoKey),
        CONSTRAINT FK_Fact_Inventario_Articulo
            FOREIGN KEY (ArticuloKey) REFERENCES Dim_Articulo(ArticuloKey)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Fact_Inventario_Tiempo_Articulo')
BEGIN
    CREATE UNIQUE INDEX IX_Fact_Inventario_Tiempo_Articulo
    ON Fact_Inventario(TiempoKey, ArticuloKey);
END
GO

PRINT 'MinimarketDW creado exitosamente.';
PRINT 'Star Schema listo: Dim_Articulo, Dim_Tiempo, Fact_Inventario.';
GO
