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
