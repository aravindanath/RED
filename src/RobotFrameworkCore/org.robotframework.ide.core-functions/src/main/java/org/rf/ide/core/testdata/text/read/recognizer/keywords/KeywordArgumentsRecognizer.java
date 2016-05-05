/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.keywords;

import org.rf.ide.core.testdata.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class KeywordArgumentsRecognizer extends
        AExecutableElementSettingsRecognizer {

    public KeywordArgumentsRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new KeywordArgumentsRecognizer();
    }
}
