package com.example.playsound.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.playsound.service.PlayAudioService
import com.example.playsound.service.PlayAudioService.Companion.ACTION

class AudioPlayBroadcast : BroadcastReceiver() {
    companion object {
        const val ACTION_AUDIO = "action_audio"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val actionAudio = intent?.getIntExtra(ACTION, 0)
        val intentService = Intent(context, PlayAudioService::class.java)
        intentService.putExtra(ACTION_AUDIO, actionAudio)
        context?.startService(intentService)
    }
}