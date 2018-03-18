package bav.onecell

import android.app.Application
import bav.onecell.di.AppComponent
import bav.onecell.di.AppModule
import bav.onecell.di.DaggerAppComponent

class OneCellApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(applicationContext))
                .build()
    }
}