plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.noarg") version "2.1.20"
    id("org.springframework.boot") version "3.3.6"
    id("io.spring.dependency-management") version "1.1.5"
    application
}

noArg {
    annotation("jakarta.xml.bind.annotation.XmlRootElement")
    annotation("jakarta.xml.bind.annotation.XmlType")
}

group = "minimarket"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web-services")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime")
    implementation("wsdl4j:wsdl4j")

    implementation("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")
    implementation("commons-net:commons-net:3.11.1")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

application {
    mainClass.set("minimarket.web.WebApplicationKt")
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
    environment("FTP_HOST", System.getenv("FTP_HOST") ?: "localhost")
    environment("FTP_PORT", System.getenv("FTP_PORT") ?: "21")
    environment("FTP_USER", System.getenv("FTP_USER") ?: "minimarket")
    environment("FTP_PASSWORD", System.getenv("FTP_PASSWORD") ?: "minimarket123")
    environment("FTP_REMOTE_FILE", System.getenv("FTP_REMOTE_FILE") ?: "/articulos.csv")
}

tasks.register<JavaExec>("runMain") {
    group = "application"
    description = "Ejecuta la interfaz gráfica principal de gestión de inventario"
    mainClass.set("minimarket.client.MainKt")
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
    mainClass.set("minimarket.dw.GenerarDatawareHouseKt")
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

tasks.register<JavaExec>("runCargarDatosReales") {
    group = "application"
    description = "Limpia y carga datos reales de minimarket en todas las bases"
    mainClass.set("minimarket.dw.CargarDatosRealesKt")
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
    mainClass.set("minimarket.dw.CreateCrossTabKt")
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
    mainClass.set("minimarket.dw.ViewCrossTabKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    environment("DB_HOST", System.getenv("DB_HOST") ?: "localhost")
    environment("DB_PORT", System.getenv("DB_PORT") ?: "1433")
    environment("DB_USER", System.getenv("DB_USER") ?: "sa")
    environment("DB_PASSWORD", System.getenv("DB_PASSWORD") ?: "DreamTeam_26")
}
