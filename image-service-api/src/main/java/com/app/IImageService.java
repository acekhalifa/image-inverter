package com.app;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface IImageService {
    CompletionStage<String> uploadAndInvertImage(File imageFile, String filename);

    Optional<File> getInvertedImage(String filename);

}
