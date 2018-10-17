package bav.onecell

import androidx.multidex.MultiDexApplication

import bav.onecell.di.AppComponent
import bav.onecell.di.AppModule
import bav.onecell.di.DaggerAppComponent

class OneCellApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(applicationContext))
                .build()
    }
}