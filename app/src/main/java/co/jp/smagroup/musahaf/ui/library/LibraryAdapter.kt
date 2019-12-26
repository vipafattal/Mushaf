package co.jp.smagroup.musahaf.ui.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Edition
import co.jp.smagroup.musahaf.model.ShortcutDetails
import co.jp.smagroup.musahaf.ui.commen.Fonts
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.library.read.ReadLibraryActivity
import co.jp.smagroup.musahaf.utils.Shortcut
import co.jp.smagroup.musahaf.utils.extensions.onClicks
import co.jp.smagroup.musahaf.utils.extensions.onLongClick
import com.codebox.lib.android.actvity.newIntent
import com.codebox.lib.android.viewGroup.inflater
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.item_library.view.*


/**
 * Created by ${User} on ${Date}
 */
class LibraryAdapter(private val dataList: List<Edition>) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_library)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        //val mContext = holder.itemView.context

        holder.bindData(data)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private fun editionToBundle(edition: Edition) = bundleOf(ReadLibraryActivity.EditionIdKey to edition.identifier)


        private fun getShortcutIntent(edition: Edition) = context.newIntent<ReadLibraryActivity>().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            action = "LOCATION_SHORTCUT"
            putExtras(editionToBundle(edition))
        }

        @SuppressLint("SetTextI18n")
        fun bindData(edition: Edition) {
            itemView.type_library.text = if (edition.type != Edition.Tafsir) edition.type else "تفسير"
            itemView.edition_name_library.text = edition.name
            itemView.edition_name_library.typeface = Fonts.getNormalFont(context, edition.language)
            onClicks(itemView.read_translation_button, itemView) { onItemClick(edition, itemView.context) }
            //In api 25 and above we're able to create create and shortcut to Icon launcher so we don't want to create popup for shortcuts.
            itemView.onLongClick { createShortcutPopup(edition) }
        }

        private fun createShortcutPopup(edition: Edition) {

            val shortcutPopup = popupMenu {
                if (MushafApplication.isDarkThemeEnabled)
                    style = R.style.Widget_MPM_Menu_Dark_DarkBackground
                section {
                    item {
                        label = context.getString(R.string.add_shortcut)
                        callback =
                            {
                                val shortcutDetails =
                                    ShortcutDetails(edition.identifier, edition.name, R.drawable.ic_book)
                                Shortcut.create(itemView.context, shortcutDetails, getShortcutIntent(edition))
                            }
                    }
                }
            }
            shortcutPopup.show(itemView.context, itemView)
        }


        private fun onItemClick(edition: Edition, context: Context) {

            val intent = context.newIntent<ReadLibraryActivity>()
            val bundle = editionToBundle(edition)
            intent.putExtras(bundle)

            val shortcutDetails = ShortcutDetails(edition.identifier, edition.name, R.drawable.ic_book)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                Shortcut.createDynamicShortcut(itemView.context, shortcutDetails, getShortcutIntent(edition))

            context.startActivity(intent)


        }

    }


}