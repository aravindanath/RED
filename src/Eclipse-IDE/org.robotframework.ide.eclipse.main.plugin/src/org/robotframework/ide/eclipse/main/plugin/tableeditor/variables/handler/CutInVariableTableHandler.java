/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutInVariableTableHandler.E4CutInVariableTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteInVariableTableHandler.E4DeleteInVariableTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInVariableTableHandler extends DIParameterizedHandler<E4CutInVariableTableHandler> {

    public CutInVariableTableHandler() {
        super(E4CutInVariableTableHandler.class);
    }

    public static class E4CutInVariableTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void cut(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
//            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
//
//            if (selectionLayerAccessor.onlyFullRowsAreSelected()) {
//                final E4CutVariablesHandler cutHandler = new E4CutVariablesHandler();
//                cutHandler.cutVariables(editor, commandsStack, selection, clipboard);
//
//                final E4DeleteInVariableTableHandler deleteHandler = new E4DeleteInVariableTableHandler();
//                deleteHandler.delete(commandsStack, editor, selection);
//            } else {
//
//            }

            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                    .getSelectionLayer()
                    .getSelectedCellPositions();
            if (selectedCellPositions.length > 0 && !variables.isEmpty()) {
                final PositionCoordinateSerializer[] serializablePositions = TableHandlersSupport
                        .createSerializablePositionsCoordinates(selectedCellPositions);
                final List<RobotVariable> variablesCopy = TableHandlersSupport.createVariablesCopy(variables);

                clipboard.setContents(
                        new Object[] { serializablePositions,
                                variablesCopy.toArray(new RobotVariable[variablesCopy.size()]) },
                        new Transfer[] { PositionCoordinateTransfer.getInstance(), VariablesTransfer.getInstance() });
            }
            
            final E4DeleteInVariableTableHandler deleteHandler = new E4DeleteInVariableTableHandler();
            deleteHandler.delete(commandsStack, editor, selection);
        }
    }
}
