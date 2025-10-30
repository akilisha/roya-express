package com.akilisha.oss.roya.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class RoyaCliNewTest {
    @Test
    void scaffoldsMinimalProject(@TempDir Path tmp) throws Exception {
        Path target = tmp.resolve("demo-app");
        int code = new picocli.CommandLine(new RoyaCli()).execute("new", target.toString(), "--group", "com.acme");
        assertThat(code).isEqualTo(0);
        assertThat(Files.exists(target.resolve("build.gradle"))).isTrue();
        assertThat(Files.exists(target.resolve("settings.gradle"))).isTrue();
        assertThat(Files.exists(target.resolve("src/main/java/app/Main.java"))).isTrue();
    }
}


