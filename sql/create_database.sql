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

PRINT 'Base de datos MinimarketDB creada exitosamente.';
PRINT 'Tabla Articulos lista para recibir datos.';
GO
