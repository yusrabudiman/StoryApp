package com.littlelemon.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.littlelemon.storyapp.component.adapter.componentadapter.LoadingAdapter
import com.littlelemon.storyapp.component.adapter.componentadapter.ListStoryAdapter
import com.littlelemon.storyapp.component.viewmodel.MainViewModel
import com.littlelemon.storyapp.component.viewmodel.StoryViewModel
import com.littlelemon.storyapp.component.viewmodel.ViewModelFactory
import com.littlelemon.storyapp.data.repository.ResultState
import com.littlelemon.storyapp.databinding.ActivityMainBinding
import com.littlelemon.storyapp.ui.page.MapsActivity
import com.littlelemon.storyapp.ui.page.StoryActivity
import com.littlelemon.storyapp.ui.page.WelcomeScreen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val storyViewModel by viewModels<StoryViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        fabUpload()
        listItemBinding()
        session()
    }

    private fun initBinding(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun fabUpload(){
        binding.fabUploadPost.setOnClickListener{
            startActivity(Intent(this, StoryActivity::class.java))
        }
    }

    private fun listItemBinding() {
        binding.listStoryItem.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).also { lm ->
                addItemDecoration(DividerItemDecoration(this@MainActivity, lm.orientation))
            }
        }
    }

    private fun session() {
        mainViewModel.getSession().observe(this) { session ->
            val intent = Intent(this, WelcomeScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (session.isLogin) {
                getDataWithPost()
                setAction()
            } else {
                startActivity(intent)
                finish()
            }
        }
    }

   private fun getDataWithPost(){
       val adapter = ListStoryAdapter()
       binding.listStoryItem.adapter = adapter.withLoadStateFooter(
           footer = LoadingAdapter{
               adapter.retry()
           }
       )
       storyViewModel.quote.observe(this) {
           adapter.submitData(lifecycle, it)
       }
   }

    private fun setAction() {
        lifecycleScope.launch {
            storyViewModel.getStory().observe(this@MainActivity) { result ->
                binding.progressBar.visibility = when (result) {
                    is ResultState.Loading -> View.VISIBLE
                    else -> View.INVISIBLE
                }
                if (result is ResultState.Error) {
                    Toast.makeText(this@MainActivity, result.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.maps -> {
                lifecycleScope.launch {
                    navigateToMapsActivity()
                }
            }
            R.id.logout -> {
                lifecycleScope.launch {
                    mainViewModel.logout()
                    navigateToWelcomeScreen()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToWelcomeScreen() {
        val intent = Intent(this, WelcomeScreen::class.java)
        startActivity(intent)
        finish()
    }
}