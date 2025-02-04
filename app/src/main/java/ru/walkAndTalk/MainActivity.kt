package ru.walkAndTalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.auth.AuthScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KoinAndroidContext {
                MaterialTheme {
                    AuthScreen()
                }
            }
        }
    }

}