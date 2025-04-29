package com.samshend.jobscheduler.store.impl

import com.samshend.jobscheduler.store.model.JobRecord
import store.JobStorage

/**
 * File-based implementation of JobStorage.
 *
 * This class will handle saving and loading job records to/from a local file
 * using JSON serialization.
 *
 * Future enhancements:
 * - Support periodic auto-saving
 * - Support versioned file formats
 * - Optional encryption for storage file
 */
class FileSystemJobStorage(
    private val storagePath: String
) : JobStorage {

    init {
        TODO("FileSystemJobStorage not implemented yet — planned for Phase 2")
    }

    override fun saveJob(record: JobRecord) {
        TODO("Not yet implemented")
    }

    override fun updateJob(record: JobRecord) {
        TODO("Not yet implemented")
    }

    override fun deleteJob(jobId: String) {
        TODO("Not yet implemented")
    }

    override fun getJob(jobId: String): JobRecord? {
        TODO("Not yet implemented")
    }

    override fun listAll(): Collection<JobRecord> {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }
}