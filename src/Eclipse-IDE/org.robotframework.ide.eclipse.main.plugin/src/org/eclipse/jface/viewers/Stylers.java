/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.FontsManager;

public class Stylers {

    public static class Common {

        public static final Styler EMPTY_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                // no styles to apply
            }
        };

        public static final Styler ECLIPSE_DECORATION_STYLER = new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();

            }
        };

        public static final Styler ECLIPSE_SEARCH_MATCH_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.background = RedTheme.getEclipseSearchMatchColor();
            };
        };

        public static final Styler STRIKEOUT_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.strikeout = true;
            }
        };

        public static final Styler ERROR_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = ColorsManager.getColor(255, 0, 0);
            }
        };

        public static final Styler WARNING_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = ColorsManager.getColor(255, 165, 0);
            }
        };

        public static final Styler MARKED_PREFIX_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle style) {
                style.foreground = ColorsManager.getColor(135, 150, 255);
                style.background = ColorsManager.getColor(230, 240, 255);
                style.borderColor = ColorsManager.getColor(135, 150, 255);
                style.borderStyle = SWT.BORDER_DOT;
            }
        };

        public static final Styler MARKED_STRIKEOUT_PREFIX_STYLER = new Styler() {

            @Override
            public void applyStyles(final TextStyle style) {
                style.foreground = ColorsManager.getColor(135, 150, 255);
                style.background = ColorsManager.getColor(230, 240, 255);
                style.borderColor = ColorsManager.getColor(135, 150, 255);
                style.strikeout = true;
                style.borderStyle = SWT.BORDER_DOT;
            }
        };

        public static final Styler MATCH_STYLER = new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.background = ColorsManager.getColor(255, 255, 175);
                textStyle.borderStyle = SWT.BORDER_DOT;
            }
        };
    }

    public static Styler mixingStyler(final Styler... stylers) {
        return new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                for (final Styler styler : stylers) {
                    styler.applyStyles(textStyle);
                }
            }
        };
    }

    public static Styler withForeground(final int red, final int green, final int blue) {
        return new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = ColorsManager.getColor(red, green, blue);
            }
        };
    }

    public static Styler withForeground(final RGB rgb) {
        return new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = ColorsManager.getColor(rgb);
            }
        };
    }

    public static Styler withForeground(final Color color) {
        return new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = color;
            }
        };
    }

    public static Styler withFontStyle(final int style) {
        return new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                final Font currentFont = textStyle.font == null ? Display.getCurrent().getSystemFont() : textStyle.font;
                FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont);
                fontDescriptor = fontDescriptor.setStyle(style);

                textStyle.font = FontsManager.getFont(fontDescriptor);
            }
        };
    }
}
