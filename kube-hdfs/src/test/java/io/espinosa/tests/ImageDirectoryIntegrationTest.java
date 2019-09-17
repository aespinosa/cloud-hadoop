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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.espinosa.hdfs.ImageDirectory;
import io.espinosa.hdfs.kubernetes.PersistentVolumeAsImageDirectory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

public class ImageDirectoryIntegrationTest {
    private static final String TEST_NAMESPACE = "default";
    private static final String POD_NAME = "namenode-0";
    @Rule public WireMockRule stubServer = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());
    private JSON json;

    @Before
    public void initialize() {
        json = new JSON();
    }

    @Test
    public void newPersistentVolumeIsUnformatted() throws Exception {
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient(), TEST_NAMESPACE, POD_NAME);

        V1PersistentVolume pv = new V1PersistentVolumeBuilder()
                .withNewMetadata()
                .withName("image-volume")
                .endMetadata()
                .build();

        stubPod(TEST_NAMESPACE, pv);

        Assert.assertEquals(false, imageDirectory.isFormatted());
    }

    @Test
    public void markAsFormatted() throws Exception {
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient(), TEST_NAMESPACE, POD_NAME);

        V1PersistentVolume pv = new V1PersistentVolumeBuilder()
                .withNewMetadata()
                .withName("image-volume")
                .endMetadata()
                .build();

        stubPod(TEST_NAMESPACE, pv);
        WireMock.stubFor(WireMock.put("/api/v1/persistentvolumes/image-volume"));

        imageDirectory.markAsFormatted();

        pv.getMetadata().setAnnotations(Map.of(PersistentVolumeAsImageDirectory.NAMENODE_IMAGE_FORMATTED, "true"));
        WireMock.verify(
                WireMock.putRequestedFor(WireMock.urlPathEqualTo("/api/v1/persistentvolumes/image-volume"))
                .withRequestBody(
                        WireMock.equalToJson(json.serialize(pv))
                )
        );
    }

    @Test
    public void annotatedPersistentVolumeIsFormatted() throws Exception {
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient(), TEST_NAMESPACE, POD_NAME);

        V1PersistentVolume pv = new V1PersistentVolumeBuilder()
                .withNewMetadata()
                .withName("image-volume")
                .withAnnotations(Map.of(PersistentVolumeAsImageDirectory.NAMENODE_IMAGE_FORMATTED, "true"))
                .endMetadata()
                .build();

        stubPod(TEST_NAMESPACE, pv);

        Assert.assertEquals(true, imageDirectory.isFormatted());
    }

    private ApiClient createApiClient() {
        ApiClient client = new ApiClient();
//        client.setDebugging(true);
        client.setBasePath(stubServer.baseUrl());
        return client;
    }

    private void stubPod(String namespace, V1PersistentVolume pv) {
        WireMock.stubFor(
                WireMock.get("/api/v1/persistentvolumes/" + pv.getMetadata().getName())
                        .willReturn(
                                aJsonResponse(pv)
                        )
        );

        V1PersistentVolumeClaim pvc = new V1PersistentVolumeClaimBuilder()
                .withNewMetadata()
                .withName("image-namenode-0")
                .endMetadata()
                .withNewSpec()
                .withVolumeName(pv.getMetadata().getName())
                .endSpec()
                .build();
        V1Volume volume = new V1VolumeBuilder()
                .withName("image")
                .withNewPersistentVolumeClaim()
                .withClaimName(pvc.getMetadata().getName())
                .endPersistentVolumeClaim()
                .build();
        V1Pod pod = new V1PodBuilder()
                .withNewMetadata()
                .withName("namenode-0")
                .endMetadata()
                .withNewSpec()
                .withVolumes(volume)
                .endSpec()
                .build();


        WireMock.stubFor(
                WireMock.get("/api/v1/namespaces/" + namespace + "/persistentvolumeclaims/" + pvc.getMetadata().getName())
                        .willReturn(
                                aJsonResponse(pvc)
                        )
        );
        WireMock.stubFor(
                WireMock.get("/api/v1/namespaces/" + namespace + "/pods/" + pod.getMetadata().getName())
                        .willReturn(
                                aJsonResponse(pod)
                        )
        );
    }

    private ResponseDefinitionBuilder aJsonResponse(Object o) {
        return WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(json.serialize(o));
    }
}
