/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.shell;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.Test;

/**
 * @author Hongwen Zang
 */
public class ShellConnectorTest extends ConnectorTest {

    private static final Logger LOGGER = Logger.getLogger(ShellConnectorTest.class.getName());

    @Test
    public void should_run_multi_line_script() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        parameters.put("script", multiLineShellScript());

        ShellConnector shell = validateAndExecute(parameters);
        assertThat((String) shell.getResult()).isNotEmpty();
    }

    @Test
    public void should_list_content_of_current_directory() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        parameters.put("script", listContentOfCurrentDirectoryShellScript());

        ShellConnector shell = validateAndExecute(parameters);
        assertThat((String) shell.getResult()).isNotEmpty();
    }

    @Test
    public void should_exit_with_non_zero_status() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        parameters.put("script", "exit 3");

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isEqualTo(3);
    }

    @Test
    public void should_run_sleep_script() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        if (isMac() || isUnix()) {
            parameters.put("script", script("echo start", "sleep 5", "echo end"));
        } else {
            parameters.put("script", script("echo start", "ping 123.45.67.89 -n 1 -w 5000 > nul", "echo end"));
        }

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isEqualTo(0);
    }

    @Test
    public void should_run_powershell_on_Windows() throws Exception {
        assumeIsWindows();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", "powershell");
        parameters.put("parameter", "-command");
        parameters.put("script", "$PSVersionTable");

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isEqualTo(0);
    }

    @Test
    public void should_detect_wrong_input_parameters() {
        assertThatThrownBy(() -> validateAndExecute(emptyMap()))
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("interpreter cannot be empty!")
                .hasMessageContaining("parameter cannot be empty!")
                .hasMessageContaining("script cannot be empty!");
    }

    @Test
    public void should_fail_when_script_contains_unknown_command() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        parameters.put("script", "hefv ghfvg ");

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isNotEqualTo(0);
    }

    @Test
    public void should_run_pipe_on_mac_and_linux() throws Exception {
        assumeIsMacOrUnix();

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        parameters.put("script", "ls | wc -l");

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isEqualTo(0);
    }

    @Test
    public void should_result_contains_standard_error_content() throws Exception {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("interpreter", defaultOsInterpreter());
        parameters.put("parameter", defaultOsParameter());
        if (isWindows()) {
            parameters.put("script", script("@echo off", "java -version"));
        } else {
            parameters.put("script", script("java -version"));
        }

        ShellConnector shell = validateAndExecute(parameters);
        assertThat(shell.getExitStatus()).as("exit status").isEqualTo(0);
        assertThat((String) shell.getResult()).isNotEmpty();
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static String defaultOsInterpreter() {
        return isUnix() || isMac() ? "/bin/sh" : "cmd.exe";
    }

    private static String defaultOsParameter() {
        return isUnix() || isMac() ? "-c" : "/c";
    }

    private static String listContentOfCurrentDirectoryShellScript() {
        return isUnix() || isMac() ? "ls -lia" : "dir";
    }

    private static String multiLineShellScript() {
        if (isUnix() || isMac()) {
            return script("ls", "var=2", "echo $var", "var=$(($var+1))", "echo var", "sleep 2");
        } else {
            return script("dir", "set var=2", "echo %var%", "set /a var=%var%+1", "echo %var%",
                    "ping 123.45.67.89 -n 1 -w 2500 > nul");
        }
    }

    private static String script(String... scriptLines) {
        return join(lineSeparator(), scriptLines);
    }

    private static ShellConnector validateAndExecute(Map<String, Object> parameters)
            throws ConnectorValidationException, ConnectorException {
        ShellConnector shell = new ShellConnector();
        shell.setInputParameters(parameters);
        shell.validateInputParameters();
        shell.execute();

        LOGGER.info("Execution status: " + shell.getExitStatus());
        LOGGER.info("Execution result: " + shell.getResult());
        return shell;
    }

}
