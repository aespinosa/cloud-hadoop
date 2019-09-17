package io.espinosa;
/*
 * Copyright 2019 Allan Espinosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
