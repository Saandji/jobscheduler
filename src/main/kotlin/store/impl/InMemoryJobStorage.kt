package com.samshend.jobscheduler.store.impl

import com.samshend.jobscheduler.store.model.JobRecord
import store.JobStorage
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory implementation of the `JobStorage` interface.
 *
 * This class provides a thread-safe, ephemeral storage mechanism for `JobRecord` objects.
 * Jobs are stored in memory using a `ConcurrentHashMap` and are lost when the application
 * terminates or the storage is cleared manually.
 *
 * This implementation is intended for lightweight, non-persistent use cases, such as
 * testing or temporary runtime job scheduling during application execution.
 *
 * Thread Safety:
 * - The storage operations are thread-safe due to the underlying `ConcurrentHashMap`.
 *
 * Main Responsibilities:
 * - Save and update job records in memory.
 * - Retrieve, list, and delete specific jobs using their `jobId`.
 * - Clear all jobs from memory when required.
 */
class InMemoryJobStorage : JobStorage {
    private val jobs = ConcurrentHashMap<String, JobRecord>()

    override fun saveJob(record: JobRecord) {
        jobs[record.definition.id] = record
    }

    override fun updateJob(record: JobRecord) {
        jobs[record.definition.id] = record
    }

    override fun deleteJob(jobId: String) {
        jobs.remove(jobId)
    }

    override fun getJob(jobId: String): JobRecord? = jobs[jobId]

    override fun listAll(): Collection<JobRecord> = jobs.values

    override fun clear() {
        jobs.clear()
    }
}