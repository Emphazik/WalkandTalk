package ru.walkAndTalk

import android.app.Application
import com.vk.id.VKID
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.walkAndTalk.data.di.appModule
import java.util.Locale
//import com.yandex.mapkit.MapKitFactory


class App : Application() {
    override fun onCreate() {
        super.onCreate()
//        // Initialize Yandex MapKit
//        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
//        MapKitFactory.initialize(this)

        // VK ID и Koin
        VKID.init(this)
        VKID.instance.setLocale(Locale.getDefault())
        VKID.logsEnabled = true
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}