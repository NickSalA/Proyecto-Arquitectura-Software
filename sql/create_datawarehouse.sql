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

-- ============================================================
-- Procedimientos almacenados para MinimarketDW
-- ============================================================

CREATE OR ALTER PROCEDURE dbo.sp_DW_LimpiarTablas
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM Fact_Inventario;
    DELETE FROM Dim_Articulo;
    DELETE FROM Dim_Tiempo;
    DBCC CHECKIDENT('Dim_Articulo', RESEED, 0);
    DBCC CHECKIDENT('Dim_Tiempo', RESEED, 0);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_InsertarDimTiempo
    @Fecha DATE,
    @Anio INT,
    @Mes INT,
    @Dia INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Dim_Tiempo (Fecha, Anio, Mes, Dia)
    OUTPUT inserted.TiempoKey
    VALUES (@Fecha, @Anio, @Mes, @Dia);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_InsertarDimArticulo
    @ArticuloID INT,
    @Descripcion VARCHAR(50),
    @Precio DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Dim_Articulo (ArticuloID, Descripcion, Precio)
    OUTPUT inserted.ArticuloKey
    VALUES (@ArticuloID, @Descripcion, @Precio);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_InsertarFact
    @TiempoKey INT,
    @ArticuloKey INT,
    @Stock INT,
    @PrecioActual DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Fact_Inventario (TiempoKey, ArticuloKey, Stock, PrecioActual)
    VALUES (@TiempoKey, @ArticuloKey, @Stock, @PrecioActual);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_ObtenerFechas
AS
BEGIN
    SET NOCOUNT ON;
    SELECT CONVERT(VARCHAR, Fecha, 23) AS FechaStr
    FROM Dim_Tiempo
    ORDER BY Fecha;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_ObtenerDimTiempoPorFecha
    @Fecha DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TiempoKey FROM Dim_Tiempo WHERE Fecha = @Fecha;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_MergeDimTiempo
    @Fecha DATE
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @TiempoKey INT;
    SELECT @TiempoKey = TiempoKey FROM Dim_Tiempo WHERE Fecha = @Fecha;
    IF @TiempoKey IS NOT NULL
    BEGIN
        SELECT @TiempoKey AS TiempoKey;
        RETURN;
    END
    INSERT INTO Dim_Tiempo (Fecha, Anio, Mes, Dia)
    OUTPUT inserted.TiempoKey
    VALUES (@Fecha, YEAR(@Fecha), MONTH(@Fecha), DAY(@Fecha));
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_MergeDimArticulo
    @ArticuloID INT,
    @Descripcion VARCHAR(50),
    @Precio DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;
    MERGE Dim_Articulo AS destino
    USING (VALUES (@ArticuloID, @Descripcion, @Precio)) AS origen (ArticuloID, Descripcion, Precio)
        ON destino.ArticuloID = origen.ArticuloID
    WHEN MATCHED THEN
        UPDATE SET Descripcion = origen.Descripcion, Precio = origen.Precio
    WHEN NOT MATCHED THEN
        INSERT (ArticuloID, Descripcion, Precio)
        VALUES (origen.ArticuloID, origen.Descripcion, origen.Precio)
    OUTPUT $action AS Accion, inserted.ArticuloKey;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_MergeFactInventario
    @TiempoKey INT,
    @ArticuloKey INT,
    @Stock INT,
    @PrecioActual DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;
    MERGE Fact_Inventario AS destino
    USING (VALUES (@TiempoKey, @ArticuloKey, @Stock, @PrecioActual)) AS origen (TiempoKey, ArticuloKey, Stock, PrecioActual)
        ON destino.TiempoKey = origen.TiempoKey AND destino.ArticuloKey = origen.ArticuloKey
    WHEN MATCHED THEN
        UPDATE SET Stock = origen.Stock, PrecioActual = origen.PrecioActual
    WHEN NOT MATCHED THEN
        INSERT (TiempoKey, ArticuloKey, Stock, PrecioActual)
        VALUES (origen.TiempoKey, origen.ArticuloKey, origen.Stock, origen.PrecioActual)
    OUTPUT $action AS Accion;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_CrearVistaCrossTab
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @pivotCols NVARCHAR(MAX);
    DECLARE @sql NVARCHAR(MAX);
    SELECT @pivotCols = STRING_AGG(QUOTENAME(CONVERT(VARCHAR, Fecha, 23)), ', ')
    FROM Dim_Tiempo;
    IF @pivotCols IS NULL
    BEGIN
        PRINT 'No hay fechas en Dim_Tiempo. No se puede crear la vista.';
        RETURN;
    END
    DROP VIEW IF EXISTS Vista_Stock_Cruzado;
    SET @sql = '
    CREATE VIEW Vista_Stock_Cruzado AS
    SELECT
        ArticuloID AS ID,
        Descripcion AS Articulo,
        ' + @pivotCols + '
    FROM (
        SELECT
            da.ArticuloID,
            da.Descripcion,
            dt.Fecha,
            fi.Stock
        FROM Fact_Inventario fi
        INNER JOIN Dim_Articulo da ON fi.ArticuloKey = da.ArticuloKey
        INNER JOIN Dim_Tiempo dt ON fi.TiempoKey = dt.TiempoKey
    ) AS SourceTable
    PIVOT (
        SUM(Stock)
        FOR Fecha IN (' + @pivotCols + ')
    ) AS PivotTable';
    EXEC sp_executesql @sql;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_DW_ConsultarVistaCrossTab
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM Vista_Stock_Cruzado ORDER BY ID;
END
GO

PRINT 'MinimarketDW creado exitosamente.';
PRINT 'Star Schema listo: Dim_Articulo, Dim_Tiempo, Fact_Inventario.';
PRINT 'Procedimientos almacenados listos.';
GO
