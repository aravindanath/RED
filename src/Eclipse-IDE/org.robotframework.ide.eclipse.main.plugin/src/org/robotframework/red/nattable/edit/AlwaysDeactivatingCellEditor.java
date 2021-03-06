/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IEditErrorHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.red.nattable.NewElementsCreator;

/**
 * @author Michal Anglart
 *
 */
public class AlwaysDeactivatingCellEditor extends AbstractCellEditor {

    private final NewElementsCreator<?> creator;

    private Canvas canvas;

    public <T> AlwaysDeactivatingCellEditor(final NewElementsCreator<T> creator) {
        this.creator = creator;
    }

    @Override
    public boolean supportMultiEdit(final IConfigRegistry configRegistry, final List<String> configLabels) {
        return false;
    }

    @Override
    public Object getCanonicalValue(final IEditErrorHandler conversionErrorHandler) {
        return getEditorValue();
    }

    @Override
    public Object getEditorValue() {
        return creator.createNew();
    }

    @Override
    public void setEditorValue(final Object value) {
        // no editor, so nothing to do
    }

    @Override
    public Control createEditorControl(final Composite parent) {
        return new Canvas(parent, SWT.NONE);
    }

    @Override
    public Control getEditorControl() {
        return canvas;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        this.canvas = (Canvas) createEditorControl(parent);
        commit(MoveDirectionEnum.NONE);
        return canvas;
    }
}
