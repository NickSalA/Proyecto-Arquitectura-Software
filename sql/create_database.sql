-- ============================================================
-- Script de creación de base de datos: MinimarketDB
-- Motor: Microsoft SQL Server
-- Sistema: MinimarketPOS - Entregable 3
-- Capa de datos gestionada con procedimientos almacenados
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
        Descripcion VARCHAR(50)     NOT NULL,
        Precio      DECIMAL(10,2)   NOT NULL,
        Stock       INT             NOT NULL
    );
END
GO

IF COL_LENGTH('Articulos', 'Descripcion') IS NOT NULL
BEGIN
    ALTER TABLE Articulos ALTER COLUMN Descripcion VARCHAR(50) NOT NULL;
END
GO

-- Índice para búsquedas por descripción (opcional, mejora rendimiento)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Articulos_Descripcion')
BEGIN
    CREATE INDEX IX_Articulos_Descripcion 
    ON Articulos(Descripcion);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Insertar
    @ID INT,
    @Descripcion VARCHAR(50),
    @Precio DECIMAL(10,2),
    @Stock INT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO Articulos (ID, Descripcion, Precio, Stock)
    VALUES (@ID, @Descripcion, @Precio, @Stock);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Buscar
    @ID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT ID, Descripcion, Precio, Stock
    FROM Articulos
    WHERE ID = @ID;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Listar
AS
BEGIN
    SET NOCOUNT ON;

    SELECT ID, Descripcion, Precio, Stock
    FROM Articulos
    ORDER BY ID;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Actualizar
    @ID INT,
    @Descripcion VARCHAR(50),
    @Precio DECIMAL(10,2),
    @Stock INT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Articulos
    SET Descripcion = @Descripcion,
        Precio = @Precio,
        Stock = @Stock
    WHERE ID = @ID;

    SELECT @@ROWCOUNT AS FilasAfectadas;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Eliminar
    @ID INT
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM Articulos
    WHERE ID = @ID;

    SELECT @@ROWCOUNT AS FilasAfectadas;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Existe
    @ID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(*) AS Total
    FROM Articulos
    WHERE ID = @ID;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Articulo_Cantidad
AS
BEGIN
    SET NOCOUNT ON;

    SELECT COUNT(*) AS Total
    FROM Articulos;
END
GO

PRINT 'Base de datos MinimarketDB creada exitosamente.';
PRINT 'Tabla Articulos y procedimientos almacenados listos para recibir datos.';
GO
