package com.markstash.server

import com.markstash.server.db.Database
import kotlinx.serialization.json.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Settings(
    private val db: Database
) {
    var databaseVersion: Int by setting(defaultValue = 1)

    private val json = Json

    private inline fun <reified R: Any> setting(name: String? = null, defaultValue: R) = SettingDelegateProvider(R::class, name, defaultValue)
    private inline fun <reified R: Any> settingNullable(name: String? = null, defaultValue: R? = null) = NullableSettingDelegate(R::class, name, defaultValue)

    @Suppress("UNCHECKED_CAST")
    private class NullableSettingDelegate<R: Any>(val clazz: KClass<R>, val name: String?, val defaultValue: R?) : ReadWriteProperty<Settings, R?> {
        override fun getValue(thisRef: Settings, property: KProperty<*>): R? {
            val value = thisRef.db.settingQueries.findByName(name ?: property.name).executeAsOneOrNull() ?: return defaultValue

            val obj = thisRef.json.parseToJsonElement(value)
            if (obj is JsonNull) return defaultValue

            return when (clazz) {
                String::class -> obj.jsonPrimitive.contentOrNull as? R? ?: defaultValue
                Int::class -> obj.jsonPrimitive.intOrNull as? R? ?: defaultValue
                Boolean::class -> obj.jsonPrimitive.booleanOrNull as? R? ?: defaultValue
                else -> defaultValue
            }
        }

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: R?) {
            val elem = when (clazz) {
                String::class -> JsonPrimitive(value as? String?)
                Int::class -> JsonPrimitive(value as? Int?)
                Boolean::class -> JsonPrimitive(value as? Boolean?)
                else -> JsonNull
            }
            val obj = thisRef.json.encodeToString(JsonElementSerializer, elem)
            thisRef.db.settingQueries.update(name ?: property.name, obj)
        }
    }

    private class SettingDelegateProvider<R: Any>(val clazz: KClass<R>, val name: String?, val defaultValue: R) {
        operator fun provideDelegate(thisRef: Settings, property: KProperty<*>): ReadWriteProperty<Settings, R> {
            return SettingDelegate(clazz, name ?: property.name, defaultValue)
        }
    }

    private class SettingDelegate<R: Any>(clazz: KClass<R>, name: String, val defaultValue: R) : ReadWriteProperty<Settings, R> {
        val delegate = NullableSettingDelegate(clazz, name, defaultValue)

        override fun getValue(thisRef: Settings, property: KProperty<*>): R = delegate.getValue(thisRef, property) ?: defaultValue

        override fun setValue(thisRef: Settings, property: KProperty<*>, value: R) = delegate.setValue(thisRef, property, value)
    }
}
