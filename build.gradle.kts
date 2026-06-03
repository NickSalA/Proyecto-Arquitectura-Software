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
}
