package it.pioppi.utils;

public interface ImageUploadCallback {
    void onSuccess(String imageUrl);
    void onFailure(Exception e);
}

