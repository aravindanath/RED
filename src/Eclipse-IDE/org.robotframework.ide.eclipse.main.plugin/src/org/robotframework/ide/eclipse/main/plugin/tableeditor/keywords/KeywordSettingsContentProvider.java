/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.base.Optional;

public class KeywordSettingsContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        @SuppressWarnings("unchecked")
        final Optional<RobotKeywordDefinition> definition = (Optional<RobotKeywordDefinition>) inputElement;
        final Map<String, RobotElement> keywordSettings = KeywordSettingsModel
                .buildKeywordSettingsMapping(definition.orNull());
        return keywordSettings.entrySet().toArray();
    }

}
