package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.LoginScreenController;
import de.uniks.stp.wedoit.accord.client.controller.MainScreenController;
import de.uniks.stp.wedoit.accord.client.controller.OptionsScreenController;
import de.uniks.stp.wedoit.accord.client.controller.WelcomeScreenController;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kong.unirest.Unirest;

public class StageManager extends Application {

    private static Editor editor;
    private static LocalUser model;
    private static LoginScreenController loginScreenController;
    private static MainScreenController mainScreenController;
    private static RestClient restClient;
    private static WelcomeScreenController welcomeScreenController;
    private static OptionsScreenController optionsScreenController;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        editor = new Editor();
        model = editor.haveLocalUser();
        restClient = new RestClient();

        showLoginScreen();
        stage.show();
    }

    @Override
    public void stop() {
        try {
            super.stop();
            Unirest.shutDown();
            cleanup();
        } catch (Exception e) {
            System.err.println("Error while shutdown program");
            e.printStackTrace();
        }
    }

    private static Stage stage;

    public static void showLoginScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/LoginScreen.fxml"));
            Scene scene = new Scene(root);

            loginScreenController = new LoginScreenController(root, model, editor, restClient);
            loginScreenController.init();

            //display
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.centerOnScreen();

            stage.setResizable(false);

        } catch (Exception e) {
            System.err.println("Error on showing start screen");
            e.printStackTrace();
        }
    }

    /**
     * load fxml of the MainScreen and show the MainScreen on the window
     */
    public static void showMainScreen() {
        cleanup();

        try {
            //load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/MainScreen.fxml"));
            Scene scene = new Scene(root);

            //init controller
            mainScreenController = new MainScreenController(root, model, editor);
            mainScreenController.init();

            // display
            stage.setTitle("Main");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error on showing MainScreen");
            e.printStackTrace();
        }
    }

    public static void showWelcomeScreen() {
        cleanup();

        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/WelcomeScreen.fxml"));
            Scene scene = new Scene(root);

            welcomeScreenController = new WelcomeScreenController(root, model, editor);
            welcomeScreenController.init();

            stage.setTitle("Welcome");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error on showing WelcomeScreen");
            e.printStackTrace();
        }
    }

    public static void showServerScreen() {

    }

    public static void showOptionsScreen() {
        cleanup();

        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/OptionsScreen.fxml"));
            Scene scene = new Scene(root);

            optionsScreenController = new OptionsScreenController(root, model, editor);
            optionsScreenController.init();

            stage.setTitle("Options");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error on showing OptionsScreen");
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        if (loginScreenController != null) {
            loginScreenController.stop();
            loginScreenController = null;
        }
        if (mainScreenController != null) {
            mainScreenController.stop();
            mainScreenController = null;
        }
        if (welcomeScreenController != null) {
            welcomeScreenController.stop();
            welcomeScreenController = null;
        }
        if (optionsScreenController != null) {
            optionsScreenController.stop();
            optionsScreenController = null;
        }
    }

}

