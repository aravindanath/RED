/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.DocumentToDocumentationWordFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.SettingSimpleWordReplacer;

/**
 * @author Michal Anglart
 */
public enum TestCasesProblem implements IProblemCause {
    DUPLICATED_CASE {

        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Duplicated test case definition '%s'";
        }
    },
    EMPTY_CASE {
        @Override
        public String getProblemDescription() {
            return "Test case '%s' contains no keywords to execute";
        }
    },
    DOCUMENT_SYNONIM {

        @Override
        public String getProblemDescription() {
            return "Test Case setting '%s' is deprecated from Robot Framework 3.0. Use Documentation syntax instead of current.";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new DocumentToDocumentationWordFixer(RobotCasesSection.class));
        }
    },
    PRECONDITION_SYNONIM {

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Setup syntax instead of current.";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new SettingSimpleWordReplacer(RobotCasesSection.class, "Precondition", "Setup"));
        }
    },
    POSTCONDITION_SYNONIM {

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Teardown syntax instead of current.";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new SettingSimpleWordReplacer(RobotCasesSection.class, "Postcondition", "Teardown"));
        }
    },
    UNKNOWN_TEST_CASE_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown test case's setting definition '%s'";
        }
    },
    EMPTY_CASE_SETTING {

        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "The %s test case setting is empty";
        }
    },
    DUPLICATED_CASE_SETTING {

        @Override
        public String getProblemDescription() {
            return "Test case '%s' %s setting is defined in multiple ways";
        }
    };

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return TestCasesProblem.class.getName();
    }
}
