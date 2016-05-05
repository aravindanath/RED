/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;

import com.google.common.collect.Range;

public class DuplicatedTestTimeoutValidator extends ADuplicatedValidator<TestTimeout> {

    public DuplicatedTestTimeoutValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 0));
    }

    @Override
    protected List<TestTimeout> getElements() {
        final SettingTable table = (SettingTable) section.getLinkedElement();
        return table.getTestTimeouts();
    }
}
