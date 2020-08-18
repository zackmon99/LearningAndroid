package com.bignerdranch.android.beatbox

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel

class BeatBoxViewModel(assets: AssetManager): ViewModel() {

    val beatBox = BeatBox(assets)
    override fun onCleared() {
        super.onCleared()
        beatBox.release()
    }
}