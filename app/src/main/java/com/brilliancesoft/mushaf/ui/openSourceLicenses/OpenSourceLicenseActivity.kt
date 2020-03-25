package com.brilliancesoft.mushaf.ui.openSourceLicenses

import android.os.Bundle
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.ui.quran.sharedComponent.BaseActivity
import com.brilliancesoft.mushaf.utils.LocalJsonParser
import kotlinx.android.synthetic.main.activity_open_source_license.*

class OpenSourceLicenseActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_license)
        val data = getLicensesData()
        recycler_open_source.adapter = OpenSourceLicenseAdapter(data)
    }


    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun getLicensesData(): List<Models.License> {
        val parsedData = LocalJsonParser.parse("open_sources_licenses.json", Models.Data.serializer())
        return parsedData.licenses
    }
}
