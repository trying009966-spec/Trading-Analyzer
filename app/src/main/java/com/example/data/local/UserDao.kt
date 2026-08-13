package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM profiles ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM profiles ORDER BY id DESC")
    suspend fun getAllUsers(): List<UserProfile>

    @Query("SELECT * FROM profiles WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Query("SELECT * FROM profiles WHERE owner_id = :ownerId LIMIT 1")
    suspend fun getUserById(ownerId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserProfile): Long

    @Query("UPDATE profiles SET status = :status WHERE owner_id = :ownerId")
    suspend fun updateUserStatus(ownerId: String, status: String)

    @Update
    suspend fun updateUser(user: UserProfile)
}
