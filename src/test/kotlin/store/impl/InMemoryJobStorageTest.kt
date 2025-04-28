package store.impl


import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.model.JobStatus
import com.samshend.jobscheduler.store.impl.InMemoryJobStorage
import com.samshend.jobscheduler.store.model.JobRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryJobStorageTest {

    private lateinit var storage: InMemoryJobStorage

    @BeforeEach
    fun setup() {
        storage = InMemoryJobStorage()
    }

    @Test
    fun `save and retrieve job`() {
        val jobDef = dummyJobDefinition("job-1")
        val record = JobRecord(jobDef)

        storage.saveJob(record)

        val retrieved = storage.getJob("job-1")
        assertNotNull(retrieved)
        assertEquals(jobDef.id, retrieved?.definition?.id)
    }

    @Test
    fun `update job`() {
        val jobDef = dummyJobDefinition("job-2")
        val record = JobRecord(jobDef)

        storage.saveJob(record)

        // Simulate updating job status
        record.status = JobStatus.RUNNING
        storage.updateJob(record)

        val updated = storage.getJob("job-2")
        assertEquals(JobStatus.RUNNING, updated?.status)
    }

    @Test
    fun `delete job`() {
        val jobDef = dummyJobDefinition("job-3")
        val record = JobRecord(jobDef)

        storage.saveJob(record)
        storage.deleteJob("job-3")

        val deleted = storage.getJob("job-3")
        assertNull(deleted)
    }

    @Test
    fun `list all jobs`() {
        val jobDef1 = dummyJobDefinition("id1")
        val jobDef2 = dummyJobDefinition("id2")

        storage.saveJob(JobRecord(jobDef1))
        storage.saveJob(JobRecord(jobDef2))

        val allJobs = storage.listAll()

        assertEquals(2, allJobs.size)
        val ids = allJobs.map { it.definition.id }
        assertTrue(ids.containsAll(listOf("id1", "id2")))
    }

    @Test
    fun `clear storage`() {
        val jobDef1 = dummyJobDefinition("id1")
        storage.saveJob(JobRecord(jobDef1))

        storage.clear()

        assertEquals(0, storage.listAll().size)
    }

    private fun dummyJobDefinition(id: String) = JobDefinition(
        id = id,
        name = "Test Job",
        resultType = String::class.java,
        action = { "Done" }
    )
}