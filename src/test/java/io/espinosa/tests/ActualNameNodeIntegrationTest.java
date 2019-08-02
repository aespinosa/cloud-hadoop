package io.espinosa.tests;

import io.espinosa.hdfs.ActualNameNode;
import io.espinosa.hdfs.WrapperNameNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.junit.Test;

public class ActualNameNodeIntegrationTest {
    @Test
    public void formattingNameNode() throws Exception {
        Configuration config = new HdfsConfiguration();
        ActualNameNode nameNode = new WrapperNameNode(config);

        nameNode.format();
    }

    @Test
    public void formattingStandby() throws Exception {
        Configuration config = new HdfsConfiguration();
        ActualNameNode nameNode = new WrapperNameNode(config);

        // FIXME: will fail unless a proper HA configuration is built
        nameNode.bootstrapStandby();
    }

    @Test
    public void startingNameNode() throws Exception {
        Configuration config = new HdfsConfiguration();
        config.set("fs.defaultFS", "hdfs://localhost");
        ActualNameNode nameNode = new WrapperNameNode(config);

        // FIXME: fails since maven will not have a proper Hadoop classpath for HDFS
        nameNode.start();
    }
}
