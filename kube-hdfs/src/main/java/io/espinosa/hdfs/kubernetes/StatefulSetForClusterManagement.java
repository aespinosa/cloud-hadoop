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

import io.espinosa.hdfs.ClusterState;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1StatefulSet;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatefulSetForClusterManagement implements ClusterState {
    public static final String HDFS_EXISTING_CLUSTER_FLAG = "hdfs.espinosa.io/existing-cluster";
    private final ApiClient client;
    private String namespace;
    private String podName;

    public StatefulSetForClusterManagement(ApiClient client, String namespace, String podName) {
        this.client = client;
        this.namespace = namespace;
        this.podName = podName;
    }

    public static ClusterState createFromCluster() throws IOException {
        ApiClient client = Config.fromCluster();
        String namespace = Util.readNamespaceFromCluster();
        String podName = Util.readPodNameFromCluster();
        return new StatefulSetForClusterManagement(client, namespace, podName);
    }

    @Override
    public void markAsExistingCluster() throws Exception {
        V1StatefulSet statefulSet = getStatefulSet();
        Map<String, String> annotations = statefulSet.getMetadata().getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<String, String>();
        }
        annotations.put(HDFS_EXISTING_CLUSTER_FLAG, "true");
        statefulSet.getMetadata().setAnnotations(annotations);

        AppsV1Api api = new AppsV1Api(client);
        api.replaceNamespacedStatefulSet(statefulSet.getMetadata().getName(), namespace, statefulSet, null, null);
    }

    @Override
    public boolean isNewCluster() throws ApiException {
        V1StatefulSet statefulSet = getStatefulSet();
        Map<String, String> annotations = statefulSet.getMetadata().getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            return true;
        }
        return annotations.getOrDefault(HDFS_EXISTING_CLUSTER_FLAG, "false").equals("false");
    }

    private V1StatefulSet getStatefulSet() throws ApiException {
        CoreV1Api api = new CoreV1Api(client);
        V1Pod pod = api.readNamespacedPod(podName, namespace, null, null, null);
        // Assumption is that the running pod only has a single ownerReference which is the StatefulSet
        // responsible for its existence;
        String statefulSetName = pod.getMetadata().getOwnerReferences().get(0).getName();
        AppsV1Api appsApi = new AppsV1Api(client);
        return appsApi.readNamespacedStatefulSet(statefulSetName, namespace, null, null, null);
    }
}
