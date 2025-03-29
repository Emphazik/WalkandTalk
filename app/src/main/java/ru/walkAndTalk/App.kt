package ru.walkAndTalk

import android.app.Application
import com.vk.id.VKID
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.walkAndTalk.data.di.appModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        VKID.init(this)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }

}