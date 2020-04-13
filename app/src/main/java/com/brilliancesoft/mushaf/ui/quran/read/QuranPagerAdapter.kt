package com.brilliancesoft.mushaf.ui.quran.read

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.model.Aya
import com.brilliancesoft.mushaf.model.QuranFormattedPage
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.ui.common.sharedComponent.UserPreferences
import com.brilliancesoft.mushaf.ui.more.SettingsPreferencesConstant
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranicSpanText
import com.brilliancesoft.mushaf.ui.quran.read.helpers.PopupActions
import com.brilliancesoft.mushaf.ui.quran.read.helpers.QuranPageTextFormatter
import com.brilliancesoft.mushaf.ui.quran.read.page.PageSurahsAdapter
import com.brilliancesoft.mushaf.ui.quran.read.page.QuranActions
import com.brilliancesoft.mushaf.utils.TextActionUtil
import com.brilliancesoft.mushaf.utils.extensions.onScroll
import com.brilliancesoft.mushaf.utils.toLocalizedNumber
import com.codebox.lib.android.resoures.Stringify
import com.codebox.lib.android.utils.isRightToLeft
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.viewGroup.inflater
import kotlinx.android.synthetic.main.item_quran_page.view.*
import kotlinx.android.synthetic.main.popup_quran.view.*
import kotlinx.coroutines.CoroutineScope

class QuranPagerAdapter(
    coroutineScope: CoroutineScope,
    private val activity: BaseActivity,
    private val quranFormattedBySurah: Map<Int, List<Aya>>,
    private val startAtSurah: Int
) : RecyclerView.Adapter<QuranPagerAdapter.ViewHolder>() {

    private var popupView: View? = null
    private val selectedTextSizeType = UserPreferences.getFontSize()
    private val quranActions =
        QuranActions(
            activity,
            coroutineScope
        )
    var zoomOut: Boolean = false
        private set

    private val pageFormatter: QuranPageTextFormatter by lazy(LazyThreadSafetyMode.NONE) {
        val popupActions = PopupActions(popupView!!.popup_quran, popupActions)
        val textDecorator = QuranicSpanText(activity, popupActions)
        return@lazy QuranPageTextFormatter(textDecorator)
    }

    private val popupActions = object : PopupActions.PopupOnAyaClickListener {
        override fun popupItemClicked(aya: Aya, view: View) {
            when (view.id) {
                R.id.bookmark_popup -> quranActions.updateBookmarkState(aya)
                R.id.share_popup -> TextActionUtil.shareAya(activity, aya)
                R.id.play_popup -> quranActions.playReciter(aya)
                R.id.translate_popup -> quranActions.showAyaTranslation(aya.number)
                R.id.wordByWord_popup -> quranActions.showWordsTranslation(aya)
            }
        }

        override fun onShow() = activity.showSystemUI()
        override fun onDismissed() = activity.hideSystemUI()
    }

    override fun getItemCount(): Int = MushafConstants.QuranPages

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (popupView == null) popupView =
            View.inflate(activity, R.layout.popup_quran, parent.parent.parent as FrameLayout)
        return ViewHolder(
            parent.inflater(R.layout.item_quran_page),
            quranActions,
            selectedTextSizeType
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quranRawPage = quranFormattedBySurah.getValue(position + 1)
        holder.bind(pageFormatter.format(quranRawPage), zoomOut, startAtSurah)
    }

/*
  fun updateZoom(viewPager: ViewPager2, zoomOut: Boolean, page: Int) {
        this.zoomOut = zoomOut
        viewPager.preserveScrollPosition(page)
    }

    private fun ViewPager2.preserveScrollPosition(page: Int) {
        val oldManager =
            findViewWithTag<RecyclerView>(PAGE_CONTAINER_VIEW_TAG + page).layoutManager as LinearLayoutManager
        val firstItem = oldManager.findFirstVisibleItemPosition()
        val firstItemView = oldManager.findViewByPosition(firstItem)
        val topOffset = firstItemView?.top ?: 0

        notifyDataSetChanged()

        val recyclerView = findViewWithTag<RecyclerView>(PAGE_CONTAINER_VIEW_TAG + page)
        recyclerView.scrollToPosition(firstItem)
        recyclerView.scrollBy(0, topOffset)
        activity.hideSystemUI()

    }
    private fun ViewPager2.animateZoom(page: Int, zoomOut: Boolean) {
        val marginAndOffset = resources.getDimensionPixelSize(R.dimen.pageMarginAndoffset)

        for (position in (page - 15)..(page + 25)) {
            val cardView: CardView? = findViewWithTag(FULL_PAGE_TAG + position)
            if (cardView == null && position > page) break
            cardView?.apply {
                if (zoomOut) {
                    radius = dp(16).toFloat()
                    setMargins(marginAndOffset, dp(30), marginAndOffset, dp(30))
                } else {
                    translationX = 0f
                    translationY = 0f
                    radius = 0f
                    setMargins(0, 0, 0, 0)
                }
            }

        }

    */


    class ViewHolder(
        itemView: View,
        private val quranActions: QuranActions,
        @StringRes private val selectedTextSize: Int
    ) : RecyclerView.ViewHolder(itemView) {

        val activity = itemView.context as BaseActivity

        fun bind(
            quranFormattedPage: List<QuranFormattedPage>,
            zoomOut: Boolean,
            startAtSurah: Int
        ) {
            itemView.apply {
                //tag = FULL_PAGE_TAG + bindingAdapterPosition
                pageSurahsRecycler.adapter =
                    PageSurahsAdapter(
                        quranFormattedPage,
                        quranActions,
                        selectedTextSize
                    )
                pageSurahsRecycler.tag = PAGE_CONTAINER_VIEW_TAG + quranFormattedPage[0].aya.page
                pageSurahsRecycler.onScroll { _, dy ->
                    if (dy > 0) activity.hideSystemUI()
                    else if (dy < -dp(12)) activity.showSystemUI()
                }
                val aya = quranFormattedPage.first().aya
                //Juz ayaNumber & surah name in English or Arabic depending on local.
                juzNumberHeader.text =
                    "${Stringify(R.string.juz, context)} ${aya.juz.toString().toLocalizedNumber()}"

                surahNameHeader.text = if (isRightToLeft == 1) aya.surah!!.englishName
                else aya.surah!!.name

                if (startAtSurah != -1)
                    quranFormattedPage.firstOrNull { it.aya.surah_number == startAtSurah }?.let {
                        pageSurahsRecycler.scrollToSurah(quranFormattedPage, startAtSurah)
                    }

                /*
                                 val marginLayoutParams = layoutParams as ViewGroup.MarginLayoutParams

                 if (zoomOut) {
                      quranPageCard.radius = dp(16).toFloat()
                      marginLayoutParams.setMargins(
                          resources.getDimensionPixelSize(R.dimen.pageMarginAndoffset),
                          dp(30),
                          resources.getDimensionPixelSize(R.dimen.pageMarginAndoffset),
                          dp(30)
                      )
                  } else {
                      quranPageCard.radius = 0f
                      marginLayoutParams.setMargins(0, 0, 0, 0)
                  }*/
            }
        }

        private fun RecyclerView.scrollToSurah(
            quranFormattedPage: List<QuranFormattedPage>,
            startAtSurah: Int
        ) {
            val surahsNumberList = quranFormattedPage.map { it.aya.surah_number }
            if (surahsNumberList.lastIndex > 0) {
                val viewPosition = surahsNumberList.indexOfFirst { it == startAtSurah }
                scrollToPosition(viewPosition)
            }
        }
    }

    init {
        if (quranFormattedBySurah.isEmpty()) activity.finish()
    }

    companion object {
        //const val FULL_PAGE_TAG = "quran page view"
        const val PAGE_CONTAINER_VIEW_TAG = "pageScroller:"
    }


}


