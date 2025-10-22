
package com.aiplayer.store

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experience")
data class Experience(
    @PrimaryKey(autoGenerate = true) val id:Long = 0,
    val timestamp: Long,
    val framePath: String,
    val action: Int,
    val reward: Int
)
