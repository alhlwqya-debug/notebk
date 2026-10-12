package com.ahd.notebk.notification
import com.ahd.notebk.security.createInternalAutoBackup
import com.ahd.notebk.MainActivity
import com.ahd.notebk.data.local.LedgerDatabase
import com.ahd.notebk.data.local.NotebkSettingsStore

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class WorkReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val settings = NotebkSettingsStore(appContext)

        // A cancelled or stale alarm must not reactivate reminders.
        if (!settings.getBoolean("daily_reminder_enabled", false)) return

        // Automatic backups are opt-in and run only with the daily reminder schedule.
        if (settings.getBoolean("auto_backup", false)) {
            val backupCreated = createInternalAutoBackup(appContext)
            if (backupCreated && settings.getBoolean("backup_notification", true)) {
                showBackupNotification(appContext)
            }
        }

        val database = LedgerDatabase(appContext)
        try {
            if (!database.hasWorkRecordedToday()) {
                showNotification(appContext)
            }
        } finally {
            database.close()
        }

        WorkReminderScheduler.schedule(appContext)
    }

    private fun showBackupNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = WorkReminderScheduler.CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكير تسجيل العمل",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            WorkReminderScheduler.OPEN_APP_REQUEST_CODE + 1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val settings = NotebkSettingsStore(context)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("اكتمل النسخ الاحتياطي")
            .setContentText("تم إنشاء نسخة احتياطية محلية من دفتر الخياط.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (settings.getBoolean("notification_sound", true)) builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        if (settings.getBoolean("notification_vibrate", true)) builder.setVibrate(longArrayOf(0, 180, 100, 180))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(2004, builder.build())
        }
    }

    private fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = WorkReminderScheduler.CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكير تسجيل العمل",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيه يومي عند عدم تسجيل العمل"
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            WorkReminderScheduler.OPEN_APP_REQUEST_CODE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val settings = NotebkSettingsStore(context)
        val soundEnabled = settings.getBoolean("notification_sound", true)
        val vibrateEnabled = settings.getBoolean("notification_vibrate", true)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("تذكير تسجيل العمل")
            .setContentText("لم تقم بتسجيل العمل اليوم، قم بتسجيل عملك")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("لم تقم بتسجيل العمل اليوم، قم بتسجيل عملك")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (soundEnabled) builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        if (vibrateEnabled) builder.setVibrate(longArrayOf(0, 250, 120, 250))
        val notification = builder.build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(WorkReminderScheduler.NOTIFICATION_ID, notification)
        }
    }
}

object WorkReminderScheduler {
    const val CHANNEL_ID = "daily_work_reminder"
    const val NOTIFICATION_ID = 2001
    const val ALARM_REQUEST_CODE = 2002
    const val OPEN_APP_REQUEST_CODE = 2003

    private const val DEFAULT_HOUR = 20
    private const val DEFAULT_MINUTE = 0

    private fun reminderTime(context: Context): Pair<Int, Int> {
        val value = NotebkSettingsStore(context).getString("reminder_time", "20:00")
        val parts = value.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: DEFAULT_HOUR
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: DEFAULT_MINUTE
        return hour to minute
    }

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, WorkReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        val (hour, minute) = reminderTime(context)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, WorkReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
