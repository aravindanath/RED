/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.txt.tables.settings.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class CreationOfSettingsSuiteDocumentationTest {

    private static final String PRETTY_NEW_DIR_LOCATION = "settings//suiteDoc//new//";

    @Test
    public void test_emptyFile_and_thanCreateSuiteDoc() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "EmptySuiteDocumentationDeclarationOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        settingTable.newSuiteDocumentation();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteDoc_andAddComments() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteDocumentationDeclarationWithCommentsOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteDoc.addCommentPart(cm1);
        suiteDoc.addCommentPart(cm2);
        suiteDoc.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteDoc_withText() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteDocumentationDeclarationWithTextOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        RobotToken text1 = new RobotToken();
        text1.setText("text1");
        RobotToken text2 = new RobotToken();
        text2.setText("text2");
        RobotToken text3 = new RobotToken();
        text3.setText("text3");
        suiteDoc.addDocumentationText(text1);
        suiteDoc.addDocumentationText(text2);
        suiteDoc.addDocumentationText(text3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }

    @Test
    public void test_emptyFile_createSuiteDoc_withTextAndComment() throws Exception {
        // prepare
        final String fileName = PRETTY_NEW_DIR_LOCATION + "SuiteDocumentationDeclarationWithTextAndCommentOnly.txt";
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeSettingTableSection();
        final SettingTable settingTable = modelFile.getSettingTable();
        final SuiteDocumentation suiteDoc = settingTable.newSuiteDocumentation();

        RobotToken text1 = new RobotToken();
        text1.setText("text1");
        RobotToken text2 = new RobotToken();
        text2.setText("text2");
        RobotToken text3 = new RobotToken();
        text3.setText("text3");
        suiteDoc.addDocumentationText(text1);
        suiteDoc.addDocumentationText(text2);
        suiteDoc.addDocumentationText(text3);

        RobotToken cm1 = new RobotToken();
        cm1.setText("cm1");
        RobotToken cm2 = new RobotToken();
        cm2.setText("cm2");
        RobotToken cm3 = new RobotToken();
        cm3.setText("cm3");
        suiteDoc.addCommentPart(cm1);
        suiteDoc.addCommentPart(cm2);
        suiteDoc.addCommentPart(cm3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(fileName, modelFile);
    }
}
