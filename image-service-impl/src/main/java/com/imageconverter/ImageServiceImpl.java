package com.imageconverter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import com.app.IImageService;
import com.imageconverter.actors.ImageCoordinatorActor;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ImageServiceImpl implements IImageService {

    private final ActorSystem actorSystem;
    private final ActorRef coordinatorActor;
    private final String storagePath = "public/inverted-images";

    @Inject
    public ImageServiceImpl(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;


        // Create coordinator actor
        this.coordinatorActor = actorSystem.actorOf(
                ImageCoordinatorActor.props(),
                "imageCoordinator"
        );
        File dir = new File(storagePath);
        if (!dir.exists()) dir.mkdirs();;
    }

    @Override
    public CompletionStage<String> uploadAndInvertImage(File imageFile, String originalFilename) {
        try {
            // Read the uploaded image
            BufferedImage sourceImage = ImageIO.read(imageFile);

            if (sourceImage == null) {
                throw new IllegalArgumentException("Invalid image format");
            }

            final String prefix = "inverted";
            // Generate unique filename

            String extension = getFileExtension(originalFilename);
            String newFilename = prefix + imageFile.getName() + "." + extension;

            ImageCoordinatorActor.InvertImageMessage message =
                    new ImageCoordinatorActor.InvertImageMessage(sourceImage);

            // Use Akka's ask pattern to get result asynchronously
            return Patterns.ask(coordinatorActor, message, Duration.ofSeconds(60))
                    .thenApply(response -> {
                        ImageCoordinatorActor.ImageProcessedMessage result =
                                (ImageCoordinatorActor.ImageProcessedMessage) response;

                        // Save the inverted image
                        saveImage(result.getProcessedImage(), newFilename);
                        return newFilename;
                    })
                    .toCompletableFuture();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file", e);
        }
    }

    @Override
    public Optional<File> getInvertedImage(String filename) {
        File file = new File(storagePath + "/" + filename);
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    private void saveImage(BufferedImage image, String filename) {
        try {
            File outputFile = new File(storagePath + "/" + filename);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save inverted image", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
