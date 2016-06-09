/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTags;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseTagsMapper extends ATestCaseSettingDeclarationMapper {

    public TestCaseTagsMapper() {
        super(RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        rt.setText(text);
        rt.setRaw(text);

        final TestCase testCase = finder.findOrCreateNearestTestCase(currentLine, processingState, robotFileOutput, rt,
                fp);
        final TestCaseTags tags = new TestCaseTags(rt);
        testCase.addTag(tags);

        processingState.push(ParsingState.TEST_CASE_SETTING_TAGS);

        return rt;
    }
}