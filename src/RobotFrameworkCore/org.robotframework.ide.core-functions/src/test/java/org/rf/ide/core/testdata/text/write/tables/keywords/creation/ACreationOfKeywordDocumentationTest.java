/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordDocumentationTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//documentation//new//";

    private final String extension;

    public ACreationOfKeywordDocumentationTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordCaseDocumentation_withName_andThreeLinesOfDocumentation()
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + "KeywordDocumentationWithThreeLinesCreation" + "."
                + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText("User Keyword");
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();

        DocumentationServiceHandler.update(keyDoc, "doc me" + "\n" + "textZero" + "\n" + "textTwo");

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationDecOnly()
            throws Exception {
        final String fileName = "EmptyKeywordDocumentationNoKeywordName";
        final String userKeywordName = "";
        test_onlyKeyDoc_decIncluded(fileName, userKeywordName);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationDecOnly()
            throws Exception {
        test_onlyKeyDoc_decIncluded("EmptyKeywordDocumentation", "User Keyword");
    }

    private void test_onlyKeyDoc_decIncluded(final String fileName, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newDocumentation();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationNoKeywordNameAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAndCommentOnly()
            throws Exception {
        test_keyDoc_withCommentOnly("KeywordDocumentationAndComment", "User Keyword");
    }

    private void test_keyDoc_withCommentOnly(final String fileName, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationNoKeywordNameAnd3WordsInText", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3Words()
            throws Exception {
        test_docOnlyWith3Words("KeywordDocumentationAnd3WordsInText", "User Keyword");
    }

    private void test_docOnlyWith3Words(final String fileName, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);
        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withoutKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationNoKeywordNameAnd3WordsInTextAndComment", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordDocumentation_withKeywordName_andDocumentationAnd3WordsAndComment()
            throws Exception {
        test_keyDoc_withDoc3Words_andComment("KeywordDocumentationAnd3WordsInTextAndComment", "User Keyword");
    }

    private void test_keyDoc_withDoc3Words_andComment(final String fileName, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileName + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordDocumentation keyDoc = uk.newDocumentation();
        RobotToken wr1 = new RobotToken();
        wr1.setText("w1");
        RobotToken wr2 = new RobotToken();
        wr2.setText("w2");
        RobotToken wr3 = new RobotToken();
        wr3.setText("w3");

        keyDoc.addDocumentationText(wr1);
        keyDoc.addDocumentationText(wr2);
        keyDoc.addDocumentationText(wr3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyDoc.addCommentPart(cmTok1);
        keyDoc.addCommentPart(cmTok2);
        keyDoc.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
