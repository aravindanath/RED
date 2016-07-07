/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public abstract class AKeywordBaseSetting<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private final RobotToken declaration;

    private RobotToken keywordName;

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    protected AKeywordBaseSetting(final RobotToken declaration) {
        this.declaration = declaration;
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public RobotToken getKeywordName() {
        return keywordName;
    }

    public void setKeywordName(final String keywordName) {
        this.keywordName = updateOrCreate(this.keywordName, keywordName, getKeywordNameType());
    }

    public void setKeywordName(final RobotToken keywordName) {
        this.keywordName = updateOrCreate(this.keywordName, keywordName, getKeywordNameType());
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void addArgument(final String argument) {
        RobotToken rt = new RobotToken();
        rt.setText(argument);

        addArgument(rt);
    }

    public void addArgument(final RobotToken argument) {
        fixForTheType(argument, getArgumentType(), true);
        arguments.add(argument);
    }

    public void setArgument(final int index, final String argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());
    }

    public void setArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    @Override
    public void setComment(String comment) {
        RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(int index) {
        this.comment.remove(index);
    }

    @Override
    public void clearComment() {
        this.comment.clear();
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            if (getKeywordName() != null) {
                tokens.add(getKeywordName());
            }
            tokens.addAll(getArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    protected abstract List<AKeywordBaseSetting<T>> getAllThisKindSettings();

    public RobotExecutableRow<T> asExecutableRow() {
        final RobotExecutableRow<T> execRow = new RobotExecutableRow<>();
        execRow.setParent(getParent());

        boolean wasAction = false;
        final List<AKeywordBaseSetting<T>> allThisKindSettings = getAllThisKindSettings();
        for (final AKeywordBaseSetting<T> baseSetting : allThisKindSettings) {
            if (baseSetting.getKeywordName() != null && !baseSetting.getKeywordName().getFilePosition().isNotSet()) {
                if (!wasAction) {
                    execRow.setAction(baseSetting.getKeywordName());
                    wasAction = true;
                } else {
                    execRow.addArgument(baseSetting.getKeywordName());
                }
            }
            for (final RobotToken arg : baseSetting.getArguments()) {
                execRow.addArgument(arg);
            }
            for (final RobotToken c : baseSetting.getComment()) {
                execRow.addCommentPart(c);
            }
        }

        return execRow;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(arguments, index);
    }

    public abstract IRobotTokenType getKeywordNameType();

    public abstract IRobotTokenType getArgumentType();
}
