/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Collection;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

public class HeaderFilterMatchesCollection {

    private final Multimap<String, Range<Integer>> matches = ArrayListMultimap.create();
    private int allMatches = 0;
    protected int rowsMatching = 0;

    @SuppressWarnings("unused")
    public void collect(final RobotElement element, final String filter) {
        // nothing to collect here, override this method
    }

    public void collect(final List<RobotElement> elements, final String filter) {
        for (final RobotElement element : elements) {
            collect(element, filter);
        }
    }
    
    public Collection<Range<Integer>> getRanges(final String label) {
        return matches.get(label);
    }

    public boolean contains(final String label) {
        return matches.containsKey(label);
    }

    public void addAll(final HeaderFilterMatchesCollection from) {
        if (from != null) {
            matches.putAll(from.matches);
            allMatches += from.allMatches;
            rowsMatching += from.rowsMatching;
        }
    }

    public int getNumberOfAllMatches() {
        return allMatches;
    }

    public int getNumberOfMatchingElement() {
        return rowsMatching;
    }

    protected final boolean collectMatches(final String filter, final String label) {
        int index = label.indexOf(filter);
        final boolean result = index >= 0;
        while (index >= 0) {
            matches.put(label, Range.closedOpen(index, index + filter.length()));
            allMatches++;
            index = label.indexOf(filter, index + 1);
        }
        return result;
    }
}