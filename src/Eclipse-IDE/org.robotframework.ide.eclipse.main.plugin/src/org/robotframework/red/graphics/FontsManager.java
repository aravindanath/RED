/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.graphics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontsManager {
    private static final Map<FontDescriptor, Font> FONTS_TABLE = new HashMap<FontDescriptor, Font>(10);

    private FontsManager() {
        // nothing to do
    }

    public static int size() {
        return FONTS_TABLE.size();
    }

    public static Font getFont(final FontDescriptor fontDescriptor) {
        return getFont(Display.getCurrent(), fontDescriptor);
    }

    public static Font getFont(final Display display, final FontDescriptor fontDescriptor) {
        Font font = FONTS_TABLE.get(fontDescriptor);
        if (font == null) {
            font = fontDescriptor.createFont(display);
            FONTS_TABLE.put(fontDescriptor, font);
        }
        return font;
    }

    public static Font getFont(final FontData fontData) {
        return getFont(Display.getCurrent(), fontData);
    }

    public static Font getFont(final Display display, final FontData fontData) {
        return getFont(display, FontDescriptor.createFrom(fontData));
    }

    /**
     * Dispose fonts manager.
     */
    public static void disposeFonts() {
        for (final Font font : FONTS_TABLE.values()) {
            font.dispose();
        }
        FONTS_TABLE.clear();
    }
}
