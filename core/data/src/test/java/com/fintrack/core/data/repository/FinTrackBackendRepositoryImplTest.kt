package com.fintrack.core.data.repository

import com.fintrack.core.common.DefaultDispatcherProvider
import com.fintrack.core.data.network.FinTrackBackendApi
import com.fintrack.core.domain.common.DomainResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class FinTrackBackendRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FinTrackBackendRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FinTrackBackendApi::class.java)
        repository = FinTrackBackendRepositoryImpl(api, DefaultDispatcherProvider())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `pingHealth returns true when status is ok`() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""{"status":"ok","service":"fintrack-backend"}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = repository.pingHealth()

        assertTrue(result is DomainResult.Success)
        assertEquals(true, (result as DomainResult.Success).data)
    }

    @Test
    fun `registerDevice succeeds on 201`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(
                    """{"device_id":"device-1","fcm_token":"","app_version":"1.0.0"}""",
                )
                .addHeader("Content-Type", "application/json"),
        )

        val result = repository.registerDevice(
            deviceId = "device-1",
            appVersion = "1.0.0",
        )

        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun `startMercadoPagoOAuth returns authorization url`() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""{"authorization_url":"https://auth.mercadopago.com/start"}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = repository.startMercadoPagoOAuth(deviceId = "device-1")

        assertTrue(result is DomainResult.Success)
        assertEquals(
            "https://auth.mercadopago.com/start",
            (result as DomainResult.Success).data,
        )
    }
}
