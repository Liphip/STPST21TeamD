package de.uniks.stp.wedoit.accord.client.network;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import javax.json.Json;
import javax.json.JsonArray;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.DESCRIPTION;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class RestClient {

    /**
     * Login the user with given name and password.
     *
     * @param name     The Name of the user to be logged in.
     * @param password The Password for the USer to be logged in.
     * @param callback The Callback to be called after the Request.
     */
    public void login(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = JsonUtil.buildLogin(name, password).toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGIN_PATH)
                .header(NAME, name)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get all Servers the currently logged in User is Member and/or owner of.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Get the explicit Information of a given Server.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param serverId The ID of the Server the explicit Information are being requested of.
     * @param callback The Callback to be called after the Request.
     */
    public void getExplicitServerInformation(String userKey, String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Create a User with a given Name and Password.
     *
     * @param name     The Name of the User to be created.
     * @param password The Password for the User to be created.
     * @param callback The Callback to be called after the Request.
     */
    public void register(String name, String password, Callback<JsonNode> callback) {
        // Build Request Body
        String body = Json.createObjectBuilder().add(NAME, name).add(PASSWORD, password).build().toString();

        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .body(body);
        sendRequest(req, callback);
    }

    /**
     * Log out the currently logged in User.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void logout(String userKey, Callback<JsonNode> callback) {
        // Use UniRest to make register request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + LOGOUT_PATH)
                .header(USER_KEY, userKey);
        sendRequest(req, callback);
    }

    /**
     * Create a Server with a given Name.
     *
     * @param name     The Name of the Server to be created.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void createServer(String name, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get all Users who are currently online.
     *
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getOnlineUsers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + USERS_PATH)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Get the Categories of a given Server.
     *
     * @param serverId The ID of the Server the Categories are being requested for.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void getCategories(String serverId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Create a Category with a given Name.
     *
     * @param name     The Name of the Category to be created.
     * @param userKey  The userKey of the currently logged in User.
     * @param callback The Callback to be called after the Request.
     */
    public void createCategory(String serverId, String name, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Update a Category with a given Name.
     *
     * @param serverId   The ID of the Server the Channel is on.
     * @param categoryId The ID of the Category the Channel belongs to.
     * @param name       The Name the Channel should be changed to.
     * @param userKey    The userKey of the currently logged in User.
     * @param callback   The Callback to be called after the Request.
     */
    public void updateCategory(String serverId, String categoryId, String name, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Get the Channels of a given Category in a given Server.
     *
     * @param serverId   The ID of the Server the Category belongs to.
     * @param categoryId The ID of the Category the Channels are being requested for.
     * @param userKey    The userKey of the currently logged in User.
     * @param callback   The Callback to be called after the Request.
     */
    public void getChannels(String serverId, String categoryId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Create a Channel with a given Name.
     *
     * @param serverId   The ID of the Server the Channel should be created on.
     * @param categoryId The ID of the Category the Channel should be created on.
     * @param name       The Name of the Channel to be created.
     * @param type       The Type of the Channel to be created, Text or Voice.
     * @param privileged Privileged channel or normal channel.
     * @param userKey    The userKey of the currently logged in User.
     * @param callback   The Callback to be called after the Request.
     */
    public void createChannel(String serverId, String categoryId, String name, String type, boolean privileged, JsonArray members, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).add(TYPE, type).add(PRIVILEGED, privileged).add(MEMBERS, members).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Update a Channel with a given Name.
     *
     * @param serverId   The ID of the Server the Channel is on.
     * @param categoryId The ID of the Category the Channel belongs to.
     * @param channelId  The ID of the Channel that is updated.
     * @param name       The Name the Channel should be changed to.
     * @param privileged Privileged channel or normal channel.
     * @param userKey    The userKey of the currently logged in User.
     * @param callback   The Callback to be called after the Request.
     */
    public void updateChannel(String serverId, String categoryId, String channelId, String name, boolean privileged, JsonArray members, String userKey, Callback<JsonNode> callback) {
        // Build request Body
        String body = Json.createObjectBuilder().add(NAME, name).add(PRIVILEGED, privileged).add(MEMBERS, members).build().toString();

        // Use UniRest to create server
        HttpRequest<?> req = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS + SLASH + channelId)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * creates a request to get a temporal invitation link
     *
     * @param serverId id of the server for which the link is
     * @param userKey  userKey of the logged in local user
     * @param callback callback which have new link
     */
    public void createInvite(String serverId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + INVITES)
                .header(USER_KEY, userKey)
                .body(Json.createObjectBuilder().add(TYPE, TEMPORAL).build().toString());

        sendRequest(req, callback);
    }

    /**
     * creates a request to get a invitation link with count type and a maximum count of users who can use the link
     *
     * @param serverId id of the server for which the link is
     * @param userKey  userKey of the logged in local user
     * @param callback callback which have new link
     */
    public void createInvite(int max, String serverId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + INVITES)
                .header(USER_KEY, userKey)
                .body(Json.createObjectBuilder().add(TYPE, COUNT).add(MAX, max).build().toString());
        sendRequest(req, callback);
    }

    /**
     * Updates the name of a server
     *
     * @param serverId      The ID of the Server which name should be changed.
     * @param newServerName The new name of the Server
     * @param userKey       The userKey of the currently logged in User.
     * @param callback      The Callback to be called after the Request.
     */
    public void changeServerName(String serverId, String newServerName, String userKey, Callback<JsonNode> callback) {
        String body = Json.createObjectBuilder().add(NAME, newServerName).build().toString();

        HttpRequest<?> req = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId)
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * sends a request to delete a server.
     */
    public void deleteServer(String userKey, String serverId, Callback<JsonNode> callback) {
        // Use UniRest to delete server
        HttpRequest<?> req = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * sends a request to delete a channel.
     */
    public void deleteChannel(String userKey, String channelId, String categoryId, String serverId, Callback<JsonNode> callback) {
        // Use UniRest to delete channel
        HttpRequest<?> req = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS + SLASH + channelId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * sends a request to delete a category.
     */
    public void deleteCategory(String userKey, String categoryId, String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Try to join a server with the given invitation link
     *
     * @param callback The Callback to be called after the Request.
     */
    public void joinServer(LocalUser localUser, String invitationLink, Callback<JsonNode> callback) {
        // Build Request Body
        String body = JsonUtil.buildLogin(localUser.getName(), localUser.getPassword()).toString();

        // Use UniRest to make login request
        HttpRequest<?> req = Unirest.post(invitationLink)
                .header(USER_KEY, localUser.getUserKey())
                .body(body);

        sendRequest(req, callback);
    }

    /**
     * Gets the last 50 Messages from timestamp
     *
     * @param userKey    userKey of localUser
     * @param serverId   The ID of the Server from which the messages should be loaded.
     * @param categoryId The ID of the Category from which the messages should be loaded.
     * @param channelId  The ID of the Channel from which the messages should be loaded.
     * @param timestamp  The time from where the message should be loaded
     * @param callback   The Callback to be called after the Request.
     */
    public void getChannelMessages(String userKey, String serverId, String categoryId, String channelId, String timestamp, Callback<JsonNode> callback) {
        // Build correct URL
        String url = REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH
                + categoryId + CHANNELS + SLASH + channelId + MESSAGES + QUESTION_MARK + TIMESTAMP
                + EQUALS + timestamp;
        HttpRequest<?> req = Unirest.get(url)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * sends a request to load a invitation
     */
    public void loadInvitations(String serverId, String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + INVITES)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * sends a request to delete a invitation
     */
    public void deleteInvitation(String userKey, String inviteId, String serverId, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + INVITES + SLASH + inviteId)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Send a Request and call the Callback asynchronously.
     *
     * @param req      The Request to be sent.
     * @param callback The Callback to be called after the Request.
     */
    private void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }

    /**
     * sends a request to leave a server.
     */
    public void leaveServer(String userKey, String serverID, Callback<JsonNode> callback) {
        // Use UniRest to leave server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverID + LEAVE)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    public void joinAudioChannel(String userKey, String serverId, String categoryId, String channelId, Callback<JsonNode> callback) {
        // Use UniRest to leave server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS + SLASH + channelId + JOIN)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    public void leaveAudioChannel(String userKey, String serverId, String categoryId, String channelId, Callback<JsonNode> callback) {
        // Use UniRest to leave server
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH + serverId + CATEGORIES + SLASH + categoryId + CHANNELS + SLASH + channelId + LEAVE)
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    public void updateMessage(String userKey, String newMessage, Message oldMessage, Callback<JsonNode> callback) {
        String body = Json.createObjectBuilder().add(TEXT, newMessage).build().toString();

        HttpRequest<?> req = Unirest.put(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH +
                        oldMessage.getChannel().getCategory().getServer().getId() + CATEGORIES + SLASH +
                        oldMessage.getChannel().getCategory().getId() + CHANNELS + SLASH +
                        oldMessage.getChannel().getId() + MESSAGES +
                        SLASH + oldMessage.getId())
                .header(USER_KEY, userKey)
                .body(body);

        sendRequest(req, callback);
    }

    public void deleteMessage(String userKey, Message message, Callback<JsonNode> callback) {

        HttpRequest<?> req = Unirest.delete(REST_SERVER_URL + API_PREFIX + SERVER_PATH + SLASH +
                        message.getChannel().getCategory().getServer().getId() + CATEGORIES + SLASH +
                        message.getChannel().getCategory().getId() + CHANNELS + SLASH +
                        message.getChannel().getId() + MESSAGES +
                        SLASH + message.getId())
                .header(USER_KEY, userKey);

        sendRequest(req, callback);
    }

    /**
     * Login the guest user with given name, password and temp key.
     *
     * @param callback The Callback to be called after the Request.
     */
    public void guestLogin(Callback<JsonNode> callback) {
        // Use UniRest to make guest login request
        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + TEMP);

        sendRequest(req, callback);
    }

    /**
     * posts a description for the local user
     *
     * @param callback The Callback to be called after the Request.
     */
    public void postDescription(Callback<JsonNode> callback, String userId, String userKey, String description) {
        // Use UniRest to make guest login request
        String body = Json.createObjectBuilder().add(TEXT, description).build().toString();

        HttpRequest<?> req = Unirest.post(REST_SERVER_URL + API_PREFIX + USERS_PATH + SLASH + userId + SLASH + DESCRIPTION)
                .header(USER_KEY, userKey).body(body);

        sendRequest(req, callback);
    }

    public void getCurrentGameForSteamUser(String UserSteamID, Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(STEAM_USER_SUMMERY_URL + UserSteamID);

        sendRequest(req, callback);
    }

    public void getSteamAppList(Callback<JsonNode> callback) {
        HttpRequest<?> req = Unirest.get(STEAM_APP_LIST_URL);

        sendRequest(req, callback);
    }
}
