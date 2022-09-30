package com.esaudev.workersgarden.image_workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams) {

    override fun doWork(): Result {

        val isAlreadyDownloaded = inputData.getBoolean("is_downloaded", false)
        val imageDownloadPath = inputData.getString("image_path")?: return Result.failure()
        val parts = imageDownloadPath.split("/")

        if (isAlreadyDownloaded) {
            val imageFile = File(applicationContext.externalMediaDirs.first(), parts.last())
            return Result.success(workDataOf("image_path" to imageFile.absolutePath))
        }

        val imageUrl = URL(imageDownloadPath)
        val connection = imageUrl.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val imagePath = "${System.currentTimeMillis()}.jpg"
        val inputStream = connection.inputStream
        val file = File(applicationContext.externalMediaDirs.first(), imagePath)

        val outputStream = FileOutputStream(file)
        outputStream.use { output ->
            val buffer = ByteArray(4*1024)

            var byteCount = inputStream.read(buffer)

            while (byteCount > 0 ){
                output.write(buffer, 0, byteCount)

                byteCount = inputStream.read(buffer)
            }

            output.flush()
        }

        val output = workDataOf("image_path" to file.absolutePath)
        return Result.success(output)
    }

}