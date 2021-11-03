package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.layout.Region;

public abstract class SavegameInfoComp {

    public SavegameInfoComp(ArrayNode node, SavegameData data) {
        init(node, data);
    }

    protected abstract void init(ArrayNode node, SavegameData data);

    public abstract Region create();
}
