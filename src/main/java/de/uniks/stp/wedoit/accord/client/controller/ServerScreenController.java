package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.SERVER_ID_URL;
import static de.uniks.stp.wedoit.accord.client.Constants.WS_SERVER_URL;

public class ServerScreenController {

    private final Server server;
    private RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Label lbServerName;
    private ListView lvServerChannels;
    private ListView lvServerUsers;
    private TextField tfInputMessage;
    private WebSocketClient websocket;

    public ServerScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
        this.server = server;
    }

    public void init() {
        // Load all view references

        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerChannels = (ListView) view.lookup("#lvServerChannels");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");

        // Load list view
        // Load users of the server
        restClient.getExplicitServerInformation(localUser.getUserKey(), server.getId(), response -> {
            if (response.getBody().getObject().getString("status").equals("success")) {
                JSONObject data = response.getBody().getObject().getJSONObject("data");
                JSONArray members = data.getJSONArray("members");
                JSONArray categories = data.getJSONArray("categories");
                server.setOwner(data.getString("owner"));
                System.out.println(members);
                // create user which are member in the server
                // Load user list view
                createUserListView(members);

            } else {
                StageManager.showLoginScreen(restClient);
            }

        });

        try {
            this.websocket = new WebSocketClient(editor, new URI(WS_SERVER_URL + SERVER_ID_URL + server.getId()), this::handleMessage);
            //this.websocket = new WebSocketClient(editor, new URI(SYSTEM_SOCKET_URL), this::handleMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error while making new URI");
            e.printStackTrace();
        }


        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
    }

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param members JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JSONArray members) {
        for (int index = 0; index < members.length(); index++) {

            String name = members.getJSONObject(index).getString("name");
            String id = members.getJSONObject(index).getString("id");
            boolean onlineStatus = members.getJSONObject(index).getBoolean("online");

            editor.haveUserWithServer(name, id, onlineStatus, server);
        }
        // load list view
        ServerUserListView serverUserListView = new ServerUserListView();
        lvServerUsers.setCellFactory(serverUserListView);
        List<User> users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus))
                .collect(Collectors.toList());
        this.lvServerUsers.setItems(FXCollections.observableList(users));
    }

    public void stop() {
        this.btnLogout.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.websocket = null;
    }

    // Additional methods

    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen(restClient);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    /**
     * Handles the response of the websocket server
     *
     * @param msg response of the websocket server
     */
    private void handleMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        System.out.println(msg);
        //TODO updateUserListView()

    }
    
}
