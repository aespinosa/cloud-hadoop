package io.espinosa.hdfs;

public interface ImageDirectory {
    public boolean isFormatted();

    public void skipFormatting();

    public void markAsFormatted();
}
