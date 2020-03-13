package by.app.puzzleimages

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager

@Suppress("DEPRECATION")
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(context, MainActivity::class.java)
        repeatingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            repeatingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationTitle: String =
            context.resources.getString(R.string.notification_title)
        val notificationSummary: String =
            context.resources.getString(R.string.notification_summary)
        val builder = NotificationCompat.Builder(context)
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(notificationTitle)
            .setContentIntent(pendingIntent)
            .setContentText(notificationSummary)
//            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setContentInfo("Info")

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "notifications_switch",
                false
            )
        ) {
            if (intent?.action.equals("MY_NOTIFICATION_MESSAGE")) {
                notificationManager.notify(100, builder.build())
                Log.d("Notify", "Alarm")
            }
        }
    }
}
