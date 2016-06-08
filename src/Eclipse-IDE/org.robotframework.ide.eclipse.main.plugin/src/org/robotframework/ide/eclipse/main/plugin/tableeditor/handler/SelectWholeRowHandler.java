/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.SelectWholeRowHandler.E4SelectWholeRowHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class SelectWholeRowHandler extends DIParameterizedHandler<E4SelectWholeRowHandler> {

    public SelectWholeRowHandler() {
        super(E4SelectWholeRowHandler.class);
    }

    public static class E4SelectWholeRowHandler {

        @Execute
        public Object selectWholeRows(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            final SelectionLayer selectionLayer = selectionLayerAccessor.getSelectionLayer();

            final Set<Integer> rowsToSelect = new LinkedHashSet<>();
            for (final PositionCoordinate selectedCellPosition : selectionLayer.getSelectedCellPositions()) {
                rowsToSelect.add(selectedCellPosition.rowPosition);
            }

            selectionLayer.clear();
            for (final Integer rowToSelect : rowsToSelect) {
                for (int i = 0; i < selectionLayer.getColumnCount(); i++) {
                    selectionLayer.selectRow(0, rowToSelect, false, true);
                    // selectionLayer.getSelectionModel().addSelection(i, rowToSelect);
                }
            }

            return null;
        }
    }
}
