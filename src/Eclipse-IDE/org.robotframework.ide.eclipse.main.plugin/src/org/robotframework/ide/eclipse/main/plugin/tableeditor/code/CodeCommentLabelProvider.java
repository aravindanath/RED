/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.eclipse.jface.viewers.Stylers.mixingStyler;
import static org.eclipse.jface.viewers.Stylers.withFontStyle;
import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Supplier;

class CodeCommentLabelProvider extends MatchesHighlightingLabelProvider {

    public CodeCommentLabelProvider(final Supplier<HeaderFilterMatchesCollection> matchesProvider) {
        super(matchesProvider);
    }

    @Override
    public final StyledString getStyledText(final Object element) {
        final String comment = getComment(element);
        final String prefix = "# ";
        final int matchesHighlightsShift = prefix.length();

        StyledString label = null;
        if (!comment.isEmpty()) {
            final Styler commentStyler = mixingStyler(
                    withForeground(RedTheme.getCommentsColor()),
                    withFontStyle(SWT.ITALIC));
            label = new StyledString(prefix + comment, commentStyler);
        }
        return highlightMatches(label, matchesHighlightsShift, comment);
    }

    private String getComment(final Object element) {
        if (element instanceof RobotFileInternalElement) {
            return ((RobotFileInternalElement) element).getComment();
        }
        return "";
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotElement) {
            return "# " + getComment(element);
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotElement) {
            return ImagesManager.getImage(RedImages.getTooltipImage());
        }
        return null;
    }
}