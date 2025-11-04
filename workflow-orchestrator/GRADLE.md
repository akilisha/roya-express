# Gradle Quick Start Guide

## Build Files

The project now supports both **Maven** and **Gradle**:
- `pom.xml` - Maven build configuration
- `build.gradle` - Gradle build configuration (Groovy DSL)
- `settings.gradle` - Gradle settings

## Prerequisites
- Java 21 or higher
- Gradle 8.0+ (or use the wrapper)

## Common Commands

### Build the Project
```bash
./gradlew build
```

### Compile Only
```bash
./gradlew compileJava
```

### Run Tests
```bash
./gradlew test
```

### Run the Example
```bash
./gradlew runExample
```

Or using the application plugin:
```bash
./gradlew run
```

### Clean Build
```bash
./gradlew clean build
```

### View Project Info
```bash
./gradlew info
```

### Generate Gradle Wrapper (if not present)
```bash
gradle wrapper --gradle-version 8.5
```

### List All Tasks
```bash
./gradlew tasks
```

### Run with Custom Main Class
```bash
./gradlew run --args="your arguments here"
```

Or create a custom task:
```groovy
task runCustom(type: JavaExec) {
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.your.MainClass'
}
```

## IDE Integration

### IntelliJ IDEA
1. File → Open → Select `build.gradle`
2. IntelliJ will automatically import the project
3. Wait for indexing to complete
4. Right-click on `CustomerSupportWorkflow.java` → Run

### Eclipse
1. File → Import → Gradle → Existing Gradle Project
2. Select the project root directory
3. Follow the wizard

### VS Code
1. Install "Extension Pack for Java"
2. Install "Gradle for Java"
3. Open project folder
4. VS Code will detect Gradle automatically

## Gradle vs Maven Commands

| Task | Maven | Gradle |
|------|-------|--------|
| Build | `mvn clean compile` | `./gradlew build` |
| Test | `mvn test` | `./gradlew test` |
| Run | `mvn exec:java -Dexec.mainClass=...` | `./gradlew run` |
| Clean | `mvn clean` | `./gradlew clean` |
| Package | `mvn package` | `./gradlew jar` |
| Install locally | `mvn install` | `./gradlew publishToMavenLocal` |

## Adding Dependencies

Edit `build.gradle`:

```groovy
dependencies {
    // Add runtime dependency
    implementation 'group:artifact:version'
    
    // Add compile-only dependency
    compileOnly 'group:artifact:version'
    
    // Add test dependency
    testImplementation 'group:artifact:version'
}
```

### Example: Adding SLF4J Logging
Uncomment these lines in `build.gradle`:
```groovy
implementation 'org.slf4j:slf4j-api:2.0.9'
implementation 'ch.qos.logback:logback-classic:1.4.11'
```

### Example: Adding Micrometer Metrics
Uncomment this line in `build.gradle`:
```groovy
implementation 'io.micrometer:micrometer-core:1.12.0'
```

## Gradle Build Features

### Incremental Builds
Gradle automatically detects unchanged files and skips rebuilding them.

### Build Cache
Enable the build cache for faster builds:
```groovy
// Add to settings.gradle
buildCache {
    local {
        enabled = true
    }
}
```

### Parallel Builds
Speed up multi-module builds:
```bash
./gradlew build --parallel
```

### Offline Mode
Build without checking for dependency updates:
```bash
./gradlew build --offline
```

## Customizing the Build

### Change Main Class
Edit `build.gradle`:
```groovy
application {
    mainClass = 'com.your.package.YourMainClass'
}
```

### Add Custom Task
Add to `build.gradle`:
```groovy
task myTask {
    group = 'custom'
    description = 'My custom task'
    doLast {
        println 'Running custom task'
    }
}
```

### Configure Test Output
Add to `build.gradle`:
```groovy
test {
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

## Gradle Daemon

The Gradle daemon speeds up builds by keeping the build environment in memory.

```bash
# Start daemon
./gradlew --daemon

# Stop daemon
./gradlew --stop

# Check daemon status
./gradlew --status
```

## Troubleshooting

### Build Fails
```bash
# Clean and rebuild
./gradlew clean build

# Show full stack trace
./gradlew build --stacktrace

# Debug mode
./gradlew build --debug
```

### Dependency Issues
```bash
# View dependency tree
./gradlew dependencies

# Refresh dependencies
./gradlew build --refresh-dependencies
```

### Wrapper Issues
```bash
# Re-generate wrapper
gradle wrapper --gradle-version 8.5

# Validate wrapper
./gradlew wrapper --validate-only
```

## Quick Start Example

```bash
# Clone or extract the project
cd workflow-orchestrator

# Build the project
./gradlew build

# Run the example
./gradlew runExample

# Run tests
./gradlew test

# View project info
./gradlew info
```

## Performance Tips

1. **Use the Gradle Daemon** - Keeps JVM warm between builds
2. **Enable Parallel Builds** - `org.gradle.parallel=true` in gradle.properties
3. **Increase Memory** - `org.gradle.jvmargs=-Xmx2g` in gradle.properties
4. **Use Build Cache** - Speeds up clean builds

## Next Steps

1. **Explore tasks**: `./gradlew tasks --all`
2. **Customize build.gradle** for your needs
3. **Add dependencies** as your project grows
4. **Create custom tasks** for automation

---

**Gradle is now configured and ready to use!** 🚀

You can use either Maven (`mvn`) or Gradle (`./gradlew`) - both are fully supported.
