package com.iw.cfapi

import org.cloudfoundry.reactor.ConnectionContext
import org.cloudfoundry.reactor.TokenProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import reactor.core.publisher.Mono

class ExistingTokenProvider(private val securityClient: OAuth2AuthorizedClientService, private val token: OAuth2AuthenticationToken) : TokenProvider {
    override fun getToken(connectionContext: ConnectionContext): Mono<String> {
        val oAuth2AuthorizedClient = securityClient.loadAuthorizedClient<OAuth2AuthorizedClient>(token.authorizedClientRegistrationId, token.name)
        return Mono.just("Bearer ${oAuth2AuthorizedClient.accessToken.tokenValue}")
    }
}
