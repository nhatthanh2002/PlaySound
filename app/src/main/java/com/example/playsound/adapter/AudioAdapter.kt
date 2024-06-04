package com.example.playsound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.playsound.databinding.ItemAudioBinding
import com.example.playsound.model.AudioModel

class AudioAdapter(val onClickItem: (AudioModel) -> Unit) :
    ListAdapter<AudioModel, AudioAdapter.AudioViewHolder>(DiffCallBackAudio()) {

    inner class AudioViewHolder(private val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: AudioModel) {
            binding.audio = model
            binding.itemLayout.setOnClickListener {
                onClickItem.invoke(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return AudioViewHolder(
            ItemAudioBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiffCallBackAudio : DiffUtil.ItemCallback<AudioModel>() {
    override fun areItemsTheSame(oldItem: AudioModel, newItem: AudioModel): Boolean {
        return oldItem.audio == newItem.audio
    }

    override fun areContentsTheSame(oldItem: AudioModel, newItem: AudioModel): Boolean {
        return oldItem == newItem
    }

}