package com.esaudev.workersgarden.image_workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class FileClearWorker(
    context: Context,
    workerParams: WorkerParameters
): Worker(context, workerParams){

    override fun doWork(): Result {
        val root = applicationContext.externalMediaDirs.first()

        return try {
            root.listFiles()?.forEach { child ->
                if (child.isDirectory) {
                    child.deleteRecursively()
                } else {
                    child.delete()
                }
            }
            Result.success()
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure()
        }
    }

}