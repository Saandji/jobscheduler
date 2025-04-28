package model

import java.time.Duration

/**
 * Configuration for job recurrence scheduling.
 */
class RecurrenceConfiguration private constructor(
    val delay: Duration? = null,
    val repeatTimes: Int? = null,
    val interval: Duration? = null,
    val cronExpression: String? = null
) {

    constructor() : this(null, null, null, null)

    fun withDelay(delay: Duration): RecurrenceConfiguration =
        RecurrenceConfiguration(delay, repeatTimes, interval, cronExpression)

    fun withRepeatTimes(times: Int): RecurrenceConfiguration =
        RecurrenceConfiguration(delay, times, interval, cronExpression)

    fun withInterval(interval: Duration): RecurrenceConfiguration =
        RecurrenceConfiguration(delay, repeatTimes, interval, cronExpression)

    fun withCronExpression(expression: String): RecurrenceConfiguration =
        RecurrenceConfiguration(delay, repeatTimes, interval, expression)

    companion object {
        fun once(): RecurrenceConfiguration = RecurrenceConfiguration()

        fun delayed(delay: Duration): RecurrenceConfiguration =
            RecurrenceConfiguration(delay = delay)

        fun repeat(times: Int, interval: Duration = Duration.ZERO): RecurrenceConfiguration =
            RecurrenceConfiguration(repeatTimes = times, interval = interval)

        fun cron(expression: String): RecurrenceConfiguration =
            RecurrenceConfiguration(cronExpression = expression)
    }
}