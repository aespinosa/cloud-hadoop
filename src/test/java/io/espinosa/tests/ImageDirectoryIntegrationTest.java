package io.espinosa.tests;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.espinosa.hdfs.ImageDirectory;
import io.espinosa.hdfs.kubernetes.PersistentVolumeAsImageDirectory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.JSON;
import io.kubernetes.client.models.*;
import org.junit.*;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class ImageDirectoryIntegrationTest {
    private static final String TEST_NAMESPACE = "default";
    @Rule public WireMockRule stubServer = new WireMockRule(wireMockConfig().dynamicPort());
    private JSON json;

    @Before
    public void initialize() {
        json = new JSON();
    }

    @Test
    public void newPersistentVolumeIsUnformatted() throws Exception {
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient());

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
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient());

        V1PersistentVolume pv = new V1PersistentVolumeBuilder()
                .withNewMetadata()
                .withName("image-volume")
                .endMetadata()
                .build();

        stubPod(TEST_NAMESPACE, pv);
        stubFor(put("/api/v1/persistentvolumes/image-volume"));

        imageDirectory.markAsFormatted();

        pv.getMetadata().setAnnotations(Map.of(PersistentVolumeAsImageDirectory.NAMENODE_IMAGE_FORMATTED, "true"));
        verify(
                putRequestedFor(urlPathEqualTo("/api/v1/persistentvolumes/image-volume"))
                .withRequestBody(
                        equalToJson(json.serialize(pv))
                )
        );
    }

    @Test
    public void annotatedPersistentVolumeIsFormatted() throws Exception {
        ImageDirectory imageDirectory = new PersistentVolumeAsImageDirectory(createApiClient());

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
        stubFor(
                get("/api/v1/persistentvolumes/" + pv.getMetadata().getName())
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


        stubFor(
                get("/api/v1/namespaces/" + namespace + "/persistentvolumeclaims/" + pvc.getMetadata().getName())
                        .willReturn(
                                aJsonResponse(pvc)
                        )
        );
        stubFor(
                get("/api/v1/namespaces/" + namespace + "/pods/" + pod.getMetadata().getName())
                        .willReturn(
                                aJsonResponse(pod)
                        )
        );
    }

    private ResponseDefinitionBuilder aJsonResponse(Object o) {
        return aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(json.serialize(o));
    }
}
