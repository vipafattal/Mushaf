package com.brilliancesoft.mushaf.ui.quran.read.reciter


import android.os.Build
import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.api.FetchDownloadListener
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.sharedComponent.BaseFragment
import com.brilliancesoft.mushaf.utils.extensions.observeOnMainThread
import com.codebox.lib.android.fragments.removeFragment
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.fragment_reciters_downloading.*
import kotlin.math.roundToInt


class DownloadingFragment : BaseFragment() {
    init {
        playerDownloadingCancelled.onNext(false)
    }

    override val layoutId: Int = R.layout.fragment_reciters_downloading

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val closeIcon =
            if (MushafApplication.isDarkThemeEnabled) R.drawable.ic_close_light else R.drawable.ic_close_dark
        playerDownloadingCancel.setImageResource(closeIcon)
        playerDownloadingCancel.setOnClickListener {
            playerDownloadingCancelled.onNext(true)
            activity?.removeFragment(this)
        }
        FetchDownloadListener.progressListener.observeOnMainThread {
            val progress = it.roundToInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                playerDownloadingProgress?.setProgress(progress, true)
            else playerDownloadingProgress?.progress = progress

            if (progress == 100) activity?.removeFragment(this)


        }
    }

    companion object {
        val playerDownloadingCancelled: BehaviorSubject<Boolean> = BehaviorSubject.create()
    }

}
