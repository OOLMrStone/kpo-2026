plugins {
    application
    java
    jacoco
    checkstyle
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
application { mainClass = "studying.vyshkat.Main" }

dependencies {
    implementation("org.springframework:spring-context:7.0.1")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
tasks.named<JavaExec>("run") { standardInput = System.`in` }
jacoco { toolVersion = "0.8.14" }
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
    }
}
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
checkstyle {
    toolVersion = "14.1.0"
    configFile = file("config/checkstyle.xml")
    maxWarnings = 0
}
tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
