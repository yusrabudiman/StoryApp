package com.littlelemon.storyapp.ui.component

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.databinding.ActivityCameraxBinding

class CameraxActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCameraxBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imgCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        handleSwitchCamera()
        handleTakePicture()
    }
    private fun handleSwitchCamera() {
        binding.switchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }
    }
    private fun handleTakePicture() {
        binding.takePicture.setOnClickListener { takePictures() }
    }
    private fun initBinding(){
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            }
            else -> {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        }
        supportActionBar?.hide()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = createPreview()
            imgCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imgCapture
                )
            } catch (exception: Exception) {
                handleCameraError(exception)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun createPreview(): Preview {
        return Preview.Builder()
            .build()
            .also { it.surfaceProvider = binding.cameraView.surfaceProvider }
    }
    private fun handleCameraError(exception: Exception) {
        Toast.makeText(
            this@CameraxActivity,
            getString(R.string.gagal_memunculkan_kamera),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun takePictures(){
        val imgCapture = imgCapture ?: return
        val photoFile = createTempFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imgCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val intent = Intent()
                    intent.putExtra(EXTRA_CAMERAX_IMAGE, outputFileResults.savedUri.toString())
                    setResult(CAMERAX_RESULT, intent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraxActivity,
                        getString(R.string.gagal_mengambil_gambar),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation = getRotationFromOrientation(orientation)
                imgCapture?.targetRotation = rotation
            }
        }
    }

    private fun getRotationFromOrientation(orientation: Int): Int {
        return when (orientation) {
            in 45 until 135 -> Surface.ROTATION_270
            in 135 until 225 -> Surface.ROTATION_180
            in 225 until 315 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
    }
    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }
    companion object{
        const val EXTRA_CAMERAX_IMAGE = "Camerax Image"
        const val CAMERAX_RESULT = 200
    }
}