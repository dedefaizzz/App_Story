package com.dicoding.appstory.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.appstory.data.api.ApiService
import com.dicoding.appstory.data.model.UserModel
import com.dicoding.appstory.data.paging.StoryPagingSource
import com.dicoding.appstory.data.preference.UserPreferences
import com.dicoding.appstory.data.preference.userPreferencesDataStore
import com.dicoding.appstory.data.response.*
import com.dicoding.appstory.data.result.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val preferences: UserPreferences
) {

    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> get() = _storiesWithLocation

    private val _storyDetail = MutableLiveData<Story>()
    val storyDetail: LiveData<Story> get() = _storyDetail

    fun userRegister(name: String, email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(ResultState.Success(response.message))
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Email is already taken"
                else -> e.response()?.errorBody()?.let { errorBody ->
                    Gson().fromJson(errorBody.string(), ErrorResponse::class.java).message
                } ?: e.message()
            }
            emit(ResultState.Error(Throwable(errorMessage)))
        }
    }

    fun userLogin(email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.login(email, password)
            emit(ResultState.Success(response.loginResult?.token))
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.let { errorBody ->
                Gson().fromJson(errorBody.string(), LoginResponse::class.java).message
            } ?: e.message()
            emit(ResultState.Error(Throwable(errorMessage)))
        }
    }

    fun getRetrieveStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService,"Bearer $token")
            }
        ).liveData
    }

    fun fetchStoryDetail(token: String, id: String) {
        apiService.detailStory("Bearer $token", id).enqueue(object : Callback<DetailStoryResponse> {
            override fun onResponse(call: Call<DetailStoryResponse>, response: Response<DetailStoryResponse>) {
                if (response.isSuccessful) {
                    _storyDetail.value = response.body()?.story
                } else {
                    logError(response.message())
                }
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                logError(t.message)
            }
        })
    }

    fun submitImage(token: String, imageFile: File, description: String) = liveData {
        emit(ResultState.Loading)
        val descriptionBody = description.toRequestBody("text/plain".toMediaType())
        val imageBody = imageFile.asRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("photo", imageFile.name, imageBody)
        try {
            val response = apiService.addStory("Bearer $token", imagePart, descriptionBody)
            emit(ResultState.Success(response))
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.let { errorBody ->
                Gson().fromJson(errorBody.string(), AddNewStoryResponse::class.java).message
            } ?: e.message()
            emit(ResultState.Error(Throwable(errorMessage)))
        }
    }

    fun retrieveStoriesWithLocation(token: String) {
        apiService.getStoriesWithLocation("Bearer $token").enqueue(object : Callback<GetAllStoryResponse> {
            override fun onResponse(call: Call<GetAllStoryResponse>, response: Response<GetAllStoryResponse>) {
                if (response.isSuccessful) {
                    _storiesWithLocation.value = response.body()?.listStory
                } else {
                    logError(response.message())
                }
            }

            override fun onFailure(call: Call<GetAllStoryResponse>, t: Throwable) {
                logError(t.message)
            }
        })
    }

    fun retrieveUserSession(): Flow<UserModel> = preferences.getUserSession()

    suspend fun storeUserSession(user: UserModel) {
        preferences.saveUserSession(user)
    }

    suspend fun userLogout() {
        preferences.clearUserSession()
    }

    private fun logError(message: String?) {
        Log.e(TAG, "Error: ${message.orEmpty()}")
    }

    companion object {
        private const val TAG = "StoryRepository"
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, context: Context): StoryRepository {
            return instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, UserPreferences.apply {
                    init(context.userPreferencesDataStore)
                }).also { instance = it }
            }
        }
    }
}
