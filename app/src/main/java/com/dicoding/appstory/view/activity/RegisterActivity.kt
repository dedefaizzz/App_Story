package com.dicoding.appstory.view.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.dicoding.appstory.customview.EmailEditText
import com.dicoding.appstory.customview.MyButton
import com.dicoding.appstory.customview.PasswordEditText
import com.dicoding.appstory.data.result.ResultState
import com.dicoding.appstory.databinding.ActivityRegisterBinding
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var emailInput: EmailEditText
    private lateinit var passwordInput: PasswordEditText
    private lateinit var signUpButton: MyButton
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupUI()
        startAnimations()
    }

    private fun initViews() {
        emailInput = binding.emailEditText
        passwordInput = binding.passwordEditText
        signUpButton = binding.btnSignup

        emailInput.addTextChangedListener(createTextWatcher())
        passwordInput.addTextChangedListener(createTextWatcher())

        signUpButton.setOnClickListener {
            if (signUpButton.isEnabled) {
                val name = binding.nameEditText.text.toString()
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()

                viewModel.userRegister(name, email, password).observe(this) { result ->
                    when (result) {
                        is ResultState.Loading -> showLoading(true)
                        is ResultState.Success -> handleSignUpSuccess(result.data ?: "Registration successful")
                        is ResultState.Error -> handleSignUpError(result.exception.message ?: "An error occurred")
                    }
                }
            }
        }
    }

    private fun setupUI() {
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

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Registration Successful!")
            setMessage("Your account has been created. Please log in.")
            setPositiveButton("Continue") { _, _ ->
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun startAnimations() {
        val moveAnimation = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        moveAnimation.start()

        val fadeInDuration = 100L
        val fadeInAnimations = listOf(
            ObjectAnimator.ofFloat(binding.tvTittle, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).setDuration(fadeInDuration),
            ObjectAnimator.ofFloat(binding.btnSignup, View.ALPHA, 0f, 1f).setDuration(fadeInDuration)
        )

        AnimatorSet().apply {
            playSequentially(fadeInAnimations)
            startDelay = 100L
        }.start()
    }

    private fun createTextWatcher() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val isEmailValid = emailInput.text.toString().isNotEmpty() && emailInput.error == null
            val isPasswordValid = passwordInput.text.toString().isNotEmpty() && passwordInput.error == null

            signUpButton.isEnabled = isEmailValid && isPasswordValid
        }
        override fun afterTextChanged(s: Editable) {}
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun handleSignUpSuccess(message: String) {
        showToast(message)
        showLoading(false)
        showSuccessDialog()
    }

    private fun handleSignUpError(message: String) {
        showToast(message)
        showLoading(false)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

