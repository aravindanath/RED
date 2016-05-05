/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.TestCasesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.VariablesProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class TestCaseTableValidatorTest {

	private MockReporter reporter;

	@Before
	public void beforeTest() {
		reporter = new MockReporter();
	}

	@Test
	public void emptyTestCaseIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .build();

		final FileValidationContext context = prepareContext();
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
	}

	@Test
	public void emptyTestCaseIsReported_whenCommentedLineIsInside() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  # kw")
                .build();

		final FileValidationContext context = prepareContext();
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(TestCasesProblem.EMPTY_CASE, new ProblemPosition(2, Range.closed(19, 23))));
	}

	@Test
	public void unknownTestCaseSettingIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Unknown]")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(TestCasesProblem.UNKNOWN_TEST_CASE_SETTING, new ProblemPosition(3, Range.closed(26, 35))));
	}

	@Test
	public void duplicatedCaseIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(2, Range.closed(19, 23))),
				new Problem(TestCasesProblem.DUPLICATED_CASE, new ProblemPosition(4, Range.closed(31, 35))));
	}

	@Test
	public void deprecatedKeywordIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newDeprecatedValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.DEPRECATED_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
	}

	@Test
	public void keywordFromNestedLibraryIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw1")
                .appendLine("    kw2")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw1",
                new Path("/res.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "lib", "kw2",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw1",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1), "kw2",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(3, Range.closed(28, 31))),
				new Problem(KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY, new ProblemPosition(4, Range.closed(36, 39))));
	}

	@Test
	public void keywordFromNestedLibraryIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw",
                new Path("/suite.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "lib", "kw",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1, entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void keywordFromLibraryIsNotReported_whenAliasIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    lib.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "library", "lib", "kw",
                new Path("/suite.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void keywordFromLibraryIsNotReported_whenLibraryPrefixIsUsed() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    library.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.REF_LIBRARY, "library", "kw",
                new Path("/suite.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void keywordWithInconsistentNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  kw1")
                .appendLine("  k w1")
                .appendLine("  k_w1")
                .appendLine("  k_w 1")
                .appendLine("  K w1")
                .appendLine("  K_w 1")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw1",
                new Path("/res.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw1",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(5);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
						new ProblemPosition(4, Range.closed(32, 36))),
				new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
						new ProblemPosition(5, Range.closed(39, 43))),
				new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
						new ProblemPosition(6, Range.closed(46, 51))),
				new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
						new ProblemPosition(7, Range.closed(54, 58))),
				new Problem(KeywordsProblem.KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION,
						new ProblemPosition(8, Range.closed(61, 66))));
	}

	@Test
	public void keywordWithAmbiguousNameIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res1", "kw",
                new Path("/res1.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res2", "kw",
                new Path("/res2.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1, entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.AMBIGUOUS_KEYWORD, new ProblemPosition(3, Range.closed(28, 30))));
	}

	@Test
	public void keywordWithAmbiguousNameIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    res1.kw")
                .appendLine("    res2.kw")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res1", "kw",
                new Path("/res1.robot"));
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res2", "kw",
                new Path("/res2.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1, entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void undeclaredVariableIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${var}")
                .appendLine("    kw  ${var2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "var2");
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final Set<String> accessibleVariables = new HashSet<>();
		accessibleVariables.add("${var2}");

		final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(32, 38))));
	}

	@Test
	public void numberVariableIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void variableInComputationIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    kw  ${var-2}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final Set<String> accessibleVariables = new HashSet<>();
		accessibleVariables.add("${var}");

		final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void variableInCommentKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Comment  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn", "Comment",
                new Path("/suite.robot"), "var");

		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("comment",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void variableInSetTestVariableKeywordIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("    Set Test Variable  ${V_ar}")
                .appendLine("    kw  ${var}")
                .build();

        final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn",
                "Set Test Variable", new Path("/suite.robot"), "arg");
        final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.RESOURCE, "res", "kw",
                new Path("/res.robot"), "arg");
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("settestvariable",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1), "kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity2));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}

	@Test
	public void undeclaredVariableAndKeywordInTestCaseSetupAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw1  ${var}")
                .appendLine("  kw")
                .build();

		final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
				new Path("/suite.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(35, 38))),
				new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(40, 46))));
	}
	
	@Test
	public void undeclaredVariableAndKeywordInTestCaseTeardownAreReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Teardown]  kw1  ${var}")
                .appendLine("  kw")
                .build();

		final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
				new Path("/suite.robot"));
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

		final FileValidationContext context = prepareContext(accessibleKws);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
		assertThat(reporter.getReportedProblems()).containsExactly(
				new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 41))),
				new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 49))));
	}

	@Test
	public void declaredVariablesAndKeywordsInTestCaseSettingsAreNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  kw  ${var1}")
                .appendLine("  [Teardown]  kw  ${var2}")
                .appendLine("  ${var2}=  Set Variable  2")
                .build();

		final KeywordEntity entity1 = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"), "arg");
		final KeywordEntity entity2 = newValidationKeywordEntity(KeywordScope.STD_LIBRARY, "BuiltIn", "Set Variable",
                new Path("/suite.robot"), "arg");
		final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity1), "setvariable",
				(Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity2));

		final Set<String> accessibleVariables = new HashSet<>();
		accessibleVariables.add("${var1}");

		final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
		final TestCaseTableValidator validator = new TestCaseTableValidator(context,
				file.findSection(RobotCasesSection.class), reporter);
		validator.validate(null);

		assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
	}
	
    @Test
    public void declaredVariableAsKeywordInTestCaseSetupAndTeardownIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Setup]  ${var}")
                .appendLine("  [Teardown]  ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final Set<String> accessibleVariables = new HashSet<>();
        accessibleVariables.add("${var}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(2);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(3, Range.closed(35, 41))),
                new Problem(GeneralSettingsProblem.VARIABLE_AS_KEYWORD_USAGE_IN_SETTING,
                        new ProblemPosition(4, Range.closed(56, 62))));
    }
	
    @Test
    public void undeclaredKeywordInTestCaseTemplateIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  kw1 ${var}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(KeywordsProblem.UNKNOWN_KEYWORD, new ProblemPosition(3, Range.closed(38, 48))));
    }
	
    @Test
    public void noneInTestTemplateIsNotReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Template]  None")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final FileValidationContext context = prepareContext(accessibleKws);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(0);
    }

    @Test
    public void undeclaredVariableInTestCaseTagsIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test")
                .appendLine("  [Tags]  ${var1}  ${var2}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));

        final Set<String> accessibleVariables = new HashSet<>();
        accessibleVariables.add("${var1}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(3, Range.closed(43, 50))));
    }
    
    @Test
    public void undeclaredVariableInTestCaseTimeoutIsReported() throws CoreException {
        final RobotSuiteFile file = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("test1")
                .appendLine("  [Timeout]  ${var1}")
                .appendLine("  kw")
                .appendLine("test2")
                .appendLine("  [Timeout]  ${var2}")
                .appendLine("  kw")
                .build();

        final KeywordEntity entity = newValidationKeywordEntity(KeywordScope.LOCAL, "suite", "kw",
                new Path("/suite.robot"));
        final ImmutableMap<String, Collection<KeywordEntity>> accessibleKws = ImmutableMap.of("kw",
                (Collection<KeywordEntity>) Lists.<KeywordEntity> newArrayList(entity));
        
        final Set<String> accessibleVariables = new HashSet<>();
        accessibleVariables.add("${var1}");

        final FileValidationContext context = prepareContext(accessibleKws, accessibleVariables);
        final TestCaseTableValidator validator = new TestCaseTableValidator(context,
                file.findSection(RobotCasesSection.class), reporter);
        validator.validate(null);

        assertThat(reporter.getNumberOfReportedProblems()).isEqualTo(1);
        assertThat(reporter.getReportedProblems()).containsExactly(
                new Problem(VariablesProblem.UNDECLARED_VARIABLE_USE, new ProblemPosition(6, Range.closed(70, 77))));
    }
	

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath, final String... args) {
        return new ValidationKeywordEntity(scope, sourceName, name, "", false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor(args));
    }

    private static KeywordEntity newValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String sourceAlias, final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, sourceAlias, false, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
    }

    private static KeywordEntity newDeprecatedValidationKeywordEntity(final KeywordScope scope, final String sourceName,
            final String name, final IPath exposingPath) {
        return new ValidationKeywordEntity(scope, sourceName, name, "", true, exposingPath, 0,
                ArgumentsDescriptor.createDescriptor());
    }

	private static FileValidationContext prepareContext() {
		return prepareContext(new HashMap<String, Collection<KeywordEntity>>());
	}

	private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> map) {
		return prepareContext(createKeywordsCollector(map), new HashSet<String>());
	}

	private static FileValidationContext prepareContext(final Map<String, Collection<KeywordEntity>> map,
			final Set<String> accessibleVariables) {
		return prepareContext(createKeywordsCollector(map), accessibleVariables);
	}

	private static FileValidationContext prepareContext(final AccessibleKeywordsCollector collector,
			final Set<String> accessibleVariables) {
		final ValidationContext parentContext = new ValidationContext(new RobotModel(), RobotVersion.from("0.0"),
				SuiteExecutor.Python, Maps.<String, LibrarySpecification> newHashMap(),
				Maps.<ReferencedLibrary, LibrarySpecification> newHashMap());
		final IFile file = mock(IFile.class);
		when(file.getFullPath()).thenReturn(new Path("/suite.robot"));
		final FileValidationContext context = new FileValidationContext(parentContext, file, collector,
				accessibleVariables);
		return context;
	}

	private static AccessibleKeywordsCollector createKeywordsCollector(
			final Map<String, Collection<KeywordEntity>> map) {
		return new AccessibleKeywordsCollector() {
			@Override
			public Map<String, Collection<KeywordEntity>> collect() {
				return map;
			}
		};
	}
}
