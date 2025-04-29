package com.samshend.jobscheduler.store.impl

import com.samshend.jobscheduler.store.model.JobRecord
import store.JobStorage
import java.util.concurrent.ConcurrentHashMap

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