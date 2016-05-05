/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

public class TextPosition {

    private final String text;
    private final int startPosition;
    private final int endPosition;


    public TextPosition(final String text, final int startPosition,
            final int endPosition) {
        this.text = text;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }


    public String getFullText() {
        return text;
    }


    public String getText() {
        String myText = null;
        if (startPosition >= 0) {
            if (endPosition >= 0) {
                myText = text.substring(startPosition, endPosition + 1);
            } else {
                myText = text.substring(startPosition);
            }
        }

        return myText;
    }


    public int getStart() {
        return startPosition;
    }


    public int getEnd() {
        return endPosition;
    }


    public int getLength() {
        int length = 0;
        if (startPosition >= 0 && endPosition >= 0) {
            length = (endPosition - startPosition) + 1;
        }

        return length;
    }


    @Override
    public String toString() {
        return String.format(
                "TextPosition [wholeText=%s, start=%s, end=%s, %s", text,
                startPosition, endPosition, getPartOfText() + "]");
    }


    private String getPartOfText() {
        String info = "";
        int textLength = endPosition - startPosition;
        if (textLength < 3) {
            int fromTheBegin = startPosition - 1;
            int fromTheEnd = textLength - endPosition;
            if (fromTheBegin < fromTheEnd) {
                info = "beforeText=" + text.substring(0, startPosition)
                        + ", text="
                        + text.substring(startPosition, endPosition + 1);
            } else {
                info = "text=" + text.substring(startPosition, endPosition + 1)
                        + ", afterText=" + text.substring(endPosition + 1);
            }
        } else {
            info = "text=" + text.substring(startPosition, endPosition + 1);
        }

        return info;
    }
}
