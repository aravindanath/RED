/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.Test;
import org.rf.ide.core.execution.context.RobotModelTestProvider;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.rf.ide.core.testdata.text.write.DumperTestHelper.TextCompareResult;

public abstract class ASettingsAndVariablesNotChangedDumperTest {

    private final String extension;

    private final FileFormat format;

    public ASettingsAndVariablesNotChangedDumperTest(final String extension, final FileFormat format) {
        this.extension = extension;
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public FileFormat getFormat() {
        return format;
    }

    @Test
    public void dumpAsItIsInFile() throws Exception {
        // prepare
        final Path inputFile = DumperTestHelper.getINSTANCE()
                .getFile("remainNotChanged/SettingsAndVariables." + getExtension());
        final String fileContent = DumperTestHelper.getINSTANCE().readWithLineSeparatorPresave(inputFile);

        final RobotFile modelFile = RobotModelTestProvider.getModelFile(fileContent, getFormat(),
                RobotModelTestProvider.getParser());
        final RobotFileDumper dumper = new RobotFileDumper();
        // execute
        final String dumpResult = dumper.dump(modelFile.getParent());

        // verify
        final TextCompareResult cmpResult = DumperTestHelper.getINSTANCE().compare(fileContent, dumpResult);

        assertThat(cmpResult.report()).isNull();
    }
}
