/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.keywords.creation;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public abstract class ACreationOfKeywordTimeoutTest {

    public static final String PRETTY_NEW_DIR_LOCATION = "keywords//setting//timeout//new//";

    private final String extension;

    public ACreationOfKeywordTimeoutTest(final String extension) {
        this.extension = extension;
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeoutNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDecOnly() throws Exception {
        test_timeoutDecOnly("EmptyKeywordTimeout", "User Keyword");
    }

    private void test_timeoutDecOnly(final String fileNameWithoutExt, final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        uk.newTimeout();

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withComment()
            throws Exception {
        test_timeoutDec_withCommentOnly("EmptyKeywordTimeoutComment", "User Keyword");
    }

    private void test_timeoutDec_withCommentOnly(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTimeout keyTimeout = uk.newTimeout();

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTimeout.addCommentPart(cmTok1);
        keyTimeout.addCommentPart(cmTok2);
        keyTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue()
            throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValueNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue() throws Exception {
        test_timeoutDec_withComment_andValue("EmptyKeywordTimeoutWithValue", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue(final String fileNameWithoutExt, final String userKeywordName)
            throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTimeout keyTimeout = uk.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        keyTimeout.setTimeout(timeout);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_andComment("EmptyKeywordTimeoutWithValueAndComment", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTimeout keyTimeout = uk.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        keyTimeout.setTimeout(timeout);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTimeout.addCommentPart(cmTok1);
        keyTimeout.addCommentPart(cmTok2);
        keyTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgsNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs("KeywordTimeoutWithValueAnd3MsgArgs", "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTimeout keyTimeout = uk.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        keyTimeout.setTimeout(timeout);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2");
        RobotToken msg3 = new RobotToken();
        msg3.setText("msg3");

        keyTimeout.addMessagePart(msg1);
        keyTimeout.addMessagePart(msg2);
        keyTimeout.addMessagePart(msg3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withoutKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(
                "KeywordTimeoutWithValueAnd3MsgArgsAndCommentNoKeywordName", "");
    }

    @Test
    public void test_emptyFile_and_thanCreateKeywordTimeout_withKeywordName_andTimeoutDec_withValue_and3MsgArgs_andComment()
            throws Exception {
        test_timeoutDec_withComment_andValue_and3MsgArgs_andComment("KeywordTimeoutWithValueAnd3MsgArgsAndComment",
                "User Keyword");
    }

    private void test_timeoutDec_withComment_andValue_and3MsgArgs_andComment(final String fileNameWithoutExt,
            final String userKeywordName) throws Exception {
        // prepare
        final String filePath = PRETTY_NEW_DIR_LOCATION + fileNameWithoutExt + "." + getExtension();
        final RobotFile modelFile = NewRobotFileTestHelper.getModelFileToModify("2.9");

        // test data prepare
        modelFile.includeKeywordTableSection();
        KeywordTable keywordTable = modelFile.getKeywordTable();

        RobotToken keyName = new RobotToken();
        keyName.setText(userKeywordName);
        UserKeyword uk = new UserKeyword(keyName);
        keywordTable.addKeyword(uk);
        KeywordTimeout keyTimeout = uk.newTimeout();

        RobotToken timeout = new RobotToken();
        timeout.setText("1 hours");
        keyTimeout.setTimeout(timeout);

        RobotToken msg1 = new RobotToken();
        msg1.setText("msg1");
        RobotToken msg2 = new RobotToken();
        msg2.setText("msg2");
        RobotToken msg3 = new RobotToken();
        msg3.setText("msg3");

        keyTimeout.addMessagePart(msg1);
        keyTimeout.addMessagePart(msg2);
        keyTimeout.addMessagePart(msg3);

        RobotToken cmTok1 = new RobotToken();
        cmTok1.setText("cm1");
        RobotToken cmTok2 = new RobotToken();
        cmTok2.setText("cm2");
        RobotToken cmTok3 = new RobotToken();
        cmTok3.setText("cm3");

        keyTimeout.addCommentPart(cmTok1);
        keyTimeout.addCommentPart(cmTok2);
        keyTimeout.addCommentPart(cmTok3);

        // verify
        NewRobotFileTestHelper.assertNewModelTheSameAsInFile(filePath, modelFile);
    }

    public String getExtension() {
        return extension;
    }
}
