package model

import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.retry.RetryPolicy
import java.util.function.Supplier

class JobDefinitionBuilder<T> {
    private var id: String? = null
    private var name: String? = null
    private var resultType: Class<T>? = null
    private var recurrenceConfig: RecurrenceConfiguration = RecurrenceConfiguration.once()
    private var retryPolicy: RetryPolicy = RetryPolicy.noRetries
    private var action: Supplier<T>? = null

    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun resultType(resultType: Class<T>) = apply { this.resultType = resultType }
    fun recurrence(recurrence: RecurrenceConfiguration) = apply { this.recurrenceConfig = recurrence }
    fun retryPolicy(retryPolicy: RetryPolicy) = apply { this.retryPolicy = retryPolicy }
    fun action(action: Supplier<T>) = apply { this.action = action }

    fun build(): JobDefinition<T> {
        return JobDefinition(
            id = id ?: throw IllegalStateException("Job ID must be set"),
            name = name ?: throw IllegalStateException("Job name must be set"),
            recurrenceConfig = recurrenceConfig,
            retryPolicy = retryPolicy,
            resultType = resultType ?: throw IllegalStateException("Result type must be set"),
            supplier = action ?: throw IllegalStateException("Action must be set")
        )
    }
}
