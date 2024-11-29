plugins {
    id("java")
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("org.bouncycastle:bcprov-jdk15on:1.70")  // Verifica la última versión disponible

}

tasks.test {
    useJUnitPlatform()
}