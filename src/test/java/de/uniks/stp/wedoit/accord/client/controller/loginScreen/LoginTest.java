package de.uniks.stp.wedoit.accord.client.controller.loginScreen;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;

import static org.mockito.Mockito.*;

public class LoginTest extends ApplicationTest {

    private Stage stage;
    private StageManager stageManager;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        this.stageManager = new StageManager();
        this.stageManager.start(stage);
        StageManager.showLoginScreen(restMock);
        this.stage.centerOnScreen();
        this.stage.setAlwaysOnTop(true);
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSuccessfulLogin() {

        String returnMessage = Json.createObjectBuilder()
                .add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("userKey", "c653b568-d987-4331-8d62-26ae617847bf")
                ).build().toString();

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        Assert.assertEquals("success", res.getBody().getObject().getString("status"));
        Assert.assertEquals("", res.getBody().getObject().getString("message"));
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", res.getBody().getObject().getJSONObject("data").getString("userKey"));

        Assert.assertEquals(username, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals("c653b568-d987-4331-8d62-26ae617847bf", stageManager.getEditor().getLocalUser().getUserKey());

        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(stage.getTitle(), "Main");
    }


    @Test
    public void testInvalidCredentials() {
        String returnMessage = Json.createObjectBuilder()
                .add("status", "failure")
                .add("message", "Invalid credentials")
                .add("data", Json.createObjectBuilder())
                .build().toString();

        //Mocking of RestClient login function
        when(res.getBody()).thenReturn(new JsonNode(returnMessage));

        //TestFX
        String username = "username";
        String password = "password";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnLogin");

        verify(restMock).login(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        Assert.assertEquals("failure", res.getBody().getObject().getString("status"));
        Assert.assertEquals("Invalid credentials", res.getBody().getObject().getString("message"));
        Assert.assertTrue(res.getBody().getObject().getJSONObject("data").isEmpty());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: red ; -fx-border-width: 2px ;", pwUserPw.getStyle());

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is wrong.", errorLabel.getText());

        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testMissingUsername() {
        //TestFX
        String password = "password";

        clickOn("#tfUserName");
        write("");

        clickOn("#pwUserPw");
        write(password);

        clickOn("#btnLogin");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is missing", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: red ; -fx-border-width: 2px ;", pwUserPw.getStyle());

        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

    @Test
    public void testMissingPassword() {

        //TestFX
        String username = "username";

        clickOn("#tfUserName");
        write(username);

        clickOn("#pwUserPw");
        write("");

        clickOn("#btnLogin");

        Label errorLabel = lookup("#lblError").query();
        Assert.assertEquals("Username or password is missing", errorLabel.getText());

        TextField tfUserName = lookup("#tfUserName").query();
        Assert.assertEquals("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;", tfUserName.getStyle());

        TextField pwUserPw = lookup("#pwUserPw").query();
        Assert.assertEquals("-fx-border-color: red ; -fx-border-width: 2px ;", pwUserPw.getStyle());

        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getName());
        Assert.assertEquals(null, stageManager.getEditor().getLocalUser().getUserKey());
    }

}