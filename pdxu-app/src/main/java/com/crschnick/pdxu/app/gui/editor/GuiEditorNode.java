package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.editor.EditorCollectorNode;
import com.crschnick.pdxu.app.editor.EditorNode;
import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.model.GameColor;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class GuiEditorNode {

    static Region createValueDisplay(EditorNode n, EditorState state) {
        if (n.isReal() && ((EditorSimpleNode) n).getBackingNode().isValue()) {
            var tf = new TextField(((EditorSimpleNode) n).getBackingNode().getString());
            tf.setAlignment(Pos.CENTER);
            tf.textProperty().addListener((c, o, ne) -> {
                var result = ((EditorSimpleNode) n).updateText(ne);
                tf.setText(result);
                if (!result.equals(ne)) {
                    state.onTextChanged();
                }
            });
            return tf;
        } else if (n.isReal() && ((EditorSimpleNode) n).getBackingNode().isTagged() &&
                ((EditorSimpleNode) n).getBackingNode().describe().getValueType().equals(Node.ValueType.COLOR)) {
            var picker = new JFXColorPicker(ColorHelper.fromGameColor(GameColor.fromColorNode(
                    ((EditorSimpleNode) n).getBackingNode())));
            picker.valueProperty().addListener((c, o, ne) -> {
                ((EditorSimpleNode) n).updateColor(ne);
                state.onColorChanged();
            });
            return picker;
        } else {
            return createArrayDisplay(n, state);
        }
    }

    private static Region createArrayDisplay(EditorNode n, EditorState state) {
        var box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(5);

        {
            int length = n.isReal() ? ((EditorSimpleNode) n).getBackingNode().getNodeArray().size() :
                    ((EditorCollectorNode) n).getNodes().size();
            int stringSize = String.valueOf(length).length();
            var lengthString = stringSize == 1 ? " " + length + " " :
                    (stringSize <= 3 ? " ".repeat(3 - stringSize) + length :
                            String.valueOf(length));
            var btn = new JFXButton("List[" + lengthString + "]");
            btn.setAlignment(Pos.CENTER);
            btn.setOnAction(e -> state.navigateTo(n));
            box.getChildren().add(btn);
        }

        {
            var preview = new Label();
            preview.getStyleClass().add("preview");
            preview.setGraphic(new FontIcon());
            preview.setOnMouseEntered(e -> {
                var tt = GuiTooltips.createTooltip(NodeWriter.writeToString(
                        n.toWritableNode(), 15, "  "));
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(preview, tt);
            });
            box.getChildren().add(preview);
        }

        return box;
    }
}