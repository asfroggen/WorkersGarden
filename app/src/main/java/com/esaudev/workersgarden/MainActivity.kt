package com.esaudev.workersgarden

import android.app.DownloadManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.esaudev.workersgarden.databinding.ActivityMainBinding
import com.esaudev.workersgarden.image_workers.DownloadWorker
import com.esaudev.workersgarden.image_workers.FileClearWorker
import com.esaudev.workersgarden.image_workers.LocalImageCheckWorker
import com.esaudev.workersgarden.image_workers.SepiaFilterWorker
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/64/Android_logo_2019_%28stacked%29.svg/1200px-Android_logo_2019_%28stacked%29.svg.png"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        downloadImage()
    }

    private fun downloadImage() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()

        val clearFilesWorker = OneTimeWorkRequestBuilder<FileClearWorker>()
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf("image_path" to imageUrl))
            .setConstraints(constraints)
            .build()

        val sepiaFilterWorker = OneTimeWorkRequestBuilder<SepiaFilterWorker>()
            .setConstraints(constraints)
            .build()

        val imageCheckWorker = OneTimeWorkRequestBuilder<LocalImageCheckWorker>()
            .setInputData(workDataOf("image_path" to imageUrl))
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.beginWith(clearFilesWorker)
            .then(imageCheckWorker)
            .then(downloadRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(imageCheckWorker.id).observe(this) { info ->
            if (info?.state?.isFinished == true) {

                val isDownloaded = info.outputData.getBoolean("is_downloaded", false)

                if (!isDownloaded) {

                    val imageFile = File(applicationContext.externalMediaDirs.first(), imageUrl)

                    val request = DownloadManager.Request(Uri.parse(imageUrl))
                        .setTitle("Image download")
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationUri(Uri.fromFile(imageFile))
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(false)

                    val downloadManager = getSystemService(DownloadManager::class.java)
                    downloadManager?.enqueue(request)
                }
            }
        }

        workManager.getWorkInfoByIdLiveData(downloadRequest.id).observe(this) { info ->
            if (info?.state?.isFinished == true) {

                val imagePath = info.outputData.getString("image_path")
                if (!imagePath.isNullOrEmpty()) {
                    displayImage(imagePath)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun displayImage(imagePath: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val bitmap = loadImageFromFile(imagePath)
            binding.image.setImageBitmap(bitmap)
        }
    }

    private suspend fun loadImageFromFile(imagePath: String) = withContext(Dispatchers.IO) {
        BitmapFactory.decodeFile(imagePath)
    }
}