package com.readrops.api.services.greader.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class GReaderFoldersTagsAdapterTest {

    private val adapter = Moshi.Builder()
        .add(GReaderFoldersTagsAdapter())
        .build()
        .adapter(GReaderFoldersTags::class.java)

    @Test
    fun validFoldersTest() {
        val stream = TestUtils.loadResource("services/greader/adapters/folders.json")

        val (folders, tags) = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(folders.size, 1)

        with(folders.first()) {
            assertEquals(name, "Blogs")
            assertEquals(remoteId, "user/-/label/Blogs")
        }

        assertEquals(2, tags.size)
    }
}