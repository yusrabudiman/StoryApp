package com.littlelemon.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.component.adapter.customview.edLoginPassword
import com.littlelemon.storyapp.component.viewmodel.SignUpViewModel
import com.littlelemon.storyapp.component.viewmodel.ViewModelFactory
import com.littlelemon.storyapp.data.repository.ResultState
import com.littlelemon.storyapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var editText: edLoginPassword
    private val viewModel by viewModels<SignUpViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }
    private var isFirstLoadAnimation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        initBinding()
        edtText()
        setView()
        setAction()
        if (isFirstLoadAnimation) {
            animateViewSignUp()
            isFirstLoadAnimation = false
        }
    }

    private fun initBinding() {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun edtText() {
        editText = binding.pwEdtTxt
    }

    @Suppress("DEPRECATION")
    private fun setView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun animateViewSignUp() {
        val loopingTextAnimation = ObjectAnimator.ofFloat(
            binding.imageView, View.TRANSLATION_X, -30f, 30f
        ).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        fun createAlphaAnimator(view: View, duration: Long = 100): ObjectAnimator {
            return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.duration = duration
            }
        }

        val viewsToAnimate = listOf(
            binding.titlePage,
            binding.usernameText,
            binding.usernameEdtTxtLayout,
            binding.emailText,
            binding.emailEdtTxtLayout,
            binding.pwTextView,
            binding.pwEdtTxtLayout,
            binding.signUpBtn
        )

        val fadeInAnimations = viewsToAnimate.map { view ->
            createAlphaAnimator(view)
        }

        AnimatorSet().apply {
            playSequentially(fadeInAnimations)
            startDelay = 100
            start()
        }
        loopingTextAnimation.start()
    }

    private fun setAction() {
        binding.signUpBtn.setOnClickListener {
            val username = binding.usernameEdtTxt.text.toString()
            val email = binding.emailEdtTxt.text.toString()
            val password = binding.pwEdtTxt.text.toString()

            viewModel.registering(username, email, password).observe(this) { user ->
                handleProgressBarVisibility(user)
                handleResultState(user)
            }
        }
    }

    private fun handleProgressBarVisibility(result: ResultState<*>) {
        binding.progressBar.visibility = when (result) {
            is ResultState.Loading -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    private fun handleResultState(result: ResultState<*>) {
        handleProgressBarVisibility(result)
        if (result is ResultState.Error) {
            val error = result.error
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        } else if (result is ResultState.Success) {
            showSuccessDialog()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.successful)
            setMessage(getString(R.string.register_successfully))
            setPositiveButton(R.string.continue_action) { _, _ ->
                val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }
}
