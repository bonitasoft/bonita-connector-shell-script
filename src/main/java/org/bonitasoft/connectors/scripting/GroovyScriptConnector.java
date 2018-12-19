/**
 * Copyright (C) 2014, 2015 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.scripting;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.expression.ExpressionConstants;

/**
 * @author Matthieu Chaffotte
 */
public class GroovyScriptConnector extends AbstractConnector {

    public static final String SCRIPT = "script";

    public static final String CONTEXT = "variables";

    public static final String RESULT = "result";

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final String script = (String) getInputParameter(SCRIPT);
        final Map<String, Object> variables = getVariables();
        final Binding binding = new Binding(variables);
        final GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding);
        try {
            final Object result = shell.evaluate(script);
            setOutputParameter(RESULT, result);
        } catch (final GroovyRuntimeException gre) {
            throw new ConnectorException(gre);
        }
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final String script = (String) getInputParameter(SCRIPT);
        if (script == null) {
            throw new ConnectorValidationException(this, "The script is null");
        }
    }

    private Map<String, Object> getVariables() {
        final List<List<Object>> context = (List<List<Object>>) getInputParameter(CONTEXT);
        final Map<String, Object> variables = new HashMap<String, Object>();
        if (context != null) {
            for (final List<Object> rows : context) {
                if (rows.size() == 2) {
                    final Object keyContent = rows.get(0);
                    final Object valueContent = rows.get(1);
                    if (keyContent != null) {
                        final String key = keyContent.toString();
                        if (ExpressionConstants.API_ACCESSOR.getEngineConstantName().equals(key)) {
                            variables.put(key, getAPIAccessor());
                        } else {
                            variables.put(key, valueContent);
                        }
                    }
                }
            }
        }
        return variables;
    }

}
