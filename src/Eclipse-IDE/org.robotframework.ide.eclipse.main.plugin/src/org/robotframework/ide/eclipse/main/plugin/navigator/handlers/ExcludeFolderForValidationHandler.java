/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import javax.inject.Named;

import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ExcludeFolderForValidationHandler.E4ExcludeFolderForValidationHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ExcludeFolderForValidationHandler extends DIParameterizedHandler<E4ExcludeFolderForValidationHandler> {

    public ExcludeFolderForValidationHandler() {
        super(E4ExcludeFolderForValidationHandler.class);
    }

    public static class E4ExcludeFolderForValidationHandler extends ChangeExclusionHandler {

        @Override
        @Execute
        public Object changeExclusion(final IEventBroker eventBroker,
                final @Named(Selections.SELECTION) IStructuredSelection selection) {
            return super.changeExclusion(eventBroker, selection);
        }

        @Override
        protected void changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            config.addExcludedPath(pathToChange);
        }
    }
}
