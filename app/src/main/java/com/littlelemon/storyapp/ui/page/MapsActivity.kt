package com.littlelemon.storyapp.ui.page

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.component.viewmodel.MapsViewModel
import com.littlelemon.storyapp.component.viewmodel.ViewModelFactory
import com.littlelemon.storyapp.data.repository.ResultState
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.databinding.ActivityMapsBinding
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private val boundsBuilder = LatLngBounds.Builder()

    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupMapFragment()
        setupPermissionLauncher()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) enableUserLocation() else showPermissionError()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            configureMapSettings()
            setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.map_style))
        }
        requestLocationPermission()
        observeStoryLocations()
        setupPoiClickListener()
    }

    private fun GoogleMap.configureMapSettings() {
        uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation() {
        if (::googleMap.isInitialized) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun observeStoryLocations() {
        lifecycleScope.launch {
            viewModel.getStoriesWithLocation().observe(this@MapsActivity) { result ->
                when (result) {
                    is ResultState.Loading -> binding.progressBar.show()
                    is ResultState.Error -> handleError(result.error)
                    is ResultState.Success -> showStoryMarkers(result.data)
                }
            }
        }
    }

    private fun handleError(error: String) {
        binding.progressBar.hide()
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun showStoryMarkers(storyList: List<ListStory>) {
        binding.progressBar.hide()
        storyList.forEach { story ->
            googleMap.addMarker(createMarkerOptions(story))?.let {
                boundsBuilder.include(it.position)
            }
        }
        adjustMapBounds()
    }

    private fun createMarkerOptions(story: ListStory): MarkerOptions {
        return MarkerOptions()
            .position(LatLng(story.lat, story.lon))
            .title(story.name)
            .snippet(story.description)
    }

    private fun adjustMapBounds() {
        val bounds = boundsBuilder.build()
        val padding = resources.displayMetrics.widthPixels / 10
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    private fun setupPoiClickListener() {
        googleMap.setOnPoiClickListener { poi ->
            googleMap.addMarker(createPoiMarkerOptions(poi))?.showInfoWindow()
        }
    }

    private fun createPoiMarkerOptions(poi: PointOfInterest): MarkerOptions {
        return MarkerOptions()
            .position(poi.latLng)
            .title(poi.name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
    }

    private fun showPermissionError() {
        Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun View.show() { visibility = View.VISIBLE }
    private fun View.hide() { visibility = View.INVISIBLE }

    companion object {
        private const val TAG = "MapsActivity"
    }
}