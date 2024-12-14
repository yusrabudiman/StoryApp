package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.ViewModel
import com.littlelemon.storyapp.data.repository.StoryRepository
import java.io.File

class UploadViewModel(private val repository: StoryRepository): ViewModel() {
    fun uploadImage(file: File, description: String, lat: Float? = null, lon: Float? = null) = repository.loadImage(file, description, lat, lon)
}