/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;

class KeywordProposalsLabelProvider extends RedCommonLabelProvider {

    @Override
    public Image getImage(final Object element) {
        return ImagesManager.getImage(((KeywordContentProposal) element).getImage());
    }

    @Override
    public String getText(final Object element) {
        return ((KeywordContentProposal) element).getLabel();
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(((KeywordContentProposal) element).getLabel());
    }
}
