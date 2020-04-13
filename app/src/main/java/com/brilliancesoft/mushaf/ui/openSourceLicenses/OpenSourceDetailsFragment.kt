package com.brilliancesoft.mushaf.ui.openSourceLicenses


import android.os.Bundle

import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.common.sharedComponent.BaseFragment
import kotlinx.android.synthetic.main.fragment_open_source_details.*


class OpenSourceDetailsFragment : BaseFragment() {

    private lateinit var licesnse: Models.License

    override val layoutId: Int = R.layout.fragment_open_source_details
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        libraryLink.text = licesnse.link
        libraryDetails.text = licesnse.licenseInfo
    }

    companion object {
        const val TAG = "Open-Source-Details-Fragment"

        @JvmStatic
        fun newInstance(license: Models.License): OpenSourceDetailsFragment {
            val fragment = OpenSourceDetailsFragment()
            fragment.licesnse = license
            return fragment
        }
    }
}
