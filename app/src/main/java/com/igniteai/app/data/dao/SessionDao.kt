package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.igniteai.app.data.model.SessionRecord

/**
 * Data access for session records.
 *
 * Sessions track the full lifecycle: consent → active → end/safeword.
 * This DAO provides the audit trail for consent and safeword events.
 */
@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionRecord)

    @Update
    suspend fun update(session: SessionRecord)

    @Query("SELECT * FROM session_record WHERE id = :id")
    suspend fun getById(id: String): SessionRecord?

    @Query("SELECT * FROM session_record ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestSession(): SessionRecord?

    @Query("SELECT * FROM session_record ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<SessionRecord>

    @Query("SELECT * FROM session_record WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActiveSession(): SessionRecord?

    @Query("SELECT COUNT(*) FROM session_record WHERE safewordTriggered = 1")
    suspend fun getSafewordCount(): Int

    @Query("DELETE FROM session_record")
    suspend fun deleteAll()
}
