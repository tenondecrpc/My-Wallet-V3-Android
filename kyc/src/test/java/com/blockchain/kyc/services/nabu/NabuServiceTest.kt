package com.blockchain.kyc.services.nabu

import com.blockchain.kyc.api.nabu.NABU_COUNTRIES
import com.blockchain.kyc.api.nabu.NABU_CREATE_USER
import com.blockchain.kyc.api.nabu.NABU_INITIAL_AUTH
import com.blockchain.kyc.api.nabu.NABU_SESSION_TOKEN
import com.blockchain.kyc.models.onfido.CheckResultAdapter
import com.blockchain.kyc.models.onfido.CheckStatusAdapter
import com.blockchain.testutils.MockedRetrofitTest
import com.blockchain.testutils.getStringFromResource
import com.blockchain.testutils.mockWebServerInit
import com.squareup.moshi.Moshi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.api.EnvironmentConfig

class NabuServiceTest {

    private lateinit var subject: NabuService
    private val moshi: Moshi = Moshi.Builder()
        .add(CheckResultAdapter())
        .add(CheckStatusAdapter())
        .build()
    private val server: MockWebServer = MockWebServer()
    private val environmentConfig: EnvironmentConfig = mock()

    @get:Rule
    val initMockServer = mockWebServerInit(server)

    @Before
    fun setUp() {
        subject = NabuService(
            environmentConfig,
            MockedRetrofitTest(moshi, server).retrofit
        )
    }

    @Test
    fun createUser() {
        // Arrange
        val guid = "GUID"
        val email = "EMAIL"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(CREATE_USER_RESPONSE)
        )
        // Act
        val testObserver = subject.createUser(
            path = NABU_CREATE_USER,
            email = email,
            guid = guid
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        // Check response
        val userId = testObserver.values().first()
        userId.userId `should equal to` "uniqueUserId"
        // Check URL
        val request = server.takeRequest()
        request.path `should equal to` "/$NABU_CREATE_USER"
    }

    @Test
    fun getAuthToken() {
        // Arrange
        val guid = "GUID"
        val email = "EMAIL"
        val userId = "USER_ID"
        val appVersion = "6.14.0"
        val deviceId = "DEVICE_ID"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromResource("com/blockchain/kyc/services/nabu/GetNabuOfflineToken.json"))
        )
        // Act
        val testObserver = subject.getAuthToken(
            path = NABU_INITIAL_AUTH,
            email = email,
            guid = guid,
            userId = userId,
            appVersion = appVersion,
            deviceId = deviceId
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        // Check response
        val (userIdResponse, token) = testObserver.values().first()
        userIdResponse `should equal to` "d753109e-34c2-42bd-82f1-cc90470234kf"
        token `should equal to` "d753109e-23jd-42bd-82f1-cc904702asdfkjf"
        // Check URL
        val request = server.takeRequest()
        request.path `should equal to` "/$NABU_INITIAL_AUTH?userId=$userId"
        // Check Headers
        request.headers.get("X-WALLET-GUID") `should equal` guid
        request.headers.get("X-WALLET-EMAIL") `should equal` email
        request.headers.get("X-APP-VERSION") `should equal` appVersion
        request.headers.get("X-CLIENT-TYPE") `should equal` NabuService.CLIENT_TYPE
        request.headers.get("X-DEVICE-ID") `should equal` deviceId
    }

    @Test
    fun getSessionToken() {
        // Arrange
        val guid = "GUID"
        val email = "EMAIL"
        val userId = "USER_ID"
        val offlineToken = "OFFLINE_TOKEN"
        val appVersion = "6.14.0"
        val deviceId = "DEVICE_ID"
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromResource("com/blockchain/kyc/services/nabu/GetNabuSessionToken.json"))
        )
        // Act
        val testObserver = subject.getSessionToken(
            path = NABU_SESSION_TOKEN,
            email = email,
            guid = guid,
            userId = userId,
            offlineToken = offlineToken,
            appVersion = appVersion,
            deviceId = deviceId
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        // Check response
        val tokenResponse = testObserver.values().first()
        tokenResponse.id `should equal to` "7af48b7c-af37-47da-b830-7cf5e6cbc52e"
        tokenResponse.token `should equal to` "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJyZXRhaW" +
            "wtY29yZSIsImV4cCI6MTUzMzY5NzI3MywiaWF0IjoxNTMzNjU0MDczLCJ1c2VySUQiOiJhMmMwODA1My0xNDQ" +
            "4LTQ2NjEtYmNhZS0yYzA5NmFhNzdjOTgiLCJqdGkiOiI3YWY0OGI3Yy1hZjM3LTQ3ZGEtYjgzMC03Y2Y1ZTZ" +
            "jYmM1MmUifQ.UzawGRtKsYX96vGhm_Hv8yXWFDqrIpeZt4eH2p6Eelk"
        // Check URL
        val request = server.takeRequest()
        request.path `should equal to` "/$NABU_SESSION_TOKEN?userId=$userId"
        // Check Header
        request.headers.get("authorization") `should equal` offlineToken
        request.headers.get("X-WALLET-GUID") `should equal` guid
        request.headers.get("X-WALLET-EMAIL") `should equal` email
        request.headers.get("X-APP-VERSION") `should equal` appVersion
        request.headers.get("X-CLIENT-TYPE") `should equal` NabuService.CLIENT_TYPE
        request.headers.get("X-DEVICE-ID") `should equal` deviceId
    }

    @Test
    fun getEeaCountries() {
        // Arrange
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(getStringFromResource("com/blockchain/kyc/services/nabu/GetEeaCountriesList.json"))
        )
        // Act
        val testObserver = subject.getEeaCountries(
            path = NABU_COUNTRIES
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        // Check Response
        val countryList = testObserver.values().first()
        countryList[0].code `should equal to` "AUT"
        // Check URL
        val request = server.takeRequest()
        request.path `should equal to` "/$NABU_COUNTRIES?region=eea"
    }

    companion object {
        private const val CREATE_USER_RESPONSE = "{\n" +
            "    \"userId\": \"uniqueUserId\"\n" +
            "}"
    }
}