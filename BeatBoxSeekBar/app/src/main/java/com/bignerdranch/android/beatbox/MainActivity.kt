package com.bignerdranch.android.beatbox

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.beatbox.databinding.ActivityMainBinding
import com.bignerdranch.android.beatbox.databinding.ListItemSoundBinding

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {


    private val beatBoxViewModel : BeatBoxViewModel by lazy {
        val factory = BeatBoxViewModelFactory(assets)
        ViewModelProvider(this@MainActivity, factory).get(BeatBoxViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate")

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)


        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = SoundAdapter(beatBoxViewModel.beatBox.sounds)
        }

        binding.viewModel = PlaybackSpeedViewModel(beatBoxViewModel.beatBox)

        binding.seekBar.apply {
            Log.d(TAG, "SETTING UP SEEK BAR")
            max = 200
            setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    Log.d(TAG, "PROGRESS BAR CHANGED TO $progress")
                    val realProgress = if(progress >= 13) {
                        progress
                    } else {
                        13
                    }
                    binding.viewModel?.speed = realProgress
                    binding.executePendingBindings()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })
        }
        binding.viewModel?.speed = 100
        binding.executePendingBindings()

    }


    private inner class SoundHolder(private val binding: ListItemSoundBinding):
            RecyclerView.ViewHolder(binding.root) {
        init {
            binding.viewModel = SoundViewModel(beatBoxViewModel.beatBox)
        }

        fun bind(sound: Sound) {
            binding.apply {
                viewModel?.sound = sound
                executePendingBindings()
            }
        }
    }

    private inner class SoundAdapter(private val sounds: List<Sound>): RecyclerView.Adapter<SoundHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
            val binding = DataBindingUtil.inflate<ListItemSoundBinding>(
                layoutInflater,
                R.layout.list_item_sound,
                parent,
                false
            )
            return SoundHolder(binding)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int) {
            val sound = sounds[position]
            holder.bind(sound)
        }

        override fun getItemCount() = sounds.size
    }
}