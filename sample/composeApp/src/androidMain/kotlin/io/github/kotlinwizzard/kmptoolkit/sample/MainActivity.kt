package io.github.kotlinwizzard.kmptoolkit.sample

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("application/pdf")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        setContent {
            App()
        }
    }
}
