package app.resolvekit.core

/**
 * Serializable metadata about a registered function, sent to the backend via
 * PUT /v1/functions/bulk.
 */
data class ResolveKitDefinition(
    val name: String,
    val description: String,
    val parametersSchema: JSONObject,
    val timeoutSeconds: Int?,
    val requiresApproval: Boolean,
    val availability: ResolveKitAvailability? = null,
    val packName: String? = null,
    val source: String = "app_inline"
)

/**
 * Optional availability constraints controlling when a function is eligible.
 * Mirrors the `availability` object sent to the backend.
 */
data class ResolveKitAvailability(
    val platforms: List<String>? = null,
    val minOsVersion: String? = null,
    val maxOsVersion: String? = null,
    val minAppVersion: String? = null,
    val maxAppVersion: String? = null
)
