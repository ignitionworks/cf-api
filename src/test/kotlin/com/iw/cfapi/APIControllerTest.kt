package com.iw.cfapi

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test

@WireMockTest(httpPort = 8080)
class APIControllerTest {
    @Test
    fun testOK(runtime: WireMockRuntimeInfo) {
        stubFor(get("/api/routes")
            .withHeader("Authorization", containing("Bearer"))
            .willReturn(ok()
                .withHeader("Content-Type", "text/json")
                .withBody("<response>SUCCESS</response>")));

        val wireMock: WireMock = runtime.wireMock
        wireMock.register(get("/api/routes").willReturn(ok()))

        verify(getRequestedFor(urlPathEqualTo(""))
            .withHeader("Authorization", equalTo("Bearer")));
    }
}
