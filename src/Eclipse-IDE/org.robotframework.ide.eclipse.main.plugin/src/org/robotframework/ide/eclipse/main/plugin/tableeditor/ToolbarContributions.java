/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

class ToolbarContributions {

    static CommandContributionItem createDeleteSectionContributionItem(final IServiceLocator serviceLocator,
            final String sectionName) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, null, "org.robotframework.ide.eclipse.deleteSection", SWT.PUSH);
        contributionParameters.icon = RedImages.getRobotCasesFileDeleteSectionImage();
        contributionParameters.tooltip = "Delete " + sectionName + " section";
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("org.robotframework.ide.eclipse.deleteSection.sectionName", sectionName);
        contributionParameters.parameters = params;

        return new CommandContributionItem(contributionParameters);
    }
}
