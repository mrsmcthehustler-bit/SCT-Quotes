package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.SctTriggerAlert

object SctNotificationHelper {

    const val CHANNEL_ID = "sct_signals_channel"
    private const val CHANNEL_NAME = "SCT Quotes Signal Alerts"
    private const val CHANNEL_DESC = "Real-time alerts for selected pairs and Ninja Mode triggers"

    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, alert: SctTriggerAlert) {
        try {
            initChannel(context)

            // Android 13+ permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) return
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SELECTED_SYMBOL", alert.symbol)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                alert.symbol.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (alert.isNinjaAlert) {
                "🥷 [NINJA USDJPY] ${alert.triggerType.title}"
            } else {
                "⚡ [SCT ALERT] ${alert.symbol} • ${alert.triggerType.title}"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setContentTitle(title)
                .setContentText(alert.detail)
                .setStyle(NotificationCompat.BigTextStyle().bigText(alert.detail))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSubText(if (alert.isNinjaAlert) "Ninja Engine V2.80" else "SCT Theory Alert")

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (alert.symbol + alert.triggerType.name).hashCode()
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission denied or revoked by user
        } catch (_: Exception) {
            // Safe fallback
        }
    }
}
