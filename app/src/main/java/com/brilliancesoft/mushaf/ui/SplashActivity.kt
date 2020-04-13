package com.brilliancesoft.mushaf.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.QuranViewModel
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import kotlinx.coroutines.*
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        MushafApplication.appComponent.inject(this)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var repository: Repository

    private lateinit var viewModel: QuranViewModel
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coroutineScope.launch() { repository.getAvailableReciters(true) }

        viewModel = viewModelOf(QuranViewModel::class.java, viewModelFactory)

        viewModel.getMainMushaf().observer(this) {
            launchActivity<MainActivity>()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancelChildren()
    }
}
