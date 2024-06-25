package com.dicoding.appstory.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.appstory.R
import com.dicoding.appstory.databinding.ActivityMainBinding
import com.dicoding.appstory.view.adapter.Adapter
import com.dicoding.appstory.view.adapter.LoadingAdapter
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStory.layoutManager = LinearLayoutManager(this)

        showLoading(true)
        viewModel.retrieveUserSession().observe(this) { user ->
            if (!user.isLogin) {
                navigateToWelcomeActivity()
            } else {
                val adapter = Adapter().apply {
                    withLoadStateFooter(LoadingAdapter { retry() })
                }
                binding.rvStory.adapter = adapter
                viewModel.getRetrieveStories(user.token).observe(this) { pagingData ->
                    adapter.submitData(lifecycle, pagingData)
                    showLoading(false)
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this@MainActivity, UploadActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                viewModel.userLogout()
                true
            }
            R.id.maps -> {
                startActivity(Intent(this@MainActivity, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToWelcomeActivity() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
