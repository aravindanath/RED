/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static com.google.common.collect.Lists.transform;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.nattable.edit.DetailCellEditorEntriesControlsSwitcher.Mode;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry.DetailEditorListener;
import org.robotframework.red.nattable.edit.DetailEntriesCollection.DetailWithEntry;

import com.google.common.base.Function;

/**
 * @author Michal Anglart
 *
 */
public class DetailCellEditorEntriesComposite<D> extends Composite {

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final MainControlChooser mainControlChooseCallback;

    private final Mode mode;

    private Composite entriesComposite;
    private final DetailEntriesCollection<D> entries = new DetailEntriesCollection<>();
    private final EntriesChangeListener<D> entriesChangesListener;

    public DetailCellEditorEntriesComposite(final Composite parent, final DetailCellEditorEditingSupport<D> editSupport,
            final Mode mode, final EntriesChangeListener<D> entriesChangesListener,
            final MainControlChooser mainControlChooseCallback) {
        super(parent, SWT.NONE);
        this.editSupport = editSupport;
        this.entriesChangesListener = entriesChangesListener;
        this.mainControlChooseCallback = mainControlChooseCallback;
        this.mode = mode;

        setBackground(getParent().getBackground());
        if (mode == Mode.INLINED) {
            addPaintListener(new PaintListener() {

                @Override
                public void paintControl(final PaintEvent e) {
                    e.gc.drawLine(0, 0, e.width, 0);
                    e.gc.drawLine(e.width - 1, 0, e.width - 1, e.height - 1);
                    e.gc.drawLine(e.width - 1, e.height - 1, 0, e.height - 1);
                    e.gc.drawLine(0, e.height - 1, 0, 0);
                }
            });
            GridLayoutFactory.fillDefaults().spacing(1, 1).extendedMargins(1, 1, 1, 1).applyTo(this);
        } else {
            GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(this);
        }

        createEntriesComposite();
        createTooltipControls();
    }

    DetailEntriesCollection<D> getEntries() {
        return entries;
    }

    private void createEntriesComposite() {
        final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL);
        scrolledComposite.setBackground(getParent().getBackground());
        scrolledComposite.setShowFocusedControl(true);
        scrolledComposite.setExpandHorizontal(true);
        final SelectionAdapter scrollingRefresher = new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                entries.redrawEntries();
            }
        };
        scrolledComposite.getVerticalBar().addSelectionListener(scrollingRefresher);
        scrolledComposite.getVerticalBar().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                scrolledComposite.getVerticalBar().removeSelectionListener(scrollingRefresher);
            }
        });
        GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

        entriesComposite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(entriesComposite);
        entriesComposite.setBackground(ColorsManager.getColor(230, 230, 230));
        GridLayoutFactory.fillDefaults().spacing(1, 1).applyTo(entriesComposite);
        entriesComposite.setSize(entriesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void createDetailEntryControls(final List<D> details) {
        for (final D detail : details) {
            final DetailCellEditorEntry<D> entry = editSupport.createDetailEntry(entriesComposite, detail);
            GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 25).grab(true, false).applyTo(entry);
            entry.setBackground(getParent().getBackground());
            entry.addKeyListener(new EntryKeyPressListener(entry));
            entry.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseUp(final MouseEvent e) {
                    if (entry.isSelected() && mode == Mode.WINDOWED) {
                        entries.openEntryForEdit(entry);
                    } else {
                        entry.select(e.stateMask == 0 || e.stateMask != SWT.CTRL);
                    }
                }
            });
            entry.setEditorListener(new DetailEditorListener() {

                @Override
                public void editorApplied(final String value) {
                    editSupport.setNewValue(detail, value);
                    entry.update(detail);
                }
            });

            entries.add(new DetailWithEntry<>(detail, entry));
        }
        entriesComposite.setSize(entriesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        entriesChangesListener.entriesChanged(entries.getEntries());
    }

    private void createTooltipControls() {
        final Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(sep);

        final Label label = new Label(this, SWT.NONE);
        label.setBackground(getBackground());
        label.setText("Edit details");
        GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(true, false).applyTo(label);
    }

    void setInput(final int column, final int row) {
        entries.disposeEntries();
        createDetailEntryControls(editSupport.getInput(column, row));
    }

    void refresh() {
        entries.disposeEntries();
        createDetailEntryControls(editSupport.getDetailElements());
    }

    void selectFirstEntry() {
        if (!entries.isEmpty()) {
            entries.selectOnlyEntry(0);
        }
    }

    static class EntriesChangeListener<D> {

        void entriesChanged(final List<DetailCellEditorEntry<D>> entries) {
            // override if needed
        }
    }

    static interface MainControlChooser {

        void focusMainControl();
    }

    private class EntryKeyPressListener extends KeyAdapter {

        private final DetailCellEditorEntry<D> entry;

        private EntryKeyPressListener(final DetailCellEditorEntry<D> entry) {
            this.entry = entry;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            if (e.keyCode == SWT.HOME && e.stateMask == 0) {
                entries.selectOnlyFirstEntry();

            } else if (e.keyCode == SWT.HOME && e.stateMask == SWT.SHIFT) {
                entries.selectTillFirstEntry(entry);

            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == 0) {
                if (!entries.isFirst(entry)) {
                    entries.selectOnlyPreviousEntry(entry);
                } else {
                    entry.deselect();
                    mainControlChooseCallback.focusMainControl();
                }
            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.SHIFT) {
                entries.selectPreviousEntry(entry);

            } else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.CTRL) {
                if (!entries.isFirstSelected()) {
                    final List<Integer> indexes = entries.getSelectedIndexes();

                    editSupport.moveLeft(entries.getSelectedDetails());
                    refresh();

                    entries.selectEntries(transform(indexes, precedessor()));
                }
            } else if (e.keyCode == SWT.END && e.stateMask == 0) {
                entries.selectOnlyLastEntry();

            } else if (e.keyCode == SWT.END && e.stateMask == SWT.SHIFT) {
                entries.selectTillLastEntry(entry);

            } else if (e.keyCode == SWT.PAGE_UP && e.stateMask == 0) {
                entries.selectOnlyPreviousEntryJumping(entry);

            } else if (e.keyCode == SWT.PAGE_UP && e.stateMask == SWT.SHIFT) {
                entries.selectPreviousEntryJumping(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == 0) {
                entries.selectOnlyNextEntry(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.SHIFT) {
                entries.selectNextEntry(entry);

            } else if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.CTRL) {
                if (!entries.isLastSelected()) {
                    final List<Integer> indexes = entries.getSelectedIndexes();

                    editSupport.moveRight(entries.getSelectedDetails());
                    refresh();

                    entries.selectEntries(transform(indexes, successor()));
                }
            } else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == 0) {
                entries.selectOnlyNextEntryJumping(entry);

            } else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == SWT.SHIFT) {
                entries.selectNextEntriesJumping(entry);

            } else if (e.keyCode == SWT.ESC) {
                entries.deselectAll();
                mainControlChooseCallback.focusMainControl();

            } else if (e.keyCode == SWT.DEL) {
                final int index = entries.getEntryIndex(entry);
                editSupport.removeDetailElements(entries.getSelectedDetails());
                refresh();
                if (index < entries.size()) {
                    entries.selectOnlyEntry(index);
                }
            } else if (e.keyCode == SWT.CR) {
                entries.openEntryForEdit(entry);

            } else if (e.keyCode == 'a' && e.stateMask == SWT.CTRL) {
                entries.selectAll();
            }
        }
    }

    private static Function<Integer, Integer> successor() {
        return new Function<Integer, Integer>() {

            @Override
            public Integer apply(final Integer number) {
                return number + 1;
            }
        };
    }

    private static Function<Integer, Integer> precedessor() {
        return new Function<Integer, Integer>() {

            @Override
            public Integer apply(final Integer number) {
                return number - 1;
            }
        };
    }
}
