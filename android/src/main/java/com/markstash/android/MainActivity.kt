package com.markstash.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.setContent
import com.markstash.android.ui.MarkstashApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = Session(applicationContext)
        setContent {
            Providers(Session.ambient provides session) {
                MarkstashApp()
            }
        }
    }
}
