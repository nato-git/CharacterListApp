// プロジェクトルートの build.gradle.kts
plugins {
    // 'alias' を使わずに直接書く場合は、このように version を指定します
    // すべて 2.0.21 で統一します
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
}