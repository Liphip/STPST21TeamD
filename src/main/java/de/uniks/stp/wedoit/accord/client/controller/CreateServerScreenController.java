package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreateServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private TextField tfServerName;
    private Button btnCreateServer;
    private Label errorLabel;

    public CreateServerScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
        // Load all view references
        this.btnCreateServer = (Button) view.lookup("#btnCreateServer");
        this.tfServerName = (TextField) view.lookup("#tfServerName");
        this.errorLabel = (Label) view.lookup("#lblError");

        // Add action listeners
        this.btnCreateServer.setOnAction(this::createServerButtonOnClick);
    }

    public void stop() {
        btnCreateServer.setOnAction(null);
    }

    // Additional methods

    /**
     * After pressing "Create Server", the server will be created with the name in the textfield, and you get redirected
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createServerButtonOnClick(ActionEvent actionEvent) {

        if (tfServerName.getText().length() < 2) {
            tfServerName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 2 symbols long"));
        } else {
            editor.getNetworkController().createServer(tfServerName.getText(), this);
        }
    }

    public void handleCreateServer(Server server) {
        if (server != null) {
            stop();
            Platform.runLater(() -> StageManager.showServerScreen(server));
        } else {
            tfServerName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");

            Platform.runLater(() -> errorLabel.setText("Something went wrong while creating the server"));
        }
    }
}
