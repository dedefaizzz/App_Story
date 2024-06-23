package com.dicoding.appstory.view.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.appstory.data.response.ListStoryItem
import com.dicoding.appstory.databinding.ListItemBinding
import com.dicoding.appstory.view.activity.DetailActivity

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val storyList: MutableList<ListStoryItem> = mutableListOf()

    inner class ViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListStoryItem) {
            binding.usernameTextView.text = item.name
            binding.description.text = item.description
            Glide.with(itemView.context)
                .load(item.photoUrl)
                .into(binding.ivPriview)
            binding.story.setOnClickListener {
                val context = it.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_ID, item.id)
                }
                context.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity).toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(storyList[position])
    }

    override fun getItemCount(): Int {
        return storyList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateStories(newStories: List<ListStoryItem>) {
        storyList.clear()
        storyList.addAll(newStories)
        notifyDataSetChanged()
    }
}
