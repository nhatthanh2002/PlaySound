package com.example.playsound.util

import android.content.Context
import com.example.playsound.model.AudioModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException


object JsonReader {
    fun loadJSONFromAsset(context: Context, fileName: String): String? {
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

    fun parseAudioList(jsonString: String): List<AudioModel> {
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