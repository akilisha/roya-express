package com.akilisha.oss.roya.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "roya", mixinStandardHelpOptions = true, version = "0.1",
    subcommands = {RoyaCli.New.class, RoyaCli.Run.class, RoyaCli.Dev.class, RoyaCli.Compose.class})
public class RoyaCli implements Callable<Integer> {
    public static void main(String[] args) {
        int code = new CommandLine(new RoyaCli()).execute(args);
        System.exit(code);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    @Command(name = "new", description = "Scaffold a new Roya app")
    static class New implements Callable<Integer> {
        @Parameters(index = "0", description = "Project directory name")
        String name;

        @Option(names = "--group", description = "Group ID", defaultValue = "com.example")
        String group;

        @Override public Integer call() throws Exception {
            Path root = Path.of(name);
            if (Files.exists(root)) {
                System.err.println("Directory already exists: " + root);
                return 1;
            }
            Files.createDirectories(root.resolve("src/main/java"));
            Files.createDirectories(root.resolve("src/main/resources"));
            // Minimal Gradle build
            write(root.resolve("build.gradle"), """
plugins { id 'application' }
repositories { mavenCentral() }
dependencies { implementation 'com.fasterxml.jackson.core:jackson-databind:2.16.1' }
application { mainClass = 'app.Main' }
tasks.withType(JavaCompile) { options.compilerArgs += ['--enable-preview'] }
tasks.withType(Test) { jvmArgs += ['--enable-preview'] }
""");
            write(root.resolve("settings.gradle"), "rootProject.name = '%s'\n".formatted(name));
            // Main.java
            Path main = root.resolve("src/main/java/app/Main.java");
            Files.createDirectories(main.getParent());
            write(main, """
package app;

import com.akilisha.oss.roya.Roya;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var app = Roya.create();
        app.get("/", (req, res, next) -> res.json(Map.of("hello","world")));
        app.listen(3000, () -> System.out.println("App on http://localhost:3000"));
    }
}
""");
            System.out.println("Scaffolded Roya app in " + root.toAbsolutePath());
            return 0;
        }
    }

    @Command(name = "run", description = "Run a class via Gradle :run or JavaExec")
    static class Run implements Callable<Integer> {
        @Option(names = "--class", required = true, description = "Main class to run")
        String mainClass;
        @Option(names = "--module", description = "Gradle module (default :roya-examples)", defaultValue = ":roya-examples")
        String module;
        @Option(names = "--args", description = "Args to pass to main")
        String args;

        @Override public Integer call() throws Exception {
            String cmd = String.format("./gradlew %s:run --no-daemon -DmainClass=%s", module, mainClass);
            if (args != null && !args.isBlank()) {
                cmd += " --args=\"" + args.replace("\"", "\\\"") + "\"";
            }
            System.out.println(cmd);
            return exec(cmd);
        }
    }

    @Command(name = "dev", description = "Run example with preview and .env support")
    static class Dev implements Callable<Integer> {
        @Parameters(index = "0", description = "Example main class (e.g., HelloWorld)")
        String exampleMain;

        @Override public Integer call() throws Exception {
            String cmd = String.format("./gradlew :roya-examples:run --no-daemon --args=\"%s\"", exampleMain);
            System.out.println(cmd);
            return exec(cmd);
        }
    }

    @Command(name = "compose", description = "Shortcuts for docker compose up/down services")
    static class Compose implements Callable<Integer> {
        @Parameters(index = "0", description = "up|down")
        String action;
        @Option(names = "--service", description = "Service name (qdrant|minio|postgres|vault)")
        String service;

        @Override public Integer call() throws Exception {
            String base = isWindows() ? "docker-compose" : "docker compose";
            String cmd;
            if ("up".equals(action)) {
                cmd = service == null ? base + " up -d" : base + " up -d " + service;
            } else if ("down".equals(action)) {
                cmd = service == null ? base + " down" : base + " rm -sf " + service;
            } else {
                System.err.println("Unknown action: " + action);
                return 1;
            }
            System.out.println(cmd);
            return exec(cmd);
        }
    }

    static int exec(String cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(shell(), shellArg(), cmd)
            .inheritIO()
            .start();
        return p.waitFor();
    }

    static String shell() { return isWindows() ? "cmd" : "bash"; }
    static String shellArg() { return isWindows() ? "/c" : "-lc"; }
    static boolean isWindows() { return System.getProperty("os.name").toLowerCase().contains("win"); }

    static void write(Path file, String content) throws IOException {
        Files.writeString(file, content);
    }
}


