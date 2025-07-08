package com.readrops.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.readrops.db.entities.account.Account

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["name", "account_id"], unique = true)
    ]
)
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "account_id", index = true) val accountId: Int = 0,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["item_id"]
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"]
        )
    ]
)
data class TagJoin(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "tag_id", index = true) val tagId: Int,
    @ColumnInfo(name = "item_id", index = true) val itemId: Int
)