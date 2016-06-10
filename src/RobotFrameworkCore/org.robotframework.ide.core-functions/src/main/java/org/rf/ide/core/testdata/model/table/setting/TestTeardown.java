/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestTeardown extends AKeywordBaseSetting<SettingTable> {

    private static final long serialVersionUID = 1L;

    public TestTeardown(final RobotToken declaration) {
        super(declaration);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.SUITE_TEST_TEARDOWN;
    }

    @Override
    protected List<AKeywordBaseSetting<SettingTable>> getAllThisKindSettings() {
        final List<AKeywordBaseSetting<SettingTable>> settings = new ArrayList<>(0);
        settings.addAll(getParent().getTestTeardowns());

        return settings;
    }

    @Override
    public IRobotTokenType getKeywordNameType() {
        return RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME;
    }

    @Override
    public IRobotTokenType getArgumentType() {
        return RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT;
    }
}
