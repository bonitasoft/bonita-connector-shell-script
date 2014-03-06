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
package org.bonitasoft.connectors.scripting.groovy.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.bonitasoft.connectors.scripting.GroovyConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Baptiste Mesta
 */
public class GroovyConnectorTest {

    public void executeSimpleScript() throws Exception {
        assertEquals("test", executeConnectorWith("test"));
    }

    private Object executeConnectorWith(final String script) throws ConnectorException, ConnectorValidationException {
        final GroovyConnector connector = new GroovyConnector();
        final HashMap<String, Object> parametersMap = new HashMap<String, Object>(1);
        parametersMap.put("script", script);
        connector.setInputParameters(parametersMap);
        connector.validateInputParameters();
        return connector.execute().get("result");
    }

}
