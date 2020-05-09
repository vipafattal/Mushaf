package com.brilliancesoft.mushaf.ui.quran.read.translation

import android.os.Bundle
import android.view.View
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.CustomToast
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.Translation
import com.brilliancesoft.mushaf.ui.common.PreferencesConstants
import com.brilliancesoft.mushaf.ui.common.ViewModelFactory
import com.brilliancesoft.mushaf.ui.common.dialog.BaseBottomSheetDialog
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.ui.library.manage.ManageLibraryActivity
import com.brilliancesoft.mushaf.utils.extensions.observer
import com.brilliancesoft.mushaf.utils.extensions.onClicks
import com.brilliancesoft.mushaf.utils.extensions.setStartDrawable
import com.brilliancesoft.mushaf.utils.extensions.viewModelOf
import com.codebox.lib.android.actvity.launchActivity
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import com.codebox.lib.extrenalLib.TinyDB
import com.codebox.lib.standard.collections.filters.singleIdx
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.dialog_translation.*
import kotlinx.android.synthetic.main.popup_translation_checkbox.view.*
import kotlinx.coroutines.*
import javax.inject.Inject

class TranslationBottomSheet : BaseBottomSheetDialog(R.layout.dialog_translation) {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val translationViewModel by lazy {
        viewModelOf(TranslationViewModel::class.java, viewModelFactory)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        translationViewModel.translations.observer(viewLifecycleOwner) { translation: Translation ->
            coroutineScope.launch {
                //@translationList represent aya in each selected edition.
                val translationList: List<Aya> = withContext(Dispatchers.IO) {
                    editionsToAyaTranslation(
                        translation,
                        translation.numberInMusahaf
                    )
                }
                if (translationList.isEmpty())
                    CustomToast.makeLong(
                        requireContext(),
                        R.string.no_translation_tafeer_downloaded
                    )
                recycler_translation.adapter = TranslationQuranAdapter(translationList)

                val languageImage =
                    if (UserPreferences.isDarkThemeEnabled) R.drawable.ic_language_light else R.drawable.ic_language_dark
                translation_selection.setStartDrawable(languageImage)
                translation_selection.onClick {
                    val allData =
                        translation.selectedEditions.map { true to it } + translation.unSelectedEditions.map { false to it }
                    showPopup(this, allData, translation.numberInMusahaf)
                }

            }
        }

        close_image.setOnClickListener {
            dismiss()
        }
    }

    private fun showPopup(view: View, data: List<Pair<Boolean, Edition>>, numberInMusahaf: Int) {
        val newData = data.toMutableList()
        val popupMenu = popupMenu {
            if (UserPreferences.isDarkThemeEnabled)
                style = R.style.Widget_MPM_Menu_Dark_DarkBackground
            section {
                for (element in data) {
                    customItem {
                        layoutResId = R.layout.popup_translation_checkbox
                        dismissOnSelect = false
                        viewBoundCallback = {
                            val isSelected = element.first
                            val edition = element.second
                            it.text_option.text =
                                if (isRightToLeft == 1) edition.englishName else edition.name
                            it.checkbox_option.isChecked = isSelected

                            onClicks(it.popup_checkbox_root, it.text_option) {
                                it.checkbox_option.isChecked = !it.checkbox_option.isChecked
                            }
                            it.checkbox_option.setOnCheckedChangeListener { _, isChecked ->
                                val oldIdx =
                                    newData.singleIdx { it.second.identifier == element.second.identifier }
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
                    if (it.first) selectedEditions.add(it.second)
                    else unSelectedEditions.add(it.second)
                }
                val newSelectedData = selectedEditions.map { it.identifier }
                TinyDB(requireContext()).putListString(
                    PreferencesConstants.LastUsedTranslation,
                    newSelectedData
                )
                translationViewModel.setAyaNumber(numberInMusahaf)

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


    private suspend fun editionsToAyaTranslation(
        translation: Translation,
        numberInMusahaf: Int
    ): List<Aya> {
        val editionToGet = extractEdition(translation)
        return editionToGet.map {
            repository.getAyaByNumberInMusahaf(it.identifier, numberInMusahaf)
        }
    }

    override fun onDialogScrolled(slideOffset: Float) {
        if (dialogOffsetLimit < slideOffset) close_image?.visible()
        else close_image?.gone()
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
        @JvmField
        val TAG: String = this::class.java.simpleName
    }
}
