package com.iw.cfapi

import org.cloudfoundry.client.v3.applications.ListApplicationsRequest
import org.cloudfoundry.client.v3.applications.ListApplicationsResponse
import org.cloudfoundry.client.v3.deployments.ListDeploymentsRequest
import org.cloudfoundry.client.v3.deployments.ListDeploymentsResponse
import org.cloudfoundry.client.v3.organizations.GetOrganizationRequest
import org.cloudfoundry.client.v3.organizations.GetOrganizationResponse
import org.cloudfoundry.client.v3.routes.ListRoutesRequest
import org.cloudfoundry.client.v3.routes.ListRoutesResponse
import org.cloudfoundry.client.v3.routes.RouteResource
import org.cloudfoundry.client.v3.spaces.GetSpaceRequest
import org.cloudfoundry.client.v3.spaces.GetSpaceResponse
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient
import org.cloudfoundry.util.PaginationUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.MalformedURLException
import java.net.URL

@RestController
class APIController ( private val cfClients: Map<String, ReactorCloudFoundryClient> ) {
    fun hostFor(url: String): String {
        val host = try {
            URL(url).host
        } catch(e: MalformedURLException) {
            URL("https://" + url).host
        } catch(e: MalformedURLException) {
            "."
        }
        return host.split(".").first()
    }

    fun apps(appIds: Iterable<String>): Flux<ListApplicationsResponse> {
        return Flux.merge(cfClients.map { (_, client) -> client.applicationsV3().list(ListApplicationsRequest.builder().applicationIds(appIds).build()) })
    }

    fun space(spaceId: String): Flux<GetSpaceResponse> {
        return Flux.merge(cfClients.map{ (_, client) -> client.spacesV3().get(GetSpaceRequest.builder().spaceId(spaceId).build())})
    }

    fun organisations(orgId: String): Flux<GetOrganizationResponse> {
        return Flux.merge(cfClients.map { (_, client) -> client.organizationsV3().get(GetOrganizationRequest.builder().organizationId(orgId).build()) })
    }

    fun deploys(appIds: List<String>): Flux<ListDeploymentsResponse> {
        return Flux.merge(cfClients.map { (_, client) -> client.deploymentsV3().list(ListDeploymentsRequest.builder().applicationIds(appIds).build()) })
    }

    fun routes(query: String): Flux<RouteResource> {
        return Flux.merge(cfClients.map { (_, client) ->
            PaginationUtils.requestClientV3Resources { page ->
                client.routesV3().list(ListRoutesRequest.builder().page(page).host(hostFor(query)).build())
            }
        })
    }

    @GetMapping("/api/routes/search")
    fun search(@RequestParam("query") query: String): Flux<RouteResult> {
        return routes(query).flatMap { r ->
            apps(r.destinations.map { it.application.applicationId }).flatMap { a ->
                deploys(a.resources.map { it.id }).flatMap { d ->
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
                                apps = a.resources.map { it.name },
                                deployments = d.resources.map { it.id }
                            )
                        }
                    }
                }
            }
        }
    }
}
