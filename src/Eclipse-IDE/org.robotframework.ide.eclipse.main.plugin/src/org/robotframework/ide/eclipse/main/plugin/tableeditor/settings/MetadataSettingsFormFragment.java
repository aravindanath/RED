/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshGeneralSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FilterSwitchRequest;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.nattable.NewElementsCreator;
import org.robotframework.red.nattable.RedNattableDataProvidersFactory;
import org.robotframework.red.nattable.RedNattableLayersFactory;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;
import org.robotframework.red.nattable.configs.AlternatingRowsStyleConfiguration;
import org.robotframework.red.nattable.configs.ColumnHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.GeneralTableStyleConfiguration;
import org.robotframework.red.nattable.configs.HeaderSortConfiguration;
import org.robotframework.red.nattable.configs.HoveredCellStyleConfiguration;
import org.robotframework.red.nattable.configs.RedTableEditConfiguration;
import org.robotframework.red.nattable.configs.RowHeaderStyleConfiguration;
import org.robotframework.red.nattable.configs.SelectionStyleConfiguration;
import org.robotframework.red.nattable.painter.SearchMatchesTextPainter;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Supplier;

public class MetadataSettingsFormFragment implements ISectionFormFragment, ISettingsFormFragment {

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedFormToolkit toolkit;

    private HeaderFilterMatchesCollection matches;

    private Section metadataSection;

    private NatTable table;

    private MetadataSettingsDataProvider dataProvider;

    private ISortModel sortModel;

    private RowSelectionProvider<Object> selectionProvider;

    private SelectionLayerAccessor selectionLayerAccessor;

    @Override
    public ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    @Override
    public SelectionLayerAccessor getSelectionLayerAccessor() {
        return selectionLayerAccessor;
    }

    @Override
    public NatTable getTable() {
        return table;
    }

    @Override
    public void initialize(final Composite parent) {
        metadataSection = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        metadataSection.setText("Metadata");
        metadataSection.setExpanded(false);
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(metadataSection);
        Sections.switchGridCellGrabbingOnExpansion(metadataSection);
        Sections.installMaximazingPossibility(metadataSection);

        setupNatTable(metadataSection);
    }

    private void setupNatTable(final Composite parent) {

        final TableTheme theme = TableThemes.getTheme(parent.getBackground().getRGB());

        final ConfigRegistry configRegistry = new ConfigRegistry();

        final RedNattableDataProvidersFactory dataProvidersFactory = new RedNattableDataProvidersFactory();
        final RedNattableLayersFactory factory = new RedNattableLayersFactory();

        // data providers
        dataProvider = new MetadataSettingsDataProvider(commandsStack, getSection());
        final IDataProvider columnHeaderDataProvider = dataProvidersFactory.createColumnHeaderDataProvider("Metadata",
                "Value", "Comment");
        final IDataProvider rowHeaderDataProvider = dataProvidersFactory.createRowHeaderDataProvider(dataProvider);

        // body layers
        final DataLayer bodyDataLayer = factory.createDataLayer(dataProvider);
        final GlazedListsEventLayer<RobotKeywordCall> bodyEventLayer = factory
                .createGlazedListEventsLayer(bodyDataLayer, dataProvider.getSortedList());
        final HoverLayer bodyHoverLayer = factory.createHoverLayer(bodyEventLayer);
        final SelectionLayer bodySelectionLayer = factory.createSelectionLayer(theme, bodyHoverLayer);
        final ViewportLayer bodyViewportLayer = factory.createViewportLayer(bodySelectionLayer);

        // column header layers
        final DataLayer columnHeaderDataLayer = factory.createColumnHeaderDataLayer(columnHeaderDataProvider);
        final ColumnHeaderLayer columnHeaderLayer = factory.createColumnHeaderLayer(columnHeaderDataLayer,
                bodySelectionLayer, bodyViewportLayer);
        final SortHeaderLayer<RobotKeywordCall> columnHeaderSortingLayer = factory.createSortingColumnHeaderLayer(
                columnHeaderDataLayer, columnHeaderLayer, dataProvider.getPropertyAccessor(), configRegistry,
                dataProvider.getSortedList());

        // row header layers
        final RowHeaderLayer rowHeaderLayer = factory.createRowsHeaderLayer(bodySelectionLayer, bodyViewportLayer,
                rowHeaderDataProvider);

        // corner layer
        final ILayer cornerLayer = factory.createCornerLayer(columnHeaderDataProvider, columnHeaderSortingLayer,
                rowHeaderDataProvider, rowHeaderLayer);

        // combined grid layer
        final GridLayer gridLayer = factory.createGridLayer(bodyViewportLayer, columnHeaderSortingLayer, rowHeaderLayer,
                cornerLayer);
        gridLayer.addConfiguration(new RedTableEditConfiguration<>(fileModel, newElementsCreator()));

        table = createTable(parent, theme, gridLayer, configRegistry);

        bodyViewportLayer.registerCommandHandler(new MoveCellSelectionCommandHandler(bodySelectionLayer,
                new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, table),
                new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, table)));

        sortModel = columnHeaderSortingLayer.getSortModel();
        selectionProvider = new RowSelectionProvider<>(bodySelectionLayer, dataProvider, false);
        selectionLayerAccessor = new SelectionLayerAccessor(bodySelectionLayer);

        // tooltips support
        new NatTableContentTooltip(table);

        metadataSection.setClient(table);
    }

    private NatTable createTable(final Composite parent, final TableTheme theme, final GridLayer gridLayer,
            final ConfigRegistry configRegistry) {
        final int style = SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.V_SCROLL | SWT.H_SCROLL;
        final NatTable table = new NatTable(parent, style, gridLayer, false);
        table.setConfigRegistry(configRegistry);
        table.setLayerPainter(
                new NatGridLayerPainter(table, theme.getGridBorderColor(), RedNattableLayersFactory.ROW_HEIGHT));
        table.setBackground(theme.getBodyBackgroundOddRowBackground());
        table.setForeground(parent.getForeground());

        addCustomStyling(table, theme);

        // sorting
        table.addConfiguration(new HeaderSortConfiguration());
        table.addConfiguration(new MetadataSettingsTableSortingConfiguration());

        // popup menus
        table.addConfiguration(new MetadataSettingsTableMenuConfiguration(site, table, selectionProvider));

        table.configure();

        table.addFocusListener(new SettingsTableFocusListener(
                "org.robotframework.ide.eclipse.tableeditor.settings.metadata.context", site));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        return table;
    }

    private void addCustomStyling(final NatTable table, final TableTheme theme) {
        final GeneralTableStyleConfiguration tableStyle = new GeneralTableStyleConfiguration(theme,
                new SearchMatchesTextPainter(new Supplier<HeaderFilterMatchesCollection>() {

                    @Override
                    public HeaderFilterMatchesCollection get() {
                        return matches;
                    }
                }, Stylers.Common.MATCH_STYLER));

        table.addConfiguration(tableStyle);
        table.addConfiguration(new HoveredCellStyleConfiguration(theme));
        table.addConfiguration(new ColumnHeaderStyleConfiguration(theme));
        table.addConfiguration(new RowHeaderStyleConfiguration(theme));
        table.addConfiguration(new AlternatingRowsStyleConfiguration(theme));
        table.addConfiguration(new SelectionStyleConfiguration(theme, table.getFont()));
        table.addConfiguration(new AddingElementStyleConfiguration(theme, fileModel.isEditable()));
    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    @Override
    public void invokeSaveAction() {
        onSave();
    }

    @Persist
    public void onSave() {
        final ICellEditor cellEditor = table.getActiveCellEditor();
        if (cellEditor != null && !cellEditor.isClosed()) {
            final boolean commited = cellEditor.commit(MoveDirectionEnum.NONE);
            if (!commited) {
                cellEditor.close();
            }
        }
        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                setFocus();
            }
        });
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    private RobotSettingsSection getSection() {
        return fileModel.findSection(RobotSettingsSection.class).orNull();
    }

    public void revealSetting(final RobotKeywordCall setting) {
        Sections.maximizeChosenSectionAndMinimalizeOthers(metadataSection);

        if (dataProvider.isFilterSet() && !dataProvider.isPassingThroughFilter(setting)) {
            final String topic = RobotSuiteEditorEvents.FORM_FILTER_SWITCH_REQUEST_TOPIC + "/"
                    + RobotSettingsSection.SECTION_NAME;
            eventBroker.send(topic, new FilterSwitchRequest(RobotSettingsSection.SECTION_NAME, ""));
        }
        selectionProvider.setSelection(new StructuredSelection(new Object[] { setting }));
        setFocus();
    }

    public void clearSettingsSelection() {
        selectionProvider.setSelection(StructuredSelection.EMPTY);
    }

    private NewElementsCreator<RobotElement> newElementsCreator() {
        return new NewElementsCreator<RobotElement>() {

            @Override
            public RobotElement createNew() {
                dataProvider.setMatches(null);
                final RobotSettingsSection section = dataProvider.getInput();
                commandsStack.execute(new CreateFreshGeneralSettingCommand(section, "Metadata", newArrayList("data")));
                SwtThread.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        table.doCommand(new EditSelectionCommand(table, table.getConfigRegistry()));
                    }
                });
                return section.getChildren().get(section.getChildren().size() - 1);
            }
        };
    }

    class MetadataSettingsTableSortingConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(final IConfigRegistry configRegistry) {
            configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                    DefaultComparator.getInstance(), DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
            configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                    DefaultComparator.getInstance(), DisplayMode.NORMAL,
                    ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);
            configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new NullComparator(),
                    DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
        }

    }

    class MetadataSettingsTableMenuConfiguration extends AbstractUiBindingConfiguration {

        private final Menu menu;

        public MetadataSettingsTableMenuConfiguration(final IEditorSite site, final NatTable table,
                final ISelectionProvider selectionProvider) {
            final String menuId = "org.robotframework.ide.eclipse.editor.page.settings.metadata.contextMenu";

            final MenuManager manager = new MenuManager("Robot suite editor metadata settings context menu", menuId);
            this.menu = manager.createContextMenu(table);
            table.setMenu(menu);

            site.registerContextMenu(menuId, manager, selectionProvider, false);
        }

        @Override
        public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
            uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, null, 3),
                    new PopupMenuAction(menu));
        }
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final MetadataSettingsMatchesCollection settingsMatches = new MetadataSettingsMatchesCollection();
        final List<RobotElement> settings = new ArrayList<>();
        settings.addAll(dataProvider.getInput().getMetadataSettings());
        settingsMatches.collect(settings, filter);
        return settingsMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotSettingsSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        this.matches = matches;
        dataProvider.setMatches(matches);
        table.refresh();
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && dataProvider.getInput() == null) {
            refreshTable();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && dataProvider.getInput() != null) {
            final ICellEditor activeCellEditor = table.getActiveCellEditor();
            if (activeCellEditor != null && !activeCellEditor.isClosed()) {
                activeCellEditor.close();
            }
            
            refreshTable();
            selectionLayerAccessor.getSelectionLayer().clear();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSettingDetailsChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_KEYWORD_CALL_DETAIL_CHANGE_ALL) final RobotSetting setting) {
        if (setting.getSuiteFile() == fileModel) {
            table.update();
            table.refresh();
            setDirty();
        }
    }
    
    @Inject
    @Optional
    private void whenSettingIsAdded(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_ADDED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
        whenSettingIsAddedOrRemoved(section);
    }

    @Inject
    @Optional
    private void whenSettingIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_REMOVED) final RobotSuiteFileSection section) {
        whenSettingIsAddedOrRemoved(section);
    }
    
    @Inject
    @Optional
    private void whenSettingIsMoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_MOVED) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
        }
        final ISelection oldSelection = selectionProvider.getSelection();
        whenSettingIsAddedOrRemoved(section);
        selectionProvider.setSelection(oldSelection);
    }

    private void whenSettingIsAddedOrRemoved(final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            sortModel.clear();
            refreshTable();
            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            final RobotSuiteFile suite = change.getElement() instanceof RobotSuiteFile
                    ? (RobotSuiteFile) change.getElement() : null;
            if (suite == fileModel) {
                refreshTable();
            }
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            refreshTable();
        }
    }

    private void refreshTable() {
        dataProvider.setInput(getSection());
        table.refresh();
    }
}
