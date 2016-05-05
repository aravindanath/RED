/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class SettingsCommentsEditingSupport extends RobotElementEditingSupport {

    SettingsCommentsEditingSupport(final ColumnViewer column, final RobotEditorCommandsStack commandsStack,
            final NewElementsCreator<RobotElement> creator) {
        super(column, ((TableViewer) column).getTable().getColumnCount(), commandsStack, creator);
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
            return setting != null ? setting.getComment() : "";
        }
        return "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            final String comment = (String) value;

            commandsStack.execute(new SetKeywordCallCommentCommand(setting, comment));
        } else {
            super.setValue(element, value);
        }
    }
}
