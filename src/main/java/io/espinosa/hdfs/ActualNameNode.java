package io.espinosa.hdfs;

public interface ActualNameNode {
    public void start();

    public void format();

    public void bootstrapStandby();
}
