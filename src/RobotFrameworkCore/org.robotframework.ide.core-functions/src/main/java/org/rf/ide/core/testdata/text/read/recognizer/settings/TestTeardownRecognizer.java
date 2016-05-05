/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestTeardownRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?(("
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Teardown") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Teardown") + ")|("
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Postcondition") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Test") + "[\\s]+"
            + createUpperLowerCaseWord("Postcondition") + "))");


    public TestTeardownRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new TestTeardownRecognizer();
    }
}
