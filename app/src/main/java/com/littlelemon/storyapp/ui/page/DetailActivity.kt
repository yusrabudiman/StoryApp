package com.littlelemon.storyapp.ui.page

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.littlelemon.storyapp.R
import com.littlelemon.storyapp.data.response.ListStory
import com.littlelemon.storyapp.databinding.ActivityDetailBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Suppress("DEPRECATION")
class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var notificationManager: NotificationManagerCompat
    private val channelId = "download_channel"
    private val notificationId = 1

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showDownloadDialog(currentPhotoUrl)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.notification_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private var currentPhotoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initBinding()
        createNotificationChannel()
        notificationManager = NotificationManagerCompat.from(this)

        val detail = intent.getParcelableExtra<ListStory>(DETAIL) as ListStory
        setAction(detail)
    }

    private fun initBinding() {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setAction(detail: ListStory) {
        Glide.with(applicationContext)
            .load(detail.photoUrl)
            .into(binding.detailUserImage)

        binding.detailUserImage.setOnLongClickListener {
            currentPhotoUrl = detail.photoUrl
            checkNotificationPermission()
            true
        }

        binding.apply {
            name.text = detail.name
            descListStory.text = detail.description
            datePickerActionsPost.text = detail.createdAt?.let { formatDateTime(it) }
        }
    }

    private fun formatDateTime(createdAt: String): String {
        return try {
            val inputFormat = SimpleDateFormat(getString(R.string.format_time_input), Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone(getString(R.string.utc))
            }
            val outputFormat = SimpleDateFormat(getString(R.string.format_time_output), Locale(
                getString(
                    R.string.id
                ), getString(R.string.country))).apply {
                timeZone = TimeZone.getTimeZone(getString(R.string.asia_jakarta))
            }
            val date = inputFormat.parse(createdAt)
            date?.let { outputFormat.format(it) } ?: createdAt
        } catch (e: Exception) {
            createdAt
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showDownloadDialog(currentPhotoUrl)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        this,
                        getString(R.string.notification_permission_turn_off),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showDownloadDialog(currentPhotoUrl)
        }
    }

    private fun showDownloadDialog(photoUrl: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.download_image))
            .setMessage(getString(R.string.do_you_want_to_download_this_image))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                startDownloadNotification()
                downloadImage(photoUrl)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun startDownloadNotification() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.downloading_image))
            .setContentText(getString(R.string.download_in_progress))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(100, 0, true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification)
    }

    private fun updateDownloadSuccessNotification(imageUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(imageUri, "image/jpeg")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.download_complete))
            .setContentText(getString(R.string.image_saved_to_gallery))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification)
    }



    private fun updateDownloadFailedNotification() {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.download_failed))
            .setContentText(getString(R.string.an_error_occurred_during_download))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, notification)
    }

    private fun downloadImage(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val imageUri = saveImageToGallery(resource)
                    if (imageUri != null) {
                        updateDownloadSuccessNotification(imageUri)
                    } else {
                        updateDownloadFailedNotification()
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream
        return try {
            val imageUri: Uri?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = resolver.openOutputStream(imageUri!!)!!
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                )
                if (!directory.exists()) directory.mkdirs()
                val imageFile = File(directory, filename)
                imageUri = Uri.fromFile(imageFile)
                outputStream = FileOutputStream(imageFile)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            getString(R.string.download_notifications),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notifications_for_image_downloads)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
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
        const val DETAIL = "detail"
    }
}
