package io.espinosa;

import io.espinosa.hdfs.ActualNameNode;
import io.espinosa.hdfs.ClusterState;
import io.espinosa.hdfs.ImageDirectory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hdfs.DFSConfigKeys;
//import org.apache.hadoop.hdfs.HdfsConfiguration;
//import org.apache.hadoop.hdfs.server.namenode.NameNode;
//import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KubeNameNode {
    private ClusterState clusterState;
    private ActualNameNode actualNameNode;
    private ImageDirectory imageDirectory;

    public KubeNameNode(ImageDirectory imageDirectory, ActualNameNode actualNameNode, ClusterState clusterState) {
        this.imageDirectory = imageDirectory;
        this.actualNameNode = actualNameNode;
        this.clusterState = clusterState;
    }

    public void start() {
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
