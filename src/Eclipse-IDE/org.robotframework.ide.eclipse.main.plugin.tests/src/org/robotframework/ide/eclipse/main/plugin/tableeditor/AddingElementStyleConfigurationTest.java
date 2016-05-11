/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

public class AddingElementStyleConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final TableTheme theme = mock(TableTheme.class);
        when(theme.getFont()).thenReturn(Display.getCurrent().getSystemFont());

        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final AddingElementStyleConfiguration configuration = new AddingElementStyleConfiguration(theme, true);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.NORMAL), eq(AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.SELECT), eq(AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(ICellPainter.class),
                eq(DisplayMode.NORMAL), eq(AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL));
    }
}
