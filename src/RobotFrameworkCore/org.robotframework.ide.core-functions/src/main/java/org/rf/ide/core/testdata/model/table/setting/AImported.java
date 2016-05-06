/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public abstract class AImported extends AModelElement<SettingTable> {

    private final Type type;

    private final RobotToken declaration;

    private RobotToken pathOrName;

    private final List<RobotToken> comment = new ArrayList<>();

    protected AImported(final Type type, final RobotToken declaration) {
        this.type = type;
        this.declaration = declaration;
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public RobotToken getPathOrName() {
        return pathOrName;
    }

    public void setPathOrName(final RobotToken pathOrName) {
        fixForTheType(pathOrName, type.getPathOrFileNameType());
        this.pathOrName = pathOrName;
    }

    public static enum Type {
        LIBRARY {

            @Override
            public IRobotTokenType getPathOrFileNameType() {
                return RobotTokenType.SETTING_LIBRARY_NAME;
            }
        },
        RESOURCE {

            @Override
            public IRobotTokenType getPathOrFileNameType() {
                return RobotTokenType.SETTING_RESOURCE_FILE_NAME;
            }
        },
        VARIABLES {

            @Override
            public IRobotTokenType getPathOrFileNameType() {
                return RobotTokenType.SETTING_VARIABLES_FILE_NAME;
            }
        };

        public abstract IRobotTokenType getPathOrFileNameType();
    }

    public Type getType() {
        return type;
    }

    @Override
    public ModelType getModelType() {
        ModelType modelType = ModelType.UNKNOWN;

        if (type == Type.LIBRARY) {
            modelType = ModelType.LIBRARY_IMPORT_SETTING;
        } else if (type == Type.RESOURCE) {
            modelType = ModelType.RESOURCE_IMPORT_SETTING;
        } else if (type == Type.VARIABLES) {
            modelType = ModelType.VARIABLES_IMPORT_SETTING;
        }

        return modelType;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }
}