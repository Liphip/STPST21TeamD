package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioReceive;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;

public class ChannelTreeView implements javafx.util.Callback<TreeView<Object>, TreeCell<Object>> {

    private final StageManager stageManager;

    public ChannelTreeView(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ChannelTreeCell();
    }

    private class ChannelTreeCell extends TreeCell<Object> {
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            this.getStyleClass().remove("newMessage");
            if (!empty) {
                if (item instanceof Category) {
                    this.setText(((Category) item).getName());
                    this.setContextMenu(addContextMenuCategory((Category) item));
                }
                if (item instanceof Channel) {
                    Channel channel = (Channel) item;
                    ImageView icon;
                    if(channel.getType().equals(TEXT)){
                        if(isSelected() && !stageManager.getModel().getOptions().isDarkmode()){
                            icon =  new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/edit.png"))));
                        }
                        else{
                            icon =  new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/edit_dark.png"))));
                        }
                        icon.setFitHeight(13);
                        icon.setFitWidth(13);
                    }
                    else{
                        if(isSelected() && !stageManager.getModel().getOptions().isDarkmode()){
                            icon =  new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound.png"))));
                        }
                        else{
                            icon =  new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound_dark.png"))));
                        }
                        icon.setFitHeight(15);
                        icon.setFitWidth(15);
                    }
                    this.setGraphic(icon);
                    this.setText(channel.getName());
                    this.setContextMenu(addContextMenuChannel((Channel) item));
                    if (!((Channel) item).isRead()) {
                        this.getStyleClass().add("newMessage");
                    }
                }
                if(item instanceof User){
                    User user = (User) item;
                    this.setText(user.getName());
                    if(stageManager.getEditor().getLocalUser().getAudioChannel() != null && stageManager.getEditor().getLocalUser().getAudioChannel().getId().equals(user.getAudioChannel().getId())) {
                        if (user.isMuted()) {
                            this.setContextMenu(addContextMenuUnMute(user, this));
                            ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/sound-off-red.png"))));
                            icon.setFitHeight(13);
                            icon.setFitWidth(13);
                            this.setGraphic(icon);
                        } else {
                            this.setContextMenu(addContextMenuMute(user, this));
                            this.setGraphic(null);
                        }
                    }
                }
            } else {
                this.setText(null);
                this.setContextMenu(null);
                this.setGraphic(null);
            }
        }


    }

    private ContextMenu addContextMenuChannel(Channel item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("ADD_CATEGORY"));
        MenuItem menuItem2 = new MenuItem("- " + LanguageResolver.getString("ADD_CHANNEL"));
        MenuItem menuItem3 = new MenuItem("- " + LanguageResolver.getString("EDIT_CHANNEL"));
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("ADD_CATEGORY"), "CreateCategoryScreen", CREATE_CATEGORY_SCREEN_CONTROLLER, false, null, null));
        menuItem2.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("ADD_CHANNEL"), "EditChannelScreen", CREATE_CHANNEL_SCREEN_CONTROLLER, true, item.getCategory(), null));
        menuItem3.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("EDIT_CHANNEL"), "EditChannelScreen", EDIT_CHANNEL_SCREEN_CONTROLLER, true, item, null));

        return contextMenu;
    }

    private ContextMenu addContextMenuCategory(Category item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- " + LanguageResolver.getString("ADD_CATEGORY"));
        MenuItem menuItem2 = new MenuItem("- " + LanguageResolver.getString("EDIT_CATEGORY"));
        MenuItem menuItem3 = new MenuItem("- " + LanguageResolver.getString("ADD_CHANNEL"));
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("ADD_CATEGORY"), "CreateCategoryScreen", CREATE_CATEGORY_SCREEN_CONTROLLER, false, null, null));
        menuItem2.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("EDIT_CATEGORY"), "EditCategoryScreen", EDIT_CATEGORY_SCREEN_CONTROLLER, false, item, null));
        menuItem3.setOnAction((event) -> this.stageManager.initView(POPUPSTAGE, LanguageResolver.getString("EDIT_CHANNEL"), "EditChannelScreen", CREATE_CHANNEL_SCREEN_CONTROLLER, true, item, null));

        return contextMenu;
    }

    public ContextMenu addContextMenuMute(User user, TreeCell cell){
        if(!user.getName().equals(stageManager.getEditor().getLocalUser().getName())) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("- " + LanguageResolver.getString("MUTE"));
            menuItem.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().muteUser(user);
                cell.getTreeView().refresh();
            });
            contextMenu.getItems().add(menuItem);
            return contextMenu;
        }
        return null;
    }

    public ContextMenu addContextMenuUnMute(User user, TreeCell cell){
        if(!user.getName().equals(stageManager.getEditor().getLocalUser().getName())) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("- " + LanguageResolver.getString("UNMUTE"));
            menuItem.setOnAction((event) -> {
                this.stageManager.getEditor().getAudioManager().unmuteUser(user);
                cell.getTreeView().refresh();
            });
            contextMenu.getItems().add(menuItem);
            return contextMenu;
        }
        return null;
    }

}
