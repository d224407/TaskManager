plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}
// Falls back to "unknown" when git is missing or the tree is not a repo (e.g. source zip)
fun gitOutput(vararg args: String): String = runCatching {
    providers.exec {
        commandLine("git", *args)
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
}.getOrDefault("").ifEmpty { "unknown" }

val gitCommitHash = gitOutput("rev-parse", "--short=8", "HEAD")
val fullGitCommitHash = gitOutput("rev-parse", "HEAD")
val gitCommitDate = gitOutput("show", "-s", "--format=%cI", "HEAD")

// owner/repo used by the in-app update check
val githubRepo = "d224407/TaskManager"


android {
    namespace = "com.rk.taskmanager"
    compileSdk {
        version = release(36)
    }
    lint {
        disable += "MissingTranslation"
    }
    buildFeatures.buildConfig = true

    defaultConfig {
        minSdk = 26

        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GIT_COMMIT_HASH", "\"$fullGitCommitHash\"")
            buildConfigField("String", "GIT_SHORT_COMMIT_HASH", "\"$gitCommitHash\"")
            buildConfigField("String", "GIT_COMMIT_DATE", "\"$gitCommitDate\"")
            buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
        }
        debug {
            buildConfigField("String", "GIT_COMMIT_HASH", "\"$fullGitCommitHash\"")
            buildConfigField("String", "GIT_SHORT_COMMIT_HASH", "\"$gitCommitHash\"")
            buildConfigField("String", "GIT_COMMIT_DATE", "\"$gitCommitDate\"")
            buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.compose)
    implementation(project(":components"))
    implementation(project(":taskmanagerd"))
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
}
