import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-application-conventions")
}


dependencies {
    implementation("de.vandermeer:asciitable:0.3.2")
    implementation(project(":domain"))
    implementation(project(":domain-implementation:jdbc"))
    implementation(project(":domain-implementation:jpa"))

}

application {
    // Define the main class for the application.
    mainClass = "org.example.app.App"
}
