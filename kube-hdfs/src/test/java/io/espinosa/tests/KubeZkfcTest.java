package io.espinosa.tests;
/*
 * Copyright 2019 Allan Espinosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.espinosa.KubeZkfc;
import io.espinosa.hdfs.ActualZkfc;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class KubeZkfcTest {
    @Test
    public void successfulFormat() throws Exception {
        ActualZkfc upstream = Mockito.mock(ActualZkfc.class);

        Mockito.when(upstream.format()).thenReturn(ActualZkfc.FORMAT_SUCCESSFUL);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(0, kube.start());
        Mockito.verify(upstream).start();
    }

    @Test
    public void idempotentFormatting() throws Exception {
        ActualZkfc upstream = Mockito.mock(ActualZkfc.class);

        Mockito.when(upstream.format()).thenReturn(ActualZkfc.FORMAT_DENIED);
        Mockito.when(upstream.start()).thenReturn(0);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(0, kube.start());
        Mockito.verify(upstream).start();
    }

    @Test
    public void otherFormatExceptions() throws Exception {
        ActualZkfc upstream = Mockito.mock(ActualZkfc.class);
        int otherExitCode = 128; // anything other than 2 or 0

        Mockito.when(upstream.format()).thenReturn(otherExitCode);

        KubeZkfc kube = new KubeZkfc(upstream);

        Assert.assertEquals(otherExitCode, kube.start());
        Mockito.verify(upstream, Mockito.never()).start();
    }
}
