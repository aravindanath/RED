/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.variables;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;


public class ListVariableDeclarationRecognizer extends
        AVariablesTokenRecognizer {

    public ListVariableDeclarationRecognizer() {
        super(VariableType.LIST);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new ListVariableDeclarationRecognizer();
    }
}
