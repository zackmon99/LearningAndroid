package com.bignerdranch.android.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.criminalintent.Crime
import java.util.*

// Data access object defined.
@Dao
interface CrimeDao {
    // getCrimes runs the query and returns a list inside a LiveData
    // Returning LiveData types automatically makes Queries executed by the background
    // thread so the Main (also known as UI thread) Thread does not get held up by
    // database queries
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    // getCrime runs the query and returns a Crime inside a LiveData
    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    // The SQL for these annotations is automatically generated!
    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}