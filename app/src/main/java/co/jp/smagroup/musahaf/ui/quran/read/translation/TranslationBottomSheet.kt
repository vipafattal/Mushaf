package co.jp.smagroup.musahaf.ui.quran.read.translation


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.framework.data.repo.Repository
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.model.Translation
import co.jp.smagroup.musahaf.ui.commen.PreferencesConstants
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.library.manage.ManageLibraryActivity
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.onClicks
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import com.codebox.lib.extrenalLib.TinyDB
import com.codebox.lib.standard.collections.filters.singleIdx
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_translation.*
import kotlinx.android.synthetic.main.dialog_word_by_word.close_image
import kotlinx.android.synthetic.main.popup_translation_checkbox.view.*
import kotlinx.coroutines.*
import javax.inject.Inject

class TranslationBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var repository: Repository
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var viewModel: TranslationViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = viewModelOf(TranslationViewModel::class.java)
        viewModel.getTranslation()
            .observer(viewLifecycleOwner) { translation: Translation ->
                coroutineScope.launch {
                    val data = withContext(Dispatchers.IO) {
                        editionsToAyaTranslation(
                            translation,
                            translation.numberInMusahaf
                        )
                    }

                    val ayatText = if (data.isNotEmpty()) data.map { it.text } else listOf(getString(
                                            R.string.no_translation_tafeer_downloaded))
                    recycler_translation.adapter = TranslationQuranAdapter(ayatText)

                    translation_selection.setImageResource(if (MushafApplication.isDarkThemeEnabled) R.drawable.ic_language_light else R.drawable.ic_language_dark)
                    translation_selection.onClick {
                        val allData =
                            translation.selectedEditions.map { true to it } + translation.unSelectedEditions.map { false to it }
                        showPopup(this, allData, translation.numberInMusahaf)
                    }

                }
            }
        val closeIcon = if (MushafApplication.isDarkThemeEnabled) R.drawable.ic_close_light else R.drawable.ic_close_dark
        close_image.setImageResource(closeIcon)
        close_image.setOnClickListener {
            dismiss()
        }
    }

    private fun showPopup(view: View, data: List<Pair<Boolean, Edition>>, numberInMusahaf: Int) {
        val newData = data.toMutableList()
        val popupMenu = popupMenu {
            if (MushafApplication.isDarkThemeEnabled)
                style = R.style.Widget_MPM_Menu_Dark_DarkBackground
            section {
                for (element in data) {
                    customItem {
                        layoutResId = R.layout.popup_translation_checkbox
                        dismissOnSelect = false
                        viewBoundCallback = {
                            val isSelected = element.first
                            val edition = element.second
                            it.text_option.text = if (isRightToLeft == 1) edition.englishName else edition.name
                            it.checkbox_option.isChecked = isSelected

                            onClicks(it.popup_checkbox_root, it.text_option) {
                                it.checkbox_option.isChecked = !it.checkbox_option.isChecked
                            }
                            it.checkbox_option.setOnCheckedChangeListener { _, isChecked ->
                                val oldIdx = newData.singleIdx { it.second.identifier == element.second.identifier }
                                newData[oldIdx.second] = isChecked to edition
                            }
                        }
                    }
                }
                item {
                    label = "More Translations"
                    callback = {
                        context?.launchActivity<ManageLibraryActivity>()
                        dismiss()
                    }
                }
            }
        }

        popupMenu.setOnDismissListener {
            if (newData != data) {
                val selectedEditions: MutableList<Edition> = mutableListOf()
                val unSelectedEditions: MutableList<Edition> = mutableListOf()
                newData.forEach {
                    if (it.first)
                        selectedEditions.add(it.second)
                    else
                        unSelectedEditions.add(it.second)
                }
                val newSelectedData = selectedEditions.map { it.identifier }
                TinyDB(requireContext()).putListString(
                    PreferencesConstants.LastUsedTranslation,
                    newSelectedData
                )
                viewModel.setTranslationData(selectedEditions, unSelectedEditions, numberInMusahaf)

            }
        }
        popupMenu.show(requireContext(), view)
    }

    private fun extractEdition(translation: Translation): List<Edition> {
        return if (translation.unSelectedEditions.isNotEmpty() && translation.selectedEditions.isEmpty()) {
            listOf(translation.unSelectedEditions[0])
        } else if (translation.unSelectedEditions.isEmpty() && translation.selectedEditions.isEmpty())
            emptyList()
        else
            translation.selectedEditions
    }


    private suspend fun editionsToAyaTranslation(translation: Translation, numberInMusahaf: Int): List<Aya> {
        val editionToGet = extractEdition(translation)
        return editionToGet.map {
            repository.getAyaByNumberInMusahaf(it.identifier, numberInMusahaf)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_translation, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { Dialog ->
            val d = Dialog as BottomSheetDialog

            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?

            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > 0.5) close_image.visible()
                    else close_image.gone()
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        dismiss()
                }
            })
        }
        return dialog
    }


    override fun onDestroy() {
        super.onDestroy()
        (activity as BaseActivity).systemUiVisibility(true)
        job.cancelChildren()
    }

    init {
        MushafApplication.appComponent.inject(this)
    }

    companion object {
        const val TAG = "TranslationBottomSheet"
    }
}
