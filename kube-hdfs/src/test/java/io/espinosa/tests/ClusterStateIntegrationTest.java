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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.espinosa.hdfs.ClusterState;
import io.espinosa.hdfs.kubernetes.StatefulSetForClusterManagement;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ClusterStateIntegrationTest {
    private static final String TEST_NAMESPACE = "default";
    private static final String POD_NAME = "namenode-0";
    @Rule public WireMockRule stubServer = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Test
    public void newCluster() throws Exception {
        ClusterState state = new StatefulSetForClusterManagement(createApiClient(), TEST_NAMESPACE, POD_NAME);

        V1StatefulSet statefulSet = new V1StatefulSetBuilder()
                .withKind("StatefulSet")
                .withNewMetadata()
                .withName("namenode")
                .withAnnotations(Map.of("foo", "bar"))
                .endMetadata()
                .build();
        stubPod(statefulSet);

        Assert.assertTrue(state.isNewCluster());
    }

    @Test
    public void existingCluster() throws Exception {
        ClusterState state = new StatefulSetForClusterManagement(createApiClient(), TEST_NAMESPACE, POD_NAME);

        V1StatefulSet statefulSet = new V1StatefulSetBuilder()
                .withKind("StatefulSet")
                .withNewMetadata()
                .withName("namenode")
                .withAnnotations(Map.of(StatefulSetForClusterManagement.HDFS_EXISTING_CLUSTER_FLAG, "true"))
                .endMetadata()
                .build();
        stubPod(statefulSet);

        Assert.assertFalse(state.isNewCluster());
    }

    @Test
    public void markAsExistingCluster() throws Exception {
        ClusterState state = new StatefulSetForClusterManagement(createApiClient(), TEST_NAMESPACE, POD_NAME);

        Map<String, String> annotations = new HashMap<String, String>();
        annotations.put("some-other", "annotation");

        V1StatefulSet statefulSet = new V1StatefulSetBuilder()
                .withKind("StatefulSet")
                .withNewMetadata()
                .withAnnotations(annotations)
                .withName("namenode")
                .endMetadata()
                .build();
        stubPod(statefulSet);
        stubServer.stubFor(
                WireMock.put("/apis/apps/v1/namespaces/" + TEST_NAMESPACE + "/statefulsets/namenode")
        );

        state.markAsExistingCluster();

        annotations.put(StatefulSetForClusterManagement.HDFS_EXISTING_CLUSTER_FLAG, "true");

        stubServer.verify(
                WireMock.putRequestedFor(WireMock.urlEqualTo("/apis/apps/v1/namespaces/" + TEST_NAMESPACE + "/statefulsets/namenode"))
        );
    }

    private ApiClient createApiClient() {
        ApiClient client = new ApiClient();
//        client.setDebugging(true);
        client.setBasePath(stubServer.baseUrl());
        return client;
    }

    private void stubPod(V1StatefulSet statefulSet) {
        JSON json = new JSON();
        V1OwnerReference ownerStatefulSet = new V1OwnerReferenceBuilder()
                .withKind(statefulSet.getKind())
                .withName(statefulSet.getMetadata().getName())
                .build();
        V1Pod pod = new V1PodBuilder()
                .withNewMetadata()
                .withName("namenode-0")
                .withOwnerReferences(ownerStatefulSet)
                .endMetadata()
                .build();

        stubServer.stubFor(
                WireMock.get("/apis/apps/v1/namespaces/" + TEST_NAMESPACE + "/statefulsets/namenode")
                .willReturn(
                        WireMock.okJson(json.serialize(statefulSet))
                )
        );
        stubServer.stubFor(
                WireMock.get("/api/v1/namespaces/" + TEST_NAMESPACE + "/pods/" + POD_NAME)
                .willReturn(
                        WireMock.okJson(json.serialize(pod))
                )
        );
    }
}
