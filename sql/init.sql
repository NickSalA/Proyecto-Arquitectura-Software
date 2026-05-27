-- ============================================================
-- Script de inicialización: MinimarketDB
-- Motor: Microsoft SQL Server 2022
-- ============================================================

-- Crear base de datos si no existe
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'MinimarketDB')
BEGIN
    CREATE DATABASE MinimarketDB;
END
GO

USE MinimarketDB;
GO

-- Crear tabla de artículos si no existe
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

PRINT 'Base de datos MinimarketDB inicializada correctamente.';
GO
