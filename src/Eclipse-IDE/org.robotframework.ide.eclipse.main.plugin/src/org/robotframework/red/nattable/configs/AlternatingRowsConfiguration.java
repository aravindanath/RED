/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;


/**
 * @author Michal Anglart
 *
 */
public class AlternatingRowsConfiguration extends DefaultRowStyleConfiguration {

    public AlternatingRowsConfiguration(final TableTheme theme) {
        this.oddRowBgColor = theme.getBodyBackgroundOddRowBackground();
        this.evenRowBgColor = theme.getBodyBackgroundEvenRowBackground();
    }
}
