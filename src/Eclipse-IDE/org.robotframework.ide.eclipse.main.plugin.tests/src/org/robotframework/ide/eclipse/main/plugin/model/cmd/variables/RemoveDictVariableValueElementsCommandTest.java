/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.RemoveDictVariableValueElementsCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand.CommandExecutionException;

public class RemoveDictVariableValueElementsCommandTest {

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromScalar() {
        final RobotVariable variable = createVariables().get(0);

        final RemoveDictVariableValueElementsCommand command = new RemoveDictVariableValueElementsCommand(variable,
                newArrayList(new DictionaryKeyValuePair(new RobotToken(), new RobotToken(), new RobotToken())));
        command.execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromScalarAsList() {
        final RobotVariable variable = createVariables().get(1);

        final RemoveDictVariableValueElementsCommand command = new RemoveDictVariableValueElementsCommand(variable,
                newArrayList(new DictionaryKeyValuePair(new RobotToken(), new RobotToken(), new RobotToken())));
        command.execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromList() {
        final RobotVariable variable = createVariables().get(2);

        final RemoveDictVariableValueElementsCommand command = new RemoveDictVariableValueElementsCommand(variable,
                newArrayList(new DictionaryKeyValuePair(new RobotToken(), new RobotToken(), new RobotToken())));
        command.execute();
    }

    @Test(expected = CommandExecutionException.class)
    public void exceptionIsThrown_whenTryingToRemoveElementsFromInvalid() {
        final RobotVariable variable = createVariables().get(4);

        final RemoveDictVariableValueElementsCommand command = new RemoveDictVariableValueElementsCommand(variable,
                newArrayList(new DictionaryKeyValuePair(new RobotToken(), new RobotToken(), new RobotToken())));
        command.execute();
    }

    @Test
    public void dictionaryEntryAreRemovedAndEventBrokerSendsEvent() {
        final RobotVariable variable = createVariables().get(3);

        final Collection<DictionaryKeyValuePair> elements = newArrayList(
                ((DictionaryVariable) variable.getLinkedElement()).getItems().subList(1, 3));

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final RemoveDictVariableValueElementsCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new RemoveDictVariableValueElementsCommand(variable, elements));
        command.execute();
        
        assertThat(variable.getValue()).isEqualTo("{a = 1, d = 4}");
        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    private static List<RobotVariable> createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3")
                .appendLine("&{dict}  a=1  b=2  c=3  d=4")
                .appendLine("invalid}  1  2  3")
                .build();
        final RobotVariablesSection varSection = model.findSection(RobotVariablesSection.class).get();
        return varSection.getChildren();
    }
}
