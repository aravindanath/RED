/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class Selections {

    public static final String SELECTION = "selection";

    public static <T> List<T> getElements(final IStructuredSelection selection, final Class<T> elementsClass) {
        return newArrayList(Iterables.filter(selection.toList(), elementsClass));
    }

    public static <T> T getSingleElement(final IStructuredSelection selection, final Class<T> elementsClass) {
        final List<T> elements = getElements(selection, elementsClass);
        if (elements.size() == 1) {
            return elements.get(0);
        }
        throw new IllegalArgumentException("Given selection should contain only one element of class "
                + elementsClass.getName() + ", but have " + elements.size() + " instead");
    }

    public static <T> Optional<T> getOptionalFirstElement(final IStructuredSelection selection,
            final Class<T> elementsClass) {
        final List<T> elements = getElements(selection, elementsClass);
        if (!elements.isEmpty()) {
            return Optional.of(elements.get(0));
        }
        return Optional.absent();
    }

    public static TreePath getFirstElementPath(final ITreeSelection selection) {
        final Object firstElement = selection.getFirstElement();
        final TreePath[] paths = firstElement == null ? new TreePath[0] : selection.getPathsFor(firstElement);
        return paths.length == 0 ? TreePath.EMPTY : paths[0];
    }
}
