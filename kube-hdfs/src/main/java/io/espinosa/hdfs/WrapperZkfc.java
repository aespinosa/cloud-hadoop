package io.espinosa.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ha.ZKFailoverController;
import org.apache.hadoop.hdfs.tools.DFSZKFailoverController;

public class WrapperZkfc implements ActualZkfc {
    public static final String[] FORMAT_ARGS = {"-formatZK", "-nonInteractive"};
    public static final String[] RUN_ARGS = {};

    private final Configuration config  ;

    public WrapperZkfc(Configuration config) {
        this.config = config;
    }

    @Override
    public int format() throws Exception {
        ZKFailoverController upstream;
        upstream = DFSZKFailoverController.create(config);
        return upstream.run(FORMAT_ARGS);
    }

    @Override
    public int start() throws Exception {
        ZKFailoverController upstream;
        upstream = DFSZKFailoverController.create(config);

        return upstream.run(RUN_ARGS);
    }
}
