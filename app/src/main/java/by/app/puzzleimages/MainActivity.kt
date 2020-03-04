package by.app.puzzleimages

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                "notifications_switch",
                true
            )
        ) {
            Log.i("Notify", "Alarm")
            notificationReminder()
        }
    }

    fun btnClick(view: View) {
        when (view.id) {
            R.id.play_btn -> {
                startActivity(Intent(this, ChooseGameActivity::class.java))
            }
            R.id.results_btn -> {
                highScoreShow(this)
            }
            R.id.settings_btn -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.exit_btn -> {
                Toast.makeText(this@MainActivity, "Приложение закрыто...", Toast.LENGTH_SHORT)
                    .show()
                this.finishAffinity()
                exitProcess(0)
            }
            R.id.help_btn -> {
                val helpDialog = AlertDialog.Builder(this)
                with(helpDialog) {
                    setTitle(R.string.help_title)
                    setMessage(R.string.help_text)
                    setIcon(android.R.drawable.ic_dialog_info)
                    setPositiveButton("Закрыть", null)
                    show()
                }
            }
        }
    }

    private fun notificationReminder() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 7)
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.action = "MY_NOTIFICATION_MESSAGE"
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
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

    companion object {
        // Show Dialog of all results
        @SuppressLint("InflateParams")
        fun highScoreShow(context: Context) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.dialog_result, null)
            val dialog = Dialog(context)
            dialog.setContentView(view)
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            val dialogButton: Button = dialog.findViewById<View>(R.id.btn_close_result) as Button
            dialogButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }
}
