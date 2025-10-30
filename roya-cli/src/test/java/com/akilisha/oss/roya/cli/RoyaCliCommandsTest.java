package com.akilisha.oss.roya.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RoyaCliCommandsTest {
    @Test
    void devBuildsGradleCommandInDryRun() {
        int code = new picocli.CommandLine(new RoyaCli()).execute("--dry-run", "dev", "HelloWorld");
        assertThat(code).isEqualTo(0);
        assertThat(RoyaCli.LAST_CMD).contains(":roya-examples:run");
        assertThat(RoyaCli.LAST_CMD).contains("HelloWorld");
    }

    @Test
    void runBuildsGradleCommandInDryRun() {
        int code = new picocli.CommandLine(new RoyaCli()).execute("--dry-run", "run", "--class", "com.acme.Main", "--module", ":app", "--args", "Foo");
        assertThat(code).isEqualTo(0);
        assertThat(RoyaCli.LAST_CMD).contains(":app:run");
        assertThat(RoyaCli.LAST_CMD).contains("-DmainClass=com.acme.Main");
    }

    @Test
    void composeUpAndDownDryRun() {
        int up = new picocli.CommandLine(new RoyaCli()).execute("--dry-run", "compose", "up", "--service", "qdrant");
        assertThat(up).isEqualTo(0);
        assertThat(RoyaCli.LAST_CMD).contains("docker");

        int down = new picocli.CommandLine(new RoyaCli()).execute("--dry-run", "compose", "down", "--service", "qdrant");
        assertThat(down).isEqualTo(0);
        assertThat(RoyaCli.LAST_CMD).contains("docker");
    }
}


