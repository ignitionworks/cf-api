package com.iw.cfapi

import org.cloudfoundry.reactor.DefaultConnectionContext
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux

data class FoundationConfig(
    val api: String,
    val name: String
)

data class ServiceAccount(
    val name: String,
    val password: String,
)

@ConstructorBinding
@ConfigurationProperties(prefix="cf")
data class CfConfig(
    val foundations: List<FoundationConfig>,
    val serviceAccount: ServiceAccount
)

@Configuration
class CfClient {
    @Bean
    fun cfClientBuilder(config: CfConfig): Map<String, ReactorCloudFoundryClient> {
        return config.foundations.associate {
            val connection = DefaultConnectionContext.builder().apiHost(it.api).build()
            val provider = PasswordGrantTokenProvider.builder().username(config.serviceAccount.name).password(config.serviceAccount.password).build()
            it.name to ReactorCloudFoundryClient.builder().connectionContext(connection).tokenProvider(provider).build()
        }
    }
}
