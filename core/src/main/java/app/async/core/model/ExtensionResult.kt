package app.async.core.model

/**
 * Sealed class representing the result of an extension operation.
 * This provides a consistent way to handle success and failure cases.
 */
sealed class ExtensionResult<out T> {
    /**
     * Successful result containing data
     */
    data class Success<T>(val data: T) : ExtensionResult<T>()
    
    /**
     * Failed result containing error information
     */
    data class Error(val exception: ExtensionException) : ExtensionResult<Nothing>()
    
    /**
     * Loading state for async operations
     */
    data object Loading : ExtensionResult<Nothing>()
    
    /**
     * Returns true if this result is successful
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Returns true if this result is an error
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Returns true if this result is loading
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Returns the data if successful, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Returns the data if successful, or throws the exception if error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }
}

/**
 * Exception types that can occur in extensions
 */
sealed class ExtensionException(
    message: String,
    val code: String
) : Exception(message) {
    
    /**
     * Network-related errors (connection, timeout, etc.)
     */
    class NetworkError(
        message: String,
        val statusCode: Int? = null
    ) : ExtensionException(message, "NETWORK_ERROR")
    
    /**
     * Parsing errors when processing response data
     */
    class ParseError(
        message: String,
        val rawData: String? = null
    ) : ExtensionException(message, "PARSE_ERROR")
    
    /**
     * Authentication/authorization errors
     */
    class AuthError(
        message: String
    ) : ExtensionException(message, "AUTH_ERROR")
    
    /**
     * Rate limiting errors
     */
    class RateLimitError(
        message: String,
        val retryAfterSeconds: Long? = null
    ) : ExtensionException(message, "RATE_LIMIT_ERROR")
    
    /**
     * Content not found errors
     */
    class NotFoundError(
        message: String
    ) : ExtensionException(message, "NOT_FOUND_ERROR")
    
    /**
     * Extension configuration errors
     */
    class ConfigurationError(
        message: String
    ) : ExtensionException(message, "CONFIGURATION_ERROR")
    
    /**
     * Extension not found errors
     */
    class ExtensionNotFound(
        message: String
    ) : ExtensionException(message, "EXTENSION_NOT_FOUND")
    
    /**
     * Generic extension errors
     */
    class GenericError(
        message: String,
        val errorCause: String? = null
    ) : ExtensionException(message, "GENERIC_ERROR")
} 
