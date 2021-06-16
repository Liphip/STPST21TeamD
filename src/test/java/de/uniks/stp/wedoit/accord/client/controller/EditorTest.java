package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EditorTest {

    private Editor editor;
    private LocalUser localUser;
    private Server server;
    private User user;

    @Before
    public void initEditor() {
        editor = new Editor();
        editor.haveAccordClient();
        editor.haveLocalUser();
        server = new Server();
        localUser = new LocalUser().setName("Amir").setId("testKey123");
        user = new User().setName("Gelareh").setId("021");
    }

    @Test
    public void testHaveLocalUser() {

        localUser = editor.haveLocalUser(localUser.getName(), localUser.getId());

        Assert.assertEquals(editor.getLocalUser().getId(), localUser.getId());
        Assert.assertEquals(editor.getLocalUser().getUserKey(), "testKey123");
        Assert.assertEquals(editor.getLocalUser().getName(), "Amir");
    }

    @Test
    public void testHaveServer() {
        server = editor.haveServer(localUser, "0098", "Accord");
        editor.setCurrentServer(server);

        Assert.assertNotNull(editor.getCurrentServer());
        Assert.assertEquals(server.getName(), "Accord");
        Assert.assertEquals(server, editor.getCurrentServer());
        Assert.assertTrue(localUser.getServers().contains(server));
    }

    @Test
    public void testHaveUserWithServer() {
        user = editor.haveUserWithServer(user.getName(), user.getId(), true, server);

        Assert.assertEquals(server.getName(), user.getServers().get(0).getName());
        Assert.assertEquals(server.getMembers().get(0).getName(), user.getName());
        Assert.assertTrue(server.getMembers().contains(user));
        Assert.assertTrue(server.getMembers().get(0).isOnlineStatus());

    }

    @Test
    public void testHaveUser() {
        localUser = editor.haveUser(user.getId(), user.getName());
        localUser.withUsers(user);

        Assert.assertEquals(localUser.getUsers().get(0).getName(), user.getName());
        Assert.assertEquals(localUser.getUsers().get(0).getName(), "Gelareh");
        Assert.assertTrue(localUser.getUsers().contains(user));
    }

    @Test
    public void testGetServerUserById() {
        Assert.assertNull(editor.getServerUserById(server, "021"));

        user.withServers(server);
        localUser.withUsers(user);

        Assert.assertEquals(user, editor.getServerUserById(server, "021"));
        Assert.assertTrue(localUser.getUsers().contains(editor.getServerUserById(server, "021")));
        Assert.assertEquals(user.getChannels(), editor.getServerUserById(server, "021").getChannels());
    }

}
