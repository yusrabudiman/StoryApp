package com.littlelemon.storyapp.component.adapter.di

import android.content.Context
import com.littlelemon.storyapp.data.preferences.UserPreferences
import com.littlelemon.storyapp.data.preferences.dataStore
import com.littlelemon.storyapp.data.repository.StoryRepository
import com.littlelemon.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepo(context: Context): StoryRepository {
        val pref = UserPreferences.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        return StoryRepository.getInstance(apiService, pref)
    }
}