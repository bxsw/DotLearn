package com.dotlearn.app.domain

import java.util.Calendar

/** 日期与时长相关的小工具。 */
object TimeUtils {

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun todayStart(now: Long = System.currentTimeMillis()): Long =
        SpacedRepetition.startOfDay(now)

    fun weekStart(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return cal.timeInMillis
    }

    fun daysAgoStart(days: Int, now: Long = System.currentTimeMillis()): Long =
        todayStart(now) - days * DAY_MS

    fun tomorrowStart(now: Long = System.currentTimeMillis()): Long =
        todayStart(now) + DAY_MS

    fun dayMs(): Long = DAY_MS

    fun formatDuration(seconds: Int): String {
        if (seconds < 60) return "${seconds}s"
        val minutes = seconds / 60
        if (minutes < 60) return "${minutes}min"
        val h = minutes / 60
        val m = minutes % 60
        return "${h}h ${m}min"
    }

    fun formatClock(timeMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return String.format("%02d:%02d", h, m)
    }

    fun formatDate(timeMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        return String.format(
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun isSameDay(a: Long, b: Long): Boolean = todayStart(a) == todayStart(b)

    /** 计算从 earliest 到 today 的连续天数（含今天）。 */
    fun streakDays(sessionTimes: List<Long>, now: Long = System.currentTimeMillis()): Int {
        if (sessionTimes.isEmpty()) return 0
        val days = sessionTimes.map { todayStart(it) }.distinct().sortedDescending()
        val today = todayStart(now)
        var expected = today
        var streak = 0
        for (day in days) {
            when {
                day == expected -> {
                    streak++
                    expected -= DAY_MS
                }
                day > expected -> Unit
                else -> break
            }
        }
        return streak
    }
}
