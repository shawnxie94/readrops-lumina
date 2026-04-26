package com.readrops.api.services.lumina

import com.readrops.api.apiModule
import com.readrops.api.services.lumina.LuminaService.Companion.INTERNAL_TOKEN_HEADER
import com.readrops.api.utils.exceptions.HttpException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LuminaDataSourceTest : KoinTest {

    private val mockServer = MockWebServer()
    private val moshi = Moshi.Builder().build()
    private val bodyAdapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule)
    }

    @Before
    fun before() {
        mockServer.start()
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun reportUrlSendsInternalTokenAndBody() = runTest {
        val dataSource = get<LuminaDataSource> {
            parametersOf(mockServer.url("/").toString())
        }

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"1","slug":"article","status":"pending","source_url":"https://example.org/post"}""")
        )

        dataSource.reportUrl(
            url = "https://example.org/post",
            internalToken = "secret",
            skipAiProcessing = false
        )

        val request = mockServer.takeRequest()
        val body = bodyAdapter.fromJson(request.body)!!

        assertEquals("/backend/api/articles/report-url", request.path)
        assertEquals("secret", request.headers[INTERNAL_TOKEN_HEADER])
        assertEquals("https://example.org/post", body["url"])
        assertFalse(body["skip_ai_processing"] as Boolean)
    }

    @Test
    fun createArticleSendsInternalTokenAndBody() = runTest {
        val dataSource = get<LuminaDataSource> {
            parametersOf(mockServer.url("/").toString())
        }

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"1","slug":"article","status":"pending","source_url":"https://mp.weixin.qq.com/s/post"}""")
        )

        dataSource.createArticle(
            request = LuminaCreateArticleRequest(
                title = "WeChat article",
                contentHtml = "<p>Hello</p>",
                contentMd = null,
                sourceUrl = "https://mp.weixin.qq.com/s/post",
                topImage = "https://example.org/image.jpg",
                author = "Author",
                publishedAt = "2026-04-26",
                sourceDomain = "mp.weixin.qq.com",
                skipAiProcessing = false
            ),
            internalToken = "secret"
        )

        val request = mockServer.takeRequest()
        val body = bodyAdapter.fromJson(request.body)!!

        assertEquals("/backend/api/articles", request.path)
        assertEquals("secret", request.headers[INTERNAL_TOKEN_HEADER])
        assertEquals("WeChat article", body["title"])
        assertEquals("<p>Hello</p>", body["content_html"])
        assertEquals("https://mp.weixin.qq.com/s/post", body["source_url"])
        assertEquals("https://example.org/image.jpg", body["top_image"])
        assertEquals("Author", body["author"])
        assertEquals("2026-04-26", body["published_at"])
        assertEquals("mp.weixin.qq.com", body["source_domain"])
        assertFalse(body["skip_ai_processing"] as Boolean)
    }

    @Test
    fun reportUrlSendsSkipAiProcessingTrue() = runTest {
        val dataSource = get<LuminaDataSource> {
            parametersOf(mockServer.url("/").toString())
        }

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"1","slug":"article","status":"pending","source_url":"https://example.org/post"}""")
        )

        dataSource.reportUrl(
            url = "https://example.org/post",
            internalToken = "secret",
            skipAiProcessing = true
        )

        val request = mockServer.takeRequest()
        val body = bodyAdapter.fromJson(request.body)!!

        assertTrue(body["skip_ai_processing"] as Boolean)
    }

    @Test
    fun normalizeBaseUrlAddsBackendPathAndTrailingSlash() {
        assertEquals(
            "http://localhost:8080/backend/",
            LuminaDataSource.normalizeBaseUrl("http://localhost:8080")
        )
        assertEquals(
            "http://localhost:8080/backend/",
            LuminaDataSource.normalizeBaseUrl("http://localhost:8080/")
        )
        assertEquals(
            "http://localhost:8080/backend/",
            LuminaDataSource.normalizeBaseUrl("http://localhost:8080/backend")
        )
        assertEquals(
            "http://localhost:8080/backend/",
            LuminaDataSource.normalizeBaseUrl("http://localhost:8080/backend/")
        )
    }

    @Test
    fun reportUrlConflictThrowsHttpException() = runTest {
        val dataSource = get<LuminaDataSource> {
            parametersOf(mockServer.url("/").toString())
        }

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setBody("""{"code":"source_url_exists"}""")
        )

        val exception = assertFailsWith<HttpException> {
            dataSource.reportUrl(
                url = "https://example.org/post",
                internalToken = "secret",
                skipAiProcessing = false
            )
        }

        assertEquals(409, exception.code)
    }
}
