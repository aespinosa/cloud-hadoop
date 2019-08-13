package io.espinosa.hdfs;

public interface ActualZkfc {
    // Copied from ZKFailOverController.ERR_CODE_FORMAT_DENIED
    public static int FORMAT_DENIED = 2;
    public static int FORMAT_SUCCESSFUL = 0;

    public int format() throws Exception;

    public int start() throws Exception;
}
