package app.resolvekit.networking.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class FunctionBulkRequest(
    val functions: List<FunctionPayload>
)

@Serializable
data class FunctionPayload(
    val name: String,
    val description: String,
    @SerialName("parameters_schema") val parametersSchema: JsonObject,
    @SerialName("timeout_seconds") val timeoutSeconds: Int? = null,
    @SerialName("requires_approval") val requiresApproval: Boolean = true,
    val availability: FunctionAvailability? = null,
    @SerialName("pack_name") val packName: String? = null,
    val source: String = "app_inline"
)

@Serializable
data class FunctionAvailability(
    val platforms: List<String>? = null,
    @SerialName("min_os_version") val minOsVersion: String? = null,
    @SerialName("max_os_version") val maxOsVersion: String? = null,
    @SerialName("min_app_version") val minAppVersion: String? = null,
    @SerialName("max_app_version") val maxAppVersion: String? = null
)

@Serializable
data class RegisteredFunction(
    val id: String,
    @SerialName("app_id") val appId: String,
    val name: String,
    val description: String,
    @SerialName("parameters_schema") val parametersSchema: JsonObject,
    @SerialName("timeout_seconds") val timeoutSeconds: Int? = null,
    @SerialName("requires_approval") val requiresApproval: Boolean = true,
    @SerialName("is_active") val isActive: Boolean = true,
    val availability: FunctionAvailability? = null,
    @SerialName("pack_name") val packName: String? = null,
    val source: String = "app_inline"
)
