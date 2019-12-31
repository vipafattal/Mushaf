package co.jp.smagroup.musahaf.ui.quran.sharedComponent

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.utils.LocaleHelper
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.standard.lambda.unitFun

/**
 * Created by ${User} on ${Date}
 */
abstract class BaseActivity(private val isRequiringFullScreen: Boolean = false) : AppCompatActivity() {
    val preferences = AppPreferences()
    protected var onPermissionGiven: unitFun? = null
        private set

    var currentSystemVisibility: Boolean = false
        protected set
    private var initialLocale: String? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(MushafApplication.appContext.getAppTheme())
        if (isRequiringFullScreen) hideFullScreen()
        super.onCreate(savedInstanceState)
        initialLocale = LocaleHelper.getLanguage(this)
    }

    fun systemUiVisibility(hide: Boolean) {
        currentSystemVisibility = hide
        if (hide) hideSystemUI()
        else showSystemUI()

        //activity.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < 19) { // lower api
            val v = window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val decorView = window.decorView

            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

            decorView.systemUiVisibility = uiOptions
        }
    }

    protected fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    protected fun hideFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun lockScreenOrientation() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val configuration = resources.configuration
        val rotation = windowManager.defaultDisplay.rotation

        // Search for the natural position of the device
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) || configuration.orientation == Configuration.ORIENTATION_PORTRAIT && (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)) {
            // Natural position is Landscape
            when (rotation) {
                Surface.ROTATION_0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_90 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_180 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                Surface.ROTATION_270 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else {
            // Natural position is Portrait
            when (rotation) {
                Surface.ROTATION_0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        }
    }

    fun unlockScreenOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    private fun requestForSpecificPermission(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    //TODO replace  Manifest.permission.WRITE_EXTERNAL_STORAGE with param
    fun executeWithPendingPermission(requestCode: Int, block: unitFun) {
        onPermissionGiven = block
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result == PackageManager.PERMISSION_GRANTED)
                block.invoke()
            else
                requestForSpecificPermission(
                    requestCode
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        } else {
            block.invoke()
        }
    }

}