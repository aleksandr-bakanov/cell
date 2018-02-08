package bav.onecell

import android.app.Application
import bav.onecell.di.ApplicationComponent
import bav.onecell.di.DaggerApplicationComponent

class OneCellApplication : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.create()
    }
}