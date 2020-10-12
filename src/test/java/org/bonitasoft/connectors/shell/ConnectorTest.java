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
