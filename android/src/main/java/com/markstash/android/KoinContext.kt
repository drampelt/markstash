package com.markstash.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.remember
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

val KoinContext = ambientOf<Koin>()

@Composable
inline fun <reified T : Any> inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    val koin = KoinContext.current
    return remember { koin.inject(qualifier, parameters) }
}
