package com.brilliancesoft.mushaf.ui.common.dialog

/**
 * Created by ${User} on ${Date}
 */

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.framework.DownloadService
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.utils.extensions.observeOnMainThread
import com.codebox.lib.android.views.listeners.onClick
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_download.view.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import javax.inject.Inject
import kotlin.math.roundToInt

class DownloadDialog : DialogFragment() {

    lateinit var progressListener: ProgressListener
    private val job = SupervisorJob()
    private val disposables: MutableList<Disposable> = mutableListOf()
    private var dialogView: View? = null
    @Inject
    lateinit var repository: Repository

    init {
        MushafApplication.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        disposables += repository.loadingStream.filter { it > 0 }.observeOnMainThread { currentProgress ->
            if (currentProgress < DownloadService.PROGRESS_MAX) {
                val progress = currentProgress / (DownloadService.PROGRESS_MAX + 0f)
                dialogView?.loading_per!!.text = "${(progress * 100).roundToInt()}%"
            }
            else
            progressListener.onSuccess(this)
        }

        repository.errorStream.onNext("")

        disposables += repository.errorStream.filter { it != "" }.observeOnMainThread {
            activity?.let { context -> MushafToast.makeLong(context, it) }
            progressListener.onCancelled()
            dismiss()
        }

        dialogView?.download_background_button!!.onClick {
            progressListener.onBackground()
            dismiss()
        }
        dialogView?.download_cancel_button!!.onClick {
            repository.errorStream.onNext(getString(R.string.cancelled))
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AppTheme_TransparentDialog)
        dialogView = View.inflate(context, R.layout.dialog_download, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        builder.setCancelable(false)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                progressListener.onCancelled()
                activity?.let { MushafToast.makeShort(it, R.string.cancelled) }
            }
            return@setOnKeyListener false
        }

        return dialog
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.onEach { it.dispose() }
        job.cancelChildren()
        progressListener.onFinish()
        val baseActivity = (activity as BaseActivity)
        if (baseActivity.currentSystemVisibility)
            baseActivity.systemUiVisibility(true)
    }

    companion object {
        const val TAG = "Dialog-Progress"
    }

    interface ProgressListener {

        fun onSuccess(dialog: DownloadDialog) {}
        fun onError() {}
        fun onCancelled() {}
        fun onFinish() {}
        fun onBackground() {}
    }
}