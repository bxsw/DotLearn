package com.dotlearn.app.domain

/**
 * 间隔重复：固定间隔 1 / 3 / 7 / 15 / 30 天。
 * stage 表示下一次要使用的间隔下标。
 */
object SpacedRepetition {

    val INTERVAL_DAYS = intArrayOf(1, 3, 7, 15, 30)

    val MAX_STAGE: Int get() = INTERVAL_DAYS.size - 1

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun daysForStage(stage: Int): Int =
        INTERVAL_DAYS[stage.coerceIn(0, MAX_STAGE)]

    /** 记住时推进到下一阶段；不熟则退回第一阶段。 */
    fun nextStage(currentStage: Int, good: Boolean): Int =
        if (good) (currentStage + 1).coerceAtMost(MAX_STAGE) else 0

    /** 按阶段计算下次到期时间。 */
    fun dueAtForStage(stage: Int, from: Long = System.currentTimeMillis()): Long {
        val days = daysForStage(stage)
        return startOfDay(from) + days * DAY_MS
    }

    fun startOfDay(timeMillis: Long): Long {
        val offset = java.util.TimeZone.getDefault().getOffset(timeMillis)
        return ((timeMillis + offset) / DAY_MS) * DAY_MS - offset
    }

    fun formatInterval(stage: Int): String = "${daysForStage(stage)}d"
}
