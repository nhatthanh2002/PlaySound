package com.example.playsound.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.playsound.ui.MainActivity
import com.example.playsound.MyApplication.Companion.CHANNEL_ID
import com.example.playsound.R
import com.example.playsound.broadcast.AudioPlayBroadcast
import com.example.playsound.broadcast.AudioPlayBroadcast.Companion.ACTION_AUDIO
import com.example.playsound.model.AudioModel
import com.example.playsound.ui.PlaySoundFragment.Companion.AUDIO_MODEL
import java.io.IOException


@Suppress("DEPRECATION")
class PlayAudioService : Service() {
    companion object {
        const val ACTION_PASSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_CLEAR = 3
        const val ACTION = "action"
    }

    private val mediaPlayers = arrayListOf<MediaPlayer>()
    private val listAudio = ArrayList<AssetFileDescriptor>()
    private var isPlayingAudio: Boolean = false
    private lateinit var mAudioModel: AudioModel
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioModel = intent?.getParcelableExtra<AudioModel>(AUDIO_MODEL)
        if (audioModel != null) {
            mAudioModel = audioModel
        }
        startAudio(getListAudio(mAudioModel))
        val actionAudio = intent?.getIntExtra(ACTION_AUDIO,0)
        actionAudio?.let { handleActionAudio(it) }
        Log.e("Count", getListAudio(audioModel).size.toString())
        sendNotification(mAudioModel)
        return START_NOT_STICKY
    }


    private fun getListAudio(audioModel: AudioModel?): List<AssetFileDescriptor> {
        val maxItem = 8
        if (listAudio.size < maxItem) {
            listAudio.add(assets.openFd("audio/${audioModel?.audio}"))
        }
        return listAudio
    }

    private fun startAudio(audioFiles: List<AssetFileDescriptor>) {
        for (afd in audioFiles) {
            try {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    prepare()
                    isLooping = true
                    start()
                }
                mediaPlayers.add(mediaPlayer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopAllAudio() {
        for (mediaPlayer in mediaPlayers) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
        mediaPlayers.clear()
    }

    private fun pauseAllAudio() {
        for (mediaPlayer in mediaPlayers) {
            if (mediaPlayer.isPlaying && isPlayingAudio) {
                mediaPlayer.pause()
                isPlayingAudio = false
            }
        }
    }

    private fun resumeAllAudio() {
        for (mediaPlayer in mediaPlayers) {
            if (!isPlayingAudio) {
                mediaPlayer.start()
                isPlayingAudio = true
            }
        }
    }

    private fun handleActionAudio(action: Int) {
        when (action) {
            ACTION_PASSE -> {
                pauseAllAudio()
            }

            ACTION_RESUME -> {
                resumeAllAudio()
            }

            ACTION_CLEAR -> {
                stopSelf()
            }
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
                setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_circle_play)
            } else {
                setOnClickPendingIntent(
                    R.id.ivPlayOrPause,
                    getPendingIntent(this@PlayAudioService, ACTION_RESUME)
                )
                setImageViewResource(R.id.ivPlayOrPause, R.drawable.ic_circle_pause)
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

    override fun onDestroy() {
        super.onDestroy()
    }
}