/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationMock;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationValidator.RobotLaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationValidator.RobotLaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RuntimeEnvironmentsMocks;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class RobotLaunchConfigurationValidatorTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationValidatorTest.class.getSimpleName();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    private final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator();

    @Test
    public void whenProjectNameIsEmpty_fatalExceptionIsThrown() {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '' does not exist in workspace.");

        validator.validate(new RobotLaunchConfigurationMock(""));
    }
    
    @Test
    public void whenProjectDoesNotExist_fatalExceptionIsThrown() throws CoreException {
        projectProvider.getProject().delete(true, null);

        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' does not exist in workspace.");

        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsClosed_fatalExceptionIsThrown() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is currently closed.");
        projectProvider.getProject().close(null);

        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_1() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment.");
        final IProject fooProject = projectProvider.getProject();

        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(null);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_2() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage("Project '" + PROJECT_NAME + "' is using invalid Python environment.");
        final IProject fooProject = projectProvider.getProject();

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidPythonEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenProjectIsUsingInvalidEnvironment_fatalExceptionIsThrown_3() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(
                "Project '" + PROJECT_NAME + "' is using invalid Python environment (missing Robot Framework).");
        final IProject fooProject = projectProvider.getProject();

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createInvalidRobotEnvironment();
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenThereAreNoSuitesSpecified_warningExceptionIsThrown() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationException.class);
        thrown.expectMessage("There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed.");

        final IProject fooProject = projectProvider.getProject();

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        validator.validate(new RobotLaunchConfigurationMock(PROJECT_NAME));
    }

    @Test
    public void whenSystemIntepreterIsUsed_warningExceptionIsThrown() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Tests will be launched using 'Python' interpreter as defined in PATH environment variable."));

        final IPath filePath = Path.fromPortableString("file.robot");
        projectProvider.createFile(filePath,
                "*** Test Cases ***", "case1", "  Log  10");

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator();
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        launchConfig.addSuite(filePath.toPortableString(), new ArrayList<String>());
        validator.validate(launchConfig);
    }

    @Test
    public void warningsAreCombinedTogetherInSingleException() throws Exception {
        thrown.expect(RobotLaunchConfigurationValidationException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Tests will be launched using 'Python' interpreter as defined in PATH environment variable.\n"
                        + "There are no suites specified. All suites in '" + PROJECT_NAME + "' will be executed."));

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator();
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.setUsingInterpreterFromProject(false);
        validator.validate(launchConfig);
    }

    @Test
    public void whenSuitesSpecifiedToRunDoesNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers
                .equalTo("Following suites does not exist: /" + PROJECT_NAME + "/file2.robot, /" + PROJECT_NAME
                        + "/suite/dir."));

        final IPath filePath = Path.fromPortableString("file.robot");
        final IProject fooProject = projectProvider.getProject();
        projectProvider.createFile(filePath,
                "*** Test Cases ***", "case1", "  Log  10");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.addSuite(filePath.toPortableString(), new ArrayList<String>());
        launchConfig.addSuite("file2.robot", new ArrayList<String>());
        launchConfig.addSuite("suite/dir", new ArrayList<String>());
        validator.validate(launchConfig);
    }

    @Test
    public void whenTestsSpecifiedInSuiteDoNotExist_fatalExceptionIsThrown() throws Exception, CoreException {
        thrown.expect(RobotLaunchConfigurationValidationFatalException.class);
        thrown.expectMessage(CoreMatchers.equalTo("Following tests does not exist: case4, case5."));

        final IPath filePath = Path.fromPortableString("file.robot");
        final IProject fooProject = projectProvider.getProject();
        projectProvider.createFile(filePath,
                        "*** Test Cases ***", 
                        "case1", "  Log  10", 
                        "case2", "  Log  20",
                        "case3", "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.addSuite(filePath.toPortableString(), newArrayList("case3", "case4", "case5"));
        validator.validate(launchConfig);
    }

    @Test
    public void nothingIsThrown_whenEverythingIsOkWithGivenConfiguration() throws Exception, CoreException {
        final IPath filePath = Path.fromPortableString("file.robot");
        final IProject fooProject = projectProvider.getProject();
        projectProvider.createFile(filePath, 
                        "*** Test Cases ***", 
                        "case1", "  Log  10", 
                        "case2", "  Log  20", 
                        "case3", "  Log  30");

        final RobotRuntimeEnvironment environment = RuntimeEnvironmentsMocks.createValidRobotEnvironment("RF 3.0");
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotModel model = mock(RobotModel.class);
        when(model.createRobotProject(fooProject)).thenReturn(robotProject);
        when(robotProject.getRuntimeEnvironment()).thenReturn(environment);

        final RobotLaunchConfigurationValidator validator = new RobotLaunchConfigurationValidator(model);
        final RobotLaunchConfigurationMock launchConfig = new RobotLaunchConfigurationMock(PROJECT_NAME);
        launchConfig.addSuite(filePath.toPortableString(), newArrayList("case2", "case3"));
        validator.validate(launchConfig);
    }
}
