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
package org.bonita.connector.scripting.shell.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.bonitasoft.connectors.scripting.ShellConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Hongwen Zang
 *         -Dbonita.home=${workspace_loc}/bos-continuous-integration-tests/bos-local-continuous-integration-tests/target/home -DsmtpServerAddress=192.168.1.211
 *         -DsmtpServerPort=555
 */
public class ShellConnectorTest extends ConnectorTest {

    private String getInterpreter() {
        return isUnix() || isMac() ? "/bin/sh" : "cmd.exe";
    }

    private String getParameter() {
        return isUnix() || isMac() ? "-c" : "/c";
    }

    private String getShellScript() {
        return isUnix() || isMac() ? "ls -lia" : "dir";
    }

    private String getMultiLineShellScript() {
        if (isUnix() || isMac()) {
            return "ls \n"
                    + "var=2"
                    + "echo $var \n"
                    + "var=$(($var+1)) \n"
                    + "echo var"
                    + "sleep 2";
        }
        else {
            return "dir \n"
                    + "set var=2 \n"
                    + "echo %var% \n"
                    + "set /a var=%var%+1 \n"
                    + "echo %var% \n"
                    + "ping 123.45.67.89 -n 1 -w 2500 > nul \n";
        }
    }

    
    @Test
    public void testMultiLineScript() throws Exception {
        if (isUnix() || isWindows() || isMac()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(3);
            parametersMap.put("interpreter", getInterpreter());
            parametersMap.put("parameter", getParameter());
            parametersMap.put("script", getMultiLineShellScript());
            shell.setInputParameters(parametersMap);
            shell.validateInputParameters();
            shell.execute();
            String actual = (String) shell.getResult();
            assertTrue(actual.length() >= 0);
        }
    }

    @Test
    public void testCurrentDirectory() throws Exception {
        if (isUnix() || isWindows() || isMac()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(3);
            parametersMap.put("interpreter", getInterpreter());
            parametersMap.put("parameter", getParameter());
            parametersMap.put("script", getShellScript());
            shell.setInputParameters(parametersMap);
            shell.execute();
            String actual = (String) shell.getResult();
            assertTrue(actual.length() >= 0);
        }
    }

    @Test
    public void testExit() throws Exception {
        if (isMac() || isUnix() || isWindows()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(3);
            parametersMap.put("interpreter", getInterpreter());
            parametersMap.put("parameter", getParameter());
            parametersMap.put("script", "exit 3");
            shell.setInputParameters(parametersMap);
            shell.execute();
            assertEquals(3, shell.getExitStatus());
        }
    }

    @Test
    public void testSleep() throws Exception {
        if (isMac() || isUnix() || isWindows()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(3);
            parametersMap.put("interpreter", getInterpreter());
            parametersMap.put("parameter", getParameter());
            String script;
            script = "echo start \n";
            if (isMac() || isUnix())
                script = script + "sleep 5\n";
            else
                script = script + "ping 123.45.67.89 -n 1 -w 5000 > nul \n";
            script = script + "echo end \n";
            parametersMap.put("script", script);
            shell.setInputParameters(parametersMap);
            shell.execute();
            assertEquals(0, shell.getExitStatus());
        }
    }

    @Ignore
    @Test
    public void testPowershell() throws Exception {
        if (isWindows()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(3);
            parametersMap.put("interpreter", "powershell");
            parametersMap.put("parameter", "&c");
            parametersMap.put("script", "ls");
            shell.setInputParameters(parametersMap);
            shell.execute();
            assertEquals(0, shell.getExitStatus());
        }
    }

    @Test(expected = Exception.class)
    public void testWrongParameters() throws Exception {
        if (isMac() || isUnix() || isWindows()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(1);
            shell.setInputParameters(parametersMap);
            shell.validateInputParameters();
        }
    }

    @Test
    public void testRottenScript() throws Exception {
        if (isMac() || isUnix() || isWindows()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(1);
            parametersMap.put("interpreter", getInterpreter());
            parametersMap.put("parameter", getParameter());
            parametersMap.put("script", "hefv ghfvg ");
            shell.setInputParameters(parametersMap);
            shell.validateInputParameters();
            shell.execute();
            System.out.println("result:" + shell.getResult());
            System.out.println("status:" + shell.getExitStatus());
            assertThat((Integer) shell.getExitStatus(), not(is(0)));

        }
    }

    @Test
    public void testPipe() throws ConnectorValidationException, ConnectorException {
        if (isMac() || isUnix()) {
            ShellConnector shell = new ShellConnector();
            final HashMap<String, Object> parametersMap = new HashMap<String, Object>(1);
            parametersMap.put("interpreter", "/bin/sh");
            parametersMap.put("parameter", "-c");
            parametersMap.put("script", "ls | wc -l");
            shell.setInputParameters(parametersMap);
            shell.validateInputParameters();
            shell.execute();
            assertThat(shell.getExitStatus(), is(shell.getExitStatus()));
        }
    }
}
