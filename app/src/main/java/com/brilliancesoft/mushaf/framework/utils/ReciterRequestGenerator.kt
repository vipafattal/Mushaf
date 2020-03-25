package com.brilliancesoft.mushaf.framework.utils

import androidx.annotation.Keep
import com.brilliancesoft.mushaf.framework.commen.MediaSourceBuilder
import com.brilliancesoft.mushaf.framework.commen.MushafConstants
import com.brilliancesoft.mushaf.model.Surah
import com.brilliancesoft.mushaf.ui.commen.sharedComponent.MushafApplication
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import java.io.File

/**
 * Created by ${User} on ${Date}
 */

@Keep
object ReciterRequestGenerator {

    fun createRequestFromFile(reciterName: String, reciterId: String, surah: Surah, ayaNumber: Int): Request {
        val file = File(
            MushafApplication.appContext.getExternalFilesDir(null),
            recitersFolder(reciterName, surah) + "$ayaNumber.mp3"
        )
        return createRequest(ayaNumber, reciterId, file.absolutePath)
    }

    private fun createRequest(number: Int, selectedReciterId: String, filePath: String): Request =
        Request(MediaSourceBuilder.linkGenerator(number, selectedReciterId), filePath).apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL
        }

    fun recitersFolder(reciterName: String, surah: Surah) =
        "${MushafConstants.AppName}/reciters/$reciterName/${surah.number}/"

}
