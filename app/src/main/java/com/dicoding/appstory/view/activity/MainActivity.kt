package com.dicoding.appstory.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.appstory.R
import com.dicoding.appstory.data.model.UserModel
import com.dicoding.appstory.databinding.ActivityMainBinding
import com.dicoding.appstory.view.adapter.Adapter
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityMainBinding
    private val adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = adapter

        mainViewModel.storiesList.observe(this) { stories ->
            adapter.updateStories(stories)
            toggleLoading(false)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        toggleLoading(true)
        mainViewModel.retrieveUserSession().observe(this) { user ->
            if (user.isLogin) {
                mainViewModel.fetchStories(user.token)
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                mainViewModel.userLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleLoading(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
