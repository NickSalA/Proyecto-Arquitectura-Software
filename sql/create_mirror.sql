-- ============================================================
-- Script DDL: MinimarketMirror
-- Motor: Microsoft SQL Server
-- Sistema: MinimarketPOS - Entregable 3
-- Capa: Mirror alimentado desde FTP
-- ============================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MinimarketMirror')
BEGIN
    CREATE DATABASE MinimarketMirror;
END
GO

USE MinimarketMirror;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ArticulosMirror')
BEGIN
    CREATE TABLE ArticulosMirror (
        ID                    INT             PRIMARY KEY,
        Descripcion           VARCHAR(50)     NOT NULL,
        Precio                DECIMAL(10,2)   NOT NULL,
        Stock                 INT             NOT NULL,
        Activo                BIT             NOT NULL DEFAULT 1,
        FechaSincronizacion   DATETIME2       NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ArticulosMirror_Activo')
BEGIN
    CREATE INDEX IX_ArticulosMirror_Activo
    ON ArticulosMirror(Activo);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Mirror_LimpiarTablas
AS
BEGIN
    SET NOCOUNT ON;
    DELETE FROM ArticulosMirror;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Mirror_Insertar
    @ID INT,
    @Descripcion VARCHAR(50),
    @Precio DECIMAL(10,2),
    @Stock INT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO ArticulosMirror (ID, Descripcion, Precio, Stock, Activo)
    VALUES (@ID, @Descripcion, @Precio, @Stock, 1);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Mirror_ExtraerActivos
AS
BEGIN
    SET NOCOUNT ON;
    SELECT ID, Descripcion, Precio, Stock
    FROM ArticulosMirror
    WHERE Activo = 1
    ORDER BY ID;
END
GO

PRINT 'MinimarketMirror creado exitosamente.';
PRINT 'Tabla ArticulosMirror lista para sincronizacion FTP.';
GO
