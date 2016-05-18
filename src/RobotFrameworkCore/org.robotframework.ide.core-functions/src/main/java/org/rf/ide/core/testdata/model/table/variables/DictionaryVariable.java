/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DictionaryVariable extends AVariable {

    private final List<DictionaryKeyValuePair> items = new ArrayList<>();

    public DictionaryVariable(final String name, final RobotToken declaration, final VariableScope scope) {
        super(VariableType.DICTIONARY, name, declaration, scope);
    }

    public void put(final RobotToken raw, final RobotToken key, final RobotToken value) {
        fixForTheType(raw, RobotTokenType.VARIABLES_VARIABLE_VALUE);
        fixForTheType(key, RobotTokenType.VARIABLES_DICTIONARY_KEY);
        fixForTheType(value, RobotTokenType.VARIABLES_DICTIONARY_VALUE);
        items.add(new DictionaryKeyValuePair(raw, key, value));
    }

    public void addKeyValuePair(final RobotToken raw, final RobotToken key, final RobotToken value,
            final int position) {
        fixForTheType(raw, RobotTokenType.VARIABLES_VARIABLE_VALUE);
        fixForTheType(key, RobotTokenType.VARIABLES_DICTIONARY_KEY);
        fixForTheType(value, RobotTokenType.VARIABLES_DICTIONARY_VALUE);
        items.set(position, new DictionaryKeyValuePair(raw, key, value));
    }

    public void removeKeyValuePair(final DictionaryKeyValuePair pair) {
        items.remove(pair);
    }

    public boolean moveLeftKeyValuePair(final DictionaryKeyValuePair pair) {
        return getMoveHelper().moveLeft(items, pair);
    }

    public boolean moveRightKeyValuePair(final DictionaryKeyValuePair pair) {
        return getMoveHelper().moveRight(items, pair);
    }

    public List<DictionaryKeyValuePair> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }

    public static class DictionaryKeyValuePair {

        private RobotToken raw;

        private RobotToken key;

        private RobotToken value;

        public DictionaryKeyValuePair(final RobotToken raw, final RobotToken key, final RobotToken value) {
            this.raw = raw;
            this.key = key;
            this.value = value;
        }

        public RobotToken getKey() {
            return key;
        }

        public void setKey(final RobotToken key) {
            this.key = key;
        }

        public RobotToken getValue() {
            return value;
        }

        public void setValue(final RobotToken value) {
            this.value = value;
        }

        public RobotToken getRaw() {
            return raw;
        }

        public void setRaw(final RobotToken raw) {
            this.raw = raw;
        }

    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            for (final DictionaryKeyValuePair p : items) {
                if (p.getRaw() != null) {
                    tokens.add(p.getRaw());
                }
            }
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
