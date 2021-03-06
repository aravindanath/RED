/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class UnknownVariable extends AVariable {

    private static final long serialVersionUID = -5596982418926479458L;

    private final List<RobotToken> items = new ArrayList<>();

    public UnknownVariable(final String name, final RobotToken declaration, final VariableScope scope) {
        super(VariableType.INVALID, name, declaration, scope);
    }

    public void addItem(final RobotToken item) {
        items.add(item);
    }

    public void addItem(final RobotToken item, final int position) {
        items.set(position, item);
    }

    public void removeItem(final RobotToken item) {
        items.remove(item);
    }

    public boolean moveLeftItem(final RobotToken item) {
        return MoveElementHelper.moveLeft(items, item);
    }

    public boolean moveRightItem(final RobotToken item) {
        return MoveElementHelper.moveRight(items, item);
    }

    public List<RobotToken> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            if (getDeclaration() != null) {
                tokens.add(getDeclaration());
            }

            tokens.addAll(getItems());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public UnknownVariable copy() {
        final RobotToken dec = RobotToken.create(getName(), getDeclaration().getTypes());

        final UnknownVariable unknown = new UnknownVariable(getName(), dec, getScope());
        for (final RobotToken valueToken : getItems()) {
            final RobotToken token = RobotToken.create(valueToken.getText(), valueToken.getTypes());
            unknown.addItem(token);
        }
        for (final RobotToken commentToken : getComment()) {
            final RobotToken token = RobotToken.create(commentToken.getText(), commentToken.getTypes());
            unknown.addCommentPart(token);
        }
        return unknown;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(items, index);
    }
}
