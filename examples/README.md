# JobScheduler Examples

This module contains **Java and Kotlin examples** that demonstrate how to use the `jobscheduler` library in real-world
scenarios.
Each example is fully runnable as a CLI application and can be executed:

- ✅ Directly via IntelliJ or command line
- ✅ As a self-contained fat JAR
- ✅ Inside Docker containers (for deployment/test)

---

## 📦 Included Examples

| Example Class          | Description                                |
|------------------------|--------------------------------------------|
| `HelloWorldJobExample` | Basic job that returns a string            |
| `CancellationExample`  | Starts and then cancels a long-running job |

---

## 🔧 Build Instructions

### Build all examples (as fat JAR)

```bash
./gradlew :examples:shadowJar
```

> Output: `build/libs/examples-all.jar`

---

## 🐳 Run in Docker

### 1. Build a Docker image for a specific example

#### HelloWorldJobExample

```bash
docker build -f Dockerfile.hello -t jobscheduler-hello .
docker run jobscheduler-hello
```

#### CancellationExample

```bash
docker build -f Dockerfile.cancel -t jobscheduler-cancel .
docker run jobscheduler-cancel
```

---

## 🧪 Run from CLI (outside Docker)

You can also run examples directly from the fat JAR:

```bash
java -cp build/libs/examples-all.jar examples.HelloWorldJobExample
```

---

## 🧠 Add New Examples

1. Create a new class in `src/main/java/examples/`
2. Use the `jobscheduler` API to define and schedule a job
3. Create a Dockerfile (e.g. `Dockerfile.myexample`):

```Dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/examples-all.jar app.jar
ENTRYPOINT ["java", "-cp", "app.jar", "examples.YourNewExample"]
```

4. Build and run:

```bash
./gradlew :examples:shadowJar
docker build -f Dockerfile.myexample -t jobscheduler-your-new-example .
docker run jobscheduler-your-new-example
```

---

## 🧼 Clean Up Docker Artifacts

```bash
docker ps -a             # running containers
docker rm <id>           # remove container
docker images            # all images
docker rmi <id>          # remove image
```

---

Happy scheduling! 🚀