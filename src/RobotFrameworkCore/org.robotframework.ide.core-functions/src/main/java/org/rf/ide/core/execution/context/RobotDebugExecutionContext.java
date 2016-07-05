/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.importer.ResourceImporter;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.ERowType;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

/**
 * @author mmarzec
 */
public class RobotDebugExecutionContext {

    public static final String MAIN_KEYWORD_TYPE = "Keyword";
    
    public static final String FOR_LOOP_ITEM_KEYWORD_TYPE = "For Item";

    private RobotFile currentModel;

    private RobotParser robotParser;

    private ResourceImporter resourceImporter;

    private final List<String> resourceImportPaths;

    private final List<KeywordContext> currentKeywords;

    private final TestCaseExecutionRowCounter testCaseExecutionRowCounter;

    private final ExecutableRowFindersManager executableRowFindersManager;

    private boolean isSetupTeardownKeywordStarted;

    private boolean isForLoopStarted;

    private int forLoopsCounter = 0;

    public RobotDebugExecutionContext() {
        currentKeywords = new ArrayList<>();
        testCaseExecutionRowCounter = new TestCaseExecutionRowCounter();
        executableRowFindersManager = new ExecutableRowFindersManager();
        resourceImportPaths = new ArrayList<>();
        resourceImporter = new ResourceImporter();
    }

    public void resourceImport(final String path) {
        if (currentModel != null) { // import during suite execution
            resourceImporter.importDebugResource(robotParser, currentModel.getParent(), path);
            executableRowFindersManager
                    .updateResourceImportReferences(currentModel.getParent().getResourceImportReferences());
        } else { // import before suite start
            resourceImportPaths.add(path);
        }
    }

    public void startSuite(final RobotFileOutput robotFileOutput, final RobotParser robotParser) {
        this.currentModel = robotFileOutput.getFileModel();
        this.robotParser = robotParser;

        for (final String path : resourceImportPaths) {
            resourceImporter.importDebugResource(robotParser, robotFileOutput, path);
        }
        resourceImportPaths.clear();

        executableRowFindersManager.initFindersAtSuiteStart(currentModel, currentModel.getKeywordTable().getKeywords(),
                robotFileOutput.getResourceImportReferences());
    }

    public boolean startTest(final String testName) {
        if (currentModel != null) {
            final TestCaseTable testCaseTable = currentModel.getTestCaseTable();
            final List<TestCase> testCases = testCaseTable.getTestCases();
            for (final TestCase testCase : testCases) {
                if (testCase.getTestName().getText().toString().equalsIgnoreCase(testName)) {
                    executableRowFindersManager.initFindersAtTestCaseStart(testCase);
                    return true;
                }
            }
        }
        return false;
    }

    public void startKeyword(final String name, final String type, final List<String> arguments) {
        currentKeywords.add(new KeywordContext(name, type));
        checkStartKeywordType(type);
    }

    public void endKeyword(final String type) {
        currentKeywords.remove(currentKeywords.size() - 1);
        checkEndKeywordType(type);
    }

    public void endTest() {
        testCaseExecutionRowCounter.reset();
        executableRowFindersManager.clearAtTestCaseEnd();
    }

    public void endSuite() {
        this.currentModel = null;
    }

    public KeywordPosition findKeywordPosition() {
        final IRobotExecutableRowFinder executableRowFinder = getExecutableRowFinder();
        final RobotExecutableRow<?> executableRow = findExecutableRow(executableRowFinder);
        return createNewKeywordPosition(executableRow);
    }

    public IRobotExecutableRowFinder getExecutableRowFinder() {
        IRobotExecutableRowFinder executableRowFinder = null;
        if (isKeywordDirectlyFromTestCase()) {
            if (isSetupTeardownKeywordStarted) {
                executableRowFinder = executableRowFindersManager.provideSetupTeardownExecutableRowFinder();
            } else {
                executableRowFinder = executableRowFindersManager
                        .provideTestCaseExecutableRowFinder(testCaseExecutionRowCounter);
            }
        } else if (isForLoopStarted) {
            executableRowFinder = executableRowFindersManager
                    .provideForLoopExecutableRowFinder(testCaseExecutionRowCounter);
        } else { // keyword from Keywords section or resource file
            executableRowFinder = executableRowFindersManager.provideUserKeywordExecutableRowFinder();
        }

        return executableRowFinder;
    }

    private RobotExecutableRow<?> findExecutableRow(final IRobotExecutableRowFinder executableRowFinder) {
        RobotExecutableRow<?> executableRow = null;
        if (executableRowFinder != null) {
            executableRow = executableRowFinder.findExecutableRow(currentKeywords);
        }
        return executableRow;
    }

    private KeywordPosition createNewKeywordPosition(final RobotExecutableRow<?> executableRow) {
        final KeywordPosition keywordPosition = new KeywordPosition();
        final KeywordContext parentKeywordContext = getParentKeywordContext();
        if (parentKeywordContext != null && parentKeywordContext.getResourceImportReference() != null) {
            keywordPosition.setFilePath(parentKeywordContext.getResourceImportReference()
                    .getReference()
                    .getProcessedFile()
                    .getAbsolutePath());
        }
        int line = -1;
        if (executableRow != null) {
            IExecutableRowDescriptor<?> buildLineDescription = executableRow.buildLineDescription();
            if (buildLineDescription.getRowType() == ERowType.FOR_CONTINUE) {
                line = ((ForLoopContinueRowDescriptor<?>) buildLineDescription).getKeywordAction()
                        .getToken()
                        .getLineNumber();
            } else {
                line = executableRow.getAction().getLineNumber();
            }
        }
        keywordPosition.setLineNumber(line);
        return keywordPosition;
    }

    private KeywordContext getParentKeywordContext() {
        KeywordContext parentKeywordContext = null;
        if ((currentKeywords.size() - 2) >= 0) {
            parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);
        }
        return parentKeywordContext;
    }

    private void checkStartKeywordType(final String type) {
        if (isForLoopKeyword(type)) {
            forLoopsCounter++;
        } else if (forLoopsCounter > 0) {
            isForLoopStarted = true;
        } else if (isSetupTeardownStartKeyword(type)) {
            isSetupTeardownKeywordStarted = true;
        }
    }

    private void checkEndKeywordType(final String keywordType) {
        if (isForLoopKeyword(keywordType)) {
            forLoopsCounter--;
            if (forLoopsCounter == 0) {
                isForLoopStarted = false;
                executableRowFindersManager.clearForLoopState();
            }
        }
        if (!isMainKeyword(keywordType) && !isForLoopItemKeyword(keywordType)) {
            isSetupTeardownKeywordStarted = false;
        }
    }
    
    private boolean isMainKeyword(final String keywordType) {
        return keywordType.equalsIgnoreCase(MAIN_KEYWORD_TYPE);
    }

    private boolean isForLoopKeyword(final String keywordType) {
        return ForLoopKeywordTypes.isForLoopKeywordType(keywordType);
    }
    
    private boolean isForLoopItemKeyword(final String keywordType) {
        return keywordType.equalsIgnoreCase(FOR_LOOP_ITEM_KEYWORD_TYPE);
    }

    private boolean isSetupTeardownStartKeyword(final String keywordType) {
        return !isMainKeyword(keywordType) && !isForLoopKeyword(keywordType);
    }

    private boolean isKeywordDirectlyFromTestCase() {
        return currentKeywords.size() == 1;
    }

    public boolean isSuiteSetupTeardownKeyword(final String keywordType) {
        return SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes.isSuiteSetupTeardownType(keywordType)
                || (!executableRowFindersManager.hasCurrentTestCase()
                        && SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes
                                .isNewSetupTeardownType(keywordType))
                || (isSetupTeardownKeywordStarted && (isMainKeyword(keywordType)
                        || ForLoopKeywordTypes.isForLoopKeywordType(keywordType) || isForLoopItemKeyword(keywordType)));
    }

    public boolean isTestCaseTeardownKeyword(final String keywordType) {
        return SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes.isTypeOf(keywordType,
                SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes.TESTCASE_TEARDOWN)
                || (executableRowFindersManager.hasCurrentTestCase()
                        && SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes.isTypeOf(keywordType,
                                SetupTeardownExecutableRowFinder.SetupTeardownKeywordTypes.NEW_TEARDOWN));
    }

    protected static class KeywordContext {

        private final String name;

        private final String type;

        private int keywordExecutableRowCounter = 0;

        private ResourceImportReference resourceImportReference;

        private UserKeyword userKeyword;

        public KeywordContext(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getKeywordExecutableRowCounter() {
            return keywordExecutableRowCounter;
        }

        public void incrementKeywordExecutableRowCounter() {
            keywordExecutableRowCounter++;
        }

        public ResourceImportReference getResourceImportReference() {
            return resourceImportReference;
        }

        public void setResourceImportReference(final ResourceImportReference resourceImportReference) {
            this.resourceImportReference = resourceImportReference;
        }

        public UserKeyword getUserKeyword() {
            return userKeyword;
        }

        public void setUserKeyword(final UserKeyword userKeyword) {
            this.userKeyword = userKeyword;
        }

        public String getType() {
            return type;
        }

    }

    public class KeywordPosition {

        private int lineNumber;

        private String filePath;

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(final int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(final String filePath) {
            this.filePath = filePath;
        }
    }

    protected static class TestCaseExecutionRowCounter {

        private int counter = 0;

        public void increment() {
            counter++;
        }

        public void reset() {
            counter = 0;
        }

        public int getCounter() {
            return counter;
        }
    }
    
    protected enum ForLoopKeywordTypes {
        NEW_FOR("For"), // since Robot 3.0 a2
        TEST_FOR("Test For"),
        SUITE_FOR("Suite For");
        
        private String typeName;
        
        private ForLoopKeywordTypes(final String typeName) {
            this.typeName = typeName;
        }
        
        private String getTypeName() {
            return typeName;
        }
        
        public static boolean isForLoopKeywordType(final String keywordType) {
            final ForLoopKeywordTypes[] values = ForLoopKeywordTypes.values();
            for (int i = 0; i < values.length; i++) {
                if(values[i].getTypeName().equalsIgnoreCase(keywordType)) {
                    return true;
                }
            }
            return false;
        }
        
    }
}
