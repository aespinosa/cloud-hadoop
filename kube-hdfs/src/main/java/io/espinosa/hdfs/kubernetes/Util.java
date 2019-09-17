package io.espinosa.hdfs.kubernetes;
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

import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static String readNamespaceFromCluster() throws IOException {
        String namespaceFilePath = Config.SERVICEACCOUNT_ROOT + "/namespace";
        return new String(Files.readAllBytes(Paths.get(namespaceFilePath)));
    }

    public static String readPodNameFromCluster() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }
}
