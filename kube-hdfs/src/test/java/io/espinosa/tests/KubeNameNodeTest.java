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

import io.espinosa.hdfs.ClusterState;
import io.espinosa.hdfs.ActualNameNode;
import io.espinosa.hdfs.ImageDirectory;
import io.espinosa.KubeNameNode;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class KubeNameNodeTest extends TestCase {
    @Test
    public void testFormattedImageDirectorySkipsFormatting() throws Exception {
        ImageDirectory imageDirectory = Mockito.mock(ImageDirectory.class);
        ActualNameNode actualNameNode = Mockito.mock(ActualNameNode.class);

        Mockito.when(imageDirectory.isFormatted()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, null);

        nameNode.start();

        Mockito.verify(imageDirectory).skipFormatting();
    }

    @Test
    public void testNewClusterFormatsImageDirectory() throws Exception {
        ImageDirectory imageDirectory = Mockito.mock(ImageDirectory.class);
        ActualNameNode actualNameNode = Mockito.mock(ActualNameNode.class);
        ClusterState clusterState = Mockito.mock(ClusterState.class);

        Mockito.when(imageDirectory.isFormatted()).thenReturn(false);
        Mockito.when(clusterState.isNewCluster()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, clusterState);
        nameNode.start();

        Mockito.verify(actualNameNode).format();
        Mockito.verify(imageDirectory).markAsFormatted();
        Mockito.verify(clusterState).markAsExistingCluster();
    }

    @Test
    public void testExistingClusterFormatsViaBootstrap() throws Exception {
        ImageDirectory imageDirectory = Mockito.mock(ImageDirectory.class);
        ActualNameNode actualNameNode = Mockito.mock(ActualNameNode.class);
        ClusterState clusterState = Mockito.mock(ClusterState.class);

        Mockito.when(imageDirectory.isFormatted()).thenReturn(false);
        Mockito.when(clusterState.isNewCluster()).thenReturn(false);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, clusterState);
        nameNode.start();

        Mockito.verify(actualNameNode).bootstrapStandby();
    }

    @Test
    public void testStartNameNodeAfterImageDirectoryChecks() throws Exception {
        ActualNameNode actualNameNode = Mockito.mock(ActualNameNode.class);
        ImageDirectory imageDirectory = Mockito.mock(ImageDirectory.class);

        Mockito.when(imageDirectory.isFormatted()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, null);
        nameNode.start();

        Mockito.verify(actualNameNode).start();
    }
}
