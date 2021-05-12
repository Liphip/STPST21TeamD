package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class JsonUtil {
    public static JsonObject parse(String json) {
        return Json.createReader(new StringReader(json)).readObject();
    }

    public static String stringify(JsonObject object) {
        return object.toString();
    }

    public static JsonObject buildLogin(String name, String password) {
        return Json.createObjectBuilder()
                .add(COM_NAME, name)
                .add(COM_PASSWORD, password)
                .build();
    }

    public static LocalUser parseTempUser(JsonObject tempUserJson) {
        return new LocalUser().setName(tempUserJson.getString(COM_NAME));
    }

    public static User parseOnlineUser(JsonObject onlineUserJson) {
        return new User().setId(onlineUserJson.getString(COM_ID))
                .setName(onlineUserJson.getString(COM_NAME))
                .setOnlineStatus(true);
    }

    public static User parseServerUser(JsonObject serverUserJson) {
        return parseOnlineUser(serverUserJson).setOnlineStatus(serverUserJson.getBoolean(COM_ONLINE));
    }

    public static List<User> parseServerUsers(JsonArray serverUsersJsonArray) {
        List<User> users = new ArrayList<>();
        serverUsersJsonArray.forEach((jsonValue) -> {
            users.add(parseServerUser(jsonValue.asJsonObject()));
        });
        return users;
    }

    public static Server parseServer(JsonObject serverJson) {
        return new Server().setId(serverJson.getString(COM_ID))
                .setName(serverJson.getString(COM_NAME));
    }

    public static List<Server> parseServers(JsonArray serversJsonArray) {
        List<Server> servers = new ArrayList<>();
        serversJsonArray.forEach((jsonValue) -> {
            servers.add(parseServer(jsonValue.asJsonObject()));
        });
        return servers;
    }

    public static Server parseServerDetails(JsonObject serverDetailsJson) {
        JsonArray categoriesJson = serverDetailsJson.getJsonArray(COM_CATEGORIES);
        List<Category> categories = new ArrayList<>();
        categoriesJson.forEach((jsonValue) -> {
            categories.add(new Category().setId(jsonValue.toString()));
        });
        List<User> members = parseServerUsers(serverDetailsJson.getJsonArray(COM_MEMBERS));
        return parseServer(serverDetailsJson)
                .withCategories(categories)
                .withMembers(members)
                .setOwner(serverDetailsJson.getString(COM_OWNER));
    }

    //TODO niklas
    /**
     * builds a category based on the server json answer
     * !!! no server and channels added
     *
     * @param categoryJson json msg from server with category information
     */
    public static Category parseCategory(JSONObject categoryJson) {
        return new Category().setId(categoryJson.getString(COM_ID))
                .setName(categoryJson.getString(COM_NAME));
    }

    //TODO niklas
    public static List<String> parseChannels(JSONObject categoryJson) {
        JSONArray channels = categoryJson.getJSONArray(COM_CHANNELS);
        List<String> channelIds = new ArrayList<>();
        for (Object channelId: channels) {
            channelIds.add(channelId.toString());
        }
        return channelIds;
    }

    //TODO niklas
    /**
     * builds a channel based on the server json answer
     * !!! no category and member added
     *
     * @param channelJson json msg from server with channel information
     */
    public static Channel parseChannel(JSONObject channelJson) {
        return new Channel().setId(channelJson.getString(COM_ID))
                .setName(channelJson.getString(COM_NAME))
                .setType(channelJson.getString(COM_TYPE))
                .setPrivileged(channelJson.getString(COM_PRIVILEGED).equals("true"));
    }

    //TODO niklas
    public static List<String> parseMembers(JSONObject channelJson) {
        JSONArray members = channelJson.getJSONArray(COM_CHANNELS);
        List<String> membersIds = new ArrayList<>();
        for (Object memberId: members) {
            membersIds.add(memberId.toString());
        }
        return membersIds;
    }


    public static JsonObject buildServerChatMessage (String channel, String message) {
        return Json.createObjectBuilder()
                .add("channel", channel)
                .add("message", message)
                .build();
    }

    public static JsonObject buildPrivateChatMessage (String to, String message) {
        return Json.createObjectBuilder()
                .add(COM_CHANNEL, "private")
                .add(COM_TO, to)
                .add(COM_MESSAGE, message)
                .build();
    }
}
