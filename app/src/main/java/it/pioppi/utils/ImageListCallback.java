package it.pioppi.utils;

import com.google.api.services.drive.model.File;

import java.util.List;

public interface ImageListCallback {
    void onResult(List<File> imageFiles);
    void onError(Exception e);
}
