package io.espinosa.tests;

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
