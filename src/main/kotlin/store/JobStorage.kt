package store

import com.samshend.jobscheduler.store.model.JobRecord

/**
 * Interface defining operations for persistent job storage.
 *
 * `JobStorage` provides methods for managing job records, including saving, updating,
 * retrieving, listing, and removing jobs. It also supports operations for clearing
 * or flushing the storage when applicable.
 *
 * This abstraction facilitates using various storage backends, such as in-memory,
 * file-based, or database implementations, to manage scheduled jobs.
 */
interface JobStorage {
    fun saveJob(record: JobRecord)
    fun updateJob(record: JobRecord)
    fun deleteJob(jobId: String)
    fun getJob(jobId: String): JobRecord?
    fun listAll(): Collection<JobRecord>
    fun clear()
}