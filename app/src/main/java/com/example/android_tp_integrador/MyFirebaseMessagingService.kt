package com.example.android_tp_integrador

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Verifico si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Sin título"
            val body = remoteMessage.data["body"] ?: "Sin contenido"

            Log.d("FCM", "Mensaje recibido con data: $title - $body")

            // Muesstra la notificación en la barra de notificaciones
            showNotification(title, body)
        } else if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "Sin título"
            val body = remoteMessage.notification?.body ?: "Sin contenido"

            //showToast("$title: $body")

            Log.d("FCM", "Mensaje recibido con notificación: $title - $body")

            // Muestra la notificación en la barra de notificaciones
            showNotification(title, body)
        }
    }

    private fun showToast(message: String) {
        // Mostrar el Toast en el hilo principal
        val handler = android.os.Handler(mainLooper)
        handler.post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun showNotification(title: String, body: String) {
        val channelId = "default_channel_id"
        val channelName = "Default Channel"

        // Crea un intent para abrir la actividad principal al tocar la notificación
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Crea el gestor de notificaciones
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configurar el canal para versiones de Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Construcción de la notificación
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Muestro la notificación
        notificationManager.notify(0, notificationBuilder.build())
    }
}