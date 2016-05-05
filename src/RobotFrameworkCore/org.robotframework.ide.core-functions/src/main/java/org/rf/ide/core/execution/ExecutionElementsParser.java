/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.rf.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.rf.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class ExecutionElementsParser implements ILineHandler {

    public static final String ROBOT_EXECUTION_PASS_STATUS = "PASS";
    
    
    private static final String START_SUITE_EVENT = "start_suite";

    private static final String END_SUITE_EVENT = "end_suite";

    private static final String START_TEST_EVENT = "start_test";

    private static final String END_TEST_EVENT = "end_test";
    
    private static final String OUTPUT_FILE_EVENT = "output_file";

    private final ObjectMapper mapper;

    private Map<String, Object> eventMap;

    private final IExecutionHandler executionHandler;

    public ExecutionElementsParser(final IExecutionHandler executionHandler) {
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<String, Object>();
        this.executionHandler = executionHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            eventMap = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        
        final String eventType = getEventType(eventMap);
        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case START_SUITE_EVENT:
                final List<Object> startSuiteList = (List<Object>) eventMap.get(START_SUITE_EVENT);
                final Map<String, String> startSuiteDetails = (Map<String, String>) startSuiteList.get(1);
                final ExecutionElement startSuiteElement = createStartSuiteExecutionElement(
                        (String) startSuiteList.get(0), startSuiteDetails.get("source"));
                executionHandler.processExecutionElement(startSuiteElement);
                break;
            case END_SUITE_EVENT:
                final List<Object> endSuiteList = (List<Object>) eventMap.get(END_SUITE_EVENT);
                final Map<String, Object> endSuiteDetails = (Map<String, Object>) endSuiteList.get(1);
                final ExecutionElement endSuiteElement = createEndSuiteExecutionElement((String) endSuiteList.get(0),
                        endSuiteDetails);
                executionHandler.processExecutionElement(endSuiteElement);
                break;
            case START_TEST_EVENT:
                final List<Object> testList = (List<Object>) eventMap.get(START_TEST_EVENT);
                final ExecutionElement startTestElement = createStartTestExecutionElement((String)testList.get(0));
                executionHandler.processExecutionElement(startTestElement);
                break;
            case END_TEST_EVENT:
                final List<Object> endTestList = (List<Object>) eventMap.get(END_TEST_EVENT);
                final Map<String, Object> endTestDetails = (Map<String, Object>) endTestList.get(1);
                final ExecutionElement endTestElement = createEndTestExecutionElement((String) endTestList.get(0),
                        endTestDetails);
                executionHandler.processExecutionElement(endTestElement);
                break;
            case OUTPUT_FILE_EVENT:
                final List<Object> outputFileList = (List<Object>) eventMap.get(OUTPUT_FILE_EVENT);
                final ExecutionElement outputFilePathElement = createOutputFileExecutionElement((String) outputFileList.get(0));
                executionHandler.processExecutionElement(outputFilePathElement);
                break;
            default:
                break;
        }
    }

    public static ExecutionElement createStartSuiteExecutionElement(final String name, final String source) {
        final ExecutionElement startElement = createNewExecutionElement(name, ExecutionElementType.SUITE);
        startElement.setSource(source);
        return startElement;
    }
    
    public static ExecutionElement createStartTestExecutionElement(final String name) {
        return createNewExecutionElement(name, ExecutionElementType.TEST);
    }

    public static ExecutionElement createEndTestExecutionElement(final String name,
            final Map<?, ?> endTestDetails) {
        return createEndExecutionElement(name, ExecutionElementType.TEST, endTestDetails);
    }

    public static ExecutionElement createEndSuiteExecutionElement(final String name,
            final Map<?, ?> endSuiteDetails) {
        return createEndExecutionElement(name, ExecutionElementType.SUITE, endSuiteDetails);
    }
    
    public static ExecutionElement createOutputFileExecutionElement(final String name) {
        return createNewExecutionElement(name, ExecutionElementType.OUTPUT_FILE);
    }

    private static ExecutionElement createEndExecutionElement(final String name, final ExecutionElementType type,
            final Map<?, ?> details) {
        final ExecutionElement endElement = createNewExecutionElement(name, type);
        endElement.setElapsedTime((Integer) details.get("elapsedtime"));
        endElement.setMessage((String) details.get("message"));
        endElement.setStatus((String) details.get("status"));
        return endElement;
    }
    
    private static ExecutionElement createNewExecutionElement(final String name, final ExecutionElementType type) {
        return new ExecutionElement(name, type);
    }
    
    private String getEventType(final Map<?, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        final Set<?> keySet = eventMap.keySet();
        if (!keySet.isEmpty()) {
            return (String) keySet.iterator().next();
        }
        return null;
    }
}
