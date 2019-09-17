package io.espinosa.hdfs;
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
