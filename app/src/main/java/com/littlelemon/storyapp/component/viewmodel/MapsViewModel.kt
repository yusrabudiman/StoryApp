package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.ViewModel
import com.littlelemon.storyapp.data.repository.StoryRepository

class MapsViewModel(private val repository: StoryRepository): ViewModel() {
    fun getStoriesWithLocation() = repository.getStoriesLocal()
}