package com.littlelemon.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.littlelemon.storyapp.data.preferences.UserModel
import com.littlelemon.storyapp.data.preferences.UserPreferences
import com.littlelemon.storyapp.data.response.ErrorResponse
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.data.response.SignInResponse
import com.littlelemon.storyapp.data.response.SignUpResponse
import com.littlelemon.storyapp.data.response.UploadResponse
import com.littlelemon.storyapp.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences,
) {
    companion object {
        private var INSTANCE: StoryRepository? = null

        fun clearInstance() {
            INSTANCE = null
        }

        fun getInstance(apiService: ApiService, userPreferences: UserPreferences): StoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(apiService, userPreferences)
            }.also { INSTANCE = it }
    }

    fun getQuote(): LiveData<PagingData<ListStory>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { StoryPagingSources(apiService) }
        ).liveData
    }

    fun loadImage(image: File, description: String, lat: Float? = null, lon: Float? = null): LiveData<ResultState<UploadResponse>> = liveData {
        emit(ResultState.Loading)
        try {
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
            val imageRequestBody = image.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData("photo", image.name, imageRequestBody)

            val response = when {
                lat == null && lon == null -> {
                    apiService.uploadImage(multipartBody, descriptionRequestBody)
                }
                else -> {
                    val latBody = lat.toString().toRequestBody("text/plain".toMediaType())
                    val lonBody = lon.toString().toRequestBody("text/plain".toMediaType())
                    apiService.uploadImage(multipartBody, descriptionRequestBody, latBody, lonBody)
                }
            }

            emit(ResultState.Success(response))
        } catch (e: HttpException) {
            val errorResponse = parseError(e)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }

    fun getStoriesLocal(): LiveData<ResultState<List<ListStory>>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.getStoryLocal()
            emit(ResultState.Success(response.listStory))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Error"))
        }
    }


    fun getStory(): LiveData<ResultState<List<ListStory>>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.getStory()
            emit(ResultState.Success(response.listStory))
        } catch (e: HttpException) {
            val errorResponse = parseError(e)
            emit(ResultState.Error(errorResponse.message))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Error"))
        }
    }

    fun login(email: String, password: String): LiveData<ResultState<SignInResponse>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.login(email, password)
            emit(ResultState.Success(response))
        } catch (e: HttpException) {
            val errorResponse = parseError(e)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun register(name: String, email: String, password: String): LiveData<ResultState<SignUpResponse>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(ResultState.Success(response))
        } catch (e: HttpException) {
            val errorResponse = parseError(e)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    suspend fun saveSession(userModel: UserModel) {
        userPreferences.saveSession(userModel)
    }

    fun getSession(): Flow<UserModel> {
        return userPreferences.getSession()
    }

    suspend fun logout() {
        userPreferences.logout()
    }

    private fun parseError(e: HttpException): ErrorResponse {
        val errorBody = e.response()?.errorBody()?.string()
        return Gson().fromJson(errorBody, ErrorResponse::class.java)
    }
}

sealed class ResultState<out R> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val error: String) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
}
