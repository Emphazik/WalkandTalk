package ru.walkAndTalk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
//import com.google.android.gms.maps.MapView
//import com.google.android.libraries.maps.MapView
import org.koin.androidx.compose.KoinAndroidContext
import ru.walkAndTalk.ui.screens.root.RootScreen
import ru.walkAndTalk.ui.theme.WalkTalkTheme

class MainActivity : ComponentActivity() {

//    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        setContent {
            KoinAndroidContext {
                WalkTalkTheme {
                    RootScreen(intent)
                }
            }
        }
    }
//    override fun onStart() {
//        super.onStart()
//        MapKitFactory.getInstance().onStart()
//    }
//
//    override fun onStop() {
//        MapKitFactory.getInstance().onStop()
//        super.onStop()
//    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

}







