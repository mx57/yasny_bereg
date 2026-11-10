package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SobrietyDao {
    // Wellness Diary
    @Query("SELECT * FROM wellness_diary_entries ORDER BY timestampMillis DESC")
    fun observeDiaryEntries(): Flow<List<WellnessDiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: WellnessDiaryEntity): Long

    @Query("DELETE FROM wellness_diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntry(id: Long)

    // Physiological Stress & Vitals Readings
    @Query("SELECT * FROM physiological_readings ORDER BY timestampMillis DESC")
    fun observePhysiologicalReadings(): Flow<List<PhysiologicalReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhysiologicalReading(reading: PhysiologicalReadingEntity): Long

    // Motivational Bookmarks
    @Query("SELECT * FROM motivational_bookmarks ORDER BY bookmarkedAtMillis DESC")
    fun observeBookmarks(): Flow<List<MotivationalBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: MotivationalBookmarkEntity)

    @Query("DELETE FROM motivational_bookmarks WHERE itemId = :itemId")
    suspend fun removeBookmark(itemId: String)

    // Community Support Posts (Offline-ready local + user-contributed)
    @Query("SELECT * FROM community_support_posts ORDER BY timestampMillis DESC")
    fun observeCommunityPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT COUNT(*) FROM community_support_posts")
    suspend fun countCommunityPosts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPosts(posts: List<CommunityPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPost(post: CommunityPostEntity)

    @Update
    suspend fun updateCommunityPost(post: CommunityPostEntity)
}
