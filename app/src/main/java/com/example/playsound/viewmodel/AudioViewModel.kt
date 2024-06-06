package com.example.playsound.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playsound.model.AudioModel
import com.example.playsound.service.PlayAudioService
import com.example.playsound.service.PlayAudioService.Companion.AUDIO_ACTION_SERVICE
import com.example.playsound.service.PlayAudioService.Companion.IS_PLAY_AUDIO
import com.example.playsound.service.PlayAudioService.Companion.OBJECT_AUDIO
import com.example.playsound.ui.PlaySoundFragment.Companion.ACTION_AUDIO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltViewModel
class AudioViewModel @Inject constructor(private val app: Application) : ViewModel() {
    private val _audioList = MutableLiveData<List<AudioModel>>()
    val audioList: LiveData<List<AudioModel>> get() = _audioList


    fun sendActionToService(action: Int) {
        val intent = Intent(app, PlayAudioService::class.java)
        intent.putExtra(ACTION_AUDIO, action)
        app.startService(intent)
    }


    fun loadAudioListFromAsset(fileName: String) {
        val jsonString = loadJSONFromAsset(app, fileName)
        jsonString?.let {
            val audioList = parseAudioList(it)
            _audioList.value = audioList
        }
    }

    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    private fun parseAudioList(jsonString: String): List<AudioModel> {
        return try {
            val listType = object : TypeToken<List<AudioModel>>() {}.type
            val audioList = Gson().fromJson<List<AudioModel>>(jsonString, listType)
            audioList.take(10) // Lấy 10 phần tử đầu tiên
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }
}