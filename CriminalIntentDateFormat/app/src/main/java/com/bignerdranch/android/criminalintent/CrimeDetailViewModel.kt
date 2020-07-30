package com.bignerdranch.android.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CrimeDetailViewModel: ViewModel() {

    // Get the CrimeRepository handle
    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    // This is a transformation.  Transformations set up a trigger-response
    // relationship between two LiveData objects.  In this case, we set crimeIdLiveData
    // as the trigger.  The lambda function is the 'mapping' function whose input
    // is whatever crimeIdLiveData base type is (UUID).  It returns a LiveData object
    // using crimeRepository.getCrime().   This means crimeLiveData will be updated
    // every time crimeIdLiveData is updated (The lambda function is ran every time
    // crimeIdLiveData is updated)
    // Why do it this way rather than just using crimeLiveData and having loadCrime()
    // change crimeLiveData?  The answer is that you should
    // not expose LiveData object directly.  As for why loadCrime
    // doesn't just set crimeLiveData... I'm not sure. If we made it so, we
    // could do it that way, but we want crimeLiveData to be public so
    // so we can get the value and we don't
    // want it to be changed outside the class in that way, so we need to set up
    // this relationship!
    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            // call getCrime on repository, which in turn calls getCrime on the
            // DAO
            crimeRepository.getCrime(crimeId)
        }

    // We call this to change crimeIdLiveData's value to trigger all that above
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
        //crimeLiveData = crimeRepository.getCrime(crimeId)
    }

    // this calls updateCrime on the DAO
    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}