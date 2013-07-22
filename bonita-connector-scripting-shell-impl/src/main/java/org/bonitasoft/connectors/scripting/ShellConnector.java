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
        final String interpreterInput = (String) getInputParameter(INTERPRETER);
        LOGGER.info(INTERPRETER + " " + interpreterInput);
        final String parameterInput = (String) getInputParameter(PARAMETER);
        LOGGER.info(PARAMETER + " " + parameterInput);
        final String scriptInput = (String) getInputParameter(SCRIPT);
        LOGGER.info(SCRIPT + " " + scriptInput);

        File tmpFile = createTempFile();
        writeScriptToFile(scriptInput, tmpFile);

        Process pr = executeScript(interpreterInput, parameterInput, tmpFile);

        StringBuilder builder;
        builder = readOutput(pr);
        setOutputParameter("result", builder.toString());
        setOutputParameter("exitStatus", getExitStatus(pr));
    }

    private int getExitStatus(Process pr) throws ConnectorException {
        try {
            return pr.waitFor();
        } catch (InterruptedException e) {
            throw new ConnectorException("Unable to retrieve script exit status", e);
        }
    }

    private StringBuilder readOutput(Process pr) throws ConnectorException {
        BufferedReader input;
        input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = readInputLine(input);
        StringBuilder builder = new StringBuilder();
        String lineSep = System.getProperty("line.separator");
        while (line != null) {
            builder.append(line);
            builder.append(lineSep);
            line = readInputLine(input);
        }
        return builder;
    }

    private String readInputLine(BufferedReader input) throws ConnectorException {
        String line;
        try {
            line = input.readLine();
        } catch (IOException e) {
            throw new ConnectorException("Unable to retrieve script output", e);
        }
        return line;
    }

    private Process executeScript(String interpreterInput, String parameterInput, File tmpFile) throws ConnectorException {
        Process pr;
        String args[];
        try {
            args = new String[]{interpreterInput, parameterInput, tmpFile.getCanonicalPath()};
        } catch (IOException e) {
            throw new ConnectorException("Unable to retrieve temporary file", e);
        }

        Runtime rt = Runtime.getRuntime();
        try {
            pr = rt.exec(args);
            pr.getOutputStream().close();
        } catch (IOException e) {
            throw new ConnectorException("Unable to execute script", e);
        }
        return pr;
    }

    private void writeScriptToFile(String scriptInput, File tmpFile) throws ConnectorException {

        FileWriter fw;
        try {
            fw = new FileWriter(tmpFile);
            fw.write(scriptInput);
            fw.close();
        } catch (IOException e) {
            throw new ConnectorException("Unable to write script to temporary file", e);
        }
    }

    private File createTempFile() throws ConnectorException {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("script", getExtension());
        } catch (IOException e) {
            throw new ConnectorException("Unable to create temporary file", e);
        }
        if (!tmpFile.setExecutable(true)) {
            throw new ConnectorException("Unable to make temporary file executable");
        }
        tmpFile.deleteOnExit();
        return tmpFile;

    }
}
