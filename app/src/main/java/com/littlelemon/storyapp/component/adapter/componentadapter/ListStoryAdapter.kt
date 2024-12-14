package com.littlelemon.storyapp.component.adapter.componentadapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.ui.page.DetailActivity
import com.littlelemon.storyapp.databinding.ListItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class ListStoryAdapter : PagingDataAdapter<ListStory, ListStoryAdapter.ViewHolder>(DIFF_CALLBACK) {
    class ViewHolder(private val itemBinding: ListItemReviewBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(story: ListStory) {
            itemBinding.apply {
                usernameListStory.text = story.name
                descListStory.text = story.description
                datePickerActionsPost.text = story.createdAt?.let { formatDateTime(it) }
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(listStoryItem)
                listStoryItem.setOnClickListener {
                    val context = listStoryItem.context
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.DETAIL, story)
                    }
                    context.startActivity(
                        intent,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity).toBundle()
                    )
                }
            }
        }
        private fun formatDateTime(createdAt: String): String {
            return try {
                val inputFormat = SimpleDateFormat(("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id", "ID")).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                }
                val date = inputFormat.parse(createdAt)
                date?.let { outputFormat.format(it) } ?: createdAt
            } catch (e: Exception) {
                createdAt
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStory>() {
            override fun areItemsTheSame(oldItem: ListStory, newItem: ListStory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStory, newItem: ListStory): Boolean {
                return oldItem == newItem
            }
        }
    }
}
