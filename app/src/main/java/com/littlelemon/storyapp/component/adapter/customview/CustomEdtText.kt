@file:Suppress("ClassName")

package com.littlelemon.storyapp.component.adapter.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import com.littlelemon.storyapp.R

class UsernameEdtText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialized()
    }
    private fun initialized() {
        hint = context.getString(R.string.enter_your_username)
        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val username = s.toString().trim()
                error = when {
                    username.split("\\s+".toRegex()).size < 2 -> {
                        context.getString(R.string.minimum_username_two_words_with_space)
                    }
                    username.length > 60 -> {
                        context.getString(R.string.maximum_60_character)
                    }
                    !username.matches("^[a-zA-Z\\s]+$".toRegex()) -> {
                        context.getString(R.string.username_only_allow_character_and_space)
                    }
                    else -> null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setBackgroundResource(android.R.color.holo_blue_light)
            } else {
                setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}

class edLoginEmail : AppCompatEditText {
    constructor(context: Context) : super(context) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialized()
    }

    private fun initialized() {
        hint = context.getString(R.string.enter_your_email)
        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                error = if (!Patterns.EMAIL_ADDRESS.matcher(s).matches() && s.isNotEmpty()) {
                    context.getString(R.string.invalid_email_address)
                } else {
                    null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setBackgroundResource(android.R.color.holo_blue_light)
            } else {
                setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}


class edLoginPassword : AppCompatEditText {

    constructor(context: Context) : super(context) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialized()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialized()
    }

    private fun initialized() {
        hint = context.getString(R.string.enter_your_password)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error = if (s.isNullOrEmpty() || s.length < 8) {
                    context.getString(R.string.password_must_be_at_least_8_characters)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
