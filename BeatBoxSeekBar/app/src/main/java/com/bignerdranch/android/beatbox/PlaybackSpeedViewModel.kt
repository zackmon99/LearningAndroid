package com.bignerdranch.android.beatbox

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class PlaybackSpeedViewModel(private val beatBox: BeatBox): BaseObservable() {
    var speed: Int? = null
        set(speed) {
            field = speed
            beatBox.speed = speed ?: 100
            notifyChange()
        }

    @get:Bindable
    val percent: Int?
        get() = speed

}