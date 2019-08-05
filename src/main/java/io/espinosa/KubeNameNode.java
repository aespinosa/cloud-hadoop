package io.espinosa;

import io.espinosa.hdfs.ActualNameNode;
import io.espinosa.hdfs.ClusterState;
import io.espinosa.hdfs.ImageDirectory;
import io.espinosa.hdfs.WrapperNameNode;
import io.espinosa.hdfs.kubernetes.PersistentVolumeAsImageDirectory;
import io.espinosa.hdfs.kubernetes.StatefulSetForClusterManagement;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.util.GenericOptionsParser;

public class KubeNameNode {
    private ClusterState clusterState;
    private ActualNameNode actualNameNode;
    private ImageDirectory imageDirectory;

    public KubeNameNode(ImageDirectory imageDirectory, ActualNameNode actualNameNode, ClusterState clusterState) {
        this.imageDirectory = imageDirectory;
        this.actualNameNode = actualNameNode;
        this.clusterState = clusterState;
    }

    public static void main(String argv[]) throws Exception {
        ImageDirectory imageDirectory = PersistentVolumeAsImageDirectory.createFromCluster();
        ClusterState clusterState = StatefulSetForClusterManagement.createFromCluster();
        // Slosh kubernetes values into the configuration
        HdfsConfiguration config = new HdfsConfiguration();
        GenericOptionsParser optionsParser = new GenericOptionsParser(config, argv);
        argv = optionsParser.getRemainingArgs();

        ActualNameNode actualNameNode = new WrapperNameNode(config);

        KubeNameNode kubeNameNode = new KubeNameNode(imageDirectory, actualNameNode, clusterState);

        kubeNameNode.start();
    }

    public void start() throws Exception {
        if (imageDirectory.isFormatted()) {
            imageDirectory.skipFormatting();
        } else {
            if (clusterState.isNewCluster()) {
                actualNameNode.format();
                clusterState.markAsExistingCluster();
            } else {
                actualNameNode.bootstrapStandby();
            }
            imageDirectory.markAsFormatted();
        }

        actualNameNode.start();
    }
}
