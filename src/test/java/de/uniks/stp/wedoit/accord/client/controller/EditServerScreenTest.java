package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.model.Invitation;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EditServerScreenTest extends ApplicationTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    WebSocketClient webSocketClientMock;
    @Mock
    WebSocketClient chatWebSocketClientMock;
    private StageManager stageManager;
    private LocalUser localUser;
    private Server server;
    @Mock
    private RestClient restMock;
    @Mock
    private HttpResponse<JsonNode> res;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackArgumentCaptor;
    private Options oldOptions;

    @Override
    public void start(Stage stage) {
        this.stageManager = new StageManager();
        this.oldOptions = new Options();
        stageManager.getResourceManager().loadOptions(oldOptions);
        stageManager.getResourceManager().saveOptions(new Options().setRememberMe(false));
        stageManager.getResourceManager().saveOptions(new Options().setLanguage("en_GB"));

        this.stageManager.start(stage);

        //create localUser to skip the login screen and create server to skip the MainScreen
        this.localUser = this.stageManager.getEditor().haveLocalUser("Alice", "userKey123");
        this.server = this.stageManager.getEditor().haveServer(localUser, "id456", "AliceServer");
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), webSocketClientMock);
        this.stageManager.getEditor().getWebSocketManager().haveWebSocket(CHAT_USER_URL + this.stageManager.getEditor().getWebSocketManager().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWebSocketClientMock);

        this.stageManager.getEditor().getRestManager().setRestClient(restMock);
        this.stageManager.initView(ControllerEnum.SERVER_SCREEN, server, null);
    }


    /**
     * Mock the rest client's getServers method and create a callback
     *
     * @param restClientJson JsonObject, which one should return from the rest client as JsonNode
     */
    public void mockRestExplicitServer(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).getExplicitServerInformation(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    public void mockRestChangeServerName(JsonObject restClientJson) {
        // mock rest client
        when(res.getBody()).thenReturn(new JsonNode(restClientJson.toString()));

        verify(restMock, atLeastOnce()).changeServerName(anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
    }

    @Test
    public void editServerScreenOpens() {
        localUser.setId("owner123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRestExplicitServer(jsonObject);
        Label labelServerName = lookup("#lbServerName").query();
        Assert.assertEquals(labelServerName.getText(), server.getName());

        openEditServerScreen();
        WaitForAsyncUtils.waitForFxEvents();
        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle());
    }

    @Test
    public void editServerScreenOpensAsAdmin() {
        localUser.setId("owner123");
        JsonObject jsonObject = buildServerInformationWithTwoMembers();
        mockRestExplicitServer(jsonObject);

        openEditServerScreen();

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle());

        // Assert that Pop-Up Window shows correct widgets
        VBox vBoxAdminOnly = lookup("#vBoxAdminOnly").query();
        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        Button btnCreateInvitation = lookup("#btnCreateInvitation").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();
        Button btnDelete = lookup("#btnDelete").query();

        Assert.assertFalse(vBoxAdminOnly.isDisabled());
        Assert.assertFalse(radioBtnTemporal.isDisabled());
        Assert.assertFalse(radioBtnMaxCount.isDisabled());
        Assert.assertFalse(btnCreateInvitation.isDisabled());
        Assert.assertFalse(tfInvitationLink.isDisabled());
        Assert.assertFalse(btnDelete.isDisabled());

        // Assert Radiobutton switch works properly
        Assert.assertTrue(radioBtnMaxCount.isSelected());
        Assert.assertTrue(tfMaxCountAmountInput.isEditable());

        clickOn("#radioBtnTemporal");

        Assert.assertTrue(radioBtnTemporal.isSelected());
        Assert.assertFalse(radioBtnMaxCount.isSelected());
        Assert.assertFalse(tfMaxCountAmountInput.isEditable());

        clickOn("#radioBtnMaxCount");

    }

    @Test
    public void changeServerNameSuccessful() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeSuccessful("TestServerName");
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        openEditServerScreen();

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle());

        Label lblError = lookup("#lblError").query();

        ((TextField) lookup("#tfNewServernameInput").query()).setText("TestServerName");

        clickOn("#btnSave");

        mockRestChangeServerName(serverChangeNameJson);

        // needed, so that we can assert that new name is set correctly, and lblError is also set correctly
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(server.getName(), "TestServerName");
        Assert.assertFalse(lblError.isVisible());
    }

    @Test
    public void changeServerNameFail() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeFailure();
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        openEditServerScreen();

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle());

        Label lblError = lookup("#lblError").query();

        ((TextField) lookup("#tfNewServernameInput").query()).setText("TestServerName");

        clickOn("#btnSave");

        mockRestChangeServerName(serverChangeNameJson);

        // needed, so that we can assert that new name is set correctly, and lblError is also set correctly
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lblError.getText(), "Error. Change Servername not successful!");
        Assert.assertTrue(lblError.isVisible());
        Assert.assertEquals(server.getName(), "AliceServer");
    }

    @Test
    public void showAttentionScreen() {
        localUser.setId("owner123");
        JsonObject serverInfoJson = buildServerInformationWithTwoMembers();
        JsonObject serverChangeNameJson = buildServerNameChangeFailure();
        mockRestExplicitServer(serverInfoJson);

        Assert.assertEquals(server.getName(), "AliceServer");

        openEditServerScreen();

        // Assert Pop-Up Window opens
        Assert.assertEquals("Edit Server", this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle());

        clickOn("#btnDelete");

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(this.stageManager.getStage(StageEnum.POPUP_STAGE).getTitle(), "Attention");
    }

    @Test
    public void createCountInvitationSuccessful() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        WaitForAsyncUtils.waitForFxEvents();
        openEditServerScreen();
        when(res.getBody()).thenReturn(new JsonNode(buildInvitationSuccessful().toString()));

        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();
        Label labelCopy = lookup("#labelCopy").query();

        clickOn(radioBtnMaxCount);
        Assert.assertTrue(tfMaxCountAmountInput.isEditable());
        tfMaxCountAmountInput.setText("15");
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyInt(), anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(buildInvitationSuccessful().getJsonObject(DATA).getString(LINK), tfInvitationLink.getText());
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());

        clickOn(tfInvitationLink);
        Assert.assertEquals("Copied", labelCopy.getText());
        Platform.runLater(() -> {
            Assert.assertEquals(Clipboard.getSystemClipboard().getString(), tfInvitationLink.getText());
        });

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void createTemporalInvitationSuccessful() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        openEditServerScreen();
        WaitForAsyncUtils.waitForFxEvents();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationSuccessful().toString()));

        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();

        clickOn(radioBtnTemporal);
        clickOn("#btnCreateInvitation");
        WaitForAsyncUtils.waitForFxEvents();

        verify(restMock).createInvite(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        ListView<Invitation> lvInvite = lookup("#lvInvitation").query();

        Assert.assertEquals(buildInvitationSuccessful().getJsonObject(DATA).getString(LINK), tfInvitationLink.getText());
        Invitation invitation = null;
        for (Invitation invite : server.getInvitations()) {
            if (invite.getId().equals(buildInvitationSuccessful().getJsonObject(DATA).getString(ID))) {
                invitation = invite;
            }
        }

        Assert.assertNotNull(invitation);
    }

    @Test
    public void createInvitationTestFailure() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        WaitForAsyncUtils.waitForFxEvents();
        openEditServerScreen();

        when(res.getBody()).thenReturn(new JsonNode(buildInvitationFailure().toString()));

        RadioButton radioBtnTemporal = lookup("#radioBtnTemporal").query();
        RadioButton radioBtnMaxCount = lookup("#radioBtnMaxCount").query();
        TextField tfMaxCountAmountInput = lookup("#tfMaxCountAmountInput").query();
        TextField tfInvitationLink = lookup("#tfInvitationLink").query();
        Label lblCountWarning = lookup("#lblCountWarning").query();

        clickOn(radioBtnMaxCount);
        clickOn("#btnCreateInvitation");
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());

        clickOn(radioBtnMaxCount);
        tfMaxCountAmountInput.setText("b");
        clickOn("#btnCreateInvitation");
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());

        tfMaxCountAmountInput.setText("0");
        clickOn("#btnCreateInvitation");
        Assert.assertEquals("Amount", tfMaxCountAmountInput.getPromptText());
        Assert.assertEquals("Insert Amount > 0", lblCountWarning.getText());

        clickOn(radioBtnMaxCount);
        tfMaxCountAmountInput.setText("15");
        clickOn("#btnCreateInvitation");

        WaitForAsyncUtils.waitForFxEvents();
        verify(restMock).createInvite(anyInt(), anyString(), anyString(), callbackArgumentCaptor.capture());
        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("generation failed", tfInvitationLink.getPromptText());

        clickOn(radioBtnTemporal);
        clickOn("#btnCreateInvitation");

        verify(restMock).createInvite(anyString(), anyString(), callbackArgumentCaptor.capture());

        callback = callbackArgumentCaptor.getValue();
        callback.completed(res);
        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals("generation failed", tfInvitationLink.getPromptText());

        WaitForAsyncUtils.waitForFxEvents();
        Label labelCopy = lookup("#labelCopy").query();

        clickOn(tfInvitationLink);
        Assert.assertEquals("First create invitation", labelCopy.getText());

        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void loadInvitationsFailureTest() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        openEditServerScreen();

        ListView<Invitation> lvInvite = lookup("#lvInvitation").query();

        when(res.getBody()).thenReturn(new JsonNode(loadInvitationFailure().toString()));
        verify(restMock).loadInvitations(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();
        Label labelCopy = lookup("#labelCopy").query();

        doubleClickOn(lvInvite);
        Assert.assertEquals("Select invitation", labelCopy.getText());

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lvInvite.getItems().size(), 0);
        List<String> list = new ArrayList<>();
        for (Invitation invite : server.getInvitations()) {
            list.add(invite.getId());
        }
        Assert.assertEquals(list.size(), 0);
        Assert.assertFalse(list.contains("invitationId3"));
        Assert.assertEquals("Error while loading invitations", ((Label) lookup("#lblError").query()).getText());
    }

    @Test
    public void loadAndDeleteInvitationsSuccessfulTest() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        openEditServerScreen();

        ListView<Invitation> lvInvite = lookup("#lvInvitation").query();

        when(res.getBody()).thenReturn(new JsonNode(loadInvitationSuccessful().toString()));
        verify(restMock).loadInvitations(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lvInvite.getItems().size(), 2);
        List<String> list = new ArrayList<>();
        for (Invitation invite : server.getInvitations()) {
            list.add(invite.getId());
        }
        Assert.assertEquals(list.size(), 2);
        Assert.assertTrue(list.contains("invitationId1"));
        Assert.assertTrue(list.contains("invitationId2"));
        Assert.assertFalse(list.contains("invitationId3"));

        Label labelCopy = lookup("#labelCopy").query();
        Label labelStatus = lookup("#lblInvitationStatus").query();
        Label labelStatusText = lookup("#lblInvitationStatusText").query();

        lvInvite.getSelectionModel().select(0);
        clickOn(lvInvite);
        Assert.assertTrue(labelStatus.getText().contains("" +
                (lvInvite.getSelectionModel().getSelectedItem().getMax() - lvInvite.getSelectionModel().getSelectedItem().getCurrent() + 1)));
        Assert.assertEquals(labelStatusText.getText(), "invitation status:");

        doubleClickOn(lvInvite);
        Assert.assertEquals("Copied", labelCopy.getText());
        Platform.runLater(() -> {
            Assert.assertEquals(Clipboard.getSystemClipboard().getString(), lvInvite.getItems().get(0).getLink());
        });

        WaitForAsyncUtils.waitForFxEvents();


        when(res.getBody()).thenReturn(new JsonNode(deleteInvitationSuccessful().toString()));
        lvInvite.getSelectionModel().select(0);
        clickOn("#btnDeleteInvitation");

        verify(restMock).deleteInvitation(anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());

        callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(labelStatus.getText(), "");
        Assert.assertEquals(labelStatusText.getText(), "");
        Assert.assertEquals(lvInvite.getItems().size(), 1);
        list = new ArrayList<>();
        for (Invitation invite : server.getInvitations()) {
            list.add(invite.getId());
        }
        Assert.assertEquals(list.size(), 1);
        Assert.assertFalse(list.contains("invitationId1"));
        Assert.assertTrue(list.contains("invitationId2"));
        Assert.assertFalse(list.contains("invitationId3"));

    }

    @Test
    public void deleteInvitationsFailureTest() {
        localUser.setId("owner123");
        mockRestExplicitServer(buildServerInformationWithTwoMembers());
        openEditServerScreen();

        ListView<Invitation> lvInvite = lookup("#lvInvitation").query();

        when(res.getBody()).thenReturn(new JsonNode(loadInvitationSuccessful().toString()));
        verify(restMock).loadInvitations(anyString(), anyString(), callbackArgumentCaptor.capture());

        Callback<JsonNode> callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lvInvite.getItems().size(), 2);
        List<String> list = new ArrayList<>();
        for (Invitation invite : server.getInvitations()) {
            list.add(invite.getId());
        }
        Assert.assertEquals(list.size(), 2);
        Assert.assertTrue(list.contains("invitationId1"));
        Assert.assertTrue(list.contains("invitationId2"));
        Assert.assertFalse(list.contains("invitationId3"));

        when(res.getBody()).thenReturn(new JsonNode(deleteInvitationFailure().toString()));
        lvInvite.getSelectionModel().select(0);
        clickOn("#btnDeleteInvitation");

        verify(restMock).deleteInvitation(anyString(), anyString(), anyString(), callbackArgumentCaptor.capture());

        callback = callbackArgumentCaptor.getValue();
        callback.completed(res);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertEquals(lvInvite.getItems().size(), 2);
        list = new ArrayList<>();
        for (Invitation invite : server.getInvitations()) {
            list.add(invite.getId());
        }
        Assert.assertEquals(list.size(), 2);
        Assert.assertTrue(list.contains("invitationId1"));
        Assert.assertTrue(list.contains("invitationId2"));
        Assert.assertFalse(list.contains("invitationId3"));
    }

    // help methods that build jsonObjects in order to mock RestClient answers.

    public void openEditServerScreen() {
        MenuButton serverMenuButton = lookup("#serverMenuButton").query();
        serverMenuButton.getItems().get(0).setId("test");
        clickOn(serverMenuButton).clickOn("#test");
    }

    private JsonObject loadInvitationSuccessful() {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                        .add(
                                Json.createObjectBuilder()
                                        .add("id", "invitationId1")
                                        .add("link", "https://ac.uniks.de/api/...invitationId1")
                                        .add("type", "count")
                                        .add("max", 29)
                                        .add("current", 2)
                                        .add("server", "serverId")

                        )
                        .add(
                                Json.createObjectBuilder()
                                        .add("id", "invitationId2")
                                        .add("link", "https://ac.uniks.de/api/...invitationId2")
                                        .add("type", "temporal")
                                        .add("max", -1)
                                        .add("current", -1)
                                        .add("server", "serverId")
                        )
                ).build();
    }

    private JsonObject loadInvitationFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createArrayBuilder()
                ).build();
    }

    public JsonObject deleteInvitationSuccessful() {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", "invitationId1")
                        .add("link", "https://ac.uniks.de/api/...invitationId1")
                        .add("type", "count")
                        .add("max", 29)
                        .add("current", 2)
                        .add("server", "serverId")
                ).build();
    }

    public JsonObject deleteInvitationFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                ).build();
    }

    private JsonObject buildServerNameChangeFailure() {
        return Json.createObjectBuilder().add("status", "failure").add("message", "Changing server name failed!")
                .add("data", Json.createObjectBuilder())
                .build();
    }

    public JsonObject buildInvitationSuccessful() {
        return Json.createObjectBuilder().add("status", "success")
                .add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", "invitationId")
                        .add("link", "https://ac.uniks.de/api/...invitationId")
                        .add("type", "temporal")
                        .add("max", -1)
                        .add("current", -1)
                        .add("server", "serverId")
                ).build();
    }

    public JsonObject buildInvitationFailure() {
        return Json.createObjectBuilder().add("status", "failure")
                .add("message", "")
                .add("data", Json.createObjectBuilder()).build();
    }


    public JsonObject buildServerInformationWithTwoMembers() {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", server.getId())
                        .add("name", server.getName())
                        .add("owner", "owner123")
                        .add("categories", Json.createArrayBuilder())
                        .add("members", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("id", "alice123").add("name", "Alice").add("description", "")
                                        .add("online", true))
                                .add(Json.createObjectBuilder().add("id", "bob123").add("name", "Bob").add("description", "")
                                        .add("online", true))))
                .build();
    }

    public JsonObject buildServerNameChangeSuccessful(String newServerName) {
        return Json.createObjectBuilder().add("status", "success").add("message", "")
                .add("data", Json.createObjectBuilder()
                        .add("id", server.getId())
                        .add("name", newServerName))
                .build();
    }


    @Override
    public void stop() {
        stageManager.getResourceManager().saveOptions(this.oldOptions);
        oldOptions = null;
        stageManager.stop();
        stageManager = null;
        localUser = null;
        server = null;
        restMock = null;
        res = null;
        webSocketClientMock = null;
        chatWebSocketClientMock = null;
        callbackArgumentCaptor = null;
        rule = null;
    }

}
