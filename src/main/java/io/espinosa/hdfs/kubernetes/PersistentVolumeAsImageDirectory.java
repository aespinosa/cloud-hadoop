package io.espinosa.hdfs.kubernetes;

import com.google.protobuf.Api;
import io.espinosa.hdfs.ImageDirectory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PersistentVolume;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Volume;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersistentVolumeAsImageDirectory implements ImageDirectory {
    public static final String NAMENODE_IMAGE_FORMATTED = "namenode.hdfs.espinosa.io/image-formatted";
    public static final String IMAGE_VOLUME_NAME = "image";
    private ApiClient apiClient;
    private String podName;
    private String podNameSpace;

    public PersistentVolumeAsImageDirectory(ApiClient client) {
        this.apiClient = client;
        // FIXME make this non-hardwired
        this.podNameSpace = "default";
        this.podName = "namenode-0";
    }

    @Override
    public boolean isFormatted() throws ApiException {
        V1PersistentVolume persistentVolume;
        persistentVolume = getPersistentVolumeFromPod();
        Map<String, String> annotations = persistentVolume.getMetadata().getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        return annotations.getOrDefault(NAMENODE_IMAGE_FORMATTED, "false").equals("true");
    }

    @Override
    public void skipFormatting() {

    }

    private V1PersistentVolume getPersistentVolumeFromPod() throws ApiException {
        CoreV1Api api = new CoreV1Api(apiClient);
        V1Pod pod = api.readNamespacedPod(podName, podNameSpace, null, null, null);
        Optional<V1Volume> volume = pod.getSpec()
                .getVolumes()
                .stream()
                .filter(
                        vol -> vol.getName().equals(IMAGE_VOLUME_NAME)
                )
                .findFirst();
        if (!volume.isPresent()) {
            return null;
        }
        String pvcName = volume.get().getPersistentVolumeClaim().getClaimName();
        V1PersistentVolumeClaim pvc = api.readNamespacedPersistentVolumeClaim(pvcName, podNameSpace, null, null, null);
        String pvName = pvc.getSpec().getVolumeName();
        return api.readPersistentVolume(pvName, null, null, null);
    }

    @Override
    public void markAsFormatted() throws ApiException {
        V1PersistentVolume persistentVolume = getPersistentVolumeFromPod();
        Map<String, String> annotations = persistentVolume.getMetadata().getAnnotations();
        if (annotations == null ) {
            annotations = new HashMap<String, String>();
        }
        annotations.put(NAMENODE_IMAGE_FORMATTED, "true");
        persistentVolume.getMetadata().setAnnotations(annotations);

        CoreV1Api api = new CoreV1Api(apiClient);
        String pvName = persistentVolume.getMetadata().getName();
        try {
            api.replacePersistentVolume(pvName, persistentVolume, null, null);
        } catch (ApiException e) {
            // TODO: Figure out what to do when this fails
        }
    }
}
