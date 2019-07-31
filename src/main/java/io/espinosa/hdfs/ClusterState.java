package io.espinosa.hdfs;

public interface ClusterState {
    public void markAsExistingCluster();

    public boolean isNewCluster();
}
