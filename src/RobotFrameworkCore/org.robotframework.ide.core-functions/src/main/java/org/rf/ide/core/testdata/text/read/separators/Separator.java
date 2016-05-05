/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;

public class Separator implements IRobotLineElement {

    private FilePosition fp = new FilePosition(NOT_SET, NOT_SET, NOT_SET);

    private String raw = "";

    private String text = "";

    private SeparatorType type = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;

    private boolean isDirty = false;

    private boolean wasFirstInit = false;

    public static enum SeparatorType implements IRobotTokenType {
        /**
         * 
         */
        TABULATOR_OR_DOUBLE_SPACE(
                VersionAvailabilityInfoBuilder.create().addRepresentation("\t").build(),
                VersionAvailabilityInfoBuilder.create().addRepresentation("  ").build()),
        /**
         *
         */
        PIPE(
                VersionAvailabilityInfoBuilder.create().addRepresentation("| ").build(),
                VersionAvailabilityInfoBuilder.create().addRepresentation(" | ").build(),
                VersionAvailabilityInfoBuilder.create().addRepresentation("\t|").build(),
                VersionAvailabilityInfoBuilder.create().addRepresentation("|\t").build(),
                VersionAvailabilityInfoBuilder.create().addRepresentation("\t|\t").build());

        private final List<String> text = new ArrayList<>(0);

        private final List<VersionAvailabilityInfo> representation = new ArrayList<>(0);

        @Override
        public List<String> getRepresentation() {
            return text;
        }

        private SeparatorType(final VersionAvailabilityInfo... representations) {
            for (VersionAvailabilityInfo vInfo : representations) {
                representation.add(vInfo);
                text.add(vInfo.getRepresentation());
            }
        }

        @Override
        public List<VersionAvailabilityInfo> getVersionAvailabilityInfos() {
            return representation;
        }

        @Override
        public VersionAvailabilityInfo findVersionAvailablilityInfo(String text) {
            VersionAvailabilityInfo vaiResult = null;
            for (VersionAvailabilityInfo vInfo : representation) {
                if (vInfo.getRepresentation().equals(text)) {
                    vaiResult = vInfo;
                    break;
                }
            }
            return vaiResult;
        }
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
        if (!Objects.equals(this.text, text)) {
            if (wasFirstInit) {
                isDirty = true;
            }
        }
        wasFirstInit = true;
        this.text = text.intern();
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
        final List<IRobotTokenType> s = new ArrayList<>();
        s.add(type);
        return s;
    }

    public void setType(final SeparatorType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Separator [filePos=%s, text=%s, type=%s]", fp, text, type);
    }

    @Override
    public FilePosition getFilePosition() {
        return fp;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public VersionAvailabilityInfo getVersionInformation() {
        VersionAvailabilityInfo vai = null;
        if (type != null) {
            type.getVersionAvailabilityInfos();
        }
        return vai;
    }
}
