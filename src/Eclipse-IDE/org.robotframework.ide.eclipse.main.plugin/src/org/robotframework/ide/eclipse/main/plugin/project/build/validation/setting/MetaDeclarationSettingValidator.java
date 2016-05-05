/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

public class MetaDeclarationSettingValidator implements ModelUnitValidator {

    private final IFile file;

    private final RobotSettingsSection section;

    private final ProblemsReportingStrategy reporter;

    private final OldMetaSynataxHelper oldMetaSyntaxHelper;

    public MetaDeclarationSettingValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        this.file = file;
        this.section = section;
        this.reporter = reporter;
        this.oldMetaSyntaxHelper = new OldMetaSynataxHelper();
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        SettingTable settingTable = (SettingTable) section.getLinkedElement();
        for (final Metadata meta : settingTable.getMetadatas()) {
            if (!oldMetaSyntaxHelper.isOldSyntax(meta, settingTable)) {
                RobotToken metaToken = meta.getDeclaration();
                String raw = metaToken.getRaw();
                final String settingPrepared = raw.replaceAll("\\s", "").toLowerCase();
                if (settingPrepared.equals("meta") || settingPrepared.equals("meta:")) {
                    reporter.handleProblem(
                            RobotProblem.causedBy(GeneralSettingsProblem.META_SYNONIM).formatMessageWith(raw), file,
                            metaToken);
                }
            }
        }
    }
}
