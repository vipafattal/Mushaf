package co.jp.smagroup.musahaf.ui.quran.read.reciter

import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.model.Aya
import co.jp.smagroup.musahaf.utils.extensions.toBitmap
import com.codebox.lib.android.resoures.Image
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class PlayerNotificationAdapter(private val playList: List<Aya>, private val activity: AppCompatActivity) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return playList[window].surah!!.name
    }

    override fun getCurrentContentText(player: Player): String? {
        val window = player.currentWindowIndex
        return playList[window].numberInSurah.toString()
    }

    val icon = Image(R.drawable.ic_app_logo, activity)!!
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
        return icon.toBitmap()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? =
         PendingIntent.getActivity(activity,0,activity.intent, PendingIntent.FLAG_UPDATE_CURRENT)

}