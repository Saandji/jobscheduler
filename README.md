# JobScheduler Library

A lightweight, coroutine-based Kotlin job scheduler that supports:

- One-time and delayed jobs
- Retry policies (fixed or exponential backoff)
- Cancellation and lifecycle management
- Java and Kotlin interop with type-safe result casting

This library was designed to be minimal, clean, and easily extendable.

---

## Project Goals

- Keep dependencies minimal (Kotlin stdlib + coroutines only)
- Be usable from both Java and Kotlin clients easily
- Support a flexible configuration surface
- Stay lightweight, pluggable, and production-ready

---

# Short-term Roadmap

## Phase 1 - Core Scheduler

| Task                                                       | Status | Notes                                 |
|:-----------------------------------------------------------|:-------|:--------------------------------------|
| Create core `Scheduler` API                                | ✅ Done | Exposed via `CoroutineScheduler`      |
| Implement in-memory scheduler (`CoroutineScheduler`)       | ✅ Done | CoroutineScope with SupervisorJob     |
| Basic job lifecycle: schedule, run, complete, fail, cancel | ✅ Done | Full lifecycle states tracked         |
| Retry system (fixed + exponential backoff)                 | ✅ Done | With configurable retry policies      |
| Graceful handling of cancellation (`CANCELLED` status)     | ✅ Done | Cancelled jobs reported correctly     |
| Return `JobHandle` to users                                | ✅ Done | Includes cancel, getStatus, and await |
| Java/Kotlin interoperability                               | ✅ Done | Clean result casting for Java clients |
| Minimal sample app to verify behavior                      | ✅ Done | Schedules, cancels, and awaits jobs   |
| In-memory job store (initial)                              | ✅ Done | Basic in-memory ConcurrentHashMap     |
| Java-friendly API                                          | ✅ Done | Builder, Supplier etc                 |

---

## Wrap Up - Phase 1

| Task                    | Status    | Notes                                                |
|:------------------------|:----------|:-----------------------------------------------------|
| Finish up documentation | ⏳ Planned | Revisit roadmap. Describe architecture, features etc |
| Deployment artifacts    | ⏳ Planned | deployment artifacts ready to be pushed to maven     |
| Sample applications     | ⏳ Planned | Create sample apps to showcase the work              |

---

# Mid-term to Long-term roadmap

## Phase 2 - Persistence Layer

| Task                                    | Status    | Notes                               |
|:----------------------------------------|:----------|:------------------------------------|
| Auto-update from Persistence on startup | ⏳ Planned | Allow custom storages like DynamoDB |
| File-based persistence                  | ⏳ Planned | Store job state to local disk       |
| Pluggable store interface (Optional)    | ⏳ Planned | Allow custom storages like DynamoDB |
| Auto-update from Persistence on startup | ⏳ Planned | Allow custom storages like DynamoDB |

## Phase 3 - Listeners and Job Hooks

| Task                            | Status    | Notes                                   |
|:--------------------------------|:----------|:----------------------------------------|
| Job listeners                   | ⏳ Planned | Hooks before and after job execution    |
| Trigger listeners               | ⏳ Planned | Events for job scheduling, rescheduling |
| Scheduler lifecycle listeners   | ⏳ Planned | Events for scheduler start/stop/etc     |
| Plugin-style listener extension | ⏳ Planned | Allow user-registered listener modules  |

## Phase 4 - Metrics and Monitoring

| Task                     | Status    | Notes                                  |
|:-------------------------|:----------|:---------------------------------------|
| Integrate Micrometer     | ⏳ Planned | Expose job counts, failures, durations |
| SLF4J structured logging | ⏳ Planned | Detailed lifecycle event logs          |

---

## Phase 5 - Cron and Recurring Scheduling

| Task                                | Status    | Notes                           |
|:------------------------------------|:----------|:--------------------------------|
| Parse and schedule cron expressions | ⏳ Planned | Recurring jobs with cron syntax |
| Calendar-based exclusion support    | ⏳ Planned | Skip holidays and blackout days |
| Retry policies for recurring jobs   | ⏳ Planned | Smart retries per run           |

## Phase 6 - Advanced Retry Strategies

| Task                                       | Status    | Notes                          |
|:-------------------------------------------|:----------|:-------------------------------|
| Smarter backoff (with jitter)              | ⏳ Planned | Avoid thundering herd problems |
| Integrate AWS SDK RetryStrategy (optional) | ⏳ Planned | High quality retries           |

---

## Phase 7 - Web/REST Management

| Task                       | Status    | Notes                           |
|:---------------------------|:----------|:--------------------------------|
| Tiny REST API server       | ⏳ Planned | List jobs, cancel jobs via HTTP |
| Web UI frontend (optional) | ⏳ Planned | Visual dashboard for jobs       |

---

## Phase 8 - Distributed Scheduling (Advanced)

| Task                          | Status    | Notes                    |
|:------------------------------|:----------|:-------------------------|
| Cluster-safe persistence      | ⏳ Planned | Locking, leader election |
| Distributed execution support | ⏳ Planned | Horizontal scaling       |

---

## License

Licensed under the MIT License . See [LICENSE](LICENSE) for details.

