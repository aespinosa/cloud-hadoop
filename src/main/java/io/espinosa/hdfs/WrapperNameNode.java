package io.espinosa.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.namenode.ha.BootstrapStandby;

import java.io.IOException;

public class WrapperNameNode implements ActualNameNode {
    private Configuration configuration;
    private NameNode nameNode;

    public WrapperNameNode(Configuration config) {
        this.configuration = config;
        disableReformat();
    }

    private void disableReformat() {
        configuration.set(DFSConfigKeys.DFS_REFORMAT_DISABLED, "true", "hdfs-kubernetes");
    }

    @Override
    public void start() throws IOException {
        nameNode = new NameNode(configuration);
        nameNode.join();
    }

    @Override
    public void format() throws IOException {
        NameNode.format(new Configuration(configuration));
    }

    @Override
    public void bootstrapStandby() throws IOException {
        BootstrapStandby.run(null, configuration);
    }
}
