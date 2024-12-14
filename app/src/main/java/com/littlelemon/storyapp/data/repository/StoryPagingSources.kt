package com.littlelemon.storyapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.data.retrofit.ApiService

class StoryPagingSources(
    private val apiService: ApiService
) : PagingSource<Int, ListStory>() {

    private companion object {
        const val INITIAL_PAGE = 1
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStory> {
        return try {
            val currentPage = params.key ?: INITIAL_PAGE
            val response = apiService.getStory(currentPage, params.loadSize)

            LoadResult.Page(
                data = response.listStory,
                prevKey = if (currentPage == INITIAL_PAGE) null else currentPage - 1,
                nextKey = if (response.listStory.isEmpty()) null else currentPage + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStory>): Int? {
        return state.anchorPosition?.let {
            val closestPage = state.closestPageToPosition(it)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }
}