package io.espinosa.hdfs;

public interface ClusterState {
    public void markAsExistingCluster() throws Exception;

    public boolean isNewCluster() throws Exception;
}
