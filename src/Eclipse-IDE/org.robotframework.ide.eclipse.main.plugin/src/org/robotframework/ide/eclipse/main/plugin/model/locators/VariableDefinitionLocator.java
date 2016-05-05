/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.importer.AVariableImported;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 */
@SuppressWarnings("PMD.GodClass")
public class VariableDefinitionLocator {

    private final IFile file;

    private final RobotModel model;

    public VariableDefinitionLocator(final IFile file) {
        this(file, RedPlugin.getModelManager().getModel());
    }

    public VariableDefinitionLocator(final IFile file, final RobotModel model) {
        this.file = file;
        this.model = model;
    }

    public void locateVariableDefinitionWithLocalScope(final VariableDetector detector, final int offset) {
        final RobotSuiteFile startingFile = model.createSuiteFile(file);

        ContinueDecision shouldContinue = locateInLocalScope(startingFile, detector, offset);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInResourceFiles(PathsResolver.getWorkspaceRelativeResourceFilesPaths(startingFile),
                newHashSet(startingFile.getFile()), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInVariableFiles(detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateGlobalVariables(startingFile, detector);
    }

    public void locateVariableDefinition(final VariableDetector detector) {
        final RobotSuiteFile startingFile = model.createSuiteFile(file);
        ContinueDecision shouldContinue = locateInCurrentFile(startingFile, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInResourceFiles(PathsResolver.getWorkspaceRelativeResourceFilesPaths(startingFile),
                newHashSet(startingFile.getFile()), detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        shouldContinue = locateInVariableFiles(detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return;
        }
        locateGlobalVariables(startingFile, detector);
    }

    private ContinueDecision locateInLocalScope(final RobotSuiteFile file, final VariableDetector detector,
            final int offset) {
        final Optional<? extends RobotElement> element = file.findElement(offset);
        if (element.isPresent() && element.get() instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) element.get();
            final IRobotCodeHoldingElement parent = call.getParent();

            final ContinueDecision shouldContinue = locateInKeywordArguments(file, detector, parent);
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            return locateInPreviousCalls(file, detector, parent, call);
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInKeywordArguments(final RobotSuiteFile file, final VariableDetector detector,
            final IRobotCodeHoldingElement parent) {
        if (parent instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) parent;
            final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
            final AModelElement<?> linkedElement = argumentsSetting == null ? null
                    : argumentsSetting.getLinkedElement();
            if (linkedElement instanceof KeywordArguments) {
                final KeywordArguments args = (KeywordArguments) linkedElement;
                for (final RobotToken token : args.getArguments()) {
                    final ContinueDecision shouldContinue = detector.localVariableDetected(file, token);
                    if (shouldContinue == ContinueDecision.STOP) {
                        return ContinueDecision.STOP;
                    }
                }
            }
            final List<VariableDeclaration> embeddedArguments = keywordDef.getEmbeddedArguments();
            for (final VariableDeclaration declaration : embeddedArguments) {
                final RobotToken variableAsToken = declaration.asToken();
                variableAsToken.setText(EmbeddedKeywordNamesSupport.removeRegex(variableAsToken.getText()));

                final ContinueDecision shouldContinue = detector.localVariableDetected(file, variableAsToken);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInPreviousCalls(final RobotSuiteFile file, final VariableDetector detector,
            final IRobotCodeHoldingElement parent, final RobotKeywordCall call) {
        final List<RobotKeywordCall> children = parent.getChildren();
        final int index = children.indexOf(call);
        
        for (int i = 0; i < index; i++) {
            final AModelElement<?> linkedElement = children.get(i).getLinkedElement();
            if (linkedElement instanceof RobotExecutableRow<?>) {
                final RobotExecutableRow<?> executableRow = (RobotExecutableRow<?>) linkedElement;
                
                for (final VariableDeclaration variableDeclaration : executableRow.buildLineDescription().getCreatedVariables()) {
                    final ContinueDecision shouldContinue = detector.localVariableDetected(file, variableDeclaration.asToken());
                    if (shouldContinue == ContinueDecision.STOP) {
                        return ContinueDecision.STOP;
                    }
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInCurrentFile(final RobotSuiteFile file, final VariableDetector detector) {
        final Optional<RobotVariablesSection> section = file.findSection(RobotVariablesSection.class);
        if (section.isPresent()) {
            for (final RobotVariable var : section.get().getChildren()) {
                final ContinueDecision shouldContinue = detector.variableDetected(file, var);
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        final ContinueDecision shouldContinue = locateInLocalVariableFiles(file, detector);
        if (shouldContinue == ContinueDecision.STOP) {
            return ContinueDecision.STOP;
        }
        return ContinueDecision.CONTINUE;
    }
    
    private ContinueDecision locateInLocalVariableFiles(final RobotSuiteFile file, final VariableDetector detector) {
        for (final VariablesFileImportReference variablesFileImportReference : file.getVariablesFromLocalReferencedFiles()) {
            final String path = variablesFileImportReference.getImportDeclaration()
                    .getPathOrName()
                    .getText()
                    .toString();
            final ReferencedVariableFile localReferencedFile = ReferencedVariableFile.create(path);
            for (final AVariableImported<?> aVariableImported : variablesFileImportReference.getVariables()) {
                final ContinueDecision shouldContinue = detector.varFileVariableDetected(localReferencedFile,
                        aVariableImported.getRobotRepresentation(), aVariableImported.getValue());
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }
    
    private ContinueDecision locateInResourceFiles(final List<IPath> resources, final Set<IFile> alreadyVisited,
            final VariableDetector detector) {
        for (final IPath path : resources) {
            final IResource resourceFile = file.getWorkspace().getRoot().findMember(path);
            if (resourceFile == null || !resourceFile.exists() || resourceFile.getType() != IResource.FILE
                    || alreadyVisited.contains(resourceFile)) {
                continue;
            }

            alreadyVisited.add((IFile) resourceFile);

            final RobotSuiteFile resourceSuiteFile = model.createSuiteFile((IFile) resourceFile);
            final List<IPath> nestedResources = PathsResolver.getWorkspaceRelativeResourceFilesPaths(resourceSuiteFile);
            ContinueDecision result = locateInResourceFiles(nestedResources, alreadyVisited, detector);
            if (result == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
            result = locateInCurrentFile(resourceSuiteFile, detector);
            if (result == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateInVariableFiles(final VariableDetector detector) {
        final List<ReferencedVariableFile> knownParamFiles = model.createRobotProject(file.getProject())
                .getVariablesFromReferencedFiles();
        for (final ReferencedVariableFile knownFile : knownParamFiles) {
            for (final Entry<String, Object> var : knownFile.getVariablesWithProperPrefixes().entrySet()) {
                final ContinueDecision shouldContinue = detector.varFileVariableDetected(knownFile, var.getKey(),
                        var.getValue());
                if (shouldContinue == ContinueDecision.STOP) {
                    return ContinueDecision.STOP;
                }
            }
        }
        return ContinueDecision.CONTINUE;
    }

    private ContinueDecision locateGlobalVariables(final RobotSuiteFile startingFile,
            final VariableDetector detector) {
        final RobotProjectHolder projectHolder = startingFile.getProject().getRobotProjectHolder();
        final List<ARobotInternalVariable<?>> globalVariables = projectHolder.getGlobalVariables();

        for (final ARobotInternalVariable<?> variable : globalVariables) {
            final ContinueDecision shouldContinue = detector.globalVariableDetected(variable.getName(),
                    variable.getValue());
            if (shouldContinue == ContinueDecision.STOP) {
                return ContinueDecision.STOP;
            }
        }
        return ContinueDecision.CONTINUE;
    }

    public interface VariableDetector {

        ContinueDecision localVariableDetected(RobotSuiteFile file, RobotToken variable);

        ContinueDecision variableDetected(RobotSuiteFile file, RobotVariable variable);

        ContinueDecision varFileVariableDetected(ReferencedVariableFile file, String variableName, Object value);

        ContinueDecision globalVariableDetected(String name, Object value);
    }
}
