package com.example.playsound.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
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
        const val ACTION = "action"
        const val SEND_DATA_TO_ACTIVITY = "send_data_to_activity"
        const val IS_PLAY_AUDIO = "is_play_audio"
        const val OBJECT_AUDIO = "abject_audio"
        const val AUDIO_ACTION_SERVICE = "audio_action"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingAudio: Boolean = false
    private var mAudioModel: AudioModel? = null
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
        if (audioModel != null) {
            mAudioModel = audioModel
        }

        if (mAudioModel != null) {
            serviceScope.launch {
                startAudio(mAudioModel!!)
            }
            sendNotification(mAudioModel!!)
        }
        val actionAudio = intent?.getIntExtra(ACTION_AUDIO, 0)
        actionAudio?.let { handleActionAudio(it) }

        return START_STICKY
    }


    private suspend fun startAudio(audioModel: AudioModel) {
        if (mediaPlayer != null) {
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        mediaPlayer = MediaPlayer()
        withContext(Dispatchers.IO) {
            try {
                val afd = assets.openFd("audio/${audioModel.audio}")
                mediaPlayer?.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    prepare()
                    withContext(Dispatchers.Main) {
                        start()
                        isPlayingAudio = true
                        sendActionToActivity(ACTION_START)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun handleActionAudio(action: Int) {
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
        }
    }

    private fun resumeAudio() {
        if (mediaPlayer != null && !isPlayingAudio) {
            mediaPlayer?.start()
            isPlayingAudio = true
            sendActionToActivity(ACTION_RESUME)
            mAudioModel?.let { sendNotification(it) }
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer != null && isPlayingAudio) {
            mediaPlayer?.pause()
            isPlayingAudio = false
            sendActionToActivity(ACTION_PASSE)
            mAudioModel?.let { sendNotification(it) }
        }
    }


    @SuppressLint("ForegroundServiceType", "RemoteViewLayout")
    private fun sendNotification(audioModel: AudioModel) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val remoteViews = RemoteViews(packageName, R.layout.layout_custom_notification).apply {
            setTextViewText(R.id.tvNameAudioNoti, audioModel.audio)
            setTextViewText(R.id.tvNumber, "8 item")
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
            putExtra(AUDIO_ACTION_SERVICE, action)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        serviceJob.cancel()
    }
}