/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.connectors.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Matthieu Chaffotte, Yanyan Liu, Hongwen Zang, Arthur Freycon
 * 
 */
public class ShellConnector extends AbstractConnector {

    private static final String INTERPRETER = "interpreter";

    private static final String PARAMETER = "parameter";

    private static final String SCRIPT = "script";

    private Logger LOGGER = Logger.getLogger(this.getClass().getName());

    public ShellConnector() {
    }

    public Object getExitStatus() {
        return getOutputParameters().get("exitStatus");
    }

    public Object getResult() {
        return getOutputParameters().get("result");
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> errors = new ArrayList<String>(3);
        final String interpreter = (String) getInputParameter(INTERPRETER);
        if (interpreter == null || !(interpreter.length() > 0)) {
            errors.add("interpreter cannot be empty!");
        }
        final String parameter = (String) getInputParameter(PARAMETER);
        if (parameter == null || !(parameter.length() > 0)) {
            errors.add("parameter cannot be empty!");
        }
        final String shell = (String) getInputParameter(SCRIPT);
        if (shell == null || !(shell.length() > 0)) {
            errors.add("script cannot be empty!");
        }

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
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
            script.delete();

        } catch (Exception e) {
            throw new ConnectorException(e.getMessage(), e.getCause());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private String getParameter(String parameterName) {
        final String interpreterInput = (String) getInputParameter(parameterName);
        LOGGER.info(parameterName + " " + interpreterInput);
        return interpreterInput;
    }

    private Process runScript(final String interpreterInput, final String parameterInput, File script) throws IOException {
        String args[] = { interpreterInput, parameterInput, script.getCanonicalPath() };
        Process process = Runtime.getRuntime().exec(args);
        
        // close unused streams
        process.getOutputStream().close();
        process.getErrorStream().close();
        return process;
    }

    private String consumeProcessOutput(Process process) throws IOException {
        BufferedReader scriptOutputReader = null;
        try {
            scriptOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = scriptOutputReader.readLine();
            StringBuilder builder = new StringBuilder();
            String lineSep = System.getProperty("line.separator");
            while (line != null) {
                builder.append(line);
                builder.append(lineSep);
                line = scriptOutputReader.readLine();
            }
            return builder.toString();
        } finally {
            try {
                if (scriptOutputReader != null) scriptOutputReader.close();
            } catch (Exception e) {
                LOGGER.warning("Unable to close " + scriptOutputReader.toString());
            }
        }
    }

    private File createExecutableScript(final String scriptInput) throws IOException {
        FileWriter fw = null;
        try {
            File tmpFile = File.createTempFile("script", getExtension());
            tmpFile.setExecutable(true);
            tmpFile.deleteOnExit();
            fw = new FileWriter(tmpFile);
            fw.write(scriptInput);
            return tmpFile;
        } finally {
            try {
                if (fw != null) fw.close();
            } catch (Exception e) {
                LOGGER.warning("Unable to close " + fw.toString());
            }
        }
    }
}
