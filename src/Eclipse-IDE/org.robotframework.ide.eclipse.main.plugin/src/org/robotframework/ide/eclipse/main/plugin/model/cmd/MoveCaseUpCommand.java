/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseUpCommand extends EditorCommand {

    private final RobotCase testCase;

    public MoveCaseUpCommand(final RobotCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement section = testCase.getParent();
        final int index = section.getChildren().indexOf(testCase);
        if (index == 0) {
            return;
        }
        Collections.swap(section.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, section);
    }

}
