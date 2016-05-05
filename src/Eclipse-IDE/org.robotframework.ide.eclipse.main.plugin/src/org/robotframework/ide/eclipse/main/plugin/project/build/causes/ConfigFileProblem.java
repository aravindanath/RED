/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveLibraryFromConfigurationFileFixer;

public enum ConfigFileProblem implements IProblemCause {
    UNREACHABLE_HOST {
        @Override
        public String getProblemDescription() {
            return "Unreachable remote server %s";
        }
    },
    ABSOLUTE_PATH {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path %s is absolute. RED prefers using workspace-relative paths which makes your projects more portable";
        }
    },
    MISSING_LIBRARY_FILE {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing library file '%s'. Keywords from this libary will not be accessible";
        }
    },
    MISSING_VARIABLE_FILE {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Missing variable file '%s'. Variables from this file will not be accessible";
        }
    },
    MISSING_EXCLUDED_FOLDER {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Missing excluded folder '%s'";
        }
    },
    JAVA_LIB_NOT_A_JAR_FILE {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' for Java library should point to .jar file. Keywords from this libary will not be visible";
        }
    },
    JAVA_LIB_MISSING_CLASS {
        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveLibraryFromConfigurationFileFixer());
        }

        @Override
        public String getProblemDescription() {
            return "Java library '%s' does not contain class '%s'. Keywords from this libary will not be visible";
        }
    },
    JAVA_LIB_IN_NON_JAVA_ENV {
        @Override
        public String getProblemDescription() {
            return "Java library '%s' requires Jython, but %s environment is in use by this project";
        }
    },
    USELESS_FOLDER_EXCLUSION {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' is already excluded by '%s'";
        }
    },
    MISSING_SEARCH_PATH {

        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "The path '%s' points to non-existing location";
        }
    },
    INVALID_SEARCH_PATH {

        @Override
        public String getProblemDescription() {
            return "The path '%s' is invalid";
        }
    };

    public static final String LIBRARY_INDEX = "marker.libraryIndex";

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
        return new ArrayList<>();
    }

    @Override
    public String getEnumClassName() {
        return ConfigFileProblem.class.getName();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }
}
