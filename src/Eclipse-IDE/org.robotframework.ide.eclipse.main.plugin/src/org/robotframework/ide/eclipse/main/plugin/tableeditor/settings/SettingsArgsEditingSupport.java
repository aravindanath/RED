/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class SettingsArgsEditingSupport extends RobotElementEditingSupport {

    SettingsArgsEditingSupport(final ColumnViewer viewer, final int index, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator<RobotElement> creator) {
        super(viewer, index, commandsStack, creator);
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        if (element instanceof RobotSetting) {
            return new TextCellEditor(((TableViewer) getViewer()).getTable());
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> arguments = setting.getArguments();
            if (index < arguments.size()) {
                return arguments.get(index);
            }
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final String arg = (String) value;

            commandsStack.execute(new SetKeywordCallArgumentCommand(setting, index, arg));
        } else {
            super.setValue(element, value);
        }
    }
}
