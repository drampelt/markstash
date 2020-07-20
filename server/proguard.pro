-dontobfuscate
-dontoptimize
#-optimizations optimize.conservatively,*
#-optimizationpasses 5

-keepattributes *Annotation*, InnerClasses

-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.markstash.api.**$$serializer { *; }
-keepclassmembers class com.markstash.api.** {
    *** Companion;
}
-keepclasseswithmembers class com.markstash.api.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.markstash.server.ApplicationKt { *; }
-keep class com.markstash.server.controllers.** { *; }
-keep class com.markstash.server.svm.** { *; }
-dontwarn com.markstash.server.svm.**

-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.text.RegexOption { *; }

-dontwarn kotlinx.atomicfu.AtomicFU
-dontwarn io.ktor.auth.jwt.JWTAuthKt

-keep class io.ktor.server.cio.EngineMain { *; }

-keep class * extends java.sql.Driver { *; }

-keep class com.fasterxml.jackson.databind.**Feature { *; }

-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

-keep public class ch.qos.logback.** { *; }
-keep public class org.slf4j.** { *; }
-keep class net.lightbody.bmp.** { *; }

-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn org.slf4j.**
-dontwarn org.littleshoot.**
-dontwarn ch.qos.logback.**
-dontwarn io.netty.**
-dontwarn org.apache.**
-dontwarn net.bytebuddy.**
