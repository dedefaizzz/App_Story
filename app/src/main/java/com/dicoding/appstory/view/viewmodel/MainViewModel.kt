package com.dicoding.appstory.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.appstory.data.model.UserModel
import com.dicoding.appstory.data.repository.StoryRepository
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val storiesList = storyRepository.storiesList

    val storyDetail = storyRepository.storyDetail

    val storiesWithLocation = storyRepository.storiesWithLocation

    fun userLogin(email: String, password: String) = storyRepository.userLogin(email, password)

    fun userRegister(name: String, email: String, password: String) = storyRepository.userRegister(name, email, password)

    fun fetchStories(authToken: String) = storyRepository.fetchAllStories(authToken)

    fun fetchStoryDetail(authToken: String, storyId: String) = storyRepository.fetchStoryDetail(authToken, storyId)

    fun submitImage(authToken: String, imageFile: File, imageDescription: String) = storyRepository.submitImage(authToken, imageFile, imageDescription)

    fun retrieveStoriesWithLocation(token: String) = storyRepository.retrieveStoriesWithLocation(token)

    fun storeUserSession(user: UserModel) {
        viewModelScope.launch {
            storyRepository.storeUserSession(user)
        }
    }

    fun retrieveUserSession(): LiveData<UserModel> {
        return storyRepository.retrieveUserSession().asLiveData()
    }

    fun userLogout() {
        viewModelScope.launch {
            storyRepository.userLogout()
        }
    }
}
