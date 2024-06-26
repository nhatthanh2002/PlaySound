package com.example.playsound.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playsound.R
import com.example.playsound.adapter.AudioAdapter
import com.example.playsound.databinding.FragmentPlaySoundBinding
import com.example.playsound.model.AudioModel
import com.example.playsound.service.PlayAudioService
import com.example.playsound.service.PlayAudioService.Companion.ACTION_CLEAR
import com.example.playsound.service.PlayAudioService.Companion.ACTION_PASSE
import com.example.playsound.service.PlayAudioService.Companion.ACTION_RESUME
import com.example.playsound.service.PlayAudioService.Companion.ACTION_START
import com.example.playsound.service.PlayAudioService.Companion.ACTION_STOP_SPECIFIC
import com.example.playsound.service.PlayAudioService.Companion.AUDIO_ACTION_SERVICE
import com.example.playsound.service.PlayAudioService.Companion.IS_PLAY_AUDIO
import com.example.playsound.service.PlayAudioService.Companion.OBJECT_AUDIO
import com.example.playsound.service.PlayAudioService.Companion.SEND_DATA_TO_ACTIVITY
import com.example.playsound.viewmodel.AudioViewModel
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class PlaySoundFragment : Fragment() {
    companion object {
        const val NAME_AUDIO = "name_audio"
        const val AUDIO_MODEL = "audio_model"
        const val ACTION_AUDIO = "action_audio"
        const val TOTAL_ITEM = "total_item"
    }

    private var isPlayAudio: Boolean = false
    private val viewModel: AudioViewModel by viewModels()
    private val listAudioCanPlay = ArrayList<AudioModel>()
    private lateinit var binding: FragmentPlaySoundBinding
    private lateinit var audioAdapter: AudioAdapter
    private val tabTitle = arrayListOf(
        "Tất cả",
        "Yêu thích",
        "ASMR",
        "Animal",
        "House",
        "Melodies",
        "Nature",
        "Sci-fi",
        "Water",
        "White Noise",
        "Life",
        "Brainwaves"
    )

    private lateinit var audioModel: AudioModel

    private val broadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                audioModel = intent.getParcelableExtra(OBJECT_AUDIO)!!
                isPlayAudio = intent.getBooleanExtra(IS_PLAY_AUDIO, false)
                val action = intent.getIntExtra(AUDIO_ACTION_SERVICE, 0)
                handleLayoutControlAudio(action)
            }
        }
    }

    private fun handleLayoutControlAudio(action: Int) {
        binding.apply {
            when (action) {
                ACTION_START -> {
                    layoutControlAudio.visibility = View.VISIBLE
                    showInfoAudio()
                    setStatusPlayOrPause()
                }

                ACTION_PASSE -> {
                    setStatusPlayOrPause()
                }

                ACTION_RESUME -> {
                    setStatusPlayOrPause()
                }

                ACTION_CLEAR -> {
                    layoutControlAudio.visibility = View.GONE
                    listAudioCanPlay.clear()
                }
            }
        }
    }

    private fun setStatusPlayOrPause() {
        binding.apply {
            if (isPlayAudio) {
                ivPlayOrPause.setImageResource(R.drawable.ic_circle_pause)
            } else {
                ivPlayOrPause.setImageResource(R.drawable.ic_circle_play)
            }
        }
    }

    private fun sendActionToService(action: Int) {
        val intent = Intent(requireContext(), PlayAudioService::class.java)
        intent.putExtra(ACTION_AUDIO, action)
        requireContext().startService(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun showInfoAudio() {
        binding.apply {
            viewModel.totalItem.observe(viewLifecycleOwner) {
                tvNameAudio.text = "$it Vật phẩm"
            }
            tvNumber.text = "Kết hợp hiện tại"
            ivPlayOrPause.setOnClickListener {
                if (isPlayAudio) {
                    sendActionToService(ACTION_PASSE)
                } else {
                    sendActionToService(ACTION_RESUME)
                }
            }

            ivClose.setOnClickListener {
                sendActionToService(ACTION_CLEAR)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaySoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver,
            IntentFilter(SEND_DATA_TO_ACTIVITY)
        )

        binding.apply {
            audioAdapter = AudioAdapter(
                onClickItem = { model ->
                    val position = audioAdapter.currentList.indexOf(model)
                    if (position != -1) {
                        val intent = Intent(requireActivity(), PlayAudioService::class.java)
                        model.isPlay = !model.isPlay
                        if (listAudioCanPlay.size < 8) {
                            if (model.isPlay) {
                                listAudioCanPlay.add(model)
                                Toast.makeText(requireContext(), model.audio, Toast.LENGTH_SHORT)
                                    .show()
                                intent.putExtra(AUDIO_MODEL, model)
                                requireActivity().startService(intent)
                            } else {
                                listAudioCanPlay.remove(model)
                                sendActionToService(ACTION_STOP_SPECIFIC)
                                requireActivity().startService(intent)
                            }
                            viewModel.setTotalItem(listAudioCanPlay.size)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Max can play is 8",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        audioAdapter.updateItem(position, model)
                    }
                }
            )

            viewModel.apply {
                loadAudioListFromAsset("data/audios.json")
                audioList.observe(viewLifecycleOwner) { list ->
                    audioAdapter.submitList(list)
                }
            }

            for (title in tabTitle) {
                tabLayout.addTab(tabLayout.newTab().setText(title))
            }
            rvData.setHasFixedSize(true)
            rvData.adapter = audioAdapter

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }
}