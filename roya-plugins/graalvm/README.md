# GraalVM Native Image Plugin

The GraalVM plugin provides native-image compilation support for Roya applications.

## Features

- **Automatic Configuration**: Pre-configured native-image settings for Jackson, Helidon, and WebSocket
- **Reflection Support**: Automatically handles Jackson databind reflection for JSON serialization
- **Resource Bundling**: Includes all resources and templates in the native binary
- **Proxy Configuration**: Handles WebSocket dynamic proxy classes
- **Zero Configuration**: Just add the dependency and build!

## Usage

### Add the Plugin

```gradle
dependencies {
    implementation project(':roya-plugins:graalvm')
}
```

### Build Native Image

```bash
./gradlew :your-module:nativeCompile
```

The native executable will be generated at:
```
build/native/nativeCompile/your-app-name
```

### Run Native Executable

```bash
./build/native/nativeCompile/your-app-name
```

### Docker Build

```bash
docker build -t your-app-native -f Dockerfile.native .
```

## What's Configured

The plugin automatically configures:

1. **Reflection** (`reflect-config.json`):
   - Jackson databind classes (`ObjectMapper`, `SerializerProvider`, `DeserializerProvider`)
   - Java 8 time types (`LocalDateTime`, `ZonedDateTime`, `OffsetDateTime`, `Instant`)

2. **Resources** (`resource-config.json`):
   - All JSON, properties, and XML files
   - Service discovery files (`META-INF/services/*`)
   - Helidon resources (`META-INF/helidon/*`)
   - Handlebars templates (`.hbs` files)

3. **Proxy Classes** (`proxy-config.json`):
   - WebSocket interfaces

4. **Build Arguments** (`native-image.properties`):
   - `--enable-preview` for Java preview features
   - `--no-fallback` for strict native compilation
   - `-H:+StaticExecutableWithDynamicLibC` for static linking
   - `-H:IncludeResources=.*` to include all resources
   - `-H:+ReportExceptionStackTraces` for debugging

## Example

See `roya-examples/src/main/java/com/akilisha/oss/roya/examples/HelloWorld.java` for a complete example.

The native image will start in <50ms and use <50MB of memory, compared to ~1 second and ~200MB for JVM builds.

## Customization

To add additional native-image configurations, create files in:

```
src/main/resources/META-INF/native-image/your-group/your-artifact/
```

Additional reflection entries:
```json
[
  {
    "name": "com.example.MyClass",
    "allDeclaredConstructors": true,
    "allPublicMethods": true,
    "allDeclaredFields": true
  }
]
```

## Limitations

- Native compilation requires GraalVM JDK 21+ with native-image installed
- Some Java features require additional configuration (e.g., `sun.misc.Unsafe`)
- First build takes 2-5 minutes (subsequent builds are faster with cache)

## Testing

```bash
# Build JVM version (for testing)
./gradlew :your-module:build

# Run on JVM
./gradlew :your-module:run

# Build native version
./gradlew :your-module:nativeCompile

# Run native executable
./build/native/nativeCompile/your-app-name
```

## References

- [Helidon GraalVM Native Image Guide](https://helidon.io/docs/v4/mp/guides/graalnative)
- [GraalVM Native Image Documentation](https://www.graalvm.org/latest/reference-manual/native-image/)
- [GraalVM Native Build Tools](https://graalvm.github.io/native-build-tools/)

