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

import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Matthieu Chaffotte, Yanyan Liu, Hongwen Zang, Arthur Freycon
 */
public class ShellConnector extends AbstractConnector {

    private static final String INTERPRETER = "interpreter";

    private static final String PARAMETER = "parameter";

    private static final String SCRIPT = "script";

    private static final Logger logger = Logger.getLogger(ShellConnector.class.getName());

    public ShellConnector() {
        // do nothing
    }

    public Object getExitStatus() {
        return getOutputParameters().get("exitStatus");
    }

    public Object getResult() {
        return getOutputParameters().get("result");
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> errors = new ArrayList<>();
        final String interpreter = (String) getInputParameter(INTERPRETER);
        if (isNullOrEmpty(interpreter)) {
            errors.add("interpreter cannot be empty!");
        }
        final String parameter = (String) getInputParameter(PARAMETER);
        if (isNullOrEmpty(parameter)) {
            errors.add("parameter cannot be empty!");
        }
        final String shell = (String) getInputParameter(SCRIPT);
        if (isNullOrEmpty(shell)) {
            errors.add("script cannot be empty!");
        }

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        String interpreterInput = getParameter(INTERPRETER);
        String parameterInput = getParameter(PARAMETER);
        String scriptInput = getParameter(SCRIPT);

        Process process = null;
        try {
            File script = createExecutableScript(scriptInput);
            process = runScript(interpreterInput, parameterInput, script);
            String processOutput = consumeProcessOutput(process);

            setOutputParameter("result", processOutput);
            setOutputParameter("exitStatus", process.waitFor());
            cleanUp(script);
        } catch (Exception e) {
            throw new ConnectorException(e.getMessage(), e.getCause());
        } finally {
            ofNullable(process).ifPresent(Process::destroy);
        }
    }

    private void cleanUp(File script) throws IOException {
        try {
            Files.delete(script.toPath());
        } catch (IOException e) {
            logger.severe(
                    () -> String.format("'%s' has not been cleaned after connector execution.", script.getAbsolutePath()));
            throw e;
        }
    }

    private String getParameter(String parameterName) {
        final String value = (String) getInputParameter(parameterName);
        logger.info(parameterName + " " + value);
        return value;
    }

    private File createExecutableScript(final String scriptInput) throws IOException {
        File tmpFile = File.createTempFile(SCRIPT, getExtension());
        if (tmpFile.setExecutable(true)) {
            tmpFile.deleteOnExit();
            try (FileWriter fw = new FileWriter(tmpFile)) {
                fw.write(scriptInput);
            }
            return tmpFile;
        }
        throw new IOException(
                String.format("An error occured while trying to make the file '%s' executable. Check your permissions.",
                        tmpFile.getAbsolutePath()));
    }

    private String getExtension() {
        final String interpreter = (String) getInputParameter(INTERPRETER);
        if (interpreter.contains("cmd"))
            return ".bat";
        else if (interpreter.contains("powershell"))
            return ".ps1";
        else if (interpreter.contains("sh"))
            return ".sh";
        else
            return "";
    }

    private Process runScript(final String interpreterInput, final String parameterInput, File script) throws IOException {
        String[] args = { interpreterInput, parameterInput, script.getCanonicalPath() };
        Process process = null;
        try {
            process = new ProcessBuilder(args).redirectErrorStream(true).start();
            return process;
        } finally {
            if (process != null) {
                closeQuietly(process.getOutputStream());
                closeQuietly(process.getErrorStream());
            }
        }
    }

    private String consumeProcessOutput(Process process) throws IOException {
        try (InputStream processInputStream = process.getInputStream();
                BufferedReader scriptOutputReader = new BufferedReader(new InputStreamReader(processInputStream))) {
            StringBuilder builder = new StringBuilder();
            String line = scriptOutputReader.readLine();
            String lineSep = System.getProperty("line.separator");
            while (line != null) {
                builder.append(line);
                builder.append(lineSep);
                line = scriptOutputReader.readLine();
            }
            return builder.toString();
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.warning("Unable to close " + e.getMessage());
            }
        }
    }

}
