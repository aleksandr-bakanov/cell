package bav.onecell.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.Common
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class MainActivity : FragmentActivity(), Main.NavigationInfoProvider {

    @Inject lateinit var gameState: Common.GameState
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val lastNavDestinationProvider: BehaviorSubject<Int> = BehaviorSubject.create()

    companion object {
        private const val TAG = "MainActivity"

        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
        setContentView(R.layout.activity_main)
        lastNavDestinationProvider.onNext(gameState.getLastNavDestinationId())
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        verifyStoragePermissions()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
    //endregion

    //region Overridden methods
    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.currentDestination?.let {
            if (it.id != R.id.mainFragment) {
                gameState.setLastNavDestinationId(it.id, true)
                lastNavDestinationProvider.onNext(it.id)
                navController.popBackStack(R.id.mainFragment, false)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUi()
    }

    override fun provideLastDestination(): Observable<Int> = lastNavDestinationProvider
    //endregion

    //region Private methods
    private fun inject() {
        (application as OneCellApplication).appComponent
                .plus(MainModule())
                .inject(this)
    }

    private fun hideSystemUi() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun verifyStoragePermissions() {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }
    }
    //endregion
}
