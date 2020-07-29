package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.criminalintent.Crime

// This database stores Crime objects
@Database(entities = [ Crime::class ], version = 1, exportSchema = false)
// The typeconverters are found in CrimeTypeConverters
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase: RoomDatabase() {
    // This function must be defined so we get a Data Access Object!
    abstract fun crimeDao(): CrimeDao
}