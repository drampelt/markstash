package com.markstash.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.ui.platform.setContent
import com.markstash.android.ui.MarkstashApp
import org.koin.android.ext.android.getKoin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Providers(
                ReceivedIntentAmbient provides ReceivedIntent(intent),
            ) {
                MarkstashApp()
            }
        }
    }
}

val ReceivedIntentAmbient = ambientOf<ReceivedIntent>()

class ReceivedIntent(intent: Intent? = null) {
    var intent = intent
        private set

    fun handle() {
        intent = null
    }
}
