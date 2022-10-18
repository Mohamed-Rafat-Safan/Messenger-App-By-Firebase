package com.mohamedrafat.messengerapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.constants.AppConstants
import com.mohamedrafat.messengerapp.ui.ChattingActivity
import java.util.*

class FirebaseNotificationService : FirebaseMessagingService() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {

            val map: Map<String, String> = remoteMessage.data

            val title = map["title"]
            val message = map["message"]
            val hisId = map["hisId"]
            val hisImage = map["hisImage"]
            val chatId = map["chatId"]

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                createOreoNotification(title!!, message!!, hisId!!, hisImage!!, chatId!!)
            else
                createNormalNotification(title!!, message!!, hisId!!, hisImage!!, chatId!!)

        }

    }

    private fun updateToken(token: String) {
        if (token.isNotEmpty()) {
            db.collection("Users").document(auth.currentUser!!.uid).update(mapOf("token" to token))
        }
    }


    private fun createNormalNotification(
        title: String,
        message: String,
        hisId: String,
        hisImage: String,
        chatId: String
    ) {

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .setSound(uri)

        val intent = Intent(this, ChattingActivity::class.java)

        intent.putExtra("uId", hisId)
        intent.putExtra("friendName", title)
        intent.putExtra("friendImage", hisImage)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(this, 2022, intent, PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(85 - 65), builder.build())

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createOreoNotification(
        title: String,
        message: String,
        hisId: String,
        hisImage: String,
        chatId: String
    ) {

        val channel = NotificationChannel(
            AppConstants.CHANNEL_ID, "Message",
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val intent = Intent(this, ChattingActivity::class.java)

        intent.putExtra("uId", hisId)
        intent.putExtra("friendName", title)
        intent.putExtra("friendImage", hisImage)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 2022, intent, PendingIntent.FLAG_ONE_SHOT)

        val notification = Notification.Builder(this, AppConstants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(100, notification)
    }


}