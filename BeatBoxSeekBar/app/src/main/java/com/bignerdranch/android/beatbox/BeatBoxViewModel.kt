package com.bignerdranch.android.beatbox

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel

class BeatBoxViewModel(private val assets: AssetManager): ViewModel() {

    val beatBox = BeatBox(assets)
    override fun onCleared() {
        super.onCleared()
        beatBox.release()
    }
}