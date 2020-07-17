package com.markstash.server.svm

import com.oracle.svm.core.annotate.AutomaticFeature
import com.oracle.svm.core.jni.JNIRuntimeAccess
import io.ktor.util.InternalAPI
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection
import org.sqlite.BusyHandler
import org.sqlite.Function
import org.sqlite.ProgressHandler
import org.sqlite.core.DB
import org.sqlite.core.NativeDB
import java.util.Arrays

@AutomaticFeature
@InternalAPI
internal class JNIReflectionClasses : Feature {
    override fun duringSetup(access: Feature.DuringSetupAccess?) {}

    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess?) {

        try {
            JNIRuntimeAccess.register(
                NativeDB::class.java.getDeclaredMethod(
                    "_open_utf8",
                    ByteArray::class.java,
                    Int::class.javaPrimitiveType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupClasses()
    }

    companion object {
        private val classes: Array<Class<*>>
            get() =
                arrayOf(
                    DB::class.java,
                    NativeDB::class.java,
                    BusyHandler::class.java,
                    Function::class.java,
                    ProgressHandler::class.java,
                    Function.Aggregate::class.java,
                    DB.ProgressObserver::class.java,
                    java.lang.Throwable::class.java,
                    BooleanArray::class.java
                )

        private fun setupClasses() {
            try {
                if (ReflectionClasses.debug) println("> Loading classes for future reflection support")
                for (clazz in classes) {
                    process(clazz)
                }
            } catch (e: Error) {
                if (e.message?.contains("The class ImageSingletons can only be used when building native images") != true) {
                    throw e
                }
            }

        }

        private fun process(clazz: Class<*>) {
            try {
                if (ReflectionClasses.debug) println("> Declaring class: ${clazz.canonicalName}")
                RuntimeReflection.register(clazz)
                for (method in clazz.declaredMethods) {
                    if (ReflectionClasses.debug) println("\t> method: ${method.name}(${Arrays.toString(method.parameterTypes)})")
                    JNIRuntimeAccess.register(method)
                    RuntimeReflection.register(method)
                }
                for (field in clazz.declaredFields) {
                    if (ReflectionClasses.debug) println("\t> field: ${field.name}")
                    JNIRuntimeAccess.register(field)
                    RuntimeReflection.register(field)
                }
                for (constructor in clazz.declaredConstructors) {
                    if (ReflectionClasses.debug) println("\t> constructor: ${constructor.name}(${constructor.parameterCount})")
                    JNIRuntimeAccess.register(constructor)
                    RuntimeReflection.register(constructor)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}
