/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.test.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ClassFieldCleaner
 */
@SuppressWarnings("PMD.MethodNamingConventions")
public class TestClassFieldCleaner {

    private NullableSetHelperClass testObject;


    @Test
    public void test_cleanerIsInvoked_onClassWithoutForCleanAnnotations()
            throws IllegalArgumentException, IllegalAccessException {
        // prepare
        ClassWithoutAnyAnnotation o = new ClassWithoutAnyAnnotation();

        // execute
        ClassFieldCleaner.init(o);

        // verify
        assertThat(o.data).isEqualTo("123456");
        assertThat(o.test).isEqualTo("text");
    }


    @Test
    public void test_cleanerIsInvoked_forNullObject_shouldNotThrowAnyException()
            throws IllegalArgumentException, IllegalAccessException {
        ClassFieldCleaner.init(null);
    }


    @Test
    public void test_cleanerIsInvoked() throws IllegalArgumentException,
            IllegalAccessException {
        // execute
        ClassFieldCleaner.init(testObject);

        // verify
        assertThat(testObject.test).isNull();
        assertThat(testObject.data).isEqualTo("123456");
        assertThat(testObject.test2).isNull();
    }


    @Before
    public void setUp() {
        testObject = new NullableSetHelperClass();
    }


    @After
    public void tearDown() {
        testObject = null;
    }

    private class ClassWithoutAnyAnnotation {

        private String test = "text";
        private String data = "123456";
    }

    private class NullableSetHelperClass {

        @ForClean
        private String test = "text";
        private String data = "123456";
        @ForClean
        private String test2 = "text2";
    }
}
