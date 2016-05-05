/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RobotSettingsSection extends RobotSuiteFileSection implements IRobotCodeHoldingElement {

    public static final String SECTION_NAME = "Settings";

    RobotSettingsSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotSetting createSetting(final String name, final String comment, final String... args) {
        RobotSetting setting;
        if (name.equals(SettingsGroup.METADATA.getName())) {
            setting = new RobotSetting(this, SettingsGroup.METADATA, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.LIBRARIES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.RESOURCES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.VARIABLES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, newArrayList(args), comment);
        } else {
            setting = new RobotSetting(this, SettingsGroup.NO_GROUP, name, newArrayList(args), comment);
        }
        elements.add(setting);
        return setting;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordCall> getChildren() {
        return (List<RobotKeywordCall>) super.getChildren();
    }

    public List<RobotKeywordCall> getMetadataSettings() {
        return getSettingsFromGroup(SettingsGroup.METADATA);
    }

    public List<RobotKeywordCall> getResourcesSettings() {
        return getSettingsFromGroup(SettingsGroup.RESOURCES);
    }
    
    public List<RobotKeywordCall> getVariablesSettings() {
        return getSettingsFromGroup(SettingsGroup.VARIABLES);
    }

    public List<RobotKeywordCall> getImportSettings() {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {
            @Override
            public boolean apply(final RobotKeywordCall element) {
                return SettingsGroup.getImportsGroupsSet()
                                .contains((((RobotSetting) element).getGroup()));
            }
        }));
    }

    private List<RobotKeywordCall> getSettingsFromGroup(final SettingsGroup group) {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {
            @Override
            public boolean apply(final RobotKeywordCall element) {
                return (((RobotSetting) element).getGroup() == group);
            }
        }));
    }

    public RobotSetting getSetting(final String name) {
        for (final RobotKeywordCall setting : getChildren()) {
            if (name.equals(setting.getName())) {
                return (RobotSetting) setting;
            }
        }
        return null;
    }

    public List<IPath> getResourcesPaths() {
        final List<RobotKeywordCall> resources = getResourcesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : resources) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }

    public List<IPath> getVariablesPaths() {
        final List<RobotKeywordCall> variables = getVariablesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : variables) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }

    @Override
    public void link(final ARobotSectionTable table) {
        super.link(table);

        final SettingTable settingsTable = (SettingTable) sectionTable;
        
        for (final Metadata metadataSetting : settingsTable.getMetadatas()) {
            final String name = metadataSetting.getDeclaration().getText().toString();
            final RobotToken metadataKey = metadataSetting.getKey();
            final List<String> args = newArrayList();
            if (metadataKey != null) {
                args.add(metadataKey.getText().toString());
            }
            args.addAll(Lists.transform(metadataSetting.getValues(), TokenFunctions.tokenToString()));
            final RobotSetting setting = new RobotSetting(this, SettingsGroup.METADATA, name, args, "");
            setting.link(metadataSetting);
            elements.add(setting);
        }
        for (final AImported importSetting : settingsTable.getImports()) {
            if (importSetting instanceof LibraryImport) {

                final LibraryImport libraryImport = (LibraryImport) importSetting;

                final String name = libraryImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = libraryImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }
                args.addAll(Lists.transform(libraryImport.getArguments(), TokenFunctions.tokenToString()));

                final RobotSetting setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, args, "");
                setting.link(libraryImport);
                elements.add(setting);
            } else if (importSetting instanceof ResourceImport) {

                final ResourceImport resourceImport = (ResourceImport) importSetting;

                final String name = resourceImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = resourceImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }

                final RobotSetting setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, args, "");
                setting.link(resourceImport);
                elements.add(setting);
            } else if (importSetting instanceof VariablesImport) {

                final VariablesImport variablesImport = (VariablesImport) importSetting;

                final String name = variablesImport.getDeclaration().getText().toString();
                final RobotToken pathOrName = variablesImport.getPathOrName();
                final List<String> args = newArrayList();
                if (pathOrName != null) {
                    args.add(pathOrName.getText().toString());
                }
                args.addAll(Lists.transform(variablesImport.getArguments(), TokenFunctions.tokenToString()));

                final RobotSetting setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, args, "");
                setting.link(variablesImport);
                elements.add(setting);
            }
        }
        for (final SuiteDocumentation documentationSetting : settingsTable.getDocumentation()) {
            final String name = documentationSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(documentationSetting.getDocumentationText(), TokenFunctions.tokenToString()));
            final RobotSetting setting = new RobotSetting(this, name, args, "");
            setting.link(documentationSetting);
            elements.add(setting);
        }
        for (final AKeywordBaseSetting<?> keywordSetting : getKeywordBasedSettings(settingsTable)) {
            final String name = keywordSetting.getDeclaration().getText().toString();
            final RobotToken settingKeywordName = keywordSetting.getKeywordName();
            final List<String> args = newArrayList();
            if (settingKeywordName != null) {
                args.add(settingKeywordName.getText().toString());
            }
            args.addAll(Lists.transform(keywordSetting.getArguments(), TokenFunctions.tokenToString()));
            final RobotSetting setting = new RobotSetting(this, name, args, "");
            setting.link(keywordSetting);
            elements.add(setting);
        }
        for (final ATags<?> tagSetting : getTagsSettings(settingsTable)) {
            final String name = tagSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(tagSetting.getTags(), TokenFunctions.tokenToString()));
            final RobotSetting setting = new RobotSetting(this, name, args, "");
            setting.link(tagSetting);
            elements.add(setting);
        }
        for (final TestTemplate templateSetting : settingsTable.getTestTemplates()) {
            final String name = templateSetting.getDeclaration().getText().toString();
            final RobotToken templateKeyword = templateSetting.getKeywordName();
            final List<String> args = newArrayList();
            if (templateKeyword != null) {
                args.add(templateKeyword.getText().toString());
            }
            final RobotSetting setting = new RobotSetting(this, name, args, "");
            setting.link(templateSetting);
            elements.add(setting);
        }
        for (final TestTimeout timeoutSetting : settingsTable.getTestTimeouts()) {
            final String name = timeoutSetting.getDeclaration().getText().toString();
            final RobotToken timeout = timeoutSetting.getTimeout();
            final List<String> args = newArrayList();
            if (timeout != null) {
                args.add(timeout.getText().toString());
            }
            args.addAll(Lists.transform(timeoutSetting.getMessageArguments(), TokenFunctions.tokenToString()));
            final RobotSetting setting = new RobotSetting(this, name, args, "");
            setting.link(timeoutSetting);
            elements.add(setting);
        }
    }

    private static List<? extends AKeywordBaseSetting<?>> getKeywordBasedSettings(final SettingTable settingTable) {
        final List<AKeywordBaseSetting<?>> elements = newArrayList();
        elements.addAll(settingTable.getSuiteSetups());
        elements.addAll(settingTable.getSuiteTeardowns());
        elements.addAll(settingTable.getTestSetups());
        elements.addAll(settingTable.getTestTeardowns());
        return elements;
    }

    private static List<? extends ATags<?>> getTagsSettings(final SettingTable settingTable) {
        final List<ATags<?>> elements = newArrayList();
        elements.addAll(settingTable.getForceTags());
        elements.addAll(settingTable.getDefaultTags());
        return elements;
    }
}
