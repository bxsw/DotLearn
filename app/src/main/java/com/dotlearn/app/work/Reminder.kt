package com.dotlearn.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dotlearn.app.ServiceLocator
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 每日复习提醒。
 * 完全由系统本地调度，不需要网络，也不需要精确闹钟权限。
 */
class ReviewReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(KEY_HOUR, 20)
        val minute = inputData.getInt(KEY_MINUTE, 0)

        val due = runCatching { ServiceLocator.repository.dueReviews().size }.getOrDefault(0)
        NotificationHelper.showReviewReminder(applicationContext, due)

        // 链式续期，保证每天稳定触发
        ReminderScheduler.scheduleAt(applicationContext, hour, minute)
        return Result.success()
    }

    companion object {
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val WORK_NAME = "dotlearn_daily_review"
    }
}

object ReminderScheduler {

    fun scheduleAt(context: Context, hour: Int, minute: Int) {
        val data = Data.Builder()
            .putInt(ReviewReminderWorker.KEY_HOUR, hour)
            .putInt(ReviewReminderWorker.KEY_MINUTE, minute)
            .build()

        val request = OneTimeWorkRequestBuilder<ReviewReminderWorker>()
            .setInitialDelay(delayUntil(hour, minute), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReviewReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReviewReminderWorker.WORK_NAME)
    }

    private fun delayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
