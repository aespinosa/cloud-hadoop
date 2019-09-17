package io.espinosa.tests;
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
