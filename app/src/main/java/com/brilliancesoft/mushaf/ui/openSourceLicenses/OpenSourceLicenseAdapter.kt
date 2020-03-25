package com.brilliancesoft.mushaf.ui.openSourceLicenses

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brilliancesoft.mushaf.R
import com.codebox.lib.android.fragments.transaction
import com.codebox.lib.android.viewGroup.inflater
import com.codebox.lib.android.views.listeners.onClick
import kotlinx.android.synthetic.main.item_open_source_license.view.*

/**
 * Created by ${User} on ${Date}
 */
class OpenSourceLicenseAdapter(private val dataList: List<Models.License>) :
    RecyclerView.Adapter<OpenSourceLicenseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = parent.inflater(R.layout.item_open_source_license)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.bindData(data)


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(license: Models.License) {
            itemView.open_source_title.text = license.title
            itemView.onClick {
                val activity = context as OpenSourceLicenseActivity
                val fragment = OpenSourceDetailsFragment.newInstance(license)
                activity.supportFragmentManager.transaction {
                    replace(R.id.licenseViewHolder, fragment, OpenSourceDetailsFragment.TAG)
                    addToBackStack(null)
                }
            }
        }
    }


}