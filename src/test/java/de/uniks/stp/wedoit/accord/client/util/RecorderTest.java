package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.Options;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecorderTest {
    private Recorder recorder;
    private Editor editor;

    @Before
    public void initRecorderTest() {
        editor = new Editor();
        editor.setStageManager(new StageManager());
        AccordClient client = editor.haveAccordClient();
        client.setOptions(new Options());
    }

    @Test
    public void recordTest() {

        recorder = new Recorder(null, editor);
        recorder.start();
        Thread t = new Thread(new Recorder(null, editor));
        t.start();
        Assert.assertTrue(t.isAlive());
        recorder.stop();

    }


}
