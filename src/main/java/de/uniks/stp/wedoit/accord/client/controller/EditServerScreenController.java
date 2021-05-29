package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final Stage stage;

    private Button btnCreateInvitation;
    private Button btnDelete;
    private Button btnSave;

    private RadioButton radioBtnTemporal;
    private RadioButton radioBtnMaxCount;

    private TextField tfNewServernameInput;
    private TextField tfMaxCountAmountInput;
    private TextField tfInvitationLink;

    private VBox vBoxAdminOnly;
    private VBox mainVBox;

    private Label lblError;


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public EditServerScreenController(Parent view, LocalUser model, Editor editor, Server server, Stage stage) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.stage = stage;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        // Load all view references
        this.btnCreateInvitation = (Button) view.lookup("#btnCreateInvitation");
        this.btnDelete = (Button) view.lookup("#btnDelete");
        this.btnSave = (Button) view.lookup("#btnSave");

        this.radioBtnTemporal = (RadioButton) view.lookup("#radioBtnTemporal");
        this.radioBtnMaxCount = (RadioButton) view.lookup("#radioBtnMaxCount");

        this.tfNewServernameInput = (TextField) view.lookup("#tfNewServernameInput");
        this.tfMaxCountAmountInput = (TextField) view.lookup("#tfMaxCountAmountInput");
        this.tfInvitationLink = (TextField) view.lookup("#tfInvitationLink");

        this.vBoxAdminOnly = (VBox) view.lookup("#vBoxAdminOnly");
        this.mainVBox = (VBox) view.lookup("#mainVBox");

        this.lblError = (Label) view.lookup("#lblError");

        // Depending on if localUser is admin or not display the correct editMenu
        loadDefaultSettings();

        // TODO: implement function of buttons and so on


        addActionListener();
    }

    private void addActionListener() {
        // Add action listeners
        this.btnCreateInvitation.setOnAction(this::createInvitationButtonOnClick);
        this.btnDelete.setOnAction(this::deleteButtonOnClick);
        this.btnSave.setOnAction(this::saveButtonOnClick);
        this.radioBtnMaxCount.setOnMouseClicked(this::radioBtnMaxCountOnClick);
        this.radioBtnTemporal.setOnMouseClicked(this::radioBtnTemporalOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.btnCreateInvitation.setOnAction(null);
        this.btnDelete.setOnAction(null);
        this.btnSave.setOnAction(null);
        this.radioBtnMaxCount.setOnMouseClicked(null);
        this.radioBtnTemporal.setOnMouseClicked(null);
    }

    /**
     * Called to load the correct EditorScreen depending on whether the localUser is admin of server or not
     */
    private void loadDefaultSettings() {
        lblError.setVisible(false);
        if (!localUser.getId().equals(server.getOwner())) {
            btnDelete.setVisible(false);
            btnDelete.setDisable(true);
            vBoxAdminOnly.setVisible(false);
            vBoxAdminOnly.setDisable(true);
            for (Node child : mainVBox.getChildren()) {
                child.getId();
                if (child.getId() != null && child.getId().equals("vBoxAdminOnly")) {
                    mainVBox.getChildren().remove(child);
                    break;
                }
            }
            mainVBox.setPrefHeight(150);
            mainVBox.setPrefWidth(350);
        } else {
            ToggleGroup toggleGroup = new ToggleGroup();
            radioBtnMaxCount.setToggleGroup(toggleGroup);
            radioBtnTemporal.setToggleGroup(toggleGroup);
            radioBtnMaxCount.setSelected(true);
        }
    }

    /**
     * In this method a new servername has to be set if set if the
     * user types in a new servername and close popup Window
     * @param actionEvent
     */
    private void saveButtonOnClick(ActionEvent actionEvent) {
        String newServerName = tfNewServernameInput.getText();
        if (!newServerName.isEmpty()) {
            editor.getNetworkController().changeServerName(localUser, server, newServerName, this);
        } else {
            stage.close();
        }
    }

    private void deleteButtonOnClick(ActionEvent actionEvent) {

    }

    private void createInvitationButtonOnClick(ActionEvent actionEvent) {

    }

    private void radioBtnMaxCountOnClick(MouseEvent mouseEvent) {
        if (this.radioBtnMaxCount.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(true);
        }
    }

    private void radioBtnTemporalOnClick(MouseEvent mouseEvent) {
        if (this.radioBtnTemporal.isFocused()) {
            this.tfMaxCountAmountInput.setEditable(false);
        }
    }

    public void handleChangeServerName(boolean status) {
        if (status) {
            Platform.runLater(() -> {
                this.stage.close();
            });
        } else {
            Platform.runLater(() -> {
                lblError.setText("Error. Change Servername not successful!");
                lblError.setVisible(true);
            });
        }
    }
}
