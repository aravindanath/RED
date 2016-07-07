/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;

public class RobotToken implements IRobotLineElement, Serializable {

    private static final long serialVersionUID = -7333635148571215189L;

    private FilePosition fp = new FilePosition(NOT_SET, NOT_SET, NOT_SET);

    private String raw = "";

    private String text = "";

    private final List<IRobotTokenType> types = new ArrayList<>(0);

    private boolean isDirty = false;

    private boolean wasFirstInit = false;

    public static RobotToken create(final String rawAndText) {
        return create(rawAndText, new ArrayList<IRobotTokenType>());
    }

    public static RobotToken create(final String rawAndText, final Collection<? extends IRobotTokenType> types) {
        final RobotToken token = new RobotToken();
        token.setRaw(rawAndText);
        token.setText(rawAndText);
        if (!types.isEmpty()) {
            token.getTypes().clear();
        }
        token.getTypes().addAll(types);
        return token;
    }

    public RobotToken() {
        types.add(RobotTokenType.UNKNOWN);
    }

    @Override
    public int getLineNumber() {
        return fp.getLine();
    }

    public void setLineNumber(final int lineNumber) {
        fp = new FilePosition(lineNumber, fp.getColumn(), fp.getOffset());
    }

    @Override
    public int getStartColumn() {
        return fp.getColumn();
    }

    public void setStartColumn(final int startColumn) {
        fp = new FilePosition(fp.getLine(), startColumn, fp.getOffset());
    }

    @Override
    public int getEndColumn() {
        int endColumn = NOT_SET;

        if (fp.getColumn() != NOT_SET) {
            endColumn = fp.getColumn() + text.length();
        }

        return endColumn;
    }

    public void setStartOffset(final int startOffset) {
        fp = new FilePosition(fp.getLine(), fp.getColumn(), startOffset);
    }

    @Override
    public int getStartOffset() {
        return fp.getOffset();
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(final String text) {
        if (wasFirstInit) {
            if (!Objects.equals(this.text, text)) {
                isDirty = true;
            }
        }
        wasFirstInit = true;
        this.text = (text != null) ? text.intern() : null;
    }

    public void clearDirtyFlag() {
        isDirty = false;
    }

    public boolean isNotEmpty() {
        return (this.getRaw() != null && !this.getRaw().isEmpty())
                || (this.getText() != null && !this.getText().isEmpty());
    }

    @Override
    public String getRaw() {
        return raw;
    }

    public void setRaw(final String raw) {
        this.raw = raw.intern();
    }

    @Override
    public List<IRobotTokenType> getTypes() {
        return types;
    }

    public void setType(final IRobotTokenType type) {
        types.clear();
        types.add(type);
    }

    @Override
    public String toString() {
        return String.format("RobotToken [filePosition=%s, text=%s, types=%s, isDirty=%s]", fp, text, types, isDirty);
    }

    @Override
    public FilePosition getFilePosition() {
        return fp;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    public boolean isVariableDeclaration() {
        boolean result = false;
        for (final IRobotTokenType type : types) {
            if (type instanceof RobotTokenType) {
                final RobotTokenType robotType = (RobotTokenType) type;
                result = (robotType == RobotTokenType.VARIABLES_SCALAR_DECLARATION)
                        || (robotType == RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION)
                        || (robotType == RobotTokenType.VARIABLES_LIST_DECLARATION)
                        || (robotType == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);

                if (result) {
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public VersionAvailabilityInfo getVersionInformation() {
        VersionAvailabilityInfo vai = null;
        if (types != null && !types.isEmpty()) {
            vai = types.get(0).findVersionAvailablilityInfo(getRaw());
        }
        return vai;
    }
}
