package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.Icons;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EmojiScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private GridPane pane;
    private TextField tfForEmoji;

    private EmojiButton emoji;
    private HashMap<EmojiButton, String> hashMapForEmojiButtons = new HashMap<EmojiButton, String>();
    private List<Icons> iconsUnicodeList = Arrays.asList(Icons.values());

    public EmojiScreenController(Parent view, LocalUser localUser, Editor editor, TextField tfForEmoji) {
        this.view = view;
        this.localUser = localUser;
        this.editor = editor;
        this.tfForEmoji = tfForEmoji;
    }

    /**
     * Initializes GridPane, the use it as emoji picker
     * call the emoji picker creator
     */
    public void init() {

        this.pane = (GridPane) this.view.lookup("#panelForEmojis");

        this.pane.setAlignment(Pos.CENTER);
        this.pane.setHgap(5);
        this.pane.setVgap(5);

        createEmojiPicker();

    }

    /**
     * create dynamic buttons to show emojis
     * add emoji buttons to a hashMap
     * add created emoji buttons to the GridPane
     * call the action listener of each created emoji button
     */
    private void createEmojiPicker() {
        //how many buttons are to fit in one row
        int gridWidth = 7;

        for (int i = 0; i < iconsUnicodeList.size(); i++) {
            emoji = new EmojiButton(iconsUnicodeList.get(i).toString());
            hashMapForEmojiButtons.put(emoji, emoji.getText());
            emoji.setOnAction(this::btnEmojiOnClick);
            this.pane.add(emoji, i % gridWidth, i / gridWidth);
        }

    }

    /**
     * The text of the selected button from the hashMap is retrieved
     * added the emoji button text in the chat text field
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        Platform.runLater(() -> {
                    this.tfForEmoji.setText(this.tfForEmoji.getText() + hashMapForEmojiButtons.get(actionEvent.getSource()));
                }
        );
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    @Override
    public void stop() {
        emoji.setOnAction(null);
    }
}
