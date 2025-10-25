package com.app;

import java.io.File;
import java.util.concurrent.CompletionStage;

public interface IImageService {
    CompletionStage<String> uploadAndInvertImage(File imageFile, String filename);

    File getInvertedImage(String filename);

}
