package com.littlelemon.storyapp.data.preferences

data class UserModel(
    val email: String,
    val token: String,
    val password: String,
    val isLogin: Boolean = false,
)
