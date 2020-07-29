package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {
    // Get the handle of the crimeRepository
    // (Was initialized in CriminalIntentApplication
    private val crimeRepository = CrimeRepository.get()

    // get the list of crimes by calling getCrimes on the repository which
    // in turn calls getCrimes on the DAO
    val crimesListLiveData = crimeRepository.getCrimes()
}