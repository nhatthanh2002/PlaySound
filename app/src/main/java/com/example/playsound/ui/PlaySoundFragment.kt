package com.example.playsound.ui

import android.content.Intent
import com.example.playsound.util.JsonReader
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.playsound.adapter.AudioAdapter
import com.example.playsound.databinding.FragmentPlaySoundBinding
import com.example.playsound.model.AudioModel
import com.example.playsound.service.PlayAudioService

class PlaySoundFragment : Fragment() {
    companion object {
        const val NAME_AUDIO = "name_audio"
        const val AUDIO_MODEL = "audio_model"
    }

    private val listAudio = ArrayList<AudioModel>()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaySoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stringJson = JsonReader.loadJSONFromAsset(requireContext(), "data/audios.json")
        val data = stringJson?.let { JsonReader.parseAudioList(it) }
        Log.e("DATA", data.toString())

        binding.apply {
            for (title in tabTitle) {
                tabLayout.addTab(tabLayout.newTab().setText(title))
            }
            audioAdapter = AudioAdapter(
                onClickItem = { model ->
                    if (listAudio.size < 8) {
                        listAudio.add(model)
                        Toast.makeText(requireContext(), model.audio, Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireActivity(), PlayAudioService::class.java)
                        intent.putExtra(AUDIO_MODEL, model)
                        requireActivity().startService(intent)
                    } else {
                        Toast.makeText(requireContext(), "Max is 8", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            audioAdapter.submitList(data)
            rvData.setHasFixedSize(true)
            rvData.adapter = audioAdapter

        }
    }
}