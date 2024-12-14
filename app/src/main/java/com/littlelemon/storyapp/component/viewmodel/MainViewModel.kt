package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.littlelemon.storyapp.data.preferences.UserModel
import com.littlelemon.storyapp.data.repository.StoryRepository

class MainViewModel(private val repository: StoryRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel>{
        return repository.getSession().asLiveData()
    }

    suspend fun logout() {
        repository.logout()
    }
}
