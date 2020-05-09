package com.brilliancesoft.mushaf.ui.common.sharedComponent

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.brilliancesoft.mushaf.framework.di.AppComponent
import com.brilliancesoft.mushaf.framework.di.DaggerAppComponent
import com.codebox.lib.android.os.MagentaX
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.standard.delegation.DelegatesExt


/**
 * Created by ${User} on ${Date}
 */
class MushafApplication : MultiDexApplication() {
    companion object {
        var appComponent: AppComponent by DelegatesExt.notNullSingleValue()
            private set

        var appContext: MushafApplication by DelegatesExt.notNullSingleValue()
            private set

    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        initDagger()

        MagentaX.init(this)
        UserPreferences.init(this, AppPreferences())
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder().build()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}