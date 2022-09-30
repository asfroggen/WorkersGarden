package com.esaudev.workersgarden.image_workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

private const val NO_IMAGE = "noImage"

class LocalImageCheckWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams) {

    override fun doWork(): Result {
        val imagePath = inputData.getString("image_path")?: NO_IMAGE
        val parts = imagePath.split("/")

        if (imagePath.isBlank() || imagePath == NO_IMAGE) {
            val outputData = workDataOf("is_downloaded" to false)
            return Result.success(outputData)
        }

        val lastSegment = parts.last()
        val rootFile = applicationContext.externalMediaDirs.first()

        return try {
            val isDownloaded = rootFile.list()?.any{ filePath ->
                lastSegment in filePath
            }

            val outputData = workDataOf("is_downloaded" to (isDownloaded?: false))
            Result.success(outputData)
        } catch (e: Throwable) {
            Result.failure()
        }
    }

}