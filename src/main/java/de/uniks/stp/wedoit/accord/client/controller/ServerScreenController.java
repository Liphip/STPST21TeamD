package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.CategoryTreeViewController;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.ChannelTreeView;
import de.uniks.stp.wedoit.accord.client.view.MessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;


public class ServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private List<User> users;
    private Channel channel, currentChannel;
    private ObservableList<Message> observableMessageList;

    // View Elements
    private Button btnOptions;
    private Button btnHome;
    private Button btnEmoji;
    private Button btnEdit;
    private Label lbServerName;
    private ListView<User> lvServerUsers;
    private TextField tfInputMessage;
    private ListView<Message> lvTextChat;
    private Label lbChannelName;

    // Websockets
    private WSCallback chatWSCallback = this::handleChatMessage;
    private WSCallback serverWSCallback;

    // PropertyChangeListener
    public final PropertyChangeListener userListViewListener = this::changeUserList;
    private final PropertyChangeListener newMessagesListener = this::newMessage;
    private final PropertyChangeListener serverNameListener = this::handleServerNameChange;

    private CategoryTreeViewController categoryTreeViewController;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public ServerScreenController(Parent view, LocalUser model, Editor editor, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.channel = new Channel();
        this.serverWSCallback = (msg) -> editor.getWebSocketManager().handleServerMessage(msg, server);
        categoryTreeViewController = new CategoryTreeViewController(view, model, editor, server, this);
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     * Add necessary webSocketClients
     */
    public void init() {
        // Load all view references
        this.editor.setCurrentServer(server);
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");
        this.btnEdit = (Button) view.lookup("#btnEdit");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        this.lvTextChat = (ListView<Message>) view.lookup("#lvTextChat");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");

        categoryTreeViewController.init();

        if (server.getName() != null && !server.getName().equals("")) {
            this.lbServerName.setText(server.getName());
        }

        // Add server websocket
        editor.getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
        // Add chat server web socket
        editor.getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.editor.getWebSocketManager().getCleanLocalUserName()
                        + AND_SERVER_ID_URL + this.server.getId(), chatWSCallback);

        this.lbServerName.setContextMenu(createContextMenuLeaveServer());
        this.btnEdit.setVisible(false);

        // get members of this server
        editor.getRestManager().getExplicitServerInformation(localUser, server, this);

        // add OnActionListeners
        addActionListener();

        initTooltips();

        // add PropertyChangeListener
        this.channel.listeners().addPropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
        this.server.listeners().addPropertyChangeListener(Server.PROPERTY_NAME, this.serverNameListener);
    }

    // ActionEvent Methods
    private void leaveServerAttention(ActionEvent actionEvent) {
        StageManager.showAttentionLeaveServerScreen(this.server);
    }

    private void btnEmojiOnClick(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        StageManager.showEmojiScreen(tfInputMessage, pos);
    }

    public void addActionListener() {
        // Add action listeners
        this.btnOptions.setOnAction(this::optionsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.btnEdit.setOnAction(this::editButtonOnClick);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);
        this.lvTextChat.setOnMousePressed(this::lvTextChatOnClick);
    }

    /**
     * Initializes the Tooltips for the Buttons
     */
    private void initTooltips() {
        Tooltip homeButton = new Tooltip();
        homeButton.setText("home");
        btnHome.setTooltip(homeButton);

        Tooltip optionsButton = new Tooltip();
        optionsButton.setText("options");
        btnOptions.setTooltip(optionsButton);

        Tooltip editButton = new Tooltip();
        editButton.setText("edit Server");
        btnEdit.setTooltip(editButton);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.btnOptions.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.btnEdit.setOnAction(null);

        this.tfInputMessage.setOnAction(null);

        this.editor.getWebSocketManager().withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        this.editor.getWebSocketManager().withOutWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + this.server.getId());

        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        }
        this.channel.listeners().removePropertyChangeListener(Channel.PROPERTY_MEMBERS, this.userListViewListener);
        this.chatWSCallback = null;
        this.serverWSCallback = null;

        editor.setCurrentServer(null);
        deleteCurrentServer();
    }

    // Additional methods

    public void deleteCurrentServer() {
        // Delete all connection to the server in the data model
        for (Category category : server.getCategories()) {
            for (Channel channel : category.getChannels()) {
                channel.withoutMembers(new ArrayList<>(channel.getMembers()));
            }
        }
        server.withoutMembers(new ArrayList<>(server.getMembers()));
        localUser.withoutServers(server);
    }

    public void handleGetExplicitServerInformation(JsonArray members) {
        if (members != null) {
            // create users which are member in the server and load user list view
            Platform.runLater(() -> {
                lbServerName.setText(server.getName());
            });

            createUserListView(members);
        } else {
            Platform.runLater(StageManager::showLoginScreen);
        }
        if (this.localUser.getId().equals(this.server.getOwner())) {
            this.lbServerName.getContextMenu().getItems().get(0).setVisible(false);
            this.btnEdit.setVisible(true);
        }

    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen();
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        StageManager.showEditServerScreen(this.server);
    }

    // PropertyChangeEvent Methods

    private void changeUserList(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            Platform.runLater(this::refreshLvUsers);
        }
    }

    private void handleServerNameChange(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> {
            this.lbServerName.setText(this.server.getName());
        });
    }

    /**
     * initialize channel Chat
     *
     * @param channel selected channel in lvServerChannels
     */
    public void initChannelChat(Channel channel) {
        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        }
        this.channel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.userListViewListener);

        channel.setRead(true);
        this.currentChannel = channel;
        this.lbChannelName.setText(this.currentChannel.getName());

        // init list view
        lvTextChat.setCellFactory(new MessageCellFactory());
        this.observableMessageList = FXCollections.observableList(currentChannel.getMessages().stream().sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList()));

        this.lvTextChat.setItems(observableMessageList);

        // display last 50 messages
        String timestamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
        this.editor.getRestManager().getChannelMessages(this.localUser, this.server, channel.getCategory(), channel, timestamp, this);


        // Add listener for the loaded listView
        this.currentChannel.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        this.currentChannel.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, this.userListViewListener);
        Platform.runLater(() -> this.lvTextChat.scrollTo(this.observableMessageList.size()));
    }

    public void handleGetChannelMessages(Channel channel, JsonArray data) {
        if (channel != null) {
            List<Message> messages = JsonUtil.parseMessageArray(data);
            Collections.reverse(messages);
            this.editor.updateChannelMessages(channel, messages);
            if (messages.size() == 50) {
                Platform.runLater(this::displayLoadMore);
            }
        } else {
            Platform.runLater(StageManager::showMainScreen);
        }
    }

    /**
     * Displays load more on first position of ListView of the Chat
     */
    private void displayLoadMore() {
        if (observableMessageList.size() >= 50) {
            Message topMessage = new Message().setText("Load more...").setId("idLoadMore");
            this.observableMessageList.add(0, topMessage);
        }
    }

    /**
     * Checks if "Load more..." is clicked and if yes, then it loads new messages
     *
     * @param mouseEvent
     */
    private void lvTextChatOnClick(MouseEvent mouseEvent) {
        Message selectedMessage = lvTextChat.getSelectionModel().getSelectedItem();
        if (selectedMessage != null && selectedMessage.getId() != null && selectedMessage.getId().equals("idLoadMore")) {
            this.observableMessageList.remove(0);
            Message oldestMessage = this.observableMessageList.get(0);
            Channel channel = oldestMessage.getChannel();
            String timestamp = String.valueOf(oldestMessage.getTimestamp());
            this.editor.getRestManager().getChannelMessages(this.localUser, this.server, channel.getCategory(), channel, timestamp, this);
        }
    }

    /**
     * update the chat when a new message arrived
     *
     * @param propertyChangeEvent event occurs when a new private message arrives
     */
    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            Message newMessage = (Message) propertyChangeEvent.getNewValue();
            Platform.runLater(() -> {
                if (this.observableMessageList.isEmpty()) {
                    this.observableMessageList.add(newMessage);
                } else if (newMessage.getTimestamp() <= this.observableMessageList.get(observableMessageList.size() - 1).getTimestamp()) {
                    this.observableMessageList.add(0, newMessage);
                } else {
                    this.observableMessageList.add(newMessage);
                }
            });
        }
    }

    /**
     * send msg via websocket if enter
     *
     * @param actionEvent occurs on enter
     */
    private void tfInputMessageOnEnter(ActionEvent actionEvent) {
        String message = this.tfInputMessage.getText();
        this.tfInputMessage.clear();

        if (message != null && !message.isEmpty() && currentChannel != null) {
            JsonObject jsonMsg = JsonUtil.buildServerChatMessage(currentChannel.getId(), message);
            editor.getWebSocketManager().sendChannelChatMessage(jsonMsg.toString());
        }
    }

    /**
     * handles channel message by adding it to the data model
     *
     * @param msg message from the server on the channel
     */
    private void handleChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (jsonObject.getString(CHANNEL).equals(currentChannel.getId())) {
            Message message = new Message();
            message.setChannel(currentChannel);
            message.setTimestamp(jsonObject.getJsonNumber(TIMESTAMP).longValue());
            message.setFrom(jsonObject.getString(FROM));
            message.setText(jsonObject.getString(TEXT));

            this.editor.addNewChannelMessage(message);
        } else {
            Channel channel = categoryTreeViewController.getChannelMap().get(jsonObject.getString(CHANNEL));
            if (channel != null) {
                channel.setRead(false);
            }
        }
    }

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param members JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JsonArray members) {
        for (int index = 0; index < members.toArray().length; index++) {

            String name = members.getJsonObject(index).getString(NAME);
            String id = members.getJsonObject(index).getString(ID);
            boolean onlineStatus = members.getJsonObject(index).getBoolean(ONLINE);

            editor.haveUserWithServer(name, id, onlineStatus, server);
        }

        // load categories
        categoryTreeViewController.initCategoryChannelList();

        // load list view
        ServerUserListView serverUserListView = new ServerUserListView();
        lvServerUsers.setCellFactory(serverUserListView);
        Platform.runLater(this::refreshLvUsers);
    }

    // Helping Methods

    private ContextMenu createContextMenuLeaveServer() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItemLeaveServer = new MenuItem("Leave Server");
        contextMenu.getItems().add(menuItemLeaveServer);
        menuItemLeaveServer.setOnAction(this::leaveServerAttention);
        return contextMenu;
    }

    /**
     * Updates ServerListView (list of displayed members).
     * Queries whether a channel is privileged:
     * If yes, it shows only members assigned to this channel.
     * If no, it shows all users of the server
     */
    public void refreshLvUsers() {
        if (channel.isPrivileged()) {
            users = channel.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus))
                    .collect(Collectors.toList());
        } else if (!channel.isPrivileged()) {
            users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus))
                    .collect(Collectors.toList());
        }
        Collections.reverse(users);
        this.lvServerUsers.getItems().removeAll();
        this.lvServerUsers.setItems(FXCollections.observableList(users));
        this.lvServerUsers.refresh();
    }
}
