package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.EMOJI_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.EMOJIPICKERSTAGE;

public class UpdateMessageScreenController implements Controller {

    private final Parent view;
    private final Editor editor;
    private final Message message;
    private final Object stage;
    private TextField tfUpdateMessage;
    private Button btnDiscard;
    private Button btnUpdateMessage;
    private Label errorLabel;
    private Button btnEmoji;

    public UpdateMessageScreenController(Parent view, Editor editor, Message message, Stage stage) {
        this.view = view;
        this.editor = editor;
        this.message = message;
        this.stage = stage;
    }

    @Override
    public void init() {
        tfUpdateMessage = (TextField) view.lookup("#tfUpdateMessage");
        btnEmoji = (Button) view.lookup("#btnEmoji");
        btnDiscard = (Button) view.lookup("#btnDiscard");
        btnUpdateMessage = (Button) view.lookup("#btnUpdateMessage");
        errorLabel = (Label) view.lookup("#lblError");

        tfUpdateMessage.setText(message.getText());

        setComponentsText();

        btnDiscard.setOnAction(this::discardChanges);
        btnUpdateMessage.setOnAction(this::updateMessage);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);
    }

    private void setComponentsText() {
        this.btnUpdateMessage.setText(LanguageResolver.getString("SAVE"));
        this.btnDiscard.setText(LanguageResolver.getString("DISCARD"));
    }

    private void updateMessage(ActionEvent actionEvent) {
        String newMessage = tfUpdateMessage.getText();

        if (newMessage.equals(message.getText())) {
            this.editor.getStageManager().getPopupStage().close();
        } else if (newMessage.length() >= 1) {
            editor.getRestManager().updateMessage(editor.getLocalUser(), newMessage, message, this);
        } else {
            Platform.runLater(() -> errorLabel.setText(LanguageResolver.getString("ERROR_UPDATE_MESSAGE_CHAR_COUNT")));
        }
    }

    private void discardChanges(ActionEvent actionEvent) {
        this.editor.getStageManager().getPopupStage().close();
    }

    public void handleUpdateMessage(Boolean status) {
        if (status) {
            Platform.runLater(editor.getStageManager().getPopupStage()::close);
        } else {
            Platform.runLater(() -> errorLabel.setText(LanguageResolver.getString("ERROR_UPDATE_MESSAGE")));
        }
    }

    /**
     * open the EmojiScreen
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        this.editor.getStageManager().initView(EMOJIPICKERSTAGE, "Emoji Picker",
                "EmojiScreen", EMOJI_SCREEN_CONTROLLER, false, tfUpdateMessage, pos);
    }

    @Override
    public void stop() {
        btnDiscard.setOnAction(null);
        btnUpdateMessage.setOnAction(null);
        btnEmoji.setOnAction(null);

        btnEmoji = null;
        tfUpdateMessage = null;
        btnDiscard = null;
        btnUpdateMessage = null;
        errorLabel = null;
    }

}