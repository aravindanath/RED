/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4PasteCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteSettingsCellContentHandler.E4PasteSettingsCellContentHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

import com.google.common.base.Optional;

public class PasteSettingsCellContentHandler extends DIParameterizedHandler<E4PasteSettingsCellContentHandler> {

    public PasteSettingsCellContentHandler() {
        super(E4PasteSettingsCellContentHandler.class);
    }

    public static class E4PasteSettingsCellContentHandler extends E4PasteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns, final String newAttribute) {
            return new SettingsAttributesCommandsProvider().provide(element, index, noOfColumns, newAttribute);
        }
    }
}
