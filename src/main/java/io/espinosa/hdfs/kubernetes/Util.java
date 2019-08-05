package io.espinosa.hdfs.kubernetes;

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
