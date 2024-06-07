package com.example.playsound.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
data class AudioModel(
    val audio: String = "",
    val name: String = "",
    val image: String = "",
    val isPremium: Boolean = false,
    val offsetTimeToLoop: Int = 0,
    var isPlay:Boolean = false
) : Parcelable