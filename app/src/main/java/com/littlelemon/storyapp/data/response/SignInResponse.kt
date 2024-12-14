package com.littlelemon.storyapp.data.response

import com.google.gson.annotations.SerializedName

data class SignInResponse(
    @field:SerializedName("loginResult")
    val loginResult: LoginRegister,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
) data class LoginRegister(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("token")
    val token: String
)
