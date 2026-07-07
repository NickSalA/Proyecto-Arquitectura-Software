-- ============================================================
-- Script de creacion de la tabla de actividad
-- Motor: Microsoft SQL Server
-- Plugin: Seguridad - Monitoreo de actividad
-- ============================================================

USE MinimarketDB;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'RegistroActividad')
BEGIN
    CREATE TABLE RegistroActividad (
        Id            INT             IDENTITY(1,1) PRIMARY KEY,
        Operador      VARCHAR(50)     NOT NULL,
        Estado        VARCHAR(20)     NOT NULL,
        UltimoLatido  DATETIME        NOT NULL DEFAULT GETDATE(),
        InicioSesion  DATETIME        NOT NULL DEFAULT GETDATE(),
        FinSesion     DATETIME        NULL
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_RegistroActividad_Operador')
BEGIN
    CREATE UNIQUE INDEX IX_RegistroActividad_Operador
    ON RegistroActividad(Operador);
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Actividad_Upsert
    @Operador VARCHAR(50),
    @Estado VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (SELECT 1 FROM RegistroActividad WHERE Operador = @Operador)
    BEGIN
        UPDATE RegistroActividad
        SET Estado = @Estado,
            UltimoLatido = GETDATE(),
            FinSesion = CASE WHEN @Estado = 'ausente' THEN GETDATE() ELSE NULL END
        WHERE Operador = @Operador;
    END
    ELSE
    BEGIN
        INSERT INTO RegistroActividad (Operador, Estado, UltimoLatido, InicioSesion)
        VALUES (@Operador, @Estado, GETDATE(), GETDATE());
    END
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Actividad_Listar
AS
BEGIN
    SET NOCOUNT ON;

    SELECT Id, Operador, Estado, UltimoLatido, InicioSesion, FinSesion
    FROM RegistroActividad
    ORDER BY UltimoLatido DESC;
END
GO

CREATE OR ALTER PROCEDURE dbo.sp_Actividad_MarcarAusentes
    @TimeoutSegundos INT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE RegistroActividad
    SET Estado = 'ausente',
        FinSesion = GETDATE()
    WHERE Estado IN ('activo', 'inactivo')
      AND DATEDIFF(SECOND, UltimoLatido, GETDATE()) > @TimeoutSegundos;
END
GO

PRINT 'Tabla RegistroActividad y procedimientos almacenados creados exitosamente.';
GO
