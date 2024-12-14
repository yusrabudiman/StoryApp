package com.littlelemon.storyapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login")
class UserPreferences private constructor(private val dataStore: DataStore<Preferences>){

    companion object{
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val IS_LOGIN = booleanPreferencesKey("isLogin")

        private var INSTANCE: UserPreferences? = null
        fun getInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return INSTANCE ?: synchronized(this){
                val instance = UserPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun saveSession(userModel: UserModel){
        dataStore.edit { pref ->
            pref[EMAIL_KEY] = userModel.email
            pref[PASSWORD_KEY] = userModel.password
            pref[TOKEN_KEY] = userModel.token
            pref[IS_LOGIN] = true
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { pref ->
            UserModel(
                pref[EMAIL_KEY] ?: "",
                pref[TOKEN_KEY] ?: "",
                pref[PASSWORD_KEY] ?: "",
                pref[IS_LOGIN] ?: false
            )
        }
    }

    suspend fun logout(){
        dataStore.edit { pref ->
            pref.clear()
        }
    }
}