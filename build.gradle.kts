import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    // Java support
    id("java")
    // Kotlin support
    alias(libs.plugins.kotlin)
    // Gradle IntelliJ Plugin
    alias(libs.plugins.gradleIntelliJPlugin)
    // Gradle Changelog Plugin
    alias(libs.plugins.changelog)
    // Gradle Qodana Plugin
    alias(libs.plugins.qodana)
    // Gradle Kover Plugin
    alias(libs.plugins.kover)
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://repo.spring.io/milestone")
    maven(url = "https://repo.spring.io/snapshot")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    implementation("com.didalgo:gpt3-tokenizer:0.1.7")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.16.1") // to remove after class detection fix in Spring
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1") // to remove after class detection fix in Spring
    implementation("com.fifesoft:rsyntaxtextarea:3.3.3")
    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-ext-tables:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-html2md-converter:0.64.8")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-SNAPSHOT") {
        exclude(group = "io.rest-assured", module = "json-path")
    }
    implementation("org.springframework.ai:spring-ai-azure-openai-spring-boot-starter:1.0.0-SNAPSHOT") {
        exclude(group = "io.rest-assured", module = "json-path")
    }
    implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter:1.0.0-SNAPSHOT") {
        exclude(group = "io.rest-assured", module = "json-path")
    }
    testImplementation("org.junit.platform:junit-platform-launcher:1.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(17)
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
koverReport {
    defaults {
        xml {
            onCheck = true
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it.get()
            targetCompatibility = it.get()
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it.get()
        }
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("DESCRIPTION.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with (it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in DESCRIPTION.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    runPluginVerifier {
        ideVersions.set(listOf(
            "IU-242.10180.25",
            "IC-241.12662.62",
            "IC-233.11799.30",
            "IC-232.10227.8",
            "IC-232.6734.9",
            "IC-231.9011.34",
            "IC-231.4840.387"))
        downloadDir.set(File(System.getProperty("user.home"), ".pluginVerifier/ides").path)
    }

    test {
        useJUnitPlatform()
    }

    signPlugin {
        certificateChainFile.set(file(project.property("JetBrains.signPlugin.certificateChain") as String))
        privateKeyFile.set(file(project.property("JetBrains.signPlugin.privateKey") as String))
        password.set(project.property("JetBrains.signPlugin.password") as String?)
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }
}
