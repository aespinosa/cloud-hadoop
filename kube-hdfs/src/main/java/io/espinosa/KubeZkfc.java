package io.espinosa;

import io.espinosa.hdfs.WrapperZkfc;
import io.espinosa.hdfs.ActualZkfc;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.util.ExitUtil;

public class KubeZkfc {
    private final ActualZkfc upstreamZkfc;

    public static void main(String args[]) throws Exception {
        Configuration config = new HdfsConfiguration();
        ActualZkfc upstreamZkfc = new WrapperZkfc(config);

        KubeZkfc kubeZkfc = new KubeZkfc(upstreamZkfc);
        try {
            System.exit(kubeZkfc.start());
        } catch (Throwable t) {
            ExitUtil.terminate(1, t);
        }
    }

    public KubeZkfc(ActualZkfc upstream) {
        this.upstreamZkfc = upstream;
    }

    public int start() throws Exception {
        int formatStatus = upstreamZkfc.format();
        if (formatStatus != ActualZkfc.FORMAT_DENIED && formatStatus != ActualZkfc.FORMAT_SUCCESSFUL) {
            return formatStatus;
        }
        return upstreamZkfc.start();
    }
}
