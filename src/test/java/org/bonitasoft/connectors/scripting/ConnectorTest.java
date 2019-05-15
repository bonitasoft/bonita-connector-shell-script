/**
 * Copyright (C) 2012-2019 BonitaSoft S.A.
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

import static org.junit.Assume.assumeTrue;

import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author Matthieu Chaffotte, Yanyan Liu, Hongwen Zang
 */
public abstract class ConnectorTest {

    protected static final Logger LOG = Logger.getLogger(ConnectorTest.class.getName());

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOG.warning("==== Starting test: " + this.getClass().getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOG.warning("==== Failed test: " + this.getClass().getName() + "." + d.getMethodName() + "() ====");
        }

        @Override
        public void succeeded(final Description d) {
            LOG.warning("==== Succeeded test: " + this.getClass().getName() + "." + d.getMethodName() + "() ====");
        }

    };

    private static final String os = System.getProperty("os.name").toLowerCase();

    protected static boolean isWindows() {
        return os.contains("win");
    }

    protected static boolean isMac() {
        return os.contains("mac");
    }

    protected static boolean isUnix() {
        return os.contains("nix") || os.contains("nux");
    }

    protected static void assumeIsMacOrUnix() {
        assumeTrue(isMac() || isUnix());
    }

    protected static void assumeIsWindows() {
        assumeTrue(isWindows());
    }

}
