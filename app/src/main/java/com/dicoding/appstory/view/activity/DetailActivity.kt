package com.dicoding.appstory.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.dicoding.appstory.R
import com.dicoding.appstory.data.response.Story
import com.dicoding.appstory.databinding.ActivityDetailBinding
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private val detailViewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var detailBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)

        configureActionBar()
        processIntentData()
    }

    private fun configureActionBar() {
        supportActionBar?.apply {
            title = getString(R.string.detail)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun processIntentData() {
        val storyId = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (storyId.isNotEmpty()) {
            toggleLoading(true)
            detailViewModel.retrieveUserSession().observe(this) { session ->
                session?.token?.let { token ->
                    detailViewModel.fetchStoryDetail(token, storyId)
                    detailViewModel.storyDetail.observe(this) { story ->
                        story?.let {
                            populateStoryDetails(it)
                            toggleLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun populateStoryDetails(story: Story) {
        Glide.with(this)
            .load(story.photoUrl)
            .into(detailBinding.ivPriview)

        detailBinding.name.text = story.name
        detailBinding.description.text = story.description
    }

    private fun toggleLoading(isVisible: Boolean) {
        detailBinding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
