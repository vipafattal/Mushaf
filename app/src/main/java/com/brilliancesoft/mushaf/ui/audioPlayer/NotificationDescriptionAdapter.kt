package com.brilliancesoft.mushaf.ui.audioPlayer

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import com.brilliancesoft.mushaf.R
import com.brilliancesoft.mushaf.model.Media
import com.brilliancesoft.mushaf.ui.common.sharedComponent.MushafApplication
import com.brilliancesoft.mushaf.ui.quran.read.ReadQuranActivity
import com.brilliancesoft.mushaf.utils.extensions.toBitmap
import com.codebox.lib.android.resoures.Image
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationDescriptionAdapter(private val playList: List<Media>) :
    PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return playList[window].title
    }

    override fun getCurrentContentText(player: Player): String? {
        val window = player.currentWindowIndex

        return playList[window].subtitle
    }

    //private val largeIcon by lazy { drawableOf(R.drawable.lock_sceen_image)!!.toBitmap() }
    private val logo: Bitmap by lazy { Image(R.drawable.launcher_app_logo)!!.toBitmap() }
    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        /* val window = player.currentWindowIndex
         val largeIcon = getLargeIcon(window)
         if (largeIcon == null && getLargeIconUri(window) != null) {
             // load bitmap async
             loadBitmap(getLargeIconUri(window), callback)
             return getPlaceholderBitmap()
         }*/

        return logo /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            largeIcon
        else
            logo*/
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {

        val notificationIntent = Intent(MushafApplication.appContext, ReadQuranActivity::class.java)

        return PendingIntent.getActivity(
            MushafApplication.appContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}