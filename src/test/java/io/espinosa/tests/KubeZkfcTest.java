package io.espinosa.tests;

import io.espinosa.hdfs.ActualZkfc;
import io.espinosa.KubeZkfc;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class KubeZkfcTest {
    @Test
    public void successfulFormat() throws Exception {
        ActualZkfc upstream = mock(ActualZkfc.class);

        when(upstream.format()).thenReturn(ActualZkfc.FORMAT_SUCCESSFUL);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(0, kube.start());
        verify(upstream).start();
    }

    @Test
    public void idempotentFormatting() throws Exception {
        ActualZkfc upstream = mock(ActualZkfc.class);

        when(upstream.format()).thenReturn(ActualZkfc.FORMAT_DENIED);
        when(upstream.start()).thenReturn(0);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(0, kube.start());
        verify(upstream).start();
    }

    @Test
    public void otherFormatExceptions() throws Exception {
        ActualZkfc upstream = mock(ActualZkfc.class);
        int otherExitCode = 128; // anything other than 2 or 0

        when(upstream.format()).thenReturn(otherExitCode);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(otherExitCode, kube.start());
        verify(upstream, never()).start();
    }
}
