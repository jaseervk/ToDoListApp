package com.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.todoapp.presentation.ui.screens.SplashScreen
import com.todoapp.presentation.ui.screens.TaskListScreen
import com.todoapp.presentation.ui.theme.TodoAppTheme
import com.todoapp.util.SoundManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry-point.
 *
 * Sound resources are released in [onDestroy] to avoid native memory leaks.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by remember { mutableStateOf(true) }

                    Crossfade(
                        targetState = showSplash,
                        label = "SplashTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(100) // Give SoundPool a moment to load
                                soundManager.playSplash()
                            }
                            SplashScreen(
                                onSplashFinished = { showSplash = false }
                            )
                        } else {
                            TaskListScreen(
                                onPlayAdd      = { soundManager.playAdd() },
                                onPlayComplete = { soundManager.playComplete() },
                                onPlayDelete   = { soundManager.playDelete() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
