/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

public abstract class ATokenRecognizer {

    private final Pattern pattern;

    private Matcher m;

    private int lineNumber = -1;

    private final RobotTokenType type;

    private String text;

    protected ATokenRecognizer(final Pattern p, final RobotTokenType type) {
        this.pattern = p;
        this.type = type;
    }

    public abstract ATokenRecognizer newInstance();

    @VisibleForTesting
    public boolean hasNext(final StringBuilder newText, final int currentLineNumber) {
        return hasNext(newText.toString(), currentLineNumber);
    }

    public boolean hasNext(final String newText, final int currentLineNumber) {
        if (m == null || lineNumber != currentLineNumber || !text.equals(newText)) {
            m = pattern.matcher(newText);
            this.text = newText;
            this.lineNumber = currentLineNumber;
        }

        return m.find();
    }

    public RobotToken next() {
        final RobotToken t = new RobotToken();
        t.setLineNumber(lineNumber);
        final int start = m.start();
        t.setStartColumn(start);
        final int end = m.end();

        t.setText(text.substring(start, end));
        t.setRaw(t.getText());
        t.setType(getProducedType());
        return t;
    }

    public RobotTokenType getProducedType() {
        return type;
    }

    public static String createUpperLowerCaseWordWithSpacesInside(final String text) {
        final StringBuilder str = new StringBuilder();
        if (text != null && text.length() > 0) {

            final char[] ca = text.toCharArray();
            final int size = ca.length;
            for (int i = 0; i < size; i++) {
                str.append('[');
                final char c = ca[i];
                if (Character.isLetter(c)) {
                    str.append(Character.toUpperCase(c)).append('|').append(Character.toLowerCase(c));
                } else {
                    str.append(c);
                }

                str.append(']');

                if (i + 1 < size) {
                    str.append("([\\s]+)?");
                }
            }
        }

        return str.toString();
    }

    public static String createUpperLowerCaseWord(final String text) {
        final StringBuilder str = new StringBuilder();
        if (text != null && text.length() > 0) {

            final char[] ca = text.toCharArray();
            final int size = ca.length;
            for (int i = 0; i < size; i++) {
                str.append('[');
                final char c = ca[i];
                if (Character.isLetter(c)) {
                    str.append(Character.toUpperCase(c)).append('|').append(Character.toLowerCase(c));
                } else {
                    str.append(c);
                }

                str.append(']');
            }
        }

        return str.toString();
    }

    public Pattern getPattern() {
        return this.pattern;
    }
}
