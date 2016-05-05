/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.junit.Test;

public class ResourceRenameParticipantTest {

    @Test
    public void checkLabelTest() {
        final ResourceRenameParticipant participant = new ResourceRenameParticipant();

        assertThat(participant.getName()).isEqualTo("Robot resource rename participant");
    }

    @Test
    public void statusIsAlwaysOk() {
        final ResourceRenameParticipant participant = new ResourceRenameParticipant();

        assertThat(participant.checkConditions(new NullProgressMonitor(), mock(CheckConditionsContext.class)).isOK())
                .isTrue();
    }

    // TODO : write more tests when this participant will do more than collecting changes for
    // red.xml file. This single thing is already tested by other junits.
}
