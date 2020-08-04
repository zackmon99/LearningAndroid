package com.bignerdranch.android.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

// We set this as an entity in the database.  Very important!  Also set primary key.
@Entity
data class Crime (@PrimaryKey val id: UUID = UUID.randomUUID(),
                  var title: String = "",
                  var date: Date = Date(),
                  var isSolved: Boolean = false,
                  var suspect: String = "",
                  var suspectPhoneNumber: String = "") {
    val photoFileName
        get() = "IMG_$id"
}