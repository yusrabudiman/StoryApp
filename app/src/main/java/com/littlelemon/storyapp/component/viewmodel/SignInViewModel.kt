package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.ViewModel
import com.littlelemon.storyapp.data.preferences.UserModel
import com.littlelemon.storyapp.data.repository.StoryRepository

class SignInViewModel(private val repository: StoryRepository): ViewModel() {
    fun login(email: String, password: String) =
        repository.login(email, password)

    suspend fun saveSession(userModel: UserModel) {
        repository.saveSession(userModel)
    }
}