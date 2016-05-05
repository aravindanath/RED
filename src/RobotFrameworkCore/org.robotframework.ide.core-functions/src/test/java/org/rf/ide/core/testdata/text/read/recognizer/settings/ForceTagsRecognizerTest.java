/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.test.helpers.ClassFieldCleaner;
import org.rf.ide.core.test.helpers.ClassFieldCleaner.ForClean;
import org.rf.ide.core.test.helpers.CombinationGenerator;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


@SuppressWarnings("PMD.MethodNamingConventions")
public class ForceTagsRecognizerTest {

    @ForClean
    private ATokenRecognizer rec;


    @Test
    public void test_forceTagsColonWord_allCombinations() {
        List<String> combinations = new CombinationGenerator()
                .combinations("Force Tags:");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(
                    textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }


    @Test
    public void test_twoSpacesAndForceTagsColonThanWord() {
        StringBuilder text = new StringBuilder(" Force Tags:");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleSpaceAndForceTagsColonThanWord() {
        StringBuilder text = new StringBuilder(" Force Tags:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleForceTagsColonThanLetterCWord() {
        StringBuilder text = new StringBuilder("Force Tags:");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleForceTagsColonWord() {
        StringBuilder text = new StringBuilder("Force Tags:");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_forceTagsWord_allCombinations() {
        List<String> combinations = new CombinationGenerator()
                .combinations("Force Tags");

        for (String comb : combinations) {
            StringBuilder textOfHeader = new StringBuilder(comb);

            assertThat(rec.hasNext(textOfHeader, 1)).isTrue();
            RobotToken token = rec.next();
            assertThat(token.getStartColumn()).isEqualTo(0);
            assertThat(token.getLineNumber()).isEqualTo(1);
            assertThat(token.getEndColumn()).isEqualTo(textOfHeader.length());
            assertThat(token.getText().toString()).isEqualTo(
                    textOfHeader.toString());
            assertThat(token.getTypes()).containsExactly(rec.getProducedType());
        }
    }


    @Test
    public void test_twoSpacesAndForceTagsThanWord() {
        StringBuilder text = new StringBuilder(" Force Tags");
        StringBuilder d = new StringBuilder(" ").append(text);
        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(1);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(d.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleSpaceAndForceTagsThanWord() {
        StringBuilder text = new StringBuilder(" Force Tags");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleForceTagsThanLetterCWord() {
        StringBuilder text = new StringBuilder("Force Tags");
        StringBuilder d = new StringBuilder(text).append("C");

        assertThat(rec.hasNext(d, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_singleForceTagsWord() {
        StringBuilder text = new StringBuilder("Force Tags");

        assertThat(rec.hasNext(text, 1)).isTrue();
        RobotToken token = rec.next();
        assertThat(token.getStartColumn()).isEqualTo(0);
        assertThat(token.getLineNumber()).isEqualTo(1);
        assertThat(token.getEndColumn()).isEqualTo(text.length());
        assertThat(token.getText().toString()).isEqualTo(text.toString());
        assertThat(token.getTypes()).containsExactly(rec.getProducedType());
    }


    @Test
    public void test_getPattern() {
        assertThat(rec.getPattern().pattern()).isEqualTo(
                "[ ]?(" + ATokenRecognizer.createUpperLowerCaseWord("Force")
                        + "[\\s]+"
                        + ATokenRecognizer.createUpperLowerCaseWord("Tags")
                        + "[\\s]*:" + "|"
                        + ATokenRecognizer.createUpperLowerCaseWord("Force")
                        + "[\\s]+"
                        + ATokenRecognizer.createUpperLowerCaseWord("Tags")
                        + ")");

    }


    @Test
    public void test_getProducedType() {
        assertThat(rec.getProducedType()).isEqualTo(
                RobotTokenType.SETTING_FORCE_TAGS_DECLARATION);
    }


    @Before
    public void setUp() {
        rec = new ForceTagsRecognizer();
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
