package com.littlelemon.storyapp.ui.page

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.littlelemon.storyapp.ui.auth.SignInActivity
import com.littlelemon.storyapp.ui.auth.SignUpActivity
import com.littlelemon.storyapp.databinding.ActivityWelcomeScreenBinding

class WelcomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityWelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActions()
        startAnimations()
    }
    private fun setupActions() {
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        binding.signUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
    private fun startAnimations() {
        fun createFadeInAnimation(view: View, duration: Long = 300): ObjectAnimator {
            return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.duration = duration
            }
        }
        val loopingTextAnimation = ObjectAnimator.ofFloat(
            binding.tvLogin, View.TRANSLATION_X, -30f, 30f
        ).apply {
            duration = 8000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val fadeInTitle = createFadeInAnimation(binding.title)
        val fadeInDescription = createFadeInAnimation(binding.desc)
        val fadeInLoginButton = createFadeInAnimation(binding.loginBtn)
        val fadeInSignUpButton = createFadeInAnimation(binding.signUpBtn)

        AnimatorSet().apply {
            playSequentially(
                fadeInTitle,
                fadeInDescription,
                AnimatorSet().apply {
                    playTogether(fadeInLoginButton, fadeInSignUpButton)
                }
            )
            start()
        }
        loopingTextAnimation.start()
    }
}
