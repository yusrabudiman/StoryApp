package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.ViewModel
import com.littlelemon.storyapp.data.repository.StoryRepository

class SignUpViewModel(private val repository: StoryRepository): ViewModel() {
    fun registering(name: String, email: String, password: String) =
        repository.register(name, email, password)
}