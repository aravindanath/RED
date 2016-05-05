/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;

import com.google.common.base.Objects;

/**
 * @author Michal Anglart
 *
 */
public abstract class KeywordEntity {

    private final KeywordScope scope;

    private final String sourceName;

    private final String keywordName;

    private final String alias;

    private final boolean isDeprecated;

    private final IPath exposingFilepath;

    protected KeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
            final String alias,
            final boolean isDeprecated, final IPath exposingFilepath) {
        this.scope = scope;
        this.sourceName = sourceName;
        this.keywordName = keywordName;
        this.alias = alias;
        this.isDeprecated = isDeprecated;
        this.exposingFilepath = exposingFilepath;
    }

    public KeywordScope getScope(final IPath useplaceFilepath) {
        if (scope != null) {
            return scope;
        } else {
            return useplaceFilepath.equals(exposingFilepath) ? KeywordScope.LOCAL : KeywordScope.RESOURCE;
        }
    }

    public String getSourceNameInUse() {
        return alias.isEmpty() ? sourceName : alias;
    }

    public String getNameFromDefinition() {
        return keywordName;
    }

    public IPath getExposingFilepath() {
        return exposingFilepath;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public boolean isSameAs(final KeywordEntity that, final IPath useplaceFilepath) {
        return Objects.equal(this.getSourceNameInUse(), that.getSourceNameInUse())
                && Objects.equal(this.keywordName, that.keywordName)
                && this.getScope(useplaceFilepath) == that.getScope(useplaceFilepath);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == getClass()) {
            final KeywordEntity that = (KeywordEntity) obj;
            return Objects.equal(this.sourceName, that.sourceName) && Objects.equal(this.alias, that.alias)
                    && Objects.equal(this.keywordName, that.keywordName)
                    && this.scope == that.scope && this.isDeprecated == that.isDeprecated
                    && this.exposingFilepath == that.exposingFilepath;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scope, sourceName, alias, keywordName, isDeprecated, exposingFilepath);
    }
}
