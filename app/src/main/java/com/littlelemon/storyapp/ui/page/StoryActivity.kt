package com.littlelemon.storyapp.ui.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.littlelemon.storyapp.MainActivity
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.component.viewmodel.UploadViewModel
import com.littlelemon.storyapp.component.viewmodel.ViewModelFactory
import com.littlelemon.storyapp.data.repository.ResultState
import com.littlelemon.storyapp.databinding.ActivityStoryBinding
import com.littlelemon.storyapp.ui.component.CameraxActivity
import com.littlelemon.storyapp.ui.component.CameraxActivity.Companion.CAMERAX_RESULT
import com.littlelemon.storyapp.ui.component.getImgUri
import com.littlelemon.storyapp.ui.component.uriToFile
import com.littlelemon.storyapp.ui.component.reduceFileImg

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private val viewModel by viewModels<UploadViewModel> {
        ViewModelFactory.getInstantFactory(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentImgUri: Uri? = null

    private val reqPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted: Boolean ->
        if (isGranted){
            Toast.makeText(this, getString(R.string.permission_request_granted), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, getString(R.string.permission_request_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun allPermission() = ContextCompat.checkSelfPermission(
        this, REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initBinding()

        if (!allPermission()) {
            reqPermission.launch(REQUIRED_PERMISSION)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.apply {
            galleryBtn.setOnClickListener { startGallery() }
            cameraBtn.setOnClickListener { startCamera() }
            cameraXBtn.setOnClickListener { startCameraX() }
            uploadBtn.setOnClickListener { startUpload() }

            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showToast(getString(R.string.lokasi_diaktifkan))
                } else {
                    showToast(getString(R.string.lokasi_dinonaktifkan))
                }
            }
        }
    }

    private fun initBinding(){
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun startGallery(){
        launchGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launchGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        if (it != null) {
            currentImgUri = it
            showImage()
        }
    }

    private fun showImage() {
        val uri = currentImgUri
        if (uri != null) {
            binding.imageView.setImageURI(uri)
        }
    }

    private fun startCamera() {
        currentImgUri = getImgUri(this)
        launchIntentCamera.launch(currentImgUri!!)
    }

    private val launchIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImgUri = null
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraxActivity::class.java)
        launchIntentCameraX.launch(intent)
    }

    private val launchIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImgUri = it.data?.getStringExtra(CameraxActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun startUpload() {
        binding.uploadBtn.isEnabled = false
        val description = binding.edtTxtDesc.text.toString()

        if (currentImgUri != null && description.isNotBlank()) {
            if (binding.switchLocation.isChecked) {
                getUserLocation()
            } else {
                uploadImageWithoutLocation()
            }
        } else {
            if (currentImgUri == null) {
                showToast(getString(R.string.emptyImage))
            }
            if (description.isBlank()) {
                showToast(getString(R.string.emptyDescription))
            }

            binding.uploadBtn.isEnabled = true
        }
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    uploadImageWithLocation(lat, lon)
                } else {
                    showToast(getString(R.string.lokasi_tidak_tersedia))
                    binding.uploadBtn.isEnabled = true
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun uploadImageWithoutLocation() {
        currentImgUri?.let {
            val imgFile = uriToFile(it, this).reduceFileImg()
            val desc = binding.edtTxtDesc.text.toString()

            viewModel.uploadImage(imgFile, desc).observe(this) { res ->
                when (res) {
                    is ResultState.Loading -> {
                        showLoad(true)
                    }
                    is ResultState.Success -> {
                        showToast(res.data.message)
                        showLoad(false)
                        showMain()
                    }
                    is ResultState.Error -> {
                        showToast(res.error)
                        showLoad(false)
                    }
                }
            }
        }
    }

    private fun uploadImageWithLocation(lat: Double, lon: Double) {
        currentImgUri?.let {
            val imgFile = uriToFile(it, this).reduceFileImg()
            val desc = binding.edtTxtDesc.text.toString()

            viewModel.uploadImage(imgFile, desc, lat.toFloat(), lon.toFloat()).observe(this) { res ->
                when (res) {
                    is ResultState.Loading -> {
                        showLoad(true)
                    }
                    is ResultState.Success -> {
                        showToast(res.data.message)
                        showLoad(false)
                        showMain()
                    }
                    is ResultState.Error -> {
                        showToast(res.error)
                        showLoad(false)
                    }
                }
            }
        }
    }

    private fun showLoad(isLoad: Boolean) {
        binding.progressBar.visibility = if (isLoad) View.VISIBLE else View.GONE
    }

    private fun showMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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

    companion object {
        const val REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}

