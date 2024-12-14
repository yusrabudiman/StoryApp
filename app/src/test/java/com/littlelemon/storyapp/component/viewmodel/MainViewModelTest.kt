package com.littlelemon.storyapp.component.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.littlelemon.storyapp.DataDummyTest
import com.littlelemon.storyapp.MainDispatchTest
import com.littlelemon.storyapp.component.adapter.componentadapter.ListStoryAdapter
import com.littlelemon.storyapp.data.repository.StoryRepository
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatch = MainDispatchTest()

    @Mock
    private lateinit var repository: StoryRepository

    @Test
    fun `when Get Story Should Not Null and Return Data Test`() = runTest {
        val dummy = DataDummyTest.generateDummyQuoteResponse()
        val data: PagingData<ListStory> = PagingSourceQuote.snapshot(dummy)
        val expected = MutableLiveData<PagingData<ListStory>>()
        expected.value = data
        Mockito.`when`(repository.getQuote()).thenReturn(expected)

        val storyViewModel = StoryViewModel(repository)
        val actual: PagingData<ListStory> = storyViewModel.quote.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actual)

        assertNotNull(differ.snapshot())
        assertEquals(dummy.size, differ.snapshot().size)
        assertEquals(dummy[0], differ.snapshot()[0])
    }
    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStory> = PagingData.from(emptyList())
        val expected = MutableLiveData<PagingData<ListStory>>()
        expected.value = data
        Mockito.`when`(repository.getQuote()).thenReturn(expected)
        val storyViewModel = StoryViewModel(repository)
        val actual: PagingData<ListStory> = storyViewModel.quote.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actual)
        assertEquals(0, differ.snapshot().size)
    }
}

class PagingSourceQuote : PagingSource<Int, LiveData<List<ListStory>>>(){
    companion object{
        fun snapshot(items: List<ListStory>): PagingData<ListStory> {
            return  PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStory>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStory>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

}
val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
}