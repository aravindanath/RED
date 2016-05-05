/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateConfigurationFileFixer;

public enum ProjectConfigurationProblem implements IProblemCause {
    CONFIG_FILE_MISSING {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new CreateConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "FATAL: project configuration file " + RobotProjectConfig.FILENAME + " does not exist";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.PROJECT_CONFIGURATION_FILE_DOES_NOT_EXIST;
        }
    },
    CONFIG_FILE_READING_PROBLEM {
        @Override
        public String getProblemDescription() {
            return "FATAL: unable to read configuration file. %s Fix this problem in order to properly build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.PROJECT_CONFIGURATION_FILE_READING_PROBLEM;
        }
    },
    ENVIRONMENT_MISSING {
        @Override
        public String getProblemDescription() {
            return "FATAL: %s Python environment is not defined in preferences. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.MISSING_ROBOT_ENVIRONMENT;
        }
    },
    ENVIRONMENT_NOT_A_PYTHON {
        @Override
        public String getProblemDescription() {
            return "FATAL: %s is not a Python installation directory. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.CHOSEN_ENVIRONMENT_IS_NOT_A_PYTHON_INSTALLATION;
        }
    },
    ENVIRONMENT_HAS_NO_ROBOT {
        @Override
        public String getProblemDescription() {
            return "FATAL: Python instalation %s has no Robot installed. Fix this problem to build project";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.CHOSEN_ENVIRONMENT_WITH_PYTHON_INSTALLATION_HAS_NO_ROBOT_INSTALLED;
        }
    },
    LIBRARY_SPEC_CANNOT_BE_GENERATED {
        @Override
        public String getProblemDescription() {
            return "FATAL: %s";
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.LIBRARY_SPECIFICATION_FILE_CANNOT_BE_GENERATED;
        }
    };

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public Severity getSeverity() {
        return Severity.FATAL;
    }

    @Override
    public String getEnumClassName() {
        return ProjectConfigurationProblem.class.getName();
    }

    public static Collection<ProblemCategory> getCategories() {
        final Set<ProblemCategory> categories = newLinkedHashSet();
        for (final IProblemCause cause : EnumSet.allOf(ProjectConfigurationProblem.class)) {
            categories.add(cause.getProblemCategory());
        }
        return categories;
    }
}
