/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;
import org.robotframework.red.swt.LabelsMeasurer;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 *
 */
class ListVariableDetailCellEditorEntry extends DetailCellEditorEntry<RobotToken> {

    private String text;

    private String indexText;

    private Text textEdit;

    private ControlDecoration decoration;

    ListVariableDetailCellEditorEntry(final Composite parent, final Color hoverColor,
            final Color selectionColor) {
        super(parent, hoverColor, selectionColor);

        addPaintListener(new ListElementPainter());
        GridLayoutFactory.fillDefaults().extendedMargins(0, HOVER_BLOCK_WIDTH, 0, 0).applyTo(this);
    }

    @Override
    public void openForEditing() {
        super.openForEditing();

        textEdit = new Text(this, SWT.BORDER);
        textEdit.setText(text);
        textEdit.setSelection(text.length());
        textEdit.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                commitEdit();
            }
        });
        textEdit.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(final TraverseEvent e) {
                if (e.keyCode == SWT.ESC) {
                    cancelEdit();
                } else if (e.keyCode == SWT.CR) {
                    commitEdit();
                }
            }
        });
        textEdit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                rescheduleValidation();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).indent(calculateControlIndent(), 2).applyTo(textEdit);
        layout();

        select(true);

        textEdit.setFocus();
    }

    @Override
    protected void validate() {
        final String value = textEdit.getText();
        if (value.contains("  ")) {
            blockClosing();

            textEdit.setForeground(ColorsManager.getColor(255, 0, 0));
            decoration = new ControlDecoration(textEdit, SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText("Single list entry cannot contain two spaces");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                    .getImage());
        } else {
            unblockClosing();

            textEdit.setForeground(getForeground());
            if (decoration != null) {
                decoration.dispose();
                decoration = null;
            }
        }
    }

    private int calculateControlIndent() {
        final GC gc = new GC(this);
        final int indexLen = gc.textExtent(indexText).x;
        gc.dispose();
        final int indent = indexLen + 2 * SPACING_AROUND_LINE + LINE_WIDTH;
        return indent;
    }

    @Override
    protected RobotToken createNewValue() {
        final RobotToken newValue = new RobotToken();
        newValue.setRaw(textEdit.getText());
        newValue.setText(textEdit.getText());
        return newValue;
    }

    @Override
    protected void closeEditing() {
        super.closeEditing();

        if (textEdit != null && !textEdit.isDisposed()) {
            textEdit.dispose();
            textEdit = null;
        }
        redraw();
    }

    @Override
    public void update(final RobotToken detail) {
        text = detail.getText();
        setToolTipText(text);

        redraw();
    }

    void setIndex(final int allElements, final int index) {
        final int maxElementLength = (int) Math.ceil(Math.log10(allElements));
        indexText = "[" + Strings.padStart(Integer.toString(index), maxElementLength, '0') + "]";
    }

    private class ListElementPainter extends EntryControlPainter {

        @Override
        protected void paintForeground(final int width, final int height, final GC bufferGC) {
            int x = 3;

            final Color fgColor = bufferGC.getForeground();
            if (isHovered()) {
                bufferGC.setForeground(ColorsManager.getColor(120, 180, 170));
            } else {
                bufferGC.setForeground(ColorsManager.getColor(210, 210, 210));
            }
            bufferGC.drawText(indexText, x, 4);

            final int indexLabelWidth = bufferGC.textExtent(indexText).x;
            x += indexLabelWidth + SPACING_AROUND_LINE;

            bufferGC.setLineWidth(LINE_WIDTH);
            bufferGC.drawLine(x, 0, x, height);

            bufferGC.setForeground(fgColor);
            x += SPACING_AROUND_LINE + LINE_WIDTH;

            final int limit = width - 10 - x;
            if (bufferGC.textExtent(text).x < limit) {
                bufferGC.drawText(text, x, 4);
            } else {
                // text is too long to be drawn; we will add ... suffix and will look for
                // longest possible prefix which will fit;
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(LabelsMeasurer.cutTextToRender(bufferGC, text, limit - suffixLength) + suffix, x, 4);
            }
        }
    }
}
