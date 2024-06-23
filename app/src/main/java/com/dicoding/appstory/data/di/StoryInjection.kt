package com.dicoding.appstory.data.di

import android.content.Context
import com.dicoding.appstory.data.api.ApiConfig
import com.dicoding.appstory.data.preference.UserPreferences
import com.dicoding.appstory.data.preference.userPreferencesDataStore
import com.dicoding.appstory.data.repository.StoryRepository

object StoryInjection {
    fun provideRepository(context: Context): StoryRepository {
        val dataStore = context.userPreferencesDataStore
        UserPreferences.init(dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(apiService, context)
    }
}
