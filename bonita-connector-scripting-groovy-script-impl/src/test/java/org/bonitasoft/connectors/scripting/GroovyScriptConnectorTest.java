/**
 * Copyright (C) 2014 BonitaSoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.junit.Test;

public class GroovyScriptConnectorTest {

    private GroovyScriptConnector buildConnector(final String script) {
        return buildConnector(script, null);
    }

    private GroovyScriptConnector buildConnector(final String script, final List<List<Object>> variables) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(GroovyScriptConnector.SCRIPT, script);
        parameters.put(GroovyScriptConnector.CONTEXT, variables);
        final GroovyScriptConnector connector = new GroovyScriptConnector();
        connector.setInputParameters(parameters);
        return connector;
    }

    @Test(expected = ConnectorValidationException.class)
    public void validate_should_throw_a_validation_exception_if_the_script_is_null() throws Exception {
        final GroovyScriptConnector connector = buildConnector(null);

        connector.validateInputParameters();
    }

    @Test
    public void validate_should_be_ok_if_the_script_is_empty() throws Exception {
        final GroovyScriptConnector connector = buildConnector("");

        connector.validateInputParameters();
    }

    @Test
    public void execute_should_return_null_if_the_script_is_empty() throws Exception {
        final GroovyScriptConnector connector = buildConnector("");

        final Map<String, Object> execute = connector.execute();

        final Object result = execute.get(GroovyScriptConnector.RESULT);
        assertThat(result).isNull();
    }

    @Test
    public void execute_should_return_the_result_of_the_script() throws Exception {
        final GroovyScriptConnector connector = buildConnector("[0, 1, 2, 3]");

        connector.validateInputParameters();
        final Map<String, Object> execute = connector.execute();

        final List<Integer> result = (List<Integer>) execute.get(GroovyScriptConnector.RESULT);
        assertThat(result).containsOnly(0, 1, 2, 3);
    }

    @Test(expected = ConnectorException.class)
    public void execute_should_throw_an_exception_if_a_variable_is_missing() throws Exception {
        final GroovyScriptConnector connector = buildConnector("count");

        connector.execute();
    }

    @Test
    public void execute_should_use_a_dependency() throws Exception {
        final List<List<Object>> context = new ArrayList<List<Object>>();
        final List<Object> var1 = new ArrayList<Object>();
        var1.add("count");
        var1.add(83);
        context.add(var1);
        final GroovyScriptConnector connector = buildConnector("count", context);

        final Map<String, Object> execute = connector.execute();

        final Integer result = (Integer) execute.get(GroovyScriptConnector.RESULT);
        assertThat(result).isEqualTo(83);
    }

    @Test
    public void execute_should_use_a_provided_dependency() throws Exception {
        final List<List<Object>> context = new ArrayList<List<Object>>();
        final List<Object> var1 = new ArrayList<Object>();
        var1.add(ExpressionConstants.API_ACCESSOR.getEngineConstantName());
        var1.add(83);
        context.add(var1);
        final APIAccessor apiAccessor = mock(APIAccessor.class);
        final IdentityAPI identityAPI = mock(IdentityAPI.class);
        when(apiAccessor.getIdentityAPI()).thenReturn(identityAPI);
        when(identityAPI.getNumberOfUsers()).thenReturn(54L);
        final GroovyScriptConnector connector = buildConnector("apiAccessor.getIdentityAPI().getNumberOfUsers();", context);
        connector.setAPIAccessor(apiAccessor);

        final Map<String, Object> execute = connector.execute();

        final Long result = (Long) execute.get(GroovyScriptConnector.RESULT);
        assertThat(result).isEqualTo(54L);
    }

}
