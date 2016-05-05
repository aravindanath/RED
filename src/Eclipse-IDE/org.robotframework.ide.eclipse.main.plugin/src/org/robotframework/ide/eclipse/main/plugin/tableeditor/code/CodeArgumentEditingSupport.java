/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ActivationCharPreservingTextCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.RowExposingTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport;

class CodeArgumentEditingSupport extends RobotElementEditingSupport {

    CodeArgumentEditingSupport(final RowExposingTreeViewer viewer, final int index,
            final RobotEditorCommandsStack commandsStack, final NewElementsCreator<RobotElement> creator) {
        super(viewer, index, commandsStack, creator);
    }

    @Override
    protected int getColumnShift() {
        return 1;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Composite parent = (Composite) getViewer().getControl();
        if (element instanceof RobotElement) {
            return new ActivationCharPreservingTextCellEditor(getViewer().getColumnViewerEditor(), parent,
                    DETAILS_EDITING_CONTEXT_ID);
        }
        return super.getCellEditor(element);
    }

    @Override
    protected Object getValue(final Object element) {
        List<String> arguments = null;
        if (element instanceof RobotKeywordDefinition) {
            arguments = getKeywordDefinitionArguments((RobotKeywordDefinition) element);
        } else if (element instanceof RobotKeywordCall) {
            arguments = ((RobotKeywordCall) element).getArguments();
        }
        return arguments != null && index < arguments.size() ? arguments.get(index) : "";
    }

    @Override
    protected void setValue(final Object element, final Object value) {
        if (element instanceof RobotKeywordDefinition) {
            final String argument = (String) value;
            commandsStack.execute(new SetKeywordDefinitionArgumentCommand((RobotKeywordDefinition) element, index,
                    argument));
        } else if (element instanceof RobotKeywordCall) {
            final String argument = (String) value;
            commandsStack.execute(new SetKeywordCallArgumentCommand((RobotKeywordCall) element, index, argument));
        } else {
            super.setValue(element, value);
        }
    }

    private List<String> getKeywordDefinitionArguments(final RobotKeywordDefinition def) {
        if (def.hasArguments()) {
            final RobotDefinitionSetting argumentsSetting = def.getArgumentsSetting();
            return argumentsSetting.getArguments();
        }
        return newArrayList();
    }
}
