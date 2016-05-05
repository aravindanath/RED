/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class FileValidationContext extends AccessibleKeywordsEntities {

    private final ValidationContext context;

    private final IFile file;

    private Set<String> accessibleVariables;

    public FileValidationContext(final ValidationContext context, final IFile file) {
        this(context, file, new ValidationKeywordCollector(file, context), null);
    }

    @VisibleForTesting
    public FileValidationContext(final ValidationContext context, final IFile file,
            final AccessibleKeywordsCollector accessibleKeywordsCollector, final Set<String> accessibleVariables) {
        super(file.getFullPath(),accessibleKeywordsCollector);
        this.context = context;
        this.file = file;
        this.accessibleVariables = accessibleVariables;
    }

    public IFile getFile() {
        return file;
    }
    
    public RobotProjectConfig getProjectConfiguration() {
        return context.getProjectConfiguration();
    }

    public RobotVersion getVersion() {
        return context.getVersion();
    }

    LibrarySpecification getLibrarySpecifications(final String libName) {
        return context.getLibrarySpecification(libName);
    }

    Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return context.getReferencedLibrarySpecifications();
    }

    Set<String> getAccessibleVariables() {
        if (accessibleVariables == null) {
            accessibleVariables = context.collectAccessibleVariables(file);
        }
        return accessibleVariables;
    }
    
    public boolean isValidatingChangedFiles() {
        return context.isValidatingChangedFiles();
    }
    
    public Optional<LibrariesAutoDiscoverer> getLibrariesAutoDiscoverer() {
        return context.getLibrariesAutoDiscoverer();
    }

    private static final class ValidationKeywordCollector implements AccessibleKeywordsCollector {

        private final IFile file;

        private final ValidationContext context;

        private ValidationKeywordCollector(final IFile file, final ValidationContext context) {
            this.file = file;
            this.context = context;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return context.collectAccessibleKeywordNames(file);
        }
    }

    public static final class ValidationKeywordEntity extends KeywordEntity {

        private final int position;

        private final ArgumentsDescriptor argumentsDescriptor;

        @VisibleForTesting
        ValidationKeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final String alias, final boolean isDeprecated, final IPath exposingFilepath, final int position,
                final ArgumentsDescriptor argumentsDescriptor) {
            super(scope, sourceName, keywordName, alias, isDeprecated, exposingFilepath);
            this.position = position;
            this.argumentsDescriptor = argumentsDescriptor;
        }

        public ArgumentsDescriptor getArgumentsDescriptor() {
            return argumentsDescriptor;
        }

        public boolean hasInconsistentName(final String useplaceName) {
            return !QualifiedKeywordName.isOccurrenceEqualToDefinition(useplaceName, getNameFromDefinition());
        }

        boolean isFromNestedLibrary(final IFile useplaceFile) {
            final IPath path = useplaceFile.getFullPath();
            final KeywordScope scope = getScope(path);
            return (scope == KeywordScope.REF_LIBRARY || scope == KeywordScope.STD_LIBRARY)
                    && !path.equals(getExposingFilepath());
        }

        @Override
        public boolean isSameAs(final KeywordEntity other, final IPath useplaceFilepath) {
            return position == ((ValidationKeywordEntity) other).position && super.isSameAs(other, useplaceFilepath);
        }

        @Override
        public boolean equals(final Object obj) {
            return super.equals(obj) || position == ((ValidationKeywordEntity) obj).position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), position);
        }
    }
}
