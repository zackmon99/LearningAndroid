package com.bignerdranch.android.photogallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhotoGalleryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor().newInstance()
    }
}