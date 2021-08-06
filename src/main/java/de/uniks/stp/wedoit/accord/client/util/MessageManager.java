package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.GameScreenController;
import de.uniks.stp.wedoit.accord.client.controller.SystemTrayController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import javafx.application.Platform;

import javax.json.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.GAME_RESULT_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.GAME_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.GAME_STAGE;

public class MessageManager {

    private final Editor editor;

    public MessageManager(Editor editor) {
        this.editor = editor;
    }

    /**
     * add message to privateChat of corresponding user
     *
     * @param message to add to the model
     */
    public void addNewPrivateMessage(PrivateMessage message) {

        if (message.getText().startsWith(GAME_PREFIX) && handleGameMessages(message)) return;

        if (message.getFrom().equals(editor.getLocalUser().getName())) {
            User user = editor.getUser(message.getTo());
            user.getPrivateChat().withMessages(message);
        } else {
            SystemTrayController systemTrayController = editor.getStageManager().getSystemTrayController();
            if (systemTrayController != null) {
                systemTrayController.displayPrivateMessageNotification(message);
            }
            User user = editor.getUser(message.getFrom());
            Chat privateChat = user.getPrivateChat();
            if (privateChat == null) {
                privateChat = new Chat().setName(user.getName()).setUser(user);
                user.setPrivateChat(privateChat);
            }
            message.setChat(privateChat);
            privateChat.withMessages(message);
            user.setChatRead(false);
            editor.updateUserChatRead(user);
        }
        editor.savePrivateMessage(message);
    }

    /**
     * @param message private message expected to have GAME_PREFIX as prefix
     * @return true if message should not be displayed in chat else false to display message
     */
    private boolean handleGameMessages(PrivateMessage message) {

        if(GAME_NOT_SUPPORTED.stream().anyMatch((e) -> message.getText().startsWith(e))) return true;

        //game messages
        /*
         * Game invites come from opponents
         * Game requests comes from localUser to opponent
         */
        System.out.println("Gamemessage: " +message);
        if (message.getText().equals(GAME_INVITE)) {
            if (message.getTo().equals(editor.getLocalUser().getName())) { //incoming !play :handshake:
                handleIncomingGameInvite(message.getFrom());
            } else { //outgoing !play :handshake:
                handleOutGoingGameInvite(message.getTo());
            }
        }
        System.out.println(editor.getLocalUser().isInGame());
        if (message.getText().equals(GAME_REVENGE) && editor.getLocalUser().isInGame()) {
            if (message.getTo().equals(editor.getLocalUser().getName())) { //incoming !revenge
                handleIncomingGameInvite(message.getFrom());
            } else { //outgoing !revenge
                handleOutGoingGameInvite(message.getTo());
            }
        }

        if (message.getText().equals(GAME_CLOSE)) {
            handleQuitGame(message);
        }

        if (message.getText().startsWith(GAME_PREFIX) && (message.getText().endsWith(GAME_ROCK) || message.getText().endsWith(GAME_PAPER) || message.getText().endsWith(GAME_SCISSORS))) {
            if (!message.getFrom().equals(editor.getLocalUser().getName()))
                editor.getUser(message.getFrom()).setGameMove(message.getText().substring(GAME_PREFIX.length() + GAME_CHOOSE_MOVE.length()));
            return true;
        }
        return false;

        /*if (message.getText().equals(GAME_INVITE) || message.getText().equals(GAME_REVENGE)) {
            if (message.getTo().equals(editor.getLocalUser().getName()))
                editor.getLocalUser().withGameInvites(editor.getUser(message.getFrom()));
            else
                editor.getLocalUser().withGameRequests(editor.getUser(message.getTo()));
        }

        if (message.getText().equals(GAME_ACCEPTS)) {
            if (!editor.getStageManager().getStage(StageEnum.GAME_STAGE).isShowing() || editor.getStageManager().getStage(StageEnum.GAME_STAGE).getTitle().equals("Result")) {
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(message.getTo().equals(editor.getLocalUser().getName()) ? message.getFrom() : message.getTo(), GAME_START);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                return true;

            } else {
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(message.getFrom().equals(editor.getLocalUser().getName()) ? message.getTo() : message.getFrom(), GAME_INGAME);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                return true;

            }
        } else if (message.getText().equals(GAME_START) && (editor.getLocalUser().getGameInvites().contains(editor.getUser(message.getTo())) || editor.getLocalUser().getGameRequests().contains(editor.getUser(message.getFrom())))) {
            //Start game
            editor.getLocalUser().withoutGameInvites(editor.getUser(message.getTo()));
            editor.getLocalUser().withoutGameRequests(editor.getUser(message.getFrom()));

            Platform.runLater(() -> {
                if (message.getFrom().equals(editor.getLocalUser().getName()))
                    editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_INGAME, editor.getUser(message.getTo()), null);
                else
                    editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_INGAME, editor.getUser(message.getFrom()), null);
            });

        } else if (message.getText().equals(GAME_CLOSE) && editor.getStageManager().getStage(StageEnum.GAME_STAGE).isShowing()) {
            Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_RESULT, editor.getUser(message.getFrom()), null));
        }
*/
    }

    private void handleQuitGame(PrivateMessage message) {
        System.out.println("handle quit from " + message.getFrom() + " before: " + editor.getLocalUser().getGameInvites().size());
        LocalUser localUser = editor.getLocalUser();

        if (localUser.getName().equals(message.getFrom())) { // outgoing quit from user --> delete game request for this opponent
            User opponent = editor.getUser(message.getTo());
            localUser.withoutGameRequests(opponent);
            System.out.println("delete game request of: " +opponent.getName() + " new size: " +localUser.getGameRequests().size());
        } else { // incoming quit from opponent --> delete invite from this opponent
            User opponent = editor.getUser(message.getFrom());
            localUser.withoutGameInvites(opponent);
            System.out.println("delete game invite of: " +opponent.getName() + " new size: " +localUser.getGameInvites().size());

            // checks if quit comes from current (inGame) opponent (if yes --> leave game since opponent quit)
            GameScreenController controller = (GameScreenController) editor.getStageManager().getControllerMap().get(GAME_SCREEN_CONTROLLER);
            if (controller != null) {
                User inGameOpponent = controller.getOpponent();
                if (inGameOpponent.getName().equals(opponent.getName())) {
                    if (localUser.isInGame() && editor.getStageManager().getStage(StageEnum.GAME_STAGE).isShowing()) {
                        Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_RESULT, opponent, null));
                    }
                }
            }
        }

        /*// this is for incoming quit --> checks if quit comes from current opponent (if yes --> leave game since opponent quit)
        GameScreenController controller = (GameScreenController) editor.getStageManager().getControllerMap().get(GAME_SCREEN_CONTROLLER);
        if (controller != null) {
            User inGameOpponent = controller.getOpponent();
            if (inGameOpponent.getName().equals(opponent.getName())) {
                if (localUser.isInGame() && editor.getStageManager().getStage(StageEnum.GAME_STAGE).isShowing()) {
                    Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_RESULT, opponent, null));
                }
            }
        }
        if (localUser.getName().equals(message.getFrom())) { // outgoing quit from user --> delete game request for this opponent
            localUser.withoutGameRequests(opponent);
            System.out.println("delete game request: " +localUser.getGameRequests().size());
        } else { // incoming quit from opponent --> delete invite from this opponent
            localUser.withoutGameInvites(opponent);
            System.out.println("delete game invite: " +localUser.getGameInvites().size());
        }*/
    }

    private void handleOutGoingGameInvite(String to) {
        LocalUser localUser = editor.getLocalUser();
        User opponent = editor.getUser(to);

        System.out.println("handle outgoing gameInvite to: " +to);
        if (localUser.getGameInvites().contains(opponent)) {
            System.out.println("start game because of outgoing: " + to);
            startGame(opponent);
            clearAllGameInvitesAndRequests(opponent);
            return;
        }
        if (!localUser.getGameRequests().contains(opponent)) {
            System.out.println("Game request to: " +to);
            localUser.withGameRequests(opponent);
        }
    }

    private void startGame(User opponent) {
        editor.getLocalUser().setInGame(true);
        Platform.runLater(() -> editor.getStageManager().initView(ControllerEnum.GAME_SCREEN_INGAME, opponent, null));
    }

    private void handleIncomingGameInvite(String from) {
        LocalUser localUser = editor.getLocalUser();
        User opponent = editor.getUser(from);

        /* not needed anymore since invites get deleted when inGame
        if (localUser.isInGame()) { // user is already inGame --> dont start another game
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(from, GAME_INGAME);
            editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
            return;
        }*/
        System.out.println("incoming game message from: " +from);
        if (localUser.getGameRequests().contains(opponent)) { // the gameInvite is an answer to our gameInvite --> start Game
            System.out.println("start game because of incoming: " + from);
            startGame(opponent);
            // delete all invites/requests in order to start no other game when
            clearAllGameInvitesAndRequests(opponent);
            return;
        }
        if (!localUser.getGameInvites().contains(opponent)) { //first invite from this player
            System.out.println("Game invite from: " + from);
            localUser.withGameInvites(opponent);
        }
    }

    private void clearAllGameInvitesAndRequests(User opponent) {
        // send a !quit to every user
        LocalUser localUser = editor.getLocalUser();
        System.out.println("enter clear");
        localUser.withoutGameInvites(opponent);
        localUser.withoutGameRequests(opponent);
        if (localUser.getGameInvites() != null) {
            for (int i = 0; i < localUser.getGameInvites().size(); i++) {
                User user = localUser.getGameInvites().get(i);
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_CLOSE);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                localUser.withoutGameInvites(user);
            }
        }
        if (localUser.getGameRequests() != null) {
            for (int i = 0; i < localUser.getGameRequests().size(); i++) {
                User user = localUser.getGameRequests().get(i);
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(user.getName(), GAME_CLOSE);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
                localUser.withoutGameRequests(user);
            }
        }
        System.out.println("invites after clean: " + editor.getLocalUser().getGameInvites().size());
        System.out.println("requests after clean: " + editor.getLocalUser().getGameRequests().size());
    }

    /**
     * add message to channel chat
     *
     * @param message to add to the model
     */
    public void addNewChannelMessage(Message message) {
        message.getChannel().withMessages(message);
    }

    /**
     * adds messages to a channel
     */
    public void updateChannelMessages(Channel channel, List<Message> messages) {
        List<Message> channelMessages = channel.getMessages();
        for (Message message : messages) {
            boolean msgExists = false;
            for (Message channelMessage : channelMessages) {
                if (channelMessage.getId().equals(message.getId())) {
                    msgExists = true;
                    break;
                }
            }
            if (!msgExists) {
                channel.withMessages(message);
            }
        }
    }


    /**
     * updates message in the data model
     *
     * @param channel in which the message should be updated
     * @param message to update
     */
    public void updateMessage(Channel channel, Message message) {
        for (Message channelMessage : channel.getMessages()) {
            if (channelMessage.getId().equals(message.getId())) {
                channelMessage.setText(message.getText());
                return;
            }
        }
    }

    /**
     * deletes the message with given id
     *
     * @param channel           channel of the message
     * @param messageToDeleteId id of the message to delete
     */
    public void deleteMessage(Channel channel, String messageToDeleteId) {
        Message foundMessage = null;
        for (Message message : channel.getMessages()) {
            if (message.getId().equals(messageToDeleteId)) {
                foundMessage = message;
                break;
            }
        }
        if (foundMessage != null) {
            channel.withoutMessages(foundMessage);
        }
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     *
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(PrivateMessage message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));
        if (message.getText().startsWith(GAME_PREFIX))
            message.setText(message.getText().substring(GAME_PREFIX.length()));
        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     *
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(Message message, String text) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + text);
    }

    /**
     * creates a clean quote from a quote
     */
    /*public String cleanQuote(PrivateMessage item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[0];
        } else return item.getText();
    }*/

    /**
     * creates a clean quote from a quote
     */
    public String cleanQuote(Message item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[0];
        } else return item.getText();
    }

    /**
     * creates a clean message from a quote
     */
    public String cleanQuoteMessage(Message item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_MESSAGE);
            if (messages.length != 2) {
                return item.getText();
            }
            return messages[1];
        } else return item.getText();
    }

    /**
     * checks whether a message is a quote
     *
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    /*public boolean isQuote(PrivateMessage item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }*/

    /**
     * checks whether a message is a quote
     *
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(Message item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_MESSAGE)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_MESSAGE.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }
}
