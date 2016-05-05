/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugValueManager;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

/**
 * @author mmarzec
 *
 */
public class RobotModelPresentation extends LabelProvider implements IDebugModelPresentation {

    @Override
    public void setAttribute(final String attribute, final Object value) {
    }

    @Override
    public Image getImage(final Object element) {
        return null;
    }

    @Override
    public String getText(final Object element) {
        try {
            if (element instanceof IThread) {
                return ((IThread) element).getName();
            } else if (element instanceof IDebugTarget) {
                return ((IDebugTarget) element).getName();
            } else if (element instanceof IStackFrame) {
                return ((IStackFrame) element).getName();
            } else if (element instanceof RobotLineBreakpoint) {
                final IMarker breakpointMarker = ((RobotLineBreakpoint) element).getMarker();
                String breakpointName = "";
                breakpointName += breakpointMarker.getAttribute(IMarker.LOCATION, "");
                breakpointName += " [line: " + breakpointMarker.getAttribute(IMarker.LINE_NUMBER) + "]";
                final int hitCount = breakpointMarker.getAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                if (hitCount > 1) {
                    breakpointName += " [hit count: " + hitCount + "]";
                }
                final String condition = breakpointMarker.getAttribute(RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                if (!"".equals(condition)) {
                    breakpointName += " [conditional]";
                }
                return breakpointName;
            }
        } catch (final CoreException e) {
            e.printStackTrace();
        }

        return "RED";
    }

    @Override
    public void computeDetail(final IValue value, final IValueDetailListener listener) {
        listener.detailComputed(value, RobotDebugValueManager.extractValueDetail(value));
    }
    
    @Override
    public IEditorInput getEditorInput(final Object element) {
        if (element instanceof IFile) {
            return new FileEditorInput((IFile) element);
        }
        if (element instanceof ILineBreakpoint) {
            return new FileEditorInput((IFile) ((ILineBreakpoint) element).getMarker().getResource());
        }
        return null;
    }

    @Override
    public String getEditorId(final IEditorInput input, final Object element) {
        if (element instanceof IFile || element instanceof ILineBreakpoint) {
            return RobotFormEditor.ID;
        }
        return null;
    }
}
