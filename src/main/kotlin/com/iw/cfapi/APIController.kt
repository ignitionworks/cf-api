package com.iw.cfapi

import org.cloudfoundry.client.v3.PaginatedResponse
import org.cloudfoundry.client.v3.applications.ApplicationResource
import org.cloudfoundry.client.v3.applications.GetApplicationRequest
import org.cloudfoundry.client.v3.applications.GetApplicationResponse
import org.cloudfoundry.client.v3.applications.ListApplicationsRequest
import org.cloudfoundry.client.v3.applications.ListApplicationsResponse
import org.cloudfoundry.client.v3.organizations.GetOrganizationRequest
import org.cloudfoundry.client.v3.organizations.GetOrganizationResponse
import org.cloudfoundry.client.v3.routes.ListRoutesRequest
import org.cloudfoundry.client.v3.routes.ListRoutesResponse
import org.cloudfoundry.client.v3.routes.RouteResource
import org.cloudfoundry.client.v3.spaces.GetSpaceRequest
import org.cloudfoundry.client.v3.spaces.GetSpaceResponse
import org.cloudfoundry.reactor.DefaultConnectionContext
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider
import org.cloudfoundry.util.PaginationUtils
import org.cloudfoundry.util.tuple.TupleUtils
import org.reactivestreams.Publisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.*
import reactor.util.function.Tuple2

@RestController
class APIController {
    @GetMapping("/api/routes")
    fun routes(@RequestHeader("Authorization") token: String): Flux<RouteResult> {
        val connection = DefaultConnectionContext.builder().apiHost("api.cloud.ignition.works").build()
        val tokenProvider = ExistingTokenProvider(token)
        val cfClient = ReactorCloudFoundryClient.builder().connectionContext(connection).tokenProvider(tokenProvider).build()

        fun space(spaceId: String): Mono<GetSpaceResponse> {
            return cfClient.spacesV3().get(GetSpaceRequest.builder().spaceId(spaceId).build())
        }

        fun organisations(orgId: String): Mono<GetOrganizationResponse> {
            return cfClient.organizationsV3().get(GetOrganizationRequest.builder().organizationId(orgId).build())
        }

        fun routes(page: Int): Mono<ListRoutesResponse> {
           return cfClient.routesV3().list(ListRoutesRequest.builder().page(page).build())
        }

        fun apps(appIds: Iterable<String>): Mono<ListApplicationsResponse> {
            return cfClient.applicationsV3().list(ListApplicationsRequest.builder().applicationIds(appIds).build())
        }

        return PaginationUtils.requestClientV3Resources { page ->
            routes(page)
        }.flatMap { r ->
            apps(r.destinations.map { it.application.applicationId }).flatMap { a ->
                space(r.relationships.space.data.id).flatMap { s ->
                    organisations(s.relationships.organization.data.id).map { org ->
                        RouteResult(
                            name = r.host,
                            url = r.url,
                            path = r.path,
                            routeId = r.id,
                            spaceName = s.name,
                            spaceId = s.id,
                            orgName = org.name,
                            orgId = org.id,
                            apps = a.resources.map { it.name }
                        )
                    }
                }
            }
        }
    }
}
