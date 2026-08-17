plugins {
    // 로컬에 JDK 21이 없어도 빌드가 되도록 툴체인을 자동 프로비저닝한다.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "entry-api"
