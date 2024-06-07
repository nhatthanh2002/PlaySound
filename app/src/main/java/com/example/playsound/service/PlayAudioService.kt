package com.example.playsound.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playsound.MyApplication.Companion.CHANNEL_ID
import com.example.playsound.R
import com.example.playsound.broadcast.AudioPlayBroadcast
import com.example.playsound.broadcast.AudioPlayBroadcast.Companion.ACTION_AUDIO
import com.example.playsound.model.AudioModel
import com.example.playsound.ui.MainActivity
import com.example.playsound.ui.PlaySoundFragment.Companion.AUDIO_MODEL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Suppress("DEPRECATION")
class PlayAudioService : Service() {
    companion object {
        const val ACTION_PASSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_CLEAR = 3
        const val ACTION_START = 4
        const val ACTION_STOP_SPECIFIC = 5 // New action for stopping a specific audio
        const val ACTION = "action"
        const val SEND_DATA_TO_ACTIVITY = "send_data_to_activity"
        const val IS_PLAY_AUDIO = "is_play_audio"
        const val OBJECT_AUDIO = "abject_audio"
        const val AUDIO_ACTION_SERVICE = "audio_action"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var isPlayingAudio: Boolean = false
    private var mAudioModel: AudioModel? = null

    private val mediaPlayerMap = mutableMapOf<String, MediaPlayer>()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (this.mAudioModel == null) {
            mAudioModel = AudioModel()
        }
        val audioModel = intent?.getParcelableExtra<AudioModel>(AUDIO_MODEL)
        Log.e("model1", audioModel.toString())
        if (audioModel != null) {
            Log.e("ISPLAY", audioModel.isPlay.toString())
            mAudioModel = audioModel
            serviceScope.launch {
                startAudio(audioModel.audio)
            }
        }
        Log.e("model2", mAudioModel.toString())
        val actionAudio = intent?.getIntExtra(ACTION_AUDIO, 0)
        Log.e("name_audio_input", mAudioModel!!.audio)
        actionAudio?.let { handleActionAudio(it, mAudioModel!!.audio) }
        Log.e("model3", mAudioModel.toString())
        Log.e("SIZE ITEM", mediaPlayerMap.size.toString())
        Log.e("AUDIO_NAME_START_COMMAND", mAudioModel!!.audio)

        return START_STICKY
    }

    private suspend fun startAudio(audioName: String) {
        withContext(Dispatchers.IO) {
            try {
                val mediaPlayer = MediaPlayer()
                val afd = assets.openFd("audio/${audioName}")
                mediaPlayer.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    prepare()
                    start()
                }
                mediaPlayerMap[audioName] = mediaPlayer
            } catch (e: IOException) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                isPlayingAudio = true
                sendNotification()
                sendActionToActivity(ACTION_START)
            }
        }
    }

    private fun handleActionAudio(action: Int, audioName: String?) {
        when (action) {
            ACTION_PASSE -> {
                pauseAudio()
            }

            ACTION_RESUME -> {
                resumeAudio()
            }

            ACTION_CLEAR -> {
                sendActionToActivity(ACTION_CLEAR)
                stopSelf()
            }

            ACTION_STOP_SPECIFIC -> {
                audioName?.let { stopSpecificAudio(it) }
            }
        }
    }

    private fun stopSpecificAudio(audioName: String) {
        Log.e("AUDIO_NAME", audioName)
        val mediaPlayer = mediaPlayerMap[audioName]
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayerMap.remove(audioName)
            Log.e("MapData", mediaPlayerMap.toString())
            if (mediaPlayerMap.isEmpty()) {
                isPlayingAudio = false
                stopSelf()
            }
            sendNotification()
            sendActionToActivity(ACTION_STOP_SPECIFIC)
        }
    }

    private fun resumeAudio() {
        if (!isPlayingAudio) {
            mediaPlayerMap.values.forEach { it.start() }
            isPlayingAudio = true
            sendActionToActivity(ACTION_RESUME)
            sendNotification()
        }
    }

    private fun pauseAudio() {
        if (isPlayingAudio) {
            mediaPlayerMap.values.forEach { it.pause() }
            isPlayingAudio = false
            sendActionToActivity(ACTION_PASSE)
            sendNotification()
        }
    }

    @SuppressLint("ForegroundServiceType", "RemoteViewLayout")
    private fun sendNotification() {
        Log.e("sendNotification", "$mediaPlayerMap")
        Log.e("MapItem", mediaPlayerMap.size.toString())
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteViews = RemoteViews(packageName, R.layout.layout_custom_notification).apply {
            setTextViewText(R.id.tvNameAudioNoti, "${mediaPlayerMap.size} Vật phẩm")
            setTextViewText(R.id.tvNumber, "Kết hợp hiện tại")
            setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_circle_pause)

            if (isPlayingAudio) {
                setOnClickPendingIntent(
                    R.id.ivPlayOrPause,
                    getPendingIntent(this@PlayAudioService, ACTION_PASSE)
                )
                setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_circle_pause)
            } else {
                setOnClickPendingIntent(
                    R.id.ivPlayOrPause,
                    getPendingIntent(this@PlayAudioService, ACTION_RESUME)
                )
                setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_circle_play)
            }

            setOnClickPendingIntent(
                R.id.ivClose,
                getPendingIntent(this@PlayAudioService, ACTION_CLEAR)
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setCustomContentView(remoteViews)
            .setSmallIcon(R.drawable.ic_premium_sound)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .build()

        startForeground(1, notification)
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent? {
        val intent = Intent(this, AudioPlayBroadcast::class.java)
        intent.putExtra(ACTION, action)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun sendActionToActivity(action: Int) {
        val intent = Intent(SEND_DATA_TO_ACTIVITY).apply {
            putExtra(OBJECT_AUDIO, mAudioModel)
            putExtra(IS_PLAY_AUDIO, isPlayingAudio)
            Log.e("IS_PLAY_AUDIO", isPlayingAudio.toString())
            putExtra(AUDIO_ACTION_SERVICE, action)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerMap.values.forEach { it.release() }
        mediaPlayerMap.clear()
        serviceJob.cancel()
    }
}
