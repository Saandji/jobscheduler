package model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class RecurrenceConfigurationTest {

    @Test
    fun `once factory creates default configuration`() {
        val config = RecurrenceConfiguration.once()

        assertNull(config.delay)
        assertNull(config.repeatTimes)
        assertNull(config.interval)
        assertNull(config.cronExpression)
    }

    @Test
    fun `delayed factory sets only delay`() {
        val delay = Duration.ofSeconds(10)
        val config = RecurrenceConfiguration.delayed(delay)

        assertEquals(delay, config.delay)
        assertNull(config.repeatTimes)
        assertNull(config.interval)
        assertNull(config.cronExpression)
    }

    @Test
    fun `fixed factory sets repeatTimes and interval`() {
        val interval = Duration.ofSeconds(5)
        val config = RecurrenceConfiguration.repeat(times = 3, interval = interval)

        assertEquals(3, config.repeatTimes)
        assertEquals(interval, config.interval)
        assertNull(config.delay)
        assertNull(config.cronExpression)
    }

    @Test
    fun `cron factory sets cron expression`() {
        val cron = "0 0 * * *"
        val config = RecurrenceConfiguration.cron(cron)

        assertEquals(cron, config.cronExpression)
        assertNull(config.delay)
        assertNull(config.repeatTimes)
        assertNull(config.interval)
    }

    @Test
    fun `fluent API allows chaining delay and repeat settings`() {
        val config = RecurrenceConfiguration()
            .withDelay(Duration.ofSeconds(5))
            .withRepeatTimes(5)
            .withInterval(Duration.ofSeconds(10))

        assertEquals(Duration.ofSeconds(5), config.delay)
        assertEquals(5, config.repeatTimes)
        assertEquals(Duration.ofSeconds(10), config.interval)
        assertNull(config.cronExpression)
    }

    @Test
    fun `fluent API allows setting cron expression separately`() {
        val config = RecurrenceConfiguration()
            .withCronExpression("0 0 12 * * ?")

        assertEquals("0 0 12 * * ?", config.cronExpression)
    }
}