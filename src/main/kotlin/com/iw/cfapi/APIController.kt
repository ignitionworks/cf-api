package com.iw.cfapi

import org.cloudfoundry.client.v3.routes.ListRoutesRequest
import org.cloudfoundry.client.v3.routes.RouteResource
import org.cloudfoundry.reactor.DefaultConnectionContext
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider
import org.cloudfoundry.util.PaginationUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

import kotlinx.serialization.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import reactor.core.publisher.Mono

@Serializable
data class Token(val value: String)

@RestController
class APIController (
    @Autowired
    val clientService: OAuth2AuthorizedClientService
) {

    @GetMapping("/api/login")
    fun login(@RequestParam("username") username: String, @RequestParam("password") password: String): Mono<Token> {
        val connection = DefaultConnectionContext.builder().apiHost("api.cloud.ignition.works").build()
        val pToken = PasswordGrantTokenProvider.builder().password(password).username(username).build()
        return pToken.getToken(connection).map { Token(it) }
    }

    @GetMapping("/api/routes")
    fun routes(token: OAuth2AuthenticationToken): Flux<RouteResource> {
        val connection = DefaultConnectionContext.builder().apiHost("api.cloud.ignition.works").build()
        val tokenProvider = ExistingTokenProvider(clientService, token)
        val cfClient = ReactorCloudFoundryClient.builder().connectionContext(connection).tokenProvider(tokenProvider).build()

        val r = PaginationUtils.requestClientV3Resources { page ->
            cfClient.routesV3().list(ListRoutesRequest.builder().page(page).build())
        }
        return r
    }
}
