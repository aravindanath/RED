/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Michal Anglart
 *
 */
@XmlRootElement(namespace = "org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification")
public class LibraryConstructor {

    private String documentation;

    private List<String> arguments;

    public String getDocumentation() {
        return documentation;
    }

    @XmlElement(name = "doc")
    public void setDocumentation(final String documentation) {
        this.documentation = documentation;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @XmlElementWrapper(name = "arguments")
    @XmlElement(name = "arg")
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }

    public ArgumentsDescriptor createArgumentsDescriptor() {
        return ArgumentsDescriptor.createDescriptor(arguments);
    }
}
