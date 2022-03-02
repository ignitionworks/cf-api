package com.iw.cfapi

import org.cloudfoundry.reactor.ConnectionContext
import org.cloudfoundry.reactor.TokenProvider
import reactor.core.publisher.Mono

class ExistingTokenProvider(private val token: String) : TokenProvider {
    override fun getToken(connectionContext: ConnectionContext): Mono<String> {
        return Mono.just(token)
    }
}
