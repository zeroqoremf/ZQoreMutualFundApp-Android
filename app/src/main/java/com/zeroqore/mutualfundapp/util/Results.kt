// app/src/main/java/com/zeroqore/mutualfundapp/util/Results.kt
package com.zeroqore.mutualfundapp.util

sealed class Results<out T> {
    data class Success<out T>(val data: T) : Results<T>()
    data class Error(val exception: Exception, val message: String? = null) : Results<Nothing>()
    object Loading : Results<Nothing>() // Optional: For initial loading state
}