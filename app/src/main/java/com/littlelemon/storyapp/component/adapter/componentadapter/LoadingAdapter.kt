package com.littlelemon.storyapp.component.adapter.componentadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.paging.LoadState
import com.littlelemon.storyapp.databinding.LoadingBinding

class LoadingAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadingAdapter.LoadingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingViewHolder {
        val binding = LoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingViewHolder(binding, retry)
    }
    override fun onBindViewHolder(holder: LoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
    class LoadingViewHolder(private val binding: LoadingBinding, retry: () -> Unit): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryBtn.setOnClickListener { retry() }
        }
        fun bind(loadState: LoadState) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                retryBtn.isVisible = loadState is LoadState.Error
                errorMsg.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    errorMsg.text = loadState.error.localizedMessage
                }
            }
        }
    }
}
