package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lab_week_11_b.FileInfo
import com.example.lab_week_11_b.FileHelper
import com.example.lab_week_11_b.MediaContentHelper
import com.example.lab_week_11_b.ProviderFileManager
import java.util.concurrent.Executors



class MainActivity : AppCompatActivity() {

    private lateinit var providerFileManager: ProviderFileManager
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    private var isCapturingVideo = false

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) {
                providerFileManager.insertImageToStore(photoInfo)
            }

        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
                providerFileManager.insertVideoToStore(videoInfo)
            }

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkStoragePermission { openImageCapture() }
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            checkStoragePermission { openVideoCapture() }
        }
    }

    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.let {
            takePictureLauncher.launch(it.uri)
        }
    }


    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.let {
            takeVideoLauncher.launch(it.uri)
        }
    }


    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                PackageManager.PERMISSION_GRANTED -> onPermissionGranted()
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }
}
