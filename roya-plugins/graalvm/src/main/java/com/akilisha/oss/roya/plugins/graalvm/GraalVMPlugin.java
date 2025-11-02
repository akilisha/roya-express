package com.akilisha.oss.roya.plugins.graalvm;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;

/**
 * GraalVM Native Image plugin - provides native compilation support.
 *
 * This plugin registers GraalVM native-image configurations for:
 * - Jackson databind reflection
 * - Helidon resources
 * - Dynamic proxies
 *
 * Usage:
 *   Add the graalvm plugin dependency to enable native-image builds:
 *   ```gradle
 *   dependencies {
 *       implementation project(':roya-plugins:graalvm')
 *   }
 *   ```
 *
 *   Build native image:
 *   ```bash
 *   ./gradlew nativeCompile
 *   ```
 */
public class GraalVMPlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "graalvm";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "GraalVM native-image compilation support with pre-configured reflection and resources";
    }

    @Override
    public void register(Services services) {
        // No service registration needed
        // The plugin provides native-image configurations via META-INF/native-image
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ GraalVMPlugin: Native image configurations loaded");
        System.out.println("  - Reflection config: Jackson databind");
        System.out.println("  - Resource config: Helidon resources and templates");
        System.out.println("  - Proxy config: WebSocket dynamic proxies");
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed
    }
}

