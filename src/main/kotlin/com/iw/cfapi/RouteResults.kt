package com.iw.cfapi


data class RouteResult(
    val name: String,
    val url: String,
    val path: String,
    val routeId: String,
    val spaceName: String,
    val spaceId: String,
    val orgName: String = "",
    val orgId: String = "",
    val apps: List<String>
)
