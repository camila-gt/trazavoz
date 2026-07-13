package com.trazavoz.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.navigation.TrazavozNavHost
import com.trazavoz.ui.theme.TrazavozTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ttsManager: TrazavozTtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrazavozTheme {
                // The Surface must fill the raw screen bounds (no inset padding here) so its
                // background paints edge-to-edge behind the now-transparent system bars; only
                // the inner content is pushed away from the notch/status bar/nav bar.
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        TrazavozNavHost(ttsManager = ttsManager)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
