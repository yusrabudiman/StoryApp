package com.littlelemon.storyapp.component.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.littlelemon.storyapp.data.repository.StoryRepository
import com.littlelemon.storyapp.data.response.ListStory

class StoryViewModel(private val repository: StoryRepository): ViewModel() {
    fun getStory() = repository.getStory()

    val quote: LiveData<PagingData<ListStory>> = repository.getQuote().cachedIn(viewModelScope)
}