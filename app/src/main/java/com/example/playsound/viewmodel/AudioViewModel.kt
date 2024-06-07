package com.example.playsound.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playsound.model.AudioModel
import com.example.playsound.service.PlayAudioService
import com.example.playsound.ui.PlaySoundFragment.Companion.ACTION_AUDIO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(private val app: Application) : ViewModel() {
    private val _audioList = MutableLiveData<List<AudioModel>>()
    val audioList: LiveData<List<AudioModel>> = _audioList

    private val _totalItem = MutableLiveData<Int>()
    val totalItem: LiveData<Int> = _totalItem

    private val _listAudioCanPlay = MutableLiveData<ArrayList<AudioModel>>()
    val listAudioCanPlay: LiveData<ArrayList<AudioModel>> = _listAudioCanPlay

    fun setTotalItem(total: Int) {
        _totalItem.value = total
    }

    fun clearListAudioCanPlay() {
        _listAudioCanPlay.value?.clear()
    }

    fun addItemListAudioCanPlay(audioModel: AudioModel) {
        val item = _listAudioCanPlay.value ?: ArrayList()
        item.add(audioModel)
        _listAudioCanPlay.value = item
        Log.e("_listAudioCanPlay", _listAudioCanPlay.value!!.size.toString())
    }

    fun removeItemListAudioCanPlay(audioModel: AudioModel) {
        _listAudioCanPlay.value?.remove(audioModel)
        Log.e("_listAudioCanPlay", _listAudioCanPlay.value!!.size.toString())
    }

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