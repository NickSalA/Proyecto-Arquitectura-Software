plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.3.6"
    id("io.spring.dependency-management") version "1.1.5"
    application
}

group = "minimarket"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")
    implementation("commons-net:commons-net:3.11.1")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("minimarket.application.MainKt")
}

springBoot {
    mainClass.set("minimarket.web.WebApplicationKt")
}

// --------------------------------------------------
// Tareas para ejecutar componentes del Entregable 3
// --------------------------------------------------

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Ejecuta la aplicacion Web MVC del Entregable 3"
    mainClass.set("minimarket.web.WebApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}

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

tasks.register<JavaExec>("runExportarFTP") {
    group = "application"
    description = "Exporta datos desde MinimarketDB y los publica en el servidor FTP"
    mainClass.set("minimarket.ftp.ExportarFTPKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
    environment("FTP_HOST", System.getenv("FTP_HOST") ?: "localhost")
    environment("FTP_PORT", System.getenv("FTP_PORT") ?: "21")
    environment("FTP_USER", System.getenv("FTP_USER") ?: "minimarket")
    environment("FTP_PASSWORD", System.getenv("FTP_PASSWORD") ?: "minimarket123")
    environment("FTP_REMOTE_FILE", System.getenv("FTP_REMOTE_FILE") ?: "/articulos.csv")
}

tasks.register<JavaExec>("runActualizarMirror") {
    group = "application"
    description = "Descarga datos desde FTP y sincroniza la base MinimarketMirror"
    mainClass.set("minimarket.mirror.ActualizarMirrorKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
    environment("FTP_HOST", System.getenv("FTP_HOST") ?: "localhost")
    environment("FTP_PORT", System.getenv("FTP_PORT") ?: "21")
    environment("FTP_USER", System.getenv("FTP_USER") ?: "minimarket")
    environment("FTP_PASSWORD", System.getenv("FTP_PASSWORD") ?: "minimarket123")
    environment("FTP_REMOTE_FILE", System.getenv("FTP_REMOTE_FILE") ?: "/articulos.csv")
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
