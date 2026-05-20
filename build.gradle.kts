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
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("minimarket.application.MainKt")
}

// --------------------------------------------------
// Tareas para ejecutar cada componente por separado
// --------------------------------------------------

tasks.register<JavaExec>("runMain") {
    group = "application"
    description = "Ejecuta la aplicación principal de gestión de inventario"
    mainClass.set("minimarket.application.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.register<JavaExec>("runSend") {
    group = "application"
    description = "Ejecuta el componente de transferencia (Send)"
    mainClass.set("minimarket.application.SendKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runUpdate") {
    group = "application"
    description = "Ejecuta el componente de consolidación (Update)"
    mainClass.set("minimarket.application.UpdateKt")
    classpath = sourceSets["main"].runtimeClasspath
}

// Crear directorio de datos al compilar
tasks.named("build") {
    doLast {
        file("data").mkdirs()
    }
}
