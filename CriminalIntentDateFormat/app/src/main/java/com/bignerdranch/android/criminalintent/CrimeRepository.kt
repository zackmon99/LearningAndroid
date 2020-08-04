package com.bignerdranch.android.criminalintent

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.criminalintent.database.CrimeDatabase
import com.bignerdranch.android.criminalintent.database.migration_1_2
import com.bignerdranch.android.criminalintent.database.migration_2_3
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"
private const val TAG = "CrimeRepository"

// Private constructor here so only the companion object function initialize()
// can create an object.  The companion object holds a crime repository.
// This means this is a singleton object!
// We set up the database and DAO in here because we want a singleton object
// with these things so the application only needs database set up once.
class CrimeRepository private constructor(context: Context) {

    // Build the database
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).addMigrations(migration_2_3) //Migrate database
        .build()

    // Create the DAO
    private val crimeDao = database.crimeDao()

    // Set up executor so update and insert run in background thread.
    // Only queries automatically run in background threads when they return
    // LiveData objects
    private val executor = Executors.newSingleThreadExecutor()

    private val filesDir = context.applicationContext.filesDir

    // Use the repository to call the functions on the DAO
    fun getCrimes(): LiveData<List<Crime>> {
        Log.d(TAG, "getCrimes Called")
        return crimeDao.getCrimes()
    }

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    // Create new threads for update crime and add crime because this is not
    // done automatically like it is for queries when returning LiveData types
    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime): File = File(filesDir, crime.photoFileName)

    // Set up companion object to make singleton
    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }


        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized.")
        }
    }
}