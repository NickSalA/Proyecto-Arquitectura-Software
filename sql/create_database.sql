-- ============================================================
-- Script de creación de base de datos: MinimarketDB
-- Motor: Microsoft SQL Server
-- Sistema: MinimarketPOS - Gestión de Inventario Local
-- ============================================================

-- Crear la base de datos
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MinimarketDB')
BEGIN
    CREATE DATABASE MinimarketDB;
END
GO

USE MinimarketDB;
GO

-- Crear la tabla de artículos
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Articulos')
BEGIN
    CREATE TABLE Articulos (
        ID          INT             PRIMARY KEY,
        Descripcion VARCHAR(20)     NOT NULL,
        Precio      DECIMAL(10,2)   NOT NULL,
        Stock       INT             NOT NULL
    );
END
GO

-- Índice para búsquedas por descripción (opcional, mejora rendimiento)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Articulos_Descripcion')
BEGIN
    CREATE INDEX IX_Articulos_Descripcion 
    ON Articulos(Descripcion);
END
GO

-- ============================================================
-- Procedimientos almacenados para CRUD de Articulos
-- Toda la logica de acceso a datos reside en el servidor
-- ============================================================

-- Insertar un articulo
IF OBJECT_ID('sp_AgregarArticulo', 'P') IS NOT NULL
    DROP PROCEDURE sp_AgregarArticulo;
GO

CREATE PROCEDURE sp_AgregarArticulo
    @ID            INT,
    @Descripcion   VARCHAR(20),
    @Precio        DECIMAL(10,2),
    @Stock         INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Articulos (ID, Descripcion, Precio, Stock)
    VALUES (@ID, @Descripcion, @Precio, @Stock);
END;
GO

-- Buscar articulo por ID
IF OBJECT_ID('sp_BuscarArticulo', 'P') IS NOT NULL
    DROP PROCEDURE sp_BuscarArticulo;
GO

CREATE PROCEDURE sp_BuscarArticulo
    @ID   INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT ID, Descripcion, Precio, Stock
    FROM Articulos
    WHERE ID = @ID;
END;
GO

-- Listar todos los articulos
IF OBJECT_ID('sp_ListarArticulos', 'P') IS NOT NULL
    DROP PROCEDURE sp_ListarArticulos;
GO

CREATE PROCEDURE sp_ListarArticulos
AS
BEGIN
    SET NOCOUNT ON;
    SELECT ID, Descripcion, Precio, Stock
    FROM Articulos
    ORDER BY ID;
END;
GO

-- Actualizar un articulo
IF OBJECT_ID('sp_ActualizarArticulo', 'P') IS NOT NULL
    DROP PROCEDURE sp_ActualizarArticulo;
GO

CREATE PROCEDURE sp_ActualizarArticulo
    @ID            INT,
    @Descripcion   VARCHAR(20),
    @Precio        DECIMAL(10,2),
    @Stock         INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Articulos
    SET Descripcion = @Descripcion,
        Precio      = @Precio,
        Stock       = @Stock
    WHERE ID = @ID;
END;
GO

-- Eliminar un articulo por ID
IF OBJECT_ID('sp_EliminarArticulo', 'P') IS NOT NULL
    DROP PROCEDURE sp_EliminarArticulo;
GO

CREATE PROCEDURE sp_EliminarArticulo
    @ID   INT
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM Articulos WHERE ID = @ID;
END;
GO

-- Verificar si un articulo existe
IF OBJECT_ID('sp_ExisteArticulo', 'P') IS NOT NULL
    DROP PROCEDURE sp_ExisteArticulo;
GO

CREATE PROCEDURE sp_ExisteArticulo
    @ID   INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Cantidad FROM Articulos WHERE ID = @ID;
END;
GO

-- Contar total de articulos
IF OBJECT_ID('sp_ContarArticulos', 'P') IS NOT NULL
    DROP PROCEDURE sp_ContarArticulos;
GO

CREATE PROCEDURE sp_ContarArticulos
AS
BEGIN
    SET NOCOUNT ON;
    SELECT COUNT(*) AS Cantidad FROM Articulos;
END;
GO

PRINT 'Base de datos MinimarketDB creada exitosamente.';
PRINT 'Tabla Articulos y procedimientos almacenados listos.';
GO
