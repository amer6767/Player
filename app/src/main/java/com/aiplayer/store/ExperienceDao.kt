
package com.aiplayer.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExperienceDao {
    @Insert
    fun insert(exp: Experience)

    @Query("SELECT * FROM experience ORDER BY timestamp DESC LIMIT :limit")
    fun recent(limit: Int): List<Experience>

    @Query("DELETE FROM experience WHERE id IN (SELECT id FROM experience ORDER BY timestamp ASC LIMIT :count)")
    fun pruneOld(count: Int)
}
