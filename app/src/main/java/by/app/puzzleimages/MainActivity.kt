package by.app.puzzleimages

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                "notifications_switch",
                true
            )
        ) {
            Log.i("Notify", "Alarm");
            NotificationReminder()
        }
        GameBtnClick()
        SettingsBtnClick()
        HelpBtnClick()
        ExitBtnClick()
    }

    fun NotificationReminder() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.HOUR, 7)
        calendar.set(Calendar.MINUTE, 0)

        val intent = Intent(getApplicationContext(), AlarmReceiver::class.java)
        intent.setAction("MY_NOTIFICATION_MESSAGE")
        val pendingIntent = PendingIntent.getBroadcast(
            getApplicationContext(),
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun ExitBtnClick() {
        exit_btn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Приложение закрыто...", Toast.LENGTH_SHORT).show()
            this.finishAffinity()
            System.exit(0)
        }
    }

    fun HelpBtnClick() {
        help_btn.setOnClickListener {
            val HelpDialog = AlertDialog.Builder(this)
            with(HelpDialog) {
                setTitle(R.string.help_title)
                setMessage(R.string.help_text)
                setIcon(android.R.drawable.ic_dialog_info)
                setPositiveButton("Закрыть", null)
                show()
            }
        }
    }

    fun SettingsBtnClick() {
        settings_btn.setOnClickListener {
            val SettingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(SettingsIntent)
        }
    }

    fun GameBtnClick() {
        play_btn.setOnClickListener {
            startActivity(Intent(this, ChooseGameActivity::class.java))
        }
    }
}
