package com.readrops.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.readrops.db.Database
import com.readrops.db.entities.Tag
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TagDaoTest {

    private lateinit var database: Database
    private lateinit var account: Account

    @Before
    fun before() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()

        account = Account(type = AccountType.FRESHRSS).apply {
            id = database.accountDao().insert(this).toInt()
        }

        repeat(2) { time ->
            database.tagDao().insert(
                Tag(
                    name = "Tag $time",
                    remoteId = "tag_$time",
                    accountId = account.id
                )
            )
        }
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun upsertTagsTest() = runTest {
        val remoteTags = listOf(
            // updated tag
            Tag(
                name = "Updated Tag 0",
                remoteId = "tag_0",
                accountId = account.id
            ),

            // deleted tag
            //Tag(name = "Tag 1", remoteId = "tag_1", accountId = account.id)

            // new tag
            Tag(
                name = "Tag 2",
                remoteId = "tag_2",
                accountId = account.id
            )
        )

        val allTags = database.tagDao().upsertTags(remoteTags, account)

        assertEquals(2, allTags.size)

        assertTrue { allTags.any { it.name == "Updated Tag 0" } }
        assertFalse { allTags.any { it.remoteId == "tag_1" } }
        assertTrue { allTags.any { it.remoteId == "tag_2" } }
    }
}