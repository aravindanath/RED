/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Lists.newArrayList;

import org.rf.ide.core.testdata.model.presenter.update.VariableTableModelUpdater;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetScalarValueCommand extends EditorCommand {

    private final RobotVariable variable;

    private final String newValue;

    public SetScalarValueCommand(final RobotVariable variable, final String newValue) {
        this.variable = variable;
        this.newValue = newValue == null ? "" : newValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.SCALAR) {
            throw new CommandExecutionException("Invalid type of variable: " + variable.getType());
        }

        new VariableTableModelUpdater().addOrSet((AVariable) variable.getLinkedElement(), 0, newArrayList(newValue));

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
