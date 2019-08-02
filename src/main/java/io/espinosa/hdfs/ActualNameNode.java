package io.espinosa.hdfs;

public interface ActualNameNode {
    public void start() throws Exception;

    public void format() throws Exception;

    public void bootstrapStandby() throws Exception;
}
