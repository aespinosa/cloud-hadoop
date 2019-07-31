package io.espinosa.tests;

import io.espinosa.hdfs.ClusterState;
import io.espinosa.hdfs.ActualNameNode;
import io.espinosa.hdfs.ImageDirectory;
import io.espinosa.KubeNameNode;
import junit.framework.TestCase;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class KubeNameNodeTest extends TestCase {
    @Test
    public void testFormattedImageDirectorySkipsFormatting() {
        ImageDirectory imageDirectory = mock(ImageDirectory.class);
        ActualNameNode actualNameNode = mock(ActualNameNode.class);

        when(imageDirectory.isFormatted()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, null);

        nameNode.start();

        verify(imageDirectory).skipFormatting();
    }

    @Test
    public void testNewClusterFormatsImageDirectory() {
        ImageDirectory imageDirectory = mock(ImageDirectory.class);
        ActualNameNode actualNameNode = mock(ActualNameNode.class);
        ClusterState clusterState = mock(ClusterState.class);

        when(imageDirectory.isFormatted()).thenReturn(false);
        when(clusterState.isNewCluster()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, clusterState);
        nameNode.start();

        verify(actualNameNode).format();
        verify(imageDirectory).markAsFormatted();
        verify(clusterState).markAsExistingCluster();
    }

    @Test
    public void testExistingClusterFormatsViaBootstrap() {
        ImageDirectory imageDirectory = mock(ImageDirectory.class);
        ActualNameNode actualNameNode = mock(ActualNameNode.class);
        ClusterState clusterState = mock(ClusterState.class);

        when(imageDirectory.isFormatted()).thenReturn(false);
        when(clusterState.isNewCluster()).thenReturn(false);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, clusterState);
        nameNode.start();

        verify(actualNameNode).bootstrapStandby();
    }

    @Test
    public void testStartNameNodeAfterImageDirectoryChecks() {
        ActualNameNode actualNameNode = mock(ActualNameNode.class);
        ImageDirectory imageDirectory = mock(ImageDirectory.class);

        when(imageDirectory.isFormatted()).thenReturn(true);

        KubeNameNode nameNode = new KubeNameNode(imageDirectory, actualNameNode, null);
        nameNode.start();

        verify(actualNameNode).start();
    }
}
