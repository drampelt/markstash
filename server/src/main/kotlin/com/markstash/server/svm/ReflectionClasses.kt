package com.markstash.server.svm

import com.google.common.reflect.ClassPath
import com.oracle.svm.core.annotate.AutomaticFeature
import io.ktor.util.InternalAPI
import org.graalvm.nativeimage.hosted.Feature
import org.graalvm.nativeimage.hosted.RuntimeReflection

@AutomaticFeature
@InternalAPI
internal class ReflectionClasses : Feature {
    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        setupClasses()
    }

    companion object {
        val debug = "false".toBoolean()

        private fun setupClasses() {
            try {
                if (debug) println("> Loading classes for future reflection support")
                processPackage("com.markstash.api")
                processPackage("com.markstash.server.controllers")
            } catch (e: Error) {
                if (e.message?.contains("The class ImageSingletons can only be used when building native images") != true) {
                    throw e
                }
            }
        }

        private fun processPackage(pkg: String) {
            if (debug) println("> Finding class in package: $pkg")
            ClassPath.from(Thread.currentThread().contextClassLoader).getTopLevelClassesRecursive(pkg).forEach { info ->
                if (debug) println("> Found class in package ${info.name}")
                processRecursive(info.load())
            }
        }

        private fun processRecursive(clazz: Class<*>) {
            process(clazz)
            clazz.declaredClasses.forEach { processRecursive(it) }
        }

        private fun process(clazz: Class<*>) {
            try {
                if (debug) println("> Declaring class: ${clazz.canonicalName}")
                RuntimeReflection.register(clazz)
                for (method in (clazz.methods + clazz.declaredMethods)) {
                    if (debug) println("\t> method: ${method.name}(${method.parameterCount})")
                    RuntimeReflection.register(method)
                }
                for (constructor in (clazz.constructors + clazz.declaredConstructors)) {
                    if (debug) println("\t> constructor: ${constructor.name}(${constructor.parameterCount})")
                    RuntimeReflection.register(constructor)
                }
                for (field in (clazz.fields + clazz.declaredFields)) {
                    if (debug) println("\t> field: ${field.name}")
                    RuntimeReflection.register(field)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
