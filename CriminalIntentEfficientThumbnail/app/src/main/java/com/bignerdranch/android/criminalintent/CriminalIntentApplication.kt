package com.bignerdranch.android.criminalintent

import android.app.Application

class CriminalIntentApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the repository as the application starts
        CrimeRepository.initialize(this)
    }
}