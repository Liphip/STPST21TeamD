package de.uniks.stp.wedoit.accord.client.view;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

public class ChannelTreeView implements javafx.util.Callback<TreeView<Object>, TreeCell<Object>> {

    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ChannelTreeCell();
    }


    private static class ChannelTreeCell extends TreeCell<Object> {
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item instanceof Category) {
                    this.setText("#" + ((Category) item).getName());
                }
                if (item instanceof Channel) {
                    this.setText(((Channel) item).getName());
                    this.setContextMenu(addContextMenuChannel((Channel)item));
                }
            } else {
                this.setText(null);
            }
        }
    }

    private static ContextMenu addContextMenuChannel(Channel item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("- add category");
        MenuItem menuItem2 = new MenuItem("- add channel");
        MenuItem menuItem3 = new MenuItem("- edit channel");
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);

        menuItem1.setOnAction((event) -> {
            StageManager.showCreateCategoryScreen();
        });
        menuItem2.setOnAction((event) -> {
            StageManager.showCreateChannelScreen(item.getCategory());
        });
        menuItem3.setOnAction((event) -> {
            StageManager.showEditChannelScreen(item);
        });

        return contextMenu;
    }

}
