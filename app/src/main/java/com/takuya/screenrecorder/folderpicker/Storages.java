package com.takuya.screenrecorder.folderpicker;

public class Storages {
    private String path;
    private StorageType type;
    public Storages(String path, StorageType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public StorageType getType() {
        return type;
    }

    public enum StorageType {Internal, External}
}
