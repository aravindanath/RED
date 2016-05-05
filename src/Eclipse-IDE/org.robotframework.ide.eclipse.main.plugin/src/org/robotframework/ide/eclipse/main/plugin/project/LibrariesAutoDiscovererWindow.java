/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.TreeContentProvider;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscovererWindow extends Dialog {

    private static final String STATUS_ELEMENT_NAME = "Status";

    private static final String SOURCE_ELEMENT_NAME = "Source";

    private static final String IMPORTERS_ELEMENT_NAME = "Importers";

    private static final String ADDITIONAL_INFO_ELEMENT_NAME = "Additional info";

    private static final String ELEMENT_SEPARATOR = ":";

    private TreeViewer discoveredLibrariesViewer;

    private StyledText detailsText;

    private List<RobotDryRunLibraryImport> importedLibraries;

    public LibrariesAutoDiscovererWindow(final Shell parent, final List<RobotDryRunLibraryImport> importedLibraries) {
        super(parent);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
        this.importedLibraries = importedLibraries;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        createMainComposite(container);
        return container;
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Discovering libraries summary");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 550);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    private Composite createMainComposite(final Composite parent) {

        final Composite mainComposite = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(mainComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);

        final Label libsLabel = new Label(mainComposite, SWT.NONE);
        libsLabel.setText("Discovered libraries (" + importedLibraries.size() + "):");

        createLibrariesViewer(mainComposite);

        createDetailsComposite(mainComposite);

        return mainComposite;
    }

    private void createLibrariesViewer(final Composite mainComposite) {
        discoveredLibrariesViewer = new TreeViewer(mainComposite);
        discoveredLibrariesViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults()
                .grab(true, true)
                .minSize(SWT.DEFAULT, 300)
                .applyTo(discoveredLibrariesViewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(discoveredLibrariesViewer.getTree());

        discoveredLibrariesViewer.setContentProvider(new DiscoveredLibrariesViewerContentProvider());
        discoveredLibrariesViewer.setLabelProvider(new DiscoveredLibrariesViewerLabelProvider());

        Collections.sort(importedLibraries, new DiscoveredLibrariesComparator());
        discoveredLibrariesViewer
                .setInput(importedLibraries.toArray(new RobotDryRunLibraryImport[importedLibraries.size()]));

        discoveredLibrariesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final Object selection = ((TreeSelection) event.getSelection()).getFirstElement();
                if (selection != null) {
                    if (selection instanceof DryRunLibraryImportChildElement) {
                        detailsText.setText(selection.toString());
                    } else if (selection instanceof RobotDryRunLibraryImport) {
                        detailsText.setText(convertDryRunLibraryImportToText(selection));
                    } else {
                        detailsText.setText("");
                    }
                }
            }

        });
    }

    private void createDetailsComposite(final Composite mainComposite) {
        final Composite detailsComposite = new Composite(mainComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(detailsComposite);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 100).grab(true, false).applyTo(detailsComposite);

        detailsText = new StyledText(detailsComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
        detailsText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(detailsText);
        GridLayoutFactory.fillDefaults().applyTo(detailsText);
        detailsText.setEditable(false);
        detailsText.setAlwaysShowScrollBars(false);
    }

    private String convertDryRunLibraryImportToText(final Object selection) {
        final StringBuilder libraryImportTxtBuilder = new StringBuilder("");
        for (final Object child : extractDryRunLibraryImportChildren((RobotDryRunLibraryImport) selection)) {
            if (child instanceof DryRunLibraryImportChildElement) {
                libraryImportTxtBuilder.append(child + "\n");
            } else if (child instanceof DryRunLibraryImportListChildElement) {
                libraryImportTxtBuilder.append(((DryRunLibraryImportListChildElement) child).getName() + "\n");
                for (final DryRunLibraryImportChildElement listChild : ((DryRunLibraryImportListChildElement) child)
                        .getList()) {
                    libraryImportTxtBuilder.append(listChild + "\n");
                }
            }
        }
        return libraryImportTxtBuilder.toString();
    }

    private List<Object> extractDryRunLibraryImportChildren(final RobotDryRunLibraryImport libraryImport) {
        final List<Object> children = new ArrayList<>();
        if (libraryImport.getStatus() != null) {
            children.add(
                    new DryRunLibraryImportChildElement(STATUS_ELEMENT_NAME, libraryImport.getStatus().getMessage()));
        }
        if (libraryImport.getSourcePath() != null && !libraryImport.getSourcePath().isEmpty()) {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME, libraryImport.getSourcePath()));
        } else {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME, "Unknown"));
        }
        final List<String> importersPaths = libraryImport.getImportersPaths();
        if (importersPaths.size() == 1) {
            children.add(new DryRunLibraryImportChildElement(IMPORTERS_ELEMENT_NAME, importersPaths.get(0)));
        } else {
            children.add(new DryRunLibraryImportListChildElement(IMPORTERS_ELEMENT_NAME, importersPaths));
        }
        final String additionalInfo = libraryImport.getAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            children.add(new DryRunLibraryImportChildElement(ADDITIONAL_INFO_ELEMENT_NAME, additionalInfo));
        }
        return children;
    }

    private class DiscoveredLibrariesViewerContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return (RobotDryRunLibraryImport[]) inputElement;
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof RobotDryRunLibraryImport) {
                final List<Object> children = extractDryRunLibraryImportChildren(
                        (RobotDryRunLibraryImport) parentElement);
                return children.toArray(new Object[children.size()]);
            } else if (parentElement instanceof DryRunLibraryImportListChildElement) {
                final List<DryRunLibraryImportChildElement> childElementList = ((DryRunLibraryImportListChildElement) parentElement)
                        .getList();
                return childElementList.toArray(new DryRunLibraryImportChildElement[childElementList.size()]);
            }
            return null;
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            if (element instanceof DryRunLibraryImportChildElement) {
                return false;
            }
            return true;
        }

    }

    private class DiscoveredLibrariesViewerLabelProvider extends StyledCellLabelProvider {

        @Override
        public void update(final ViewerCell cell) {
            final StyledString label = getStyledString(cell.getElement());
            cell.setText(label.getString());
            cell.setStyleRanges(label.getStyleRanges());
            cell.setImage(getImage(cell.getElement()));

            super.update(cell);
        }

        private StyledString getStyledString(final Object element) {

            StyledString label = new StyledString("");

            if (element instanceof RobotDryRunLibraryImport) {
                final String name = ((RobotDryRunLibraryImport) element).getName().replaceAll("\\n", "/n");
                label = new StyledString(name);
            } else if (element instanceof DryRunLibraryImportChildElement) {
                final DryRunLibraryImportChildElement libraryImportChildElement = (DryRunLibraryImportChildElement) element;
                final String childElementName = libraryImportChildElement.getName();
                if (childElementName != null && !childElementName.isEmpty()) {
                    label.append(new StyledString(childElementName, new Styler() {

                        @Override
                        public void applyStyles(final TextStyle textStyle) {
                            textStyle.font = getFont(textStyle.font, SWT.BOLD);
                        }
                    }));
                    label.append(" ");
                }
                final String childElementValue = libraryImportChildElement.getValue();
                if (childElementValue != null && !childElementValue.isEmpty()) {
                    label.append(childElementValue);
                }
            } else if (element instanceof DryRunLibraryImportListChildElement) {
                label = new StyledString(((DryRunLibraryImportListChildElement) element).getName(), new Styler() {

                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.font = getFont(textStyle.font, SWT.BOLD);
                    }
                });
            }

            return label;
        }

        private Font getFont(final Font fontToReuse, final int style) {
            final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
            final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
            return FontsManager.getFont(fontDescriptor);
        }

        private Image getImage(final Object element) {
            if (element instanceof RobotDryRunLibraryImport) {
                final RobotDryRunLibraryImport libraryImport = (RobotDryRunLibraryImport) element;
                if (libraryImport.getStatus() == null
                        || libraryImport.getStatus() == DryRunLibraryImportStatus.NOT_ADDED) {
                    return ImagesManager.getImage(RedImages.getFatalErrorImage());
                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.ADDED) {
                    return ImagesManager.getImage(RedImages.getBigSuccessImage());
                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING) {
                    return ImagesManager.getImage(RedImages.getBigWarningImage());
                }
            } else if (element instanceof DryRunLibraryImportChildElement
                    || element instanceof DryRunLibraryImportListChildElement) {
                return ImagesManager.getImage(RedImages.getElementImage());
            }
            return null;
        }
    }

    private static class DiscoveredLibrariesComparator implements Comparator<RobotDryRunLibraryImport> {

        @Override
        public int compare(final RobotDryRunLibraryImport import1, final RobotDryRunLibraryImport import2) {
            final DryRunLibraryImportStatus firstStatus = import1.getStatus();
            final DryRunLibraryImportStatus secStatus = import2.getStatus();
            if (firstStatus == secStatus) {
                return import1.getName().compareToIgnoreCase(import2.getName());
            }
            if (firstStatus == DryRunLibraryImportStatus.ADDED && secStatus != DryRunLibraryImportStatus.ADDED) {
                return -1;
            } else if (firstStatus == DryRunLibraryImportStatus.NOT_ADDED
                    && secStatus != DryRunLibraryImportStatus.NOT_ADDED) {
                return 1;
            } else if (firstStatus == DryRunLibraryImportStatus.ALREADY_EXISTING
                    && secStatus == DryRunLibraryImportStatus.ADDED) {
                return 1;
            } else if (firstStatus == DryRunLibraryImportStatus.ALREADY_EXISTING
                    && secStatus == DryRunLibraryImportStatus.NOT_ADDED) {
                return -1;
            }

            return 0;
        }
    }

    private static class DryRunLibraryImportChildElement {

        private String name;

        private String value;

        public DryRunLibraryImportChildElement(final String name, final String value) {
            if (name != null) {
                this.name = name + ELEMENT_SEPARATOR;
            }
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            if (value == null && name != null) {
                return name;
            }
            if (value != null && name == null) {
                return value;
            }
            if (value == null && name == null) {
                return "";
            }
            return name + " " + value;
        }

    }

    private static class DryRunLibraryImportListChildElement {

        private String name;

        private List<DryRunLibraryImportChildElement> list = new ArrayList<>();

        public DryRunLibraryImportListChildElement(final String name, final List<String> list) {
            if (name != null) {
                this.name = name + ELEMENT_SEPARATOR;
            }
            if (list != null) {
                for (final String listElement : list) {
                    this.list.add(new DryRunLibraryImportChildElement(null, listElement));
                }
            }
        }

        public List<DryRunLibraryImportChildElement> getList() {
            return list;
        }

        public String getName() {
            return name;
        }

    }
}
