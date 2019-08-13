package io.espinosa.hdfs.kubernetes;

import io.espinosa.hdfs.ImageDirectory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PersistentVolume;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Volume;
import io.kubernetes.client.util.Config;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersistentVolumeAsImageDirectory implements ImageDirectory {
    public static final String NAMENODE_IMAGE_FORMATTED = "namenode.hdfs.espinosa.io/image-formatted";
    public static final String IMAGE_VOLUME_NAME = "image";
    private ApiClient apiClient;
    private String podName;
    private String podNameSpace;

    public PersistentVolumeAsImageDirectory(ApiClient client, String podNameSpace, String podName) {
        this.apiClient = client;
        this.podNameSpace = podNameSpace;
        this.podName = podName;
    }

    public static ImageDirectory createFromCluster() throws IOException, UnknownHostException {
        ApiClient client = Config.fromCluster();
        String namespace = Util.readNamespaceFromCluster();
        String podName = Util.readPodNameFromCluster();

        return new PersistentVolumeAsImageDirectory(client, namespace, podName);
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
        api.replacePersistentVolume(pvName, persistentVolume, null, null);
    }
}
