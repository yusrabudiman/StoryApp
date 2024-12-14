package com.littlelemon.storyapp.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.littlelemon.storyapp.MainActivity
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.component.adapter.customview.edLoginEmail
import com.littlelemon.storyapp.component.adapter.customview.edLoginPassword
import com.littlelemon.storyapp.component.viewmodel.SignInViewModel
import com.littlelemon.storyapp.component.viewmodel.ViewModelFactory
import com.littlelemon.storyapp.data.preferences.UserModel
import com.littlelemon.storyapp.data.repository.ResultState
import com.littlelemon.storyapp.data.response.SignInResponse
import com.littlelemon.storyapp.databinding.ActivitySignInBinding
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var emailEdt: edLoginEmail
    private lateinit var edtTxt: edLoginPassword
    private val viewModel by viewModels<SignInViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }
    private var isFirstLoadAnimation = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        initBinding()
        initEdtText()
        setFullScreenView()
        setupLoginAction()
        if (isFirstLoadAnimation) {
            animateViews()
            isFirstLoadAnimation = false
        }
    }

    private fun initBinding() {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initEdtText() {
        emailEdt = binding.edLoginEmail
        edtTxt = binding.edLoginPassword
    }

    @Suppress("DEPRECATION")
    private fun setFullScreenView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupLoginAction() {
        binding.loginBtn.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val pass = binding.edLoginPassword.text.toString()
            viewModel.login(email, pass).observe(this) { result ->
                handleResultState(result)
            }
        }
    }

    private fun animateViews() {
        val loopingTextAnimation = ObjectAnimator.ofFloat(
            binding.imageView, View.TRANSLATION_X, -30f, 30f
        ).apply {
            duration = 8000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        fun createFadeInAnimation(view: View, duration: Long = 100): ObjectAnimator {
            return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.duration = duration
            }
        }

        val viewsToAnimate = listOf(
            binding.titlePage,
            binding.msgPage,
            binding.emailText,
            binding.emailEdtTxtLayout,
            binding.pwTextView,
            binding.pwEdtTxtLayout,
            binding.loginBtn
        )

        val fadeInAnimations = viewsToAnimate.map { view -> createFadeInAnimation(view) }

        AnimatorSet().apply {
            playSequentially(fadeInAnimations)
            startDelay = 100
            start()
        }
        loopingTextAnimation.start()
    }

    private fun handleResultState(result: ResultState<*>) {
        when (result) {
            is ResultState.Loading -> handleProgressBarVisibility(true)
            is ResultState.Success<*> -> {
                handleProgressBarVisibility(false)

                val signInResponse = result.data as? SignInResponse
                if (signInResponse != null) {
                    showSuccessDialog(signInResponse)
                } else {
                    showErrorDialog()
                }
            }
            is ResultState.Error -> {
                handleProgressBarVisibility(false)
                showErrorDialog()
            }
        }
    }

    private fun handleProgressBarVisibility(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    private fun showSuccessDialog(data: SignInResponse) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.successful))
            setMessage(getString(R.string.message_successfully))
            setPositiveButton(getString(R.string.continue_action)) { _, _ ->
                saveSession(UserModel(data.loginResult.name, data.loginResult.token, data.loginResult.userId, true))
            }
            create()
            show()
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.wrong))
            setMessage(getString(R.string.wrong_email_or_password))
            setPositiveButton(getString(R.string.back)) { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun saveSession(session: UserModel) {
        lifecycleScope.launch {
            viewModel.saveSession(session)
            val intent = Intent(this@SignInActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ViewModelFactory.clearInstantFactory()
            startActivity(intent)
        }
    }
}
