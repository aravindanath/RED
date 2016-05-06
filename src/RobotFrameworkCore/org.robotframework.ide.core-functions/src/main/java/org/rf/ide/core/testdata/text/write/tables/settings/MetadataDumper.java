/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class MetadataDumper extends ANotExecutableTableElementDumper {

    public MetadataDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.METADATA_SETTING);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        Metadata metadata = (Metadata) currentElement;

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        List<RobotToken> keys = new ArrayList<>();
        if (metadata.getKey() != null) {
            keys.add(metadata.getKey());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_METADATA_KEY, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_METADATA_VALUE, 2, metadata.getValues());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                getElementHelper().filter(metadata.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                getElementHelper().filter(metadata.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}
