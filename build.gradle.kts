plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "minimarket"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // JDBC Driver para Microsoft SQL Server
    implementation("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("minimarket.application.MainKt")
}

// --------------------------------------------------
// Tareas para ejecutar componentes del Entregable 2
// --------------------------------------------------

tasks.register<JavaExec>("runMain") {
    group = "application"
    description = "Ejecuta la interfaz gráfica principal de gestión de inventario"
    mainClass.set("minimarket.application.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}

tasks.register<JavaExec>("runGenerarDatawareHouse") {
    group = "application"
    description = "Ejecuta el proceso ETL para generar el DataWarehouse"
    mainClass.set("minimarket.etl.GenerarDatawareHouseKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}

tasks.register<JavaExec>("runCreateCrossTab") {
    group = "application"
    description = "Ejecuta la creación de la vista OLAP (CrossTab)"
    mainClass.set("minimarket.olap.CreateCrossTabKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}

tasks.register<JavaExec>("runViewCrossTab") {
    group = "application"
    description = "Ejecuta la visualización del cubo OLAP"
    mainClass.set("minimarket.olap.ViewCrossTabKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}
