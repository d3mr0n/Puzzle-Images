package by.app.puzzleimages

import android.R
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

@Suppress("DEPRECATION")
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeating_intent = Intent(context, MainActivity::class.java)
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            repeating_intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification_title: String =
            context.getResources().getString(by.app.puzzleimages.R.string.notification_title)
        val notification_summary: String =
            context.getResources().getString(by.app.puzzleimages.R.string.notification_summary)
        val builder = NotificationCompat.Builder(context)
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_lock_lock)
            .setContentTitle(notification_title)
            .setContentIntent(pendingIntent)
            .setContentText(notification_summary)
//            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setContentInfo("Info")

//        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sound_switch", true)) {
            if (intent?.action.equals("MY_NOTIFICATION_MESSAGE")) {
//                Toast.makeText(context, "is ON", Toast.LENGTH_SHORT).show()
                notificationManager.notify(100, builder.build())
//                Log.i("Notify", "Alarm");
            }
    }
}
