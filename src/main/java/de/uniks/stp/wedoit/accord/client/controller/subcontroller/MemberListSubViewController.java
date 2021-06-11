package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.CreateChannelScreenController;
import de.uniks.stp.wedoit.accord.client.controller.EditChannelScreenController;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class MemberListSubViewController implements Controller {

    private final User user;
    private final Parent view;
    private final Controller controller;
    private HBox hBoxPlaceHolder;
    private VBox vBoxMemberName, vBoxCheckBox;
    private CheckBox checkBox;
    private Boolean isPrivilegedUser;

    public MemberListSubViewController(User user, Parent view, Controller controller, Boolean isPrivilegedUser) {
        this.user = user;
        this.view = view;
        this.controller = controller;
        this.isPrivilegedUser = isPrivilegedUser;
    }

    @Override
    public void init() {
        this.hBoxPlaceHolder = (HBox) this.view.lookup("#hBoxPlaceHolder");
        this.vBoxMemberName = (VBox) this.view.lookup("#vBoxMemberName");
        this.vBoxCheckBox = (VBox) this.view.lookup("#vBoxCheckBox");

        this.checkBox = new CheckBox();
        this.checkBox.setSelected(isPrivilegedUser);
        this.checkBox.setOnAction(this::checkBoxOnClick);
        this.vBoxMemberName.getChildren().add(new Label(user.getName()));
        this.vBoxCheckBox.getChildren().add(checkBox);
    }

    private void checkBoxOnClick(ActionEvent actionEvent) {
        if (controller.getClass().equals(CreateChannelScreenController.class)) {
            CreateChannelScreenController createChannelScreenController = (CreateChannelScreenController) controller;
            if (checkBox.isSelected()) {
                createChannelScreenController.addToUserList(user);
            } else if (!checkBox.isSelected()) {
                createChannelScreenController.removeFromUserList(user);
            }
        } else if (controller.getClass().equals(EditChannelScreenController.class)) {
            EditChannelScreenController createChannelScreenController = (EditChannelScreenController) controller;
            if (checkBox.isSelected()) {
                createChannelScreenController.addToUserList(user);
            } else if (!checkBox.isSelected()) {
                createChannelScreenController.removeFromUserList(user);
            }
        }
    }

    @Override
    public void stop() {
        this.checkBox.setOnAction(null);
    }
}
