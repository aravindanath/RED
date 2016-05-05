/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.List;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class MetadataKeyInSameColumnFixer extends RedSuiteMarkerResolution {

    public MetadataKeyInSameColumnFixer() {
    }

    @Override
    public String getLabel() {
        return "Split Metadata key from setting declaration.";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        Optional<ICompletionProposal> proposal = Optional.absent();
        final Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            final Range<Integer> range = getRange(marker);
            final List<RobotKeywordCall> metadataSettings = section.get().getMetadataSettings();
            for (final RobotKeywordCall robotKeywordCall : metadataSettings) {
                if (range.contains(robotKeywordCall.getDefinitionPosition().getOffset())) {
                    try {
                        proposal = createProposal(document, robotKeywordCall, suiteModel);
                    } catch (final BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return proposal;
    }

    private Optional<ICompletionProposal> createProposal(final IDocument document,
            final RobotKeywordCall robotKeywordCall, final RobotSuiteFile suiteModel) throws BadLocationException {
        final RobotToken metadataDec = robotKeywordCall.getLinkedElement().getDeclaration();
        final String metaText = metadataDec.getRaw().endsWith(":") ? "Metadata:" : "Metadata";
        final int offset = metadataDec.getStartOffset();
        final String correctedMetadata = new StringBuilder().append(metaText)
                .append(getSeparator(suiteModel, offset))
                .toString();
        final ICompletionProposal proposal = new CompletionProposal(correctedMetadata, offset,
                metadataDec.getRaw().length(), offset, ImagesManager.getImage(RedImages.getUserKeywordImage()),
                getLabel(), null, null);
        return Optional.of(proposal);
    }

    private String getSeparator(final RobotSuiteFile suiteModel, final int offset) {
        String separator = "  ";

        final RobotFile fileModel = suiteModel.getLinkedElement();
        final FileFormat fileFormat = fileModel.getParent().getFileFormat();
        if (fileFormat == FileFormat.TSV) {
            separator = "\t";
        } else {
            final Optional<Integer> line = fileModel.getRobotLineIndexBy(offset);
            if (line.isPresent()) {
                final RobotLine robotLine = fileModel.getFileContent().get(line.get());
                final SeparatorType separatorForLine = robotLine.getSeparatorForLine().get();
                if (separatorForLine == SeparatorType.PIPE) {
                    separator = " | ";
                } else {
                    separator = "  ";
                }
            }
        }

        return separator;
    }

    private Range<Integer> getRange(final IMarker marker) {
        try {
            return Range.closed((Integer) marker.getAttribute(IMarker.CHAR_START),
                    (Integer) marker.getAttribute(IMarker.CHAR_END));
        } catch (final CoreException e) {
            throw new IllegalStateException("Given marker should have offsets defined", e);
        }
    }
}
