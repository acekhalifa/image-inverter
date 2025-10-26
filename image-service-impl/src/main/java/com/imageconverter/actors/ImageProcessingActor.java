package com.imageconverter.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.image.BufferedImage;

public class ImageProcessingActor extends AbstractActor {

    @Getter
    @AllArgsConstructor
    public static final class ProcessChunkMessage{
        private final BufferedImage sourceImage;
        private final BufferedImage targetImage;
        private final int startY;
        private final int endY;
        private final int width;
        private final int chunkId;

    }

    @AllArgsConstructor
    @Getter
    public static final class ChunkProcessedMessage{
        private final int chunkId;
    }

    public static Props props() {
        return Props.create(ImageProcessingActor.class);
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessChunkMessage.class, this::onProcessChunk)
                .build();
    }

    private void onProcessChunk(ProcessChunkMessage message){
        BufferedImage source = message.getSourceImage();
        BufferedImage target = message.getTargetImage();
        int startY = message.getStartY();
        int endY = message.getEndY();
        int width = message.getWidth();

        // Process each pixel in the chunk
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = source.getRGB(x, y);
                int invertedRgb = invertColor(rgb);
                target.setRGB(x, y, invertedRgb);
            }
        }
        getSender().tell(new ChunkProcessedMessage(message.getChunkId()), getSelf());
    }

    private int invertColor(int rgb) {
        int alpha = (rgb >> 24) & 0xff;
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;

        // R -> G, G -> B, B -> R
        int newRed = blue;
        int newGreen = red;
        int newBlue = green;

        return (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
    }
}
