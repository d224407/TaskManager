import java.util.Properties

val isIzzyOrFdroid = false

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.baselineprofile)
}


android {
    namespace = "com.rk.taskmanager.app"
    compileSdk = 36

    lint {
        disable += "MissingTranslation"
    }

    dependenciesInfo {
        includeInApk = isIzzyOrFdroid.not()
        includeInBundle = isIzzyOrFdroid.not()
    }

    signingConfigs {
        create("release") {
            val isGITHUB_ACTION = System.getenv("GITHUB_ACTIONS") == "true"

            if (isGITHUB_ACTION) {
                // ---- GitHub Actions: đọc trực tiếp từ env ----
                val keystorePath = System.getenv("KEYSTORE_FILE") ?: "/tmp/xed.keystore"
                val keystoreFile = File(keystorePath)

                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                } else {
                    println("Keystore file not found at $keystorePath")
                }
            } else {
                // ---- Local build: đọc từ signing.properties ----
                val propertiesFilePath = "/home/rohit/Android/xed-signing/signing.properties"
                val propertiesFile = File(propertiesFilePath)

                if (propertiesFile.exists()) {
                    val properties = Properties()
                    properties.load(propertiesFile.inputStream())
                    keyAlias = properties["keyAlias"] as String?
                    keyPassword = properties["keyPassword"] as String?
                    storeFile = (properties["storeFile"] as String?)?.let { File(it) }
                    storePassword = properties["storePassword"] as String?
                } else {
                    println("Signing properties file not found at $propertiesFilePath")
                }
            }
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = isIzzyOrFdroid.not()
            isCrunchPngs = isIzzyOrFdroid.not()
            isShrinkResources = isIzzyOrFdroid.not()

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            versionNameSuffix = "-DEBUG"
        }
    }

    defaultConfig {
        applicationId = "com.rk.taskmanager"
        minSdk = 26
        targetSdk = 37

        //versioning
        versionCode = 52
        versionName = "1.5.2"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        // isCoreLibraryDesugaringEnabled = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

tasks.whenTaskAdded {
    if (isIzzyOrFdroid && name.contains("ArtProfile")) {
        println("Skipped Task $name")
        enabled = false
    }
}

dependencies {
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))

    implementation(libs.androidx.room.ktx)
    implementation(project(":main"))
}