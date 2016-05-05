/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.viewers.IStructuredSelection;


class WizardNewRobotSuiteFileCreationPage extends WizardNewRobotResourceFileCreationPage {

    WizardNewRobotSuiteFileCreationPage(final String pageName, final IStructuredSelection selection,
            final String firstExtension, final String... restExtensions) {
        super(pageName, selection, firstExtension, restExtensions);
    }

    @Override
    protected InputStream getInitialContents() {
        return new ByteArrayInputStream("*** Test Cases ***".getBytes(StandardCharsets.UTF_8));
    }
}
