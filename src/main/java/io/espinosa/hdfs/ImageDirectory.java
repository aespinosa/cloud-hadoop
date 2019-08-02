package io.espinosa.hdfs;

public interface ImageDirectory {
    public boolean isFormatted() throws Exception;

    public void skipFormatting();

    public void markAsFormatted() throws Exception;
}
