/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.collect.Range;

public class MetadataKeyInColumnOfSettingValidatorStartFromRF30 extends AMetadataKeyInColumnOfSettingValidator {

    public MetadataKeyInColumnOfSettingValidatorStartFromRF30(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return GeneralSettingsProblem.METADATA_SETTING_JOINED_WITH_KEY_IN_COLUMN_30;
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.atLeast(new RobotVersion(3, 0));
    }
}
