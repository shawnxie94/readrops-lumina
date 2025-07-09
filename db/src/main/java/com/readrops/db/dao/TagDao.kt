package com.readrops.db.dao

import androidx.room.Dao
import com.readrops.db.entities.Tag

@Dao
interface TagDao : BaseDao<Tag> {
}