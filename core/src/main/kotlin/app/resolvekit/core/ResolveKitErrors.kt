package app.resolvekit.core

sealed class ResolveKitFunctionError(message: String) : Exception(message) {
    class InvalidArguments(reason: String) : ResolveKitFunctionError("Invalid arguments: $reason")
    class UnknownFunction(val name: String) : ResolveKitFunctionError("Unknown function: $name")
    class DuplicateFunctionName(val name: String) : ResolveKitFunctionError("Duplicate function name: $name")
}

sealed class ResolveKitAPIClientError(message: String) : Exception(message) {
    object MissingAPIKey : ResolveKitAPIClientError("API key is missing")
    object InvalidResponse : ResolveKitAPIClientError("Invalid response from server")
    object ChatUnavailable : ResolveKitAPIClientError("Chat is unavailable")
    class ServerError(val statusCode: Int, val body: String) :
        ResolveKitAPIClientError("Server error $statusCode: $body")
    class MethodNotAllowed(val method: String, val path: String) :
        ResolveKitAPIClientError("Method not allowed: $method $path")
}

sealed class ResolveKitRuntimeError(message: String) : Exception(message) {
    class UnsupportedSDK(reason: String) : ResolveKitRuntimeError("Unsupported SDK: $reason")
    class DuplicateFunctionName(val name: String) : ResolveKitRuntimeError("Duplicate function name: $name")
}
