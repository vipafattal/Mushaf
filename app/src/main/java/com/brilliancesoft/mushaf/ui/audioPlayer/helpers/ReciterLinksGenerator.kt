package com.brilliancesoft.mushaf.ui.audioPlayer.helpers

/**
 * Created by  on
 */
object ReciterLinksGenerator {

    fun getLink(reciterLink:String, surahNumber: Int) :String = "$reciterLink/$surahNumber.mp3"
}