package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;

import javax.json.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.ID;

public class ChannelManager {

    private final Editor editor;

    public ChannelManager(Editor editor){
        this.editor = editor;
    }

    /**
     * This method
     * <p>
     * - creates a channel with the given arguments
     * <p>
     * - updates a channel with the given name, type, privileged, category and members
     * if the channel has already been created
     * <p>
     * to update a channel use updateChannel()
     *
     * @param id id of the channel which channels compared by
     * @return category with given id and name and with server server
     */
    public Channel haveChannel(String id, String name, String type, Boolean privileged, Category category, JsonArray members, JsonArray audioMembers) {
        Server server = category.getServer();
        Channel channel = null;
        for (Channel channelIterator : category.getChannels()) {
            if (channelIterator.getId().equals(id)) {
                channel = channelIterator;
                break;
            }
        }
        if (channel == null) {
            channel = new Channel();
        }
        channel.setName(name).setPrivileged(privileged).setType(type).setId(id).setCategory(category).setRead(true);
        channel.withoutMembers(new ArrayList<>(channel.getMembers()));
        channel.withoutAudioMembers(channel.getAudioMembers());

        if(members != null){
            List<String> membersIds = new ArrayList<>();
            for (int index = 0; index < members.toArray().length; index++) {
                membersIds.add(members.getString(index));
            }
            if (privileged) {
                for (User user : server.getMembers()) {
                    if (membersIds.contains(user.getId())) {
                        channel.withMembers(user);
                    }
                }
            }
        }
        if(audioMembers != null){
            List<String> membersAudioIds = new ArrayList<>();
            for (int index = 0; index < audioMembers.toArray().length; index++) {
                membersAudioIds.add(audioMembers.getString(index));
            }
            for (User user : server.getMembers()) {
                if (membersAudioIds.contains(user.getId())) {
                    channel.withAudioMembers(user);
                }
            }
        }
        return channel;
    }

    /**
     * This method
     * <p>
     * updates a channel with the given name, privileged and members. Only name, privileged and members will upgraded
     *
     * @param id id of the channel which channels compared by
     * @return channel upgraded channel or null
     */
    public Channel updateChannel(Server server, String id, String name, String type, Boolean privileged, String categoryId, JsonArray members, JsonArray audioMembers) {
        for (Category category : server.getCategories()) {
            if (category.getId().equals(categoryId)) {
                return haveChannel(id, name, type, privileged, category, members, audioMembers);
            }
        }
        return null;
    }

    /**
     * This method gives the category channels which are created with the data of the JSONArray
     *  @param category                  category which gets the channels
     * @param categoriesChannelResponse server answer for channels of the category
     */
    public void haveChannels(Category category, JsonArray categoriesChannelResponse) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(categoriesChannelResponse);

        List<String> channelIds = new ArrayList<>();
        for (Channel channel : category.getChannels()) {
            channelIds.add(channel.getId());
        }
        for (int index = 0; index < categoriesChannelResponse.toArray().length; index++) {

            if (!channelIds.contains(categoriesChannelResponse.getJsonObject(index).getString(ID))) {
                Channel channel = JsonUtil.parseChannel(categoriesChannelResponse.getJsonObject(index)).setRead(true);
                channel.setCategory(category);
                List<String> memberIds = JsonUtil.parseMembers(categoriesChannelResponse.getJsonObject(index));
                for (String memberId : memberIds) {
                    User user = editor.getServerUserById(category.getServer(), memberId);

                    channel.withMembers(user);
                }
            }
        }
        category.getChannels();
    }

}
