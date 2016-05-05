/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.viewers.Selections;

public class ShowLibraryDocumentationAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ShowLibraryDocumentationAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Show library documentation");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof LibrarySpecification);
    }

    @Override
    public void run() {
        final LibrarySpecification spec = Selections.getSingleElement(
                (IStructuredSelection) selectionProvider.getSelection(), LibrarySpecification.class);

        new LibraryDocumentationPopup(page.getActivePart().getSite().getShell(), spec).open();
    }
}
