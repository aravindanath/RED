/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.navigator.NavigatorLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

class RobotOutlinePage extends ContentOutlinePage {

    private final RobotFormEditor editor;

    private final RobotSuiteFile suiteModel;

    private ISelectionChangedListener selectionListener;

    AtomicBoolean shouldUpdateEditorSelection = new AtomicBoolean(true);

    private Job modelQueryJob;

    public RobotOutlinePage(final RobotFormEditor editor, final RobotSuiteFile suiteModel) {
        this.editor = editor;
        this.suiteModel = suiteModel;
    }

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);

        getTreeViewer().setContentProvider(new RobotOutlineContentProvider());
        ViewerColumnsFactory.newColumn("")
            .withWidth(400)
            .labelsProvidedBy(new NavigatorLabelProvider())
            .createFor(getTreeViewer());

        getTreeViewer().setInput(new Object[] { suiteModel });
        getTreeViewer().expandToLevel(3);

        selectionListener = createSelectionListener();
        getTreeViewer().addSelectionChangedListener(selectionListener);

        editor.getSourceEditor().getViewer().getTextWidget().addCaretListener(createCaretListener());
    }

    private CaretListener createCaretListener() {
        return new CaretListener() {
            @Override
            public void caretMoved(final CaretEvent event) {
                if (modelQueryJob != null && modelQueryJob.getState() == Job.SLEEPING) {
                    modelQueryJob.cancel();
                }
                modelQueryJob = createModelQueryJob(event.display, event.caretOffset);
                modelQueryJob.schedule(300);
            }
        };
    }

    private Job createModelQueryJob(final Display display, final int caretOffset) {
        final Job job = new Job("Looking for model element") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final Optional<? extends RobotElement> element = suiteModel.findElement(caretOffset);
                if (element.isPresent() && !display.isDisposed()) {
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            shouldUpdateEditorSelection.set(false);
                            final ISelection selection = new StructuredSelection(new Object[] { element.get() });
                            getTreeViewer().setSelection(selection);
                        }
                    });
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        return job;
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                if (!shouldUpdateEditorSelection.getAndSet(true)) {
                    return;
                }
                final Optional<RobotFileInternalElement> element = Selections.getOptionalFirstElement(
                        (IStructuredSelection) event.getSelection(), RobotFileInternalElement.class);
                if (!element.isPresent()) {
                    return;
                }
                final RobotFileInternalElement robotElement = element.get();
                if (editor.getActiveEditor() instanceof SuiteSourceEditor) {
                    final ISelectionProvider selectionProvider = editor.getActiveEditor()
                            .getSite()
                            .getSelectionProvider();
                    final DefinitionPosition position = robotElement.getDefinitionPosition();
                    selectionProvider.setSelection(new TextSelection(position.getOffset(), position.getLength()));
                } else {
                    final ISectionEditorPart activatedPage = editor.activatePage(getSection(element.get()));
                    if (activatedPage != null) {
                        activatedPage.setFocus();
                        activatedPage.revealElement(element.get());
                    }
                }
            }

            private RobotSuiteFileSection getSection(final RobotFileInternalElement element) {
                RobotElement current = element;
                while (current != null && !(current instanceof RobotSuiteFileSection)) {
                    current = current.getParent();
                }
                return (RobotSuiteFileSection) current;
            }
        };
    }

    @Override
    public void dispose() {
        getTreeViewer().removeSelectionChangedListener(selectionListener);

        super.dispose();
    }
}
