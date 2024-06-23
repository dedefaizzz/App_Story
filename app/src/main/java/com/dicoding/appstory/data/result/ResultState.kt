package com.dicoding.appstory.data.result

sealed class ResultState<out T> {
    data class Success<out R>(val data: R) : ResultState<R>()
    data class Error(val exception: Throwable) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
}
