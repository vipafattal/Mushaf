package co.jp.smagroup.musahaf.ui.quran.read.helpers

import android.widget.ImageView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.ui.quran.read.ReadQuranActivity
import co.jp.smagroup.musahaf.utils.extensions.onClicks
import com.codebox.lib.android.utils.screenHelpers.dp
import com.codebox.lib.android.utils.screenHelpers.screenWidth
import com.codebox.lib.android.views.utils.invisible
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.activity_read_quran.*
import kotlinx.android.synthetic.main.popup_quran.*

/**
 * Created by ${User} on ${Date}
 */
class PopupActions(private val activity: ReadQuranActivity, private val clickListener: OnQuranPopupItemClickListener) {
    private var lastClickedPopupItemId = 0
    fun show(x: Int, y: Int, aya: Aya, doOnHide: (isBookmarkStatusChanged: Boolean) -> Unit) {
        activity.popup_parent.visible()
        menuSelectionPopup(x, y)
        activity.enableOnClicks(aya)
        activity.setOutSideClickListener(doOnHide)
    }

    private inline fun ReadQuranActivity.setOutSideClickListener(crossinline doOnHide: (isBookmarkStatusChanged: Boolean) -> Unit) {
        popup_parent.setOnClickListener {
            popup_parent.invisible()
            doOnHide.invoke(lastClickedPopupItemId == R.id.bookmark_popup)
            lastClickedPopupItemId = 0
        }
    }

    private fun ReadQuranActivity.enableOnClicks(aya: Aya) {
        onClicks(
            play_popup,
            share_popup,
            translate_popup,
            wordByWord_popup,
            bookmark_popup
        ) {
            lastClickedPopupItemId = id
            clickListener.popupItemClicked(aya, this)
            popup_parent.callOnClick()
        }
    }

    private fun menuSelectionPopup(x: Int, y: Int) {
        val yCoordinate =
            if (y - activity.popup_rootView.height < 0)
                activity.popup_rootView.height + dp(100)
            else {
                y - activity.popup_rootView.height - dp(50)
            }

        val xCoordinate =
            if (x + activity.popup_rootView.width > screenWidth) {
                screenWidth - activity.popup_rootView.width - dp(25)
            } else {
                x
            }
        val bubblePosition = when {
            x > activity.popup_rootView.width + dp(10) -> 0.90f
            x < activity.popup_rootView.width -> 0.1f
            else -> 0.5f
        }

        activity.popup_rootView.setPositionPer(bubblePosition)
        activity.popup_rootView.y = yCoordinate.toFloat()
        activity.popup_rootView.x = xCoordinate.toFloat()
    }


    interface OnQuranPopupItemClickListener {
        fun popupItemClicked(aya: Aya, view: ImageView)
    }
}