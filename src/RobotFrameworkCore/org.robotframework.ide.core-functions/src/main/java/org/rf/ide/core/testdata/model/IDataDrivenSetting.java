/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public interface IDataDrivenSetting {

    RobotToken getDeclaration();


    RobotToken getKeywordName();


    List<RobotToken> getUnexpectedTrashArguments();


    List<RobotToken> getComment();

}
