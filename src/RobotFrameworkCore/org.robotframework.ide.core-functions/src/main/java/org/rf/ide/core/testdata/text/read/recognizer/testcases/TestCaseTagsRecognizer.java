/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.testcases;

import org.rf.ide.core.testdata.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseTagsRecognizer extends
        AExecutableElementSettingsRecognizer {

    public TestCaseTagsRecognizer() {
        super(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new TestCaseTagsRecognizer();
    }
}
