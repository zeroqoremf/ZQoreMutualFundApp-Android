// app/src/main/java/com/zeroqore/mutualfundapp/util/Results.kt
package com.zeroqore.mutualfundapp.util

sealed class Results<out T> {
    data class Success<out T>(val data: T) : Results<T>()
    // MODIFIED: Changed 'exception: Exception' to 'exception: Throwable'
    data class Error(val exception: Throwable, val message: String? = null) : Results<Nothing>()
    object Loading : Results<Nothing>() // Optional: For initial loading state
}