package com.littlelemon.storyapp.component.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.littlelemon.storyapp.component.adapter.di.Injection
import com.littlelemon.storyapp.data.repository.StoryRepository
import java.lang.IllegalArgumentException

class ViewModelFactory(private val repository: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun clearInstantFactory() {
            StoryRepository.clearInstance()
            INSTANCE = null
        }

        fun getInstantFactory(ctx: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ViewModelFactory(Injection.provideRepo(ctx)).also { INSTANCE = it }
            }
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            val constructor = modelClass.getConstructor(StoryRepository::class.java)
            constructor.newInstance(repository) as T
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("ViewModel class does not have the required constructor: ${modelClass.name}", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}", e)
        }
    }
}