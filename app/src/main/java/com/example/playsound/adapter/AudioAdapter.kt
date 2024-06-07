package com.example.playsound.adapter

import android.os.Bundle
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
                it.isActivated = model.isPlay
                onClickItem.invoke(model)
            }
        }

        fun updateItem(bundle: Bundle) {
            with(binding) {
                with(bundle) {
                    if (containsKey("check")) {
                        itemLayout.isActivated = getBoolean("check")
                    }
                }
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

    override fun onBindViewHolder(
        holder: AudioViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty() || payloads[0] !is Bundle) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val bundle = payloads[0] as Bundle
            holder.updateItem(bundle)
        }
    }

    fun updateItem(position: Int, model: AudioModel) {
        val currentList = currentList.toMutableList()
        currentList[position] = model
        submitList(currentList) {
            notifyItemChanged(position, Bundle().apply {
                putBoolean("check", model.isPlay)
            })
        }
    }
}

class DiffCallBackAudio : DiffUtil.ItemCallback<AudioModel>() {
    override fun areItemsTheSame(oldItem: AudioModel, newItem: AudioModel): Boolean {
        return oldItem.audio == newItem.audio
    }

    override fun areContentsTheSame(oldItem: AudioModel, newItem: AudioModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: AudioModel, newItem: AudioModel): Any? {
        val diffBundle = Bundle()
        if (oldItem.audio != newItem.audio) {
            diffBundle.putString("audio", newItem.audio)
        }
        if (oldItem.isPlay != newItem.isPlay) {
            diffBundle.putBoolean("check", newItem.isPlay)
        }
        return if (diffBundle.size() == 0) null else diffBundle
    }

}