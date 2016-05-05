/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.Map;

import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 *
 */
public class RobotDebugVariablesContext {

    private Map<String, IVariable> variablesMap;

    private int stackTraceId;

    public RobotDebugVariablesContext(int stackTraceId, Map<String, IVariable> variablesMap) {
        this.variablesMap = variablesMap;
        this.stackTraceId = stackTraceId;
    }

    public Map<String, IVariable> getVariablesMap() {
        return variablesMap;
    }

    public void setVariablesMap(Map<String, IVariable> variablesMap) {
        this.variablesMap = variablesMap;
    }

    public int getStackTraceId() {
        return stackTraceId;
    }

    public void setStackTraceId(int stackTraceId) {
        this.stackTraceId = stackTraceId;
    }

}
