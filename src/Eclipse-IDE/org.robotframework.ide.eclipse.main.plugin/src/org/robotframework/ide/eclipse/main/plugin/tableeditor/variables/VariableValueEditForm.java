/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;

/**
 * @author mmarzec
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.TooManyMethods" })
public class VariableValueEditForm {
    
    private static final String COLLECTION_SEPARATOR_REGEX = "(\\s{2,}|\t)"; // two or more spaces or tab

    private final IEventBroker eventBroker;

    private Text valueTxt;
    
    private Text nameTxt;

    private boolean isList;

    private boolean isDictionary;

    private boolean isScalar;

    private List<RobotCollectionElement> collectionElements;

    private RowExposingTableViewer tableViewer;

    private Table table;

    private final RedFormToolkit toolkit;

    private final Section section;

    private RobotVariable variable;
    
    private boolean isEditedInForm;
    
    ModifyListener valueTxtModifyListener = new ModifyListener() {

        @Override
        public void modifyText(final ModifyEvent e) {
            variableChangedInSection();
        }
    };

    public VariableValueEditForm(final RedFormToolkit toolkit, final Section section, final IEventBroker eventBroker) {
        this.toolkit = toolkit;
        this.section = section;
        this.eventBroker = eventBroker;
    }

    public Composite createVariableValueEditForm(final RobotVariable variable) {
        this.variable = variable;
        this.isList = variable.getType() == VariableType.LIST;
        this.isDictionary = variable.getType() == VariableType.DICTIONARY;
        this.isScalar = variable.getType() == VariableType.SCALAR;

        final Composite form = toolkit.createComposite(section);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 2).applyTo(form);

        createEditControls(form);

        return form;
    }

    private void createEditControls(final Composite composite) {
        toolkit.createLabel(composite, "Name: ");
        nameTxt = toolkit.createText(composite, variable.getName(), SWT.NONE);
        nameTxt.setEnabled(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(nameTxt);

        final Label valueLbl = toolkit.createLabel(composite, "");
        if (isScalar) {
            valueLbl.setText("Scalar:");
        } else if (isList) {
            valueLbl.setText("List:");
        } else {
            valueLbl.setText("Dictionary:");
        }
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(valueLbl);

        if (isScalar) {
            valueTxt = toolkit.createText(composite, "", SWT.BORDER);
            valueTxt.setText(variable.getValue());
            valueTxt.addModifyListener(valueTxtModifyListener);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(valueTxt);
            tableViewer = null;
            
        } else {

            tableViewer = new RowExposingTableViewer(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
                    | SWT.V_SCROLL);
            CellsActivationStrategy.addActivationStrategy(tableViewer, RowTabbingStrategy.MOVE_TO_NEXT);

            final String[] values = variable.getValue().split(COLLECTION_SEPARATOR_REGEX); 

            collectionElements = new ArrayList<RobotCollectionElement>();
            if (isList) {
                createInputForList(collectionElements, values);
            } else {
                createInputForDictionary(collectionElements, values);
            }
            createTable();
            tableViewer.setContentProvider(new CollectionContentProvider());
            tableViewer.setInput(collectionElements);
        }
    }

    private void createTable() {
        if (isList) {
            createColumnsForList(tableViewer);
        } else {
            createColumnsForDictionary(tableViewer);
        }

        table = tableViewer.getTable();
        GridDataFactory.fillDefaults().grab(true, true).minSize(1, 120).applyTo(table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
    }

    private void createInputForList(final List<RobotCollectionElement> elements, final String[] values) {
        int j = 0;
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals("")) {
                elements.add(createNewCollectionElement(j, values[i]));
                j++;
            }
        }
    }

    private void createInputForDictionary(final List<RobotCollectionElement> elements, final String[] values) {
        String[] keyValuePair;
        int j = 0;
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals("") && values[i].contains("=")) {
                keyValuePair = values[i].split("=");
                if (keyValuePair.length == 2) {
                    elements.add(createNewCollectionElement(j, keyValuePair[0], keyValuePair[1]));
                } else if (keyValuePair.length == 1) {
                    elements.add(createNewCollectionElement(j, keyValuePair[0], ""));
                } else {
                    elements.add(createNewCollectionElement(j, "", ""));
                }
                j++;
            }
        }
    }

    private RobotCollectionElement createNewCollectionElement(final int index, final String key, final String value) {
        return new RobotCollectionElement(index, key, value);
    }

    private RobotCollectionElement createNewCollectionElement(final int index, final String value) {
        return createNewCollectionElement(index, null, value);
    }

    private void createColumnsForList(final RowExposingTableViewer tableViewer) {
        ViewerColumnsFactory.newColumn("Index").withWidth(50).labelsProvidedBy(new ColumnLabelProvider() {

            @Override
            public String getText(final Object element) {
                if (element instanceof RobotCollectionElement) {
                    return String.valueOf(((RobotCollectionElement) element).getIndex());
                }
                return "";
            }
        }).createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(300)
                .labelsProvidedBy(new CollectionLabelProvider(true))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, true))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
    }

    private void createColumnsForDictionary(final RowExposingTableViewer tableViewer) {
        ViewerColumnsFactory.newColumn("No.").withWidth(50).labelsProvidedBy(new ColumnLabelProvider() {

            @Override
            public String getText(final Object element) {
                if (element instanceof RobotCollectionElement) {
                    return String.valueOf(((RobotCollectionElement) element).getIndex() + 1);
                }
                return "";
            }
        }).createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Key")
                .withWidth(150)
                .labelsProvidedBy(new CollectionLabelProvider(false))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, false))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(150)
                .labelsProvidedBy(new CollectionLabelProvider(true))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, true))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
    }

    private String convertListToString() {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < collectionElements.size(); i++) {
            final String value = collectionElements.get(i).getValue();
            if (!value.equals("")) {
                strBuilder.append(value);
                if (i < collectionElements.size() - 1) {
                    // TODO: separators typed by User should be used
                    strBuilder.append("  ");
                }
            }
        }
        final int strLen = strBuilder.length();
        if (strLen > 1 && strBuilder.substring(strLen - 2).equals("  ")) {
            strBuilder.delete(strLen - 2, strLen);
        }

        return strBuilder.toString();
    }

    private String convertDictionaryToString() {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < collectionElements.size(); i++) {
            final String key = collectionElements.get(i).getKey();
            final String value = collectionElements.get(i).getValue();
            if (!"".equals(key) && !"".equals(value)) {
                strBuilder.append(key + "=" + value);
                if (i < collectionElements.size() - 1) {
                    // TODO: separators typed by User should be used
                    strBuilder.append("  ");
                }
            }
        }
        final int strLen = strBuilder.length();
        if (strLen > 1 && strBuilder.substring(strLen - 2).equals("  ")) {
            strBuilder.delete(strLen - 2, strLen);
        }
        return strBuilder.toString();
    }

    public void addNewElement() {
        if (table != null) {
            final int selectionIndex = table.getSelectionIndex();
            if (selectionIndex >= 0 && table.getSelectionCount() == 1) {
                collectionElements.add(selectionIndex, createNewCollectionElement(selectionIndex, "", ""));
                updateIndexesAboveNewElement(selectionIndex);
                variableChangedInSection();
                tableViewer.refresh();
            }
        }
    }

    public void moveSelectedElementUp() {
        if (table != null) {
            final int selectionIndex = table.getSelectionIndex();
            if (selectionIndex > 0 && selectionIndex < collectionElements.size() && table.getSelectionCount() == 1) {
                final RobotCollectionElement elementAbove = collectionElements.get(selectionIndex - 1);
                elementAbove.setIndex(elementAbove.getIndex() + 1);
                final RobotCollectionElement elementBelow = collectionElements.get(selectionIndex);
                elementBelow.setIndex(elementAbove.getIndex() - 1);
                collectionElements.set(selectionIndex - 1, elementBelow);
                collectionElements.set(selectionIndex, elementAbove);
                variableChangedInSection();
                tableViewer.refresh();
            }
        }
    }

    public void moveSelectedElementDown() {
        if (table != null) {
            final int selectionIndex = table.getSelectionIndex();
            if (selectionIndex >= 0 && selectionIndex < collectionElements.size() - 1 && table.getSelectionCount() == 1) {
                final RobotCollectionElement elementAbove = collectionElements.get(selectionIndex);
                elementAbove.setIndex(elementAbove.getIndex() + 1);
                final RobotCollectionElement elementBelow = collectionElements.get(selectionIndex + 1);
                elementBelow.setIndex(elementAbove.getIndex() - 1);
                collectionElements.set(selectionIndex, elementBelow);
                collectionElements.set(selectionIndex + 1, elementAbove);
                variableChangedInSection();
                tableViewer.refresh();
            }
        }
    }

    public void deleteElement() {
        if (table != null) {
            final TableItem[] selectedItems = table.getSelection();
            for (int i = 0; i < selectedItems.length; i++) {
                final Object element = selectedItems[i].getData();
                if (element instanceof RobotCollectionElement) {
                    collectionElements.remove(element);
                }
            }
            if (selectedItems.length > 0) {
                updateIndexes();
                variableChangedInSection();
                tableViewer.refresh();
            }
        }
    }

    private void updateIndexesAboveNewElement(final int newElementIndex) {
        for (int i = newElementIndex + 1; i < collectionElements.size(); i++) {
            collectionElements.get(i).incrementIndex();
        }
    }

    private void updateIndexes() {
        for (int i = 0; i < collectionElements.size(); i++) {
            collectionElements.get(i).setIndex(i);
        }
    }
    
    private void variableChangedInSection() {

        String resultValue = "";
        if (isList) {
            resultValue = convertListToString();
        } else if (isDictionary) {
            resultValue = convertDictionaryToString();
        } else {
            if (valueTxt != null) {
                resultValue = valueTxt.getText();
            }
        }
        isEditedInForm = true;
        // variable.setValue(resultValue);
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);   //notify main variables table that value is changed in section
    }
    
    public void variableChangedInMainTable(final RobotVariable variable) {
        if (!isEditedInForm && variable == this.variable) {  //change input only when event is from main variables table and selected variable is the same as edited
            final String[] values = variable.getValue().split(COLLECTION_SEPARATOR_REGEX);

            collectionElements = new ArrayList<RobotCollectionElement>();
            if (variable.getType() == VariableType.DICTIONARY && tableViewer != null) {
                createInputForDictionary(collectionElements, values);
                tableViewer.setInput(collectionElements);
            } else if (variable.getType() == VariableType.LIST && tableViewer != null) {
                createInputForList(collectionElements, values);
                tableViewer.setInput(collectionElements);
            } else if (variable.getType() == VariableType.SCALAR && valueTxt != null) {
                    valueTxt.removeModifyListener(valueTxtModifyListener);
                    valueTxt.setText(variable.getValue());
                    valueTxt.addModifyListener(valueTxtModifyListener);
            }
        }
        isEditedInForm = false;
    }
    
    public void variableNameChanged(final String name) {
        if (nameTxt != null) {
            nameTxt.setText(name);
        }
    }

    public RowExposingTableViewer getTableViewer() {
        return tableViewer;
    }

    private class CollectionEditingSupport extends EditingSupport {

        private final CellEditor editor;

        private final boolean isValueColumn;

        public CollectionEditingSupport(final TableViewer viewer, final boolean isValueColumn) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
            this.isValueColumn = isValueColumn;
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {

            if (element instanceof ElementAddingToken) {
                return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
            }
            return editor;

        }

        @Override
        protected boolean canEdit(final Object element) {
            return true;
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof RobotCollectionElement) {
                if (isValueColumn) {
                    return ((RobotCollectionElement) element).getValue();
                } else {
                    return ((RobotCollectionElement) element).getKey();
                }
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object userInputValue) {
            if (element instanceof RobotCollectionElement) {
                final int ind = collectionElements.indexOf(element);
                if (ind >= 0) {
                    if (isValueColumn) {
                        collectionElements.get(ind).setValue(userInputValue.toString());
                        if (collectionElements.get(ind).getValue() != null
                                && collectionElements.get(ind).getValue() != "") {
                            variableChangedInSection();
                        }
                    } else {
                        collectionElements.get(ind).setKey(userInputValue.toString());
                        if (collectionElements.get(ind).getValue() != null
                                && collectionElements.get(ind).getValue() != "") {
                            variableChangedInSection();
                        }
                    }

                    getViewer().refresh();
                }
            } else if (element instanceof ElementAddingToken) {
                collectionElements.add(createNewCollectionElement(collectionElements.size(), "", ""));
                getViewer().getControl().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        getViewer().refresh();
                        if (element != null) {
                            getViewer().editElement(collectionElements.get(collectionElements.size() - 1), 1);
                        }
                    }
                });
            }
        }
    }

    private class CollectionContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

        @Override
        public Object[] getElements(final Object inputElement) {

            final Object[] elements = ((List<?>) inputElement).toArray();
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("element", true);
            return newElements;
        }

    }

    private class CollectionLabelProvider extends RedCommonLabelProvider {

        private final boolean isValueColumn;

        public CollectionLabelProvider(final boolean isValueColumn) {
            this.isValueColumn = isValueColumn;
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken && (isList || !isValueColumn)) {
                return ((ElementAddingToken) element).getImage();
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RobotCollectionElement) {
                final StyledString label = new StyledString();
                if (isValueColumn) {
                    label.append(((RobotCollectionElement) element).getValue());
                } else {
                    label.append(((RobotCollectionElement) element).getKey());
                }
                return label;
            } else if (element instanceof ElementAddingToken && (isList || !isValueColumn)) {
                return ((ElementAddingToken) element).getStyledText();
            }
            return new StyledString();
        }
    }

}
