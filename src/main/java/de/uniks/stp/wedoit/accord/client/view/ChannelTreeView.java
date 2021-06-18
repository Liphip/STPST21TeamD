package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

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
                    this.setText("#" + ((Category) item).getName());
                    this.setContextMenu(addContextMenuCategory((Category) item));
                }
                if (item instanceof Channel) {
                    this.setText(((Channel) item).getName());
                    this.setContextMenu(addContextMenuChannel((Channel) item));
                    if (!((Channel) item).isRead()) {
                        this.getStyleClass().add("newMessage");
                    }
                }
            } else {
                this.setText(null);
                this.setContextMenu(null);
            }
        }
    }

    private ContextMenu addContextMenuChannel(Channel item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- add category");
        MenuItem menuItem2 = new MenuItem("- add channel");
        MenuItem menuItem3 = new MenuItem("- edit channel");
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> {
            this.stageManager.showCreateCategoryScreen();
        });
        menuItem2.setOnAction((event) -> {
            this.stageManager.showCreateChannelScreen(item.getCategory());
        });
        menuItem3.setOnAction((event) -> {
            this.stageManager.showEditChannelScreen(item);
        });

        return contextMenu;
    }

    private ContextMenu addContextMenuCategory(Category item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- add category");
        MenuItem menuItem2 = new MenuItem("- edit category");
        MenuItem menuItem3 = new MenuItem("- add channel");
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> {
            this.stageManager.showCreateCategoryScreen();
        });
        menuItem2.setOnAction((event) -> {
            this.stageManager.showEditCategoryScreen(item);
        });
        menuItem3.setOnAction((event) -> {
            this.stageManager.showCreateChannelScreen(item);
        });

        return contextMenu;
    }

}
