/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.IElementComparer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.red.viewers.ElementAddingToken;


class VariableElementsComparer implements IElementComparer {

    @Override
    public boolean equals(final Object a, final Object b) { // NOPMD we have to implement this method
        if (a instanceof RobotElement && b instanceof RobotElement) {
            return getPositionInTable((RobotElement) a).equals(getPositionInTable((RobotElement) b));
        }
        return a == null && b == null || a instanceof ElementAddingToken && b instanceof ElementAddingToken;
    }

    @Override
    public int hashCode(final Object element) {
        if (element instanceof RobotElement) {
            return getPositionInTable((RobotElement) element).hashCode();
        } else if (element instanceof ElementAddingToken) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    private Integer index(final RobotElement element) {
        if (element.getParent() != null) {
            for (int i = 0; i < element.getParent().getChildren().size(); i++) {
                if (element.getParent().getChildren().get(i) == element) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Integer getPositionInTable(final RobotElement element) {
        if (element.getParent() instanceof RobotVariablesSection) {
            return index(element);
        } else {
            return Integer.MIN_VALUE;
        }
    }
}
