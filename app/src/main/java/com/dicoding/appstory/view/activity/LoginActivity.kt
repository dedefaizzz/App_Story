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
import com.dicoding.appstory.data.model.UserModel
import com.dicoding.appstory.data.result.ResultState
import com.dicoding.appstory.databinding.ActivityLoginBinding
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailField: EmailEditText
    private lateinit var passwordField: PasswordEditText
    private lateinit var loginBtn: MyButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListeners()
        setupUI()
        startAnimations()
    }

    private fun initViews() {
        emailField = binding.emailEditText
        passwordField = binding.passwordEditText
        loginBtn = binding.btnLogin
    }

    private fun setupListeners() {
        emailField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmailValid = emailField.text.toString().isNotEmpty() && emailField.error == null
                val isPasswordValid = passwordField.text.toString().isNotEmpty() && passwordField.error == null

                loginBtn.isEnabled = isEmailValid && isPasswordValid
                if (loginBtn.isEnabled) {
                    loginBtn.setOnClickListener { attemptLogin() }
                } else {
                    loginBtn.setOnClickListener(null)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        passwordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmailValid = emailField.text.toString().isNotEmpty() && emailField.error == null
                val isPasswordValid = passwordField.text.toString().isNotEmpty() && passwordField.error == null

                loginBtn.isEnabled = isEmailValid && isPasswordValid
                if (loginBtn.isEnabled) {
                    loginBtn.setOnClickListener { attemptLogin() }
                } else {
                    loginBtn.setOnClickListener(null)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    private fun startAnimations() {
        val imageAnimator = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val animators = listOf(
            ObjectAnimator.ofFloat(binding.tvTittle, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.tvMessage, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(100)
        )

        AnimatorSet().apply {
            playSequentially(animators)
            startDelay = 100
        }.start()
        imageAnimator.start()
    }

    private fun attemptLogin() {
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        mainViewModel.userLogin(email, password).observe(this) { result ->
            when (result) {
                is ResultState.Loading -> setLoadingState(true)
                is ResultState.Success -> handleLoginSuccess(result.data.toString())
                is ResultState.Error -> handleLoginError(result.exception.message)
            }
        }
    }

    private fun handleLoginSuccess(token: String) {
        val email = emailField.text.toString()
        val password = passwordField.text.toString()
        mainViewModel.storeUserSession(UserModel(email, password, token))
        showToast("Login successful")
        setLoadingState(false)
        showLoginSuccessDialog()
    }

    private fun handleLoginError(message: String?) {
        showToast(message ?: "An error occurred")
        setLoadingState(false)
    }

    private fun showLoginSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Success!")
            setMessage("You have successfully logged in. Ready to read and write stories?")
            setPositiveButton("Continue") { _, _ ->
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
