/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-library-conventions")
}
dependencies {
    implementation(project(":domain"))
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.hibernate:hibernate-core:6.5.0.Final")

}