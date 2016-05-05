/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Function;

class TokenFunctions {

    static Function<DictionaryKeyValuePair, String> pairToString() {
        return new Function<DictionaryKeyValuePair, String>() {

            @Override
            public String apply(final DictionaryKeyValuePair pair) {
                return pair.getKey().getText().toString() + "=" + pair.getValue().getText().toString();
            }
        };
    }

    static Function<RobotToken, String> tokenToString() {
        return new Function<RobotToken, String>() {

            @Override
            public String apply(final RobotToken token) {
                return token.getText().toString();
            }
        };
    }
}
