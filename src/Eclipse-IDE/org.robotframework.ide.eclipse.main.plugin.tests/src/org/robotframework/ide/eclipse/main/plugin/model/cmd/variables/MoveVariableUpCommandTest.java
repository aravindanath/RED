/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.MoveVariableUpCommand;

public class MoveVariableUpCommandTest {

    @Test
    public void nothingHappens_whenFirstVariableIsTriedToBeMovedUp() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(0);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveVariableUpCommand(variableToMove))
                .execute();

        assertThat(varSection.getChildren()).hasSize(5);
        assertThat(varSection.getChildren().indexOf(variableToMove)).isEqualTo(0);

        final VariableTable varTable = varSection.getLinkedElement();
        assertThat(varTable.getVariables().get(0).getName()).isEqualTo("scalar");
        assertThat(varTable.getVariables().get(1).getName()).isEqualTo("scalar_as_list");
        assertThat(varTable.getVariables().get(2).getName()).isEqualTo("list");
        assertThat(varTable.getVariables().get(3).getName()).isEqualTo("dict");
        assertThat(varTable.getVariables().get(4).getName()).isEqualTo("invalid}");

        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void lastVariableIsMovedUpProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(varSection.getChildren().size() - 1);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveVariableUpCommand(variableToMove))
                .execute();

        assertThat(varSection.getChildren()).hasSize(5);
        assertThat(varSection.getChildren().indexOf(variableToMove)).isEqualTo(3);

        final VariableTable varTable = varSection.getLinkedElement();
        assertThat(varTable.getVariables().get(0).getName()).isEqualTo("scalar");
        assertThat(varTable.getVariables().get(1).getName()).isEqualTo("scalar_as_list");
        assertThat(varTable.getVariables().get(2).getName()).isEqualTo("list");
        assertThat(varTable.getVariables().get(3).getName()).isEqualTo("invalid}");
        assertThat(varTable.getVariables().get(4).getName()).isEqualTo("dict");

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_MOVED, varSection);
    }

    @Test
    public void someInnerVariableIsMovedUpProperly() {
        final IEventBroker eventBroker = mock(IEventBroker.class);

        final RobotVariablesSection varSection = createVariables();
        final RobotVariable variableToMove = varSection.getChildren().get(2);

        ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new MoveVariableUpCommand(variableToMove))
                .execute();

        assertThat(varSection.getChildren()).hasSize(5);
        assertThat(varSection.getChildren().indexOf(variableToMove)).isEqualTo(1);

        final VariableTable varTable = varSection.getLinkedElement();
        assertThat(varTable.getVariables().get(0).getName()).isEqualTo("scalar");
        assertThat(varTable.getVariables().get(1).getName()).isEqualTo("list");
        assertThat(varTable.getVariables().get(2).getName()).isEqualTo("scalar_as_list");
        assertThat(varTable.getVariables().get(3).getName()).isEqualTo("dict");
        assertThat(varTable.getVariables().get(4).getName()).isEqualTo("invalid}");

        verify(eventBroker).send(RobotModelEvents.ROBOT_VARIABLE_MOVED, varSection);
    }

    private static RobotVariablesSection createVariables() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Variables ***")
                .appendLine("${scalar}  0")
                .appendLine("${scalar_as_list}  0  1  2")
                .appendLine("@{list}  1  2  3")
                .appendLine("&{dict}  a=1  b=2  c=3  d=4")
                .appendLine("invalid}  1  2  3")
                .build();
        return model.findSection(RobotVariablesSection.class).get();
    }

}
