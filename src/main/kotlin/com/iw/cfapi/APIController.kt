package com.iw.cfapi

import org.cloudfoundry.client.v3.routes.ListRoutesRequest
import org.cloudfoundry.client.v3.routes.RouteResource
import org.cloudfoundry.reactor.DefaultConnectionContext
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider
import org.cloudfoundry.reactor.uaa.ReactorUaaClient
import org.cloudfoundry.uaa.tokens.Tokens
import org.cloudfoundry.util.PaginationUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.net.URI

@RestController
class APIController () {
    @GetMapping("/api/login")
    fun login(@RequestParam password: String, @RequestParam api: String, @RequestParam username: String): Tokens {
        val connection = DefaultConnectionContext.builder().apiHost(api).build()
        val token = PasswordGrantTokenProvider.builder().password(password).username(username).build()
        val uuaClient = ReactorUaaClient.builder().connectionContext(connection).tokenProvider(token).build()
        return uuaClient.tokens()
    }

    @GetMapping("/api/routes")
    fun routes(@RequestParam(defaultValue = "") token: String): Flux<RouteResource> {
        val searchUrl = "https://awareness.cloud.ignintion.works"
        val searchTerms = URI(searchUrl).host
        val connection = DefaultConnectionContext.builder().apiHost("api.cloud.ignition.works").build()
        val token = PasswordGrantTokenProvider.builder().password("password").username("admin").build()
        val cfClient = ReactorCloudFoundryClient.builder().connectionContext(connection).tokenProvider(token).build()
        val dopplerClient = ReactorDopplerClient.builder().connectionContext(connection).tokenProvider(token).build()
        val uuaClient = ReactorUaaClient.builder().connectionContext(connection).tokenProvider(token).build()

        val r = PaginationUtils.requestClientV3Resources { page ->
            cfClient.routesV3().list(ListRoutesRequest.builder().page(page).build())
        }

        return r

    }
}
