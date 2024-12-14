package com.littlelemon.storyapp.ui.component

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.littlelemon.storyapp.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAXIMAL_SIZE = 1000000
private const val FILENAME_FORMAT = "yyyMMdd_HHmmss"
private val timestamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

fun getImgUri(context: Context): Uri{
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
        val contentValue = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timestamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCamera/")
        }
        uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValue
        )
    }
    return uri ?: getImgUriForPreq(context)
}

private fun getImgUriForPreq(context: Context): Uri{
    val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imgFile = File(fileDir, "/MyCamera/$timestamp.jpg")
    if(imgFile.parentFile?.exists() == false) imgFile.parentFile?.mkdir()
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        imgFile
    )
}

fun createTempFile(context: Context): File{
    val fileDir = context.externalCacheDir
    return File.createTempFile(timestamp, ".jpg", fileDir)
}

fun uriToFile(imgUri: Uri, context: Context): File{
    val myFile = createTempFile(context)
    val inputStream = context.contentResolver.openInputStream(imgUri) as InputStream
    val outputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also {
        length = it
        } > 0) outputStream.write(buffer, 0, length)
    outputStream.close()
    inputStream.close()
    return myFile
}

fun File.reduceFileImg(): File{
    val file = this
    val bitmap = BitmapFactory.decodeFile(file.path).getBitmapRotationImage(file)
    var compressQuality = 100
    var streamLength: Int
    do{
        val bitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bitmapStream)
        val bitmapPicByteArray = bitmapStream.toByteArray()
        streamLength = bitmapPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun Bitmap.getBitmapRotationImage(file: File): Bitmap {
    val orientation = ExifInterface(file).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
    )
    return when(orientation){
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
        ExifInterface.ORIENTATION_NORMAL -> this
        else -> this
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(
        source, 0, 0, source.width, source.height, matrix, true
    )
}