package com.markstash.android

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.danielrampelt.coil.ico.IcoDecoder
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MarkstashApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MarkstashApplication)
            modules(appModule)
        }

        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .componentRegistry {
                    add(SvgDecoder(this@MarkstashApplication))
                    add(IcoDecoder(this@MarkstashApplication))
                }
                .build()
        )
    }
}
