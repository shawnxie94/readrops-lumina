package com.readrops.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.readrops.db.entities.Tag

@Dao
interface TagDao : BaseDao<Tag> {

    @Query("Select * From Tag Where account_id = :accountId")
    suspend fun selectAll(accountId: Int): List<Tag>
}