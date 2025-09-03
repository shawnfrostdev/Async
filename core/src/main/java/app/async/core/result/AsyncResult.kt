package app.async.core.result

/**
 * A custom Result type for async operations that supports both success and error types
 */
sealed class AsyncResult<out T, out E> {
    
    /**
     * Success case with data
     */
    data class Success<out T>(val data: T) : AsyncResult<T, Nothing>()
    
    /**
     * Error case with error data
     */
    data class Error<out E>(val error: E) : AsyncResult<Nothing, E>()
    
    /**
     * Loading state (optional)
     */
    object Loading : AsyncResult<Nothing, Nothing>()
    
    /**
     * Check if result is success
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Check if result is error
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Check if result is loading
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Get data if success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Get error if error, null otherwise
     */
    fun getErrorOrNull(): E? = when (this) {
        is Error -> error
        else -> null
    }
    
    /**
     * Get data or throw exception
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw AsyncException("Result is error: $error")
        is Loading -> throw AsyncException("Result is still loading")
    }
    
    /**
     * Map success data to another type
     */
    inline fun <R> map(transform: (T) -> R): AsyncResult<R, E> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
    
    /**
     * Map error to another type
     */
    inline fun <R> mapError(transform: (E) -> R): AsyncResult<T, R> = when (this) {
        is Success -> this
        is Error -> Error(transform(error))
        is Loading -> this
    }
    
    /**
     * Flat map for chaining operations
     */
    inline fun <R> flatMap(transform: (T) -> AsyncResult<R, @UnsafeVariance E>): AsyncResult<R, E> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
    
    /**
     * Handle result with callbacks
     */
    inline fun onSuccess(action: (T) -> Unit): AsyncResult<T, E> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Handle error with callback
     */
    inline fun onError(action: (E) -> Unit): AsyncResult<T, E> {
        if (this is Error) action(error)
        return this
    }
    
    /**
     * Handle loading with callback
     */
    inline fun onLoading(action: () -> Unit): AsyncResult<T, E> {
        if (this is Loading) action()
        return this
    }
    
    companion object {
        /**
         * Create success result
         */
        fun <T> success(data: T): AsyncResult<T, Nothing> = Success(data)
        
        /**
         * Create error result
         */
        fun <E> error(error: E): AsyncResult<Nothing, E> = Error(error)
        
        /**
         * Create loading result
         */
        fun loading(): AsyncResult<Nothing, Nothing> = Loading
        
        /**
         * Convert Kotlin Result to AsyncResult
         */
        fun <T> fromResult(result: kotlin.Result<T>): AsyncResult<T, Throwable> {
            return result.fold(
                onSuccess = { success(it) },
                onFailure = { error(it) }
            )
        }
        
        /**
         * Convert to Kotlin Result (loses error type information)
         */
        fun <T> AsyncResult<T, *>.toResult(): kotlin.Result<T> = when (this) {
            is Success -> kotlin.Result.success(data)
            is Error -> kotlin.Result.failure(Exception(error.toString()))
            is Loading -> kotlin.Result.failure(Exception("Result is loading"))
        }
    }
}

/**
 * Custom exception for AsyncResult operations
 */
class AsyncException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Type aliases for common use cases
 */
typealias DataResult<T> = AsyncResult<T, String>
typealias UnitResult<E> = AsyncResult<Unit, E> 
