package com.brilliancesoft.mushaf.ui.library.read

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafToast
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.framework.data.repo.Repository
import com.brilliancesoft.mushaf.model.Edition
import com.brilliancesoft.mushaf.model.ReadTranslation
import com.brilliancesoft.mushaf.ui.common.Fonts
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.utils.TextActionUtil
import com.brilliancesoft.mushaf.utils.extensions.setTextSizeFromType
import com.brilliancesoft.mushaf.utils.extensions.updatePadding
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Colour
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.AppPreferences
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.invisible
import com.codebox.lib.android.views.utils.visible
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.item_library_read.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by ${User} on ${Date}
 */
class ReadLibraryAdapter(private val dataList: MutableList<ReadTranslation>,
                         private val repository: Repository,
                         private val coroutineScope:CoroutineScope) :
    RecyclerView.Adapter<ReadLibraryAdapter.ViewHolder>() {

    private val textSizeType = UserPreferences.getFontSize()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(R.layout.item_library_read, parent)

    override fun getItemCount(): Int = dataList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val isLastPosition = dataList.size - 1 == position
        holder.bindData(data, position, isLastPosition)
    }
    inner class ViewHolder(
        viewType: Int,
        parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflater(viewType)) {

        private val appPreferences = AppPreferences()

        fun bindData(readTranslation: ReadTranslation, position: Int, isLastPosition: Boolean) {
            val context = itemView.context
            if (position == 0) itemView.updatePadding(top = dp(65))
            else itemView.updatePadding(top = 0)

            val isTranslationWithAya =
                appPreferences.getBoolean(SettingsPreferencesConstant.TranslationWithAyaKey, true)

            if (isTranslationWithAya && readTranslation.editionInfo.type != Edition.Quran){
                itemView.aya_text.text =
                    readTranslation.quranicText
                itemView.aya_text.setTextSizeFromType(textSizeType)
            }
            //whiteSpaceMagnifier(readTranslation.quranicText)
            else itemView.aya_text.gone()

            itemView.translation_tafseer_text_library.typeface = Fonts.getTranslationFont(context,readTranslation.editionInfo.language)
            itemView.translation_tafseer_text_library.text = readTranslation.translationText
            itemView.translation_tafseer_text_library.setTextSizeFromType(textSizeType)
            updateViewToBookmarked(readTranslation.isBookmarked)

            itemView.aya_number_library.text = readTranslation.numberInSurah.toString().toLocalizedNumber()
            itemView.aya_number_library.setTextSizeFromType(textSizeType)

            itemView.item_read_library_root_view.onClick {
                createPopup(readTranslation, context as BaseActivity,position)
            }

            if (isLastPosition) itemView.divider_item_library.invisible()
            else itemView.divider_item_library.visible()
        }

        private fun updateViewToBookmarked(isBookmarked: Boolean) {
            if (isBookmarked) itemView.aya_number_library.setTextColor(Colour(R.color.colorAlert))
            else itemView.aya_number_library.setTextColor(Colour(R.color.colorSecondary))
        }

        private fun createPopup(readTranslation: ReadTranslation, activity: BaseActivity, index: Int) {
            val popupMenu = popupMenu {
                if (UserPreferences.isDarkThemeEnabled)
                    style = R.style.Widget_MPM_Menu_Dark_DarkBackground
                section {
                    item {
                        label = Stringify(R.string.share, activity)
                        callback = {
                            val shareText = convertTranslationToShare(readTranslation, activity)
                            TextActionUtil.shareText(activity, shareText, R.string.share_translation)
                            dismissOnSelect = true
                        }
                    }
                    item {
                        val shareText = convertTranslationToShare(readTranslation, activity)
                        label = Stringify(R.string.copy_text , activity)
                        callback = {
                            TextActionUtil.copyToClipboard(activity, shareText)
                            MushafToast.makeShort(activity, R.string.text_copied)
                            dismissOnSelect = true
                        }
                    }
                    item {
                        label = Stringify(R.string.save_in_bookmarks, activity)
                        callback = {
                            val oldData = dataList[index]
                            val newData =   oldData.copy(isBookmarked = !oldData.isBookmarked)
                            dataList[index] = newData

                            if (newData.isBookmarked) {
                                MushafToast.makeShort(itemView.context, R.string.bookmard_saved)
                                updateViewToBookmarked(true)
                            } else {
                                MushafToast.makeShort(itemView.context, R.string.bookmark_removed)
                                updateViewToBookmarked(false)
                            }
                            coroutineScope.launch(Dispatchers.IO) {
                            repository.updateBookmarkStatus(
                                readTranslation.ayaNumber,
                                readTranslation.editionInfo.identifier,
                                newData.isBookmarked
                            )
                            }
                        }
                    }
                }
            }
            popupMenu.show(activity, itemView.translation_tafseer_text_library)
        }

        private fun convertTranslationToShare(readTranslation: ReadTranslation, context: Context): String {
            var shareText = ""

            val ayaNumber = "${context.getString(R.string.aya_number)}: ${readTranslation.numberInSurah}\n"
            val pageNumber = "${context.getString(R.string.page)}: ${readTranslation.page}\n"
            val surahName = "${readTranslation.surah.name}\n"
            val ayaInfo = ayaNumber + pageNumber + surahName
            val ayaText = "{ ${readTranslation.quranicText} }\n$ayaInfo\n"

            shareText += ayaText

            if (readTranslation.editionInfo.identifier != MushafConstants.SimpleQuran) {
                val translationText = """ "${readTranslation.translationText}" """ + "\n"
                val translationInfo =
                    if (readTranslation.translationOrTafsir == Edition.Tafsir)
                    "${Stringify(R.string.tafseer, context)}: ${readTranslation.editionInfo.name}\n"
                    else
                        "${Stringify(
                            R.string.translation,
                            context
                        )}: ${readTranslation.editionInfo.name}\n"

                shareText += translationText + translationInfo
            }

            shareText += "via @${Stringify(R.string.app_name, context)} for Android"

            return shareText
        }
    }
}