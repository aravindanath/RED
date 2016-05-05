/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.ColumnViewer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

public class ImportSettingsEditingSupport extends RobotElementEditingSupport {

    public ImportSettingsEditingSupport(final ColumnViewer viewer, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator<RobotElement> creator) {
        super(viewer, 0, commandsStack, creator);
    }

    @Override
    protected Object getValue(final Object element) {
        return "";
    }

}
