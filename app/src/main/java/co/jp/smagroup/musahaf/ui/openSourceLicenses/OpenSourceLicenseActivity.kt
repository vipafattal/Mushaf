package co.jp.smagroup.musahaf.ui.openSourceLicenses

import android.os.Bundle
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.utils.LocalJsonParser
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
