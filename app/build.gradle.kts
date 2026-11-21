import java.util.Properties

// Set -Pfdroid=true (or env FDROID=true) for F-Droid/IzzyOnDroid builds
val isIzzyOrFdroid: Boolean =
    providers.gradleProperty("fdroid").orNull == "true" ||
        providers.environmentVariable("FDROID").orNull == "true"

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
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
            val envStore = System.getenv("KEYSTORE_FILE")?.let { File(it) }
            val propsFile = rootProject.file("signing.properties")

            if (envStore != null && envStore.length() > 0L && System.getenv("KEYSTORE_PASSWORD") != null) {
                // CI: read from env
                storeFile = envStore
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else if (propsFile.exists()) {
                // Local: read from <root>/signing.properties
                val properties = Properties()
                propsFile.inputStream().use { properties.load(it) }
                keyAlias = properties["keyAlias"] as String?
                keyPassword = properties["keyPassword"] as String?
                storeFile = (properties["storeFile"] as String?)?.let { File(it) }
                storePassword = properties["storePassword"] as String?
            } else {
                println("No release keystore found, release will use debug signing")
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
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile != null) releaseSigning
            else signingConfigs.getByName("debug")
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
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.room.ktx)
    implementation(project(":main"))
}