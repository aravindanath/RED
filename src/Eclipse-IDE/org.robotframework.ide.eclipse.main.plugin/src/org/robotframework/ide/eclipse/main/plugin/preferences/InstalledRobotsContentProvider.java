/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.List;

import org.robotframework.red.viewers.StructuredContentProvider;

public class InstalledRobotsContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        return ((List<?>) inputElement).toArray();
    }
}
