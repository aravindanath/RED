/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteSettingKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.DeleteSettingsHandler.E4DeleteSettingsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteSettingsHandler extends DIParameterizedHandler<E4DeleteSettingsHandler> {

    public DeleteSettingsHandler() {
        super(E4DeleteSettingsHandler.class);
    }

    public static class E4DeleteSettingsHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object deleteSettings(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotSetting> settings = Selections.getElements(selection, RobotSetting.class);
            commandsStack.execute(new DeleteSettingKeywordCallCommand(settings));

            return null;
        }
    }
}
