/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.IRobotFileDumper;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.SettingTableElementsComparator;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TableHeaderComparator;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;
import org.rf.ide.core.testdata.text.write.tables.ISectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.KeywordsSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.SettingsSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.TestCasesSectionTableDumper;
import org.rf.ide.core.testdata.text.write.tables.VariablesSectionTableDumper;

import com.google.common.base.Optional;
import com.google.common.io.Files;

public abstract class ARobotFileDumper implements IRobotFileDumper {

    private final List<ISectionTableDumper> tableDumpers;

    private final DumperHelper aDumpHelper;

    private DumpContext dumpContext;

    public ARobotFileDumper() {
        this.aDumpHelper = new DumperHelper(this);
        this.tableDumpers = new ArrayList<ISectionTableDumper>(
                Arrays.asList(new SettingsSectionTableDumper(aDumpHelper), new VariablesSectionTableDumper(aDumpHelper),
                        new TestCasesSectionTableDumper(aDumpHelper), new KeywordsSectionTableDumper(aDumpHelper)));
    }

    @Override
    public void dump(final File robotFile, final RobotFile model) throws Exception {
        Files.write(dump(model), robotFile, Charset.forName("utf-8"));
    }

    @Override
    public List<RobotLine> dumpToLines(final RobotFile model) {
        return newLines(model);
    }

    @Override
    public String dump(final List<RobotLine> lines) {
        final StringBuilder strLine = new StringBuilder();

        for (final RobotLine line : lines) {
            for (final IRobotLineElement elem : line.getLineElements()) {
                strLine.append(elem.getRaw());
            }

            strLine.append(line.getEndOfLine().getRaw());
        }

        return strLine.toString();
    }

    @Override
    public String dump(final RobotFile model) {
        final List<RobotLine> lines = dumpToLines(model);
        return dump(lines);
    }

    private List<RobotLine> newLines(final RobotFile model) {
        final List<RobotLine> lines = new ArrayList<>(0);

        final SectionBuilder sectionBuilder = new SectionBuilder();
        final List<Section> sections = sectionBuilder.build(model);

        dumpUntilRobotHeaderSection(model, sections, 0, lines);

        final SettingTable settingTable = model.getSettingTable();
        final List<AModelElement<SettingTable>> sortedSettings = sortSettings(settingTable);
        final VariableTable variableTable = model.getVariableTable();
        final List<AModelElement<VariableTable>> sortedVariables = sortVariables(variableTable);

        final TestCaseTable testCaseTable = model.getTestCaseTable();
        final List<AModelElement<TestCaseTable>> sortedTestCases = getTestCases(testCaseTable);
        final KeywordTable keywordTable = model.getKeywordTable();
        final List<AModelElement<KeywordTable>> sortedKeywords = getKeywords(keywordTable);

        final List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>(0);
        headers.addAll(settingTable.getHeaders());
        headers.addAll(variableTable.getHeaders());
        headers.addAll(testCaseTable.getHeaders());
        headers.addAll(keywordTable.getHeaders());
        Collections.sort(headers, new TableHeaderComparator());

        for (final TableHeader<? extends ARobotSectionTable> th : headers) {
            List<AModelElement<ARobotSectionTable>> sorted = null;
            int sectionWithHeader = getSectionWithHeader(sections, th);

            ISectionTableDumper dumperToUse = null;
            for (final ISectionTableDumper dumper : tableDumpers) {
                if (dumper.isServedType(th)) {
                    dumperToUse = dumper;
                    break;
                }
            }

            if (th.getModelType() == ModelType.SETTINGS_TABLE_HEADER) {
                sorted = copySettings(sortedSettings);
            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {
                sorted = copyVariables(sortedVariables);
            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {
                sorted = copyKeywords(sortedKeywords);
            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {
                sorted = copyTestCases(sortedTestCases);
            }

            dumperToUse.dump(model, sections, sectionWithHeader, th, sorted, lines);

            if (th.getModelType() == ModelType.SETTINGS_TABLE_HEADER) {
                sortedSettings.clear();
                sortedSettings.addAll(copyUpSettings(sorted));
            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {
                sortedVariables.clear();
                sortedVariables.addAll(copyUpVariables(sorted));
            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {
                sortedKeywords.clear();
                sortedKeywords.addAll(copyUpKeywords(sorted));
            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {
                sortedTestCases.clear();
                sortedTestCases.addAll(copyUpTestCases(sorted));
            }

            if (sectionWithHeader > -1) {
                dumpUntilRobotHeaderSection(model, sections, sectionWithHeader + 1, lines);
            }
        }

        final List<Section> userSections = filterUserTableHeadersOnly(sections);
        dumpUntilRobotHeaderSection(model, userSections, 0, lines);

        aDumpHelper.addEOFinCaseIsMissing(model, lines);

        return lines;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<ARobotSectionTable>> copySettings(final List<AModelElement<SettingTable>> elems) {
        final List<AModelElement<ARobotSectionTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<ARobotSectionTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<ARobotSectionTable>> copyVariables(final List<AModelElement<VariableTable>> elems) {
        final List<AModelElement<ARobotSectionTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<ARobotSectionTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<ARobotSectionTable>> copyTestCases(final List<AModelElement<TestCaseTable>> elems) {
        final List<AModelElement<ARobotSectionTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<ARobotSectionTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<ARobotSectionTable>> copyKeywords(final List<AModelElement<KeywordTable>> elems) {
        final List<AModelElement<ARobotSectionTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<ARobotSectionTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<SettingTable>> copyUpSettings(List<AModelElement<ARobotSectionTable>> elems) {
        final List<AModelElement<SettingTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<SettingTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<VariableTable>> copyUpVariables(List<AModelElement<ARobotSectionTable>> elems) {
        final List<AModelElement<VariableTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<VariableTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<TestCaseTable>> copyUpTestCases(List<AModelElement<ARobotSectionTable>> elems) {
        final List<AModelElement<TestCaseTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<TestCaseTable>) stE));
        }

        return copied;
    }

    @SuppressWarnings("unchecked")
    private List<AModelElement<KeywordTable>> copyUpKeywords(List<AModelElement<ARobotSectionTable>> elems) {
        final List<AModelElement<KeywordTable>> copied = new ArrayList<>();
        for (final AModelElement<?> stE : elems) {
            copied.add(((AModelElement<KeywordTable>) stE));
        }

        return copied;
    }

    private List<AModelElement<KeywordTable>> getKeywords(final KeywordTable keywordTable) {
        List<AModelElement<KeywordTable>> list = new ArrayList<>();

        for (final UserKeyword uk : keywordTable.getKeywords()) {
            list.add(uk);
        }

        return list;
    }

    private List<AModelElement<TestCaseTable>> getTestCases(final TestCaseTable testCaseTable) {
        List<AModelElement<TestCaseTable>> list = new ArrayList<>();

        for (final TestCase tc : testCaseTable.getTestCases()) {
            list.add(tc);
        }

        return list;
    }

    private List<AModelElement<SettingTable>> sortSettings(final SettingTable settingTable) {
        List<AModelElement<SettingTable>> list = new ArrayList<>();

        list.addAll(settingTable.getDefaultTags());
        list.addAll(settingTable.getDocumentation());
        list.addAll(settingTable.getForceTags());
        list.addAll(settingTable.getSuiteSetups());
        list.addAll(settingTable.getSuiteTeardowns());
        list.addAll(settingTable.getTestSetups());
        list.addAll(settingTable.getTestTeardowns());
        list.addAll(settingTable.getTestTemplates());
        list.addAll(settingTable.getTestTimeouts());
        list.addAll(settingTable.getUnknownSettings());

        list.addAll(settingTable.getMetadatas());
        list.addAll(settingTable.getImports());

        Collections.sort(list, new SettingTableElementsComparator());
        repositionElementsBaseOnList(list, settingTable.getImports());
        repositionElementsBaseOnList(list, settingTable.getMetadatas());

        return list;
    }

    private void repositionElementsBaseOnList(final List<AModelElement<SettingTable>> src,
            final List<? extends AModelElement<SettingTable>> correctors) {
        if (!correctors.isEmpty()) {
            int hitCorrectors = 0;

            AModelElement<SettingTable> currentCorrector = correctors.get(hitCorrectors);
            for (int i = 0; i < src.size(); i++) {
                final AModelElement<SettingTable> m = src.get(i);
                if (correctors.contains(m)) {
                    if (currentCorrector == m) {
                        hitCorrectors++;
                        if (hitCorrectors < correctors.size()) {
                            currentCorrector = correctors.get(hitCorrectors);
                        }
                    } else {
                        if (currentCorrector.getBeginPosition().isNotSet()) {
                            src.add(i, currentCorrector);
                        } else {
                            src.set(i, currentCorrector);
                        }
                        i--;
                    }
                }
            }

            if (hitCorrectors != correctors.size()) {
                throw new IllegalStateException("Not all elements included in output before.");
            }
        }
    }

    private List<AModelElement<VariableTable>> sortVariables(final VariableTable variableTable) {
        List<AModelElement<VariableTable>> list = new ArrayList<>();
        for (final AVariable var : variableTable.getVariables()) {
            list.add(var);
        }

        return list;
    }

    private List<Section> filterUserTableHeadersOnly(final List<Section> sections) {
        List<Section> userSections = new ArrayList<>(0);
        for (final Section section : sections) {
            SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                userSections.add(section);
            }
        }

        return userSections;
    }

    private void dumpUntilRobotHeaderSection(final RobotFile model, final List<Section> sections,
            final int currentSection, final List<RobotLine> outLines) {
        int removedIndex = -1;

        int sectionSize = sections.size();
        for (int sectionId = currentSection; sectionId < sectionSize; sectionId++) {
            final Section section = sections.get(sectionId);
            SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                dumpFromTo(model, section.getStart(), section.getEnd(), outLines);
                removedIndex++;
            } else {
                break;
            }
        }

        for (int i = removedIndex; i > -1; i--) {
            sections.remove(currentSection);
        }
    }

    private void dumpFromTo(final RobotFile model, final FilePosition start, final FilePosition end,
            final List<RobotLine> outLines) {
        boolean meetEnd = false;

        final List<RobotLine> fileContent = model.getFileContent();
        for (final RobotLine line : fileContent) {
            for (final IRobotLineElement elem : line.getLineElements()) {
                final FilePosition elemPos = elem.getFilePosition();
                if (elemPos.isBefore(start)) {
                    continue;
                } else if (elemPos.isSamePlace(start) || elemPos.isSamePlace(end)
                        || (elemPos.isAfter(start) && elemPos.isBefore(end))) {
                    aDumpHelper.updateLine(model, outLines, elem);
                } else {
                    meetEnd = true;
                    break;
                }
            }

            if (meetEnd) {
                break;
            } else {
                final IRobotLineElement endOfLine = line.getEndOfLine();
                final FilePosition endOfLineFP = endOfLine.getFilePosition();
                if (endOfLineFP.isSamePlace(start) || endOfLineFP.isSamePlace(end)
                        || (endOfLineFP.isAfter(start) && endOfLineFP.isBefore(end))) {
                    aDumpHelper.updateLine(model, outLines, endOfLine);
                }
            }
        }
    }

    private int getSectionWithHeader(final List<Section> sections,
            final TableHeader<? extends ARobotSectionTable> theader) {
        int section = -1;
        final int sectionsSize = sections.size();
        for (int sectionId = 0; sectionId < sectionsSize; sectionId++) {
            final Section s = sections.get(sectionId);
            final FilePosition thPos = theader.getDeclaration().getFilePosition();
            if (thPos.isSamePlace(s.getStart()) || (thPos.isAfter(s.getStart()) && thPos.isBefore(s.getEnd()))) {
                section = sectionId;
                break;
            }
        }

        return section;
    }

    public Separator getSeparator(final RobotFile model, final List<RobotLine> lines, final IRobotLineElement lastToken,
            final IRobotLineElement currentToken) {
        Separator sep = null;
        FilePosition fp = lastToken.getFilePosition();
        FilePosition fpTok = currentToken.getFilePosition();

        IRobotLineElement tokenToSearch = null;
        final int offset;
        if (fpTok.isNotSet()) {
            if (fp.isNotSet()) {
                tokenToSearch = lastToken;
                offset = -1;
            } else {
                tokenToSearch = lastToken;
                offset = fp.getOffset();
            }
        } else {
            tokenToSearch = currentToken;
            offset = fpTok.getOffset();
        }

        final RobotLine line;
        if (offset > -1) {
            line = model.getFileContent().get(model.getRobotLineIndexBy(offset).get());
        } else {
            if (!lines.isEmpty()) {
                line = lines.get(lines.size() - 1);
            } else {
                line = new RobotLine(0, model);
            }
        }

        final List<IRobotLineElement> elems = line.getLineElements();
        final Optional<Integer> tokenPos = line.getElementPositionInLine(tokenToSearch);
        if (tokenPos.isPresent()) {
            Integer tokPos = tokenPos.get();
            for (int index = tokPos - 1; index >= 0; index--) {
                IRobotLineElement elem = elems.get(index);
                if (elem instanceof RobotToken) {
                    break;
                } else if (elem instanceof Separator) {
                    sep = (Separator) elem;
                    break;
                } else {
                    continue;
                }
            }

            if (sep != null) {
                final Optional<SeparatorType> separatorForLine = line.getSeparatorForLine();
                if (separatorForLine.isPresent()) {
                    if (sep.getTypes().get(0) != separatorForLine.get()) {
                        if (separatorForLine.get() == SeparatorType.PIPE) {
                            List<Separator> seps = new ArrayList<>(0);
                            for (final IRobotLineElement e : elems) {
                                if (e instanceof Separator) {
                                    seps.add((Separator) e);
                                }
                            }

                            if (seps.size() > 1) {
                                sep = seps.get(seps.size() - 1);
                            } else {
                                sep = new Separator();
                                sep.setRaw(" | ");
                                sep.setText(" | ");
                                sep.setType(SeparatorType.PIPE);
                            }
                        }
                    }
                }
            }
        }

        if (sep == null) {
            sep = Separator.matchSeparator(dumpContext.getPreferedSeparator());
        }

        if (sep == null) {
            sep = getSeparatorDefault();
        }

        return sep;
    }

    public void setContext(final DumpContext ctx) {
        this.dumpContext = ctx;
    }

    protected abstract Separator getSeparatorDefault();
}
