/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class RobotSuiteFileDescriber extends ASuiteFileDescriber {

    public RobotSuiteFileDescriber() {
        super(new TokenSeparatorBuilder(FileFormat.TXT_OR_ROBOT));
    }
}
