package com.brilliancesoft.mushaf.ui.audioPlayer.helpers

import com.google.android.exoplayer2.extractor.Extractor
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor

class Mp3ExtractorsFactory : ExtractorsFactory {
    override fun createExtractors(): Array<Extractor> {
        return arrayOf(Mp3Extractor())
    }
}