package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.view.MainScreenServerListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.WS_SERVER_ID_URL;
import static de.uniks.stp.wedoit.accord.client.constants.Network.WS_SERVER_URL;

public class MainScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private Button privateChatsButton;
    private Button optionsButton;
    private Button addServerButton;
    private Button enterInvitationButton;
    private ListView<Server> lvServer;
    private PropertyChangeListener serverListListener = this::serverListViewChanged;
    private WSCallback serverWSCallback = this::handleServersMessage;
    private final List<String> webSocketServerUrls = new ArrayList<>();

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public MainScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
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
        this.privateChatsButton = (Button) view.lookup("#btnPrivateChats");
        this.optionsButton = (Button) view.lookup("#btnOptions");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.enterInvitationButton = (Button) view.lookup("#btnEnterInvitation");
        this.lvServer = (ListView<Server>) view.lookup("#lwServerList");

        this.editor.getStageManager().getStage(StageEnum.STAGE).setTitle(LanguageResolver.getString("MAIN"));

        // load servers of the localUser
        editor.getRestManager().getServers(localUser, this);

        // load localUserId in order to later be able to edit server
        editor.getRestManager().getLocalUserId(localUser);

        // Add action listeners
        this.privateChatsButton.setOnAction(this::privateChatsButtonOnClick);
        this.optionsButton.setOnAction(this::optionsButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.enterInvitationButton.setOnAction(this::enterInvitationButtonOnClick);
        this.lvServer.setOnMouseReleased(this::onServerListViewClicked);
    }

    /**
     * handles a response of a get servers request and inits the server list view.
     *
     * @param success success of the get servers request
     */
    public void handleGetServers(boolean success) {
        if (success) {
            // load list view
            MainScreenServerListView mainScreenServerListView = new MainScreenServerListView(this.editor);
            lvServer.setCellFactory(mainScreenServerListView);
            List<Server> localUserServers = localUser.getServers().stream().sorted(Comparator.comparing(Server::getName)).collect(Collectors.toList());
            this.lvServer.setItems(FXCollections.observableList(localUserServers));

            // Add listener for the loaded listView
            this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);
            // Add server websockets
            for (Server server : localUser.getServers()) {
                webSocketServerUrls.add(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
                editor.getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
            }
        } else {
            Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN, null, null));
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        for (String url : webSocketServerUrls) {
            editor.getWebSocketManager().withOutWebSocket(url);
        }
        serverWSCallback = null;
        privateChatsButton.setOnAction(null);
        optionsButton.setOnAction(null);
        addServerButton.setOnAction(null);
        enterInvitationButton.setOnAction(null);

        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);
        this.serverListListener = null;
    }

    /**
     * Redirect to the PrivateChatsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void privateChatsButtonOnClick(ActionEvent actionEvent) {
        Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null));
    }

    /**
     * Redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }

    /**
     * Redirect to the ServerScreen of the chosen server
     *
     * @param mouseEvent Expects an mouse event, such as when the listView is doubleclicked
     */
    private void onServerListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Server server = lvServer.getSelectionModel().getSelectedItem();
            if (server != null) {
                this.editor.getStageManager().initView(ControllerEnum.SERVER_SCREEN, server, null);
            }
        }
    }

    /**
     * Opens a pop-up windows, where you can enter the servername
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.CREATE_SERVER_SCREEN, null, null);
    }

    /**
     * Opens a pop-up windows, where you can enter a invitation
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void enterInvitationButtonOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.JOIN_SERVER_SCREEN, null, null);
    }

    /**
     * update automatically the listView when localUser.getServers changed
     *
     * @param propertyChangeEvent event which changed the Listener for the servers of the local user
     */
    private void serverListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            lvServer.getItems().removeAll();
            List<Server> localUserServers = localUser.getServers().stream().sorted(Comparator.comparing(Server::getName))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.lvServer.setItems(FXCollections.observableList(localUserServers)));
            lvServer.refresh();
        }
    }

    /**
     * This method
     * <p>
     * handels a server message
     *
     * @param msg message from the web socket
     */
    private void handleServersMessage(JsonStructure msg) {
        JsonObject data = ((JsonObject) msg).getJsonObject(DATA);
        String action = ((JsonObject) msg).getString(ACTION);

        if (action.equals(SERVER_UPDATED)) {
            editor.haveServer(localUser, data.getString(ID), data.getString(NAME));
            lvServer.refresh();
        }
        if (action.equals(SERVER_DELETED)) {
            for (Server server : localUser.getServers()) {
                if (server.getId().equals(data.getString(ID))) {
                    Platform.runLater(() -> {
                        this.lvServer.getItems().remove(server);
                        server.removeYou();
                        lvServer.getItems().sort(Comparator.comparing(Server::getName));
                        lvServer.refresh();
                    });
                }
            }
        }
    }

}
