/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class VariablesImport extends AImported {

    private final List<RobotToken> arguments = new ArrayList<>();


    public VariablesImport(final RobotToken variablesDeclaration) {
        super(Type.VARIABLES, variablesDeclaration);
    }


    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }


    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
    }


    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }


    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            final RobotToken pathOrName = getPathOrName();
            if (pathOrName != null) {
                tokens.add(pathOrName);
            }
            tokens.addAll(getArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
