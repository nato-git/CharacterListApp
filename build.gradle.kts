// プロジェクトルートの build.gradle.kts
plugins {
    // 👈 これが抜けていると、古い方式（BaseVariantが必要な古いAGP）を探しに行ってしまいます
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    
    // Kotlin本体とSerializationのバージョンを 2.0.21 で統一
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}