package com.imageconverter.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class ImageCoordinatorActor extends AbstractActor {
    private final int numWorkers =  Runtime.getRuntime().availableProcessors();;
    private final ActorRef workerRouter;

    public ImageCoordinatorActor(){
        this.workerRouter = getContext().actorOf(
                new RoundRobinPool(numWorkers).props(ImageProcessingActor.props()),
                "workerRouter");
    }

    public static Props props(){
        return Props.create(ImageCoordinatorActor.class, ImageCoordinatorActor::new);
    }

    @AllArgsConstructor
    @Getter
    public static class InvertImageMessage{
        private final BufferedImage sourceImage;
    }

    @AllArgsConstructor
    @Getter
    public static class ImageProcessedMessage{
        private final BufferedImage processedImage;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InvertImageMessage.class, this::onHandleInvertMessage)
                .build();
    }

    private void onHandleInvertMessage(InvertImageMessage message) {
        BufferedImage srcImage = message.getSourceImage();
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Calculate chunk size for parallel processing
        int chunkSize = Math.max(1, height / numWorkers);
        int totalChunks = (int) Math.ceil((double) height / chunkSize);

        // Create a context to track completion
        ProcessingContext context = new ProcessingContext(totalChunks, targetImage, getSender());

        getContext().become(waitingForCompletion(context));

        // Distribute work to worker actors
        int chunkId = 0;
        for (int startY = 0; startY < height; startY += chunkSize) {
            int endY = Math.min(startY + chunkSize, height);

            ImageProcessingActor.ProcessChunkMessage chunkMsg =
                    new ImageProcessingActor.ProcessChunkMessage(
                            srcImage, targetImage, startY, endY, width, chunkId++
                    );

            workerRouter.tell(chunkMsg, getSelf());
        }

    }


    @Getter
    private static class ProcessingContext {
        private final int totalChunks;
        private final Set<Integer> completedChunks;
        private final BufferedImage targetImage;
        private final ActorRef requester;

        public ProcessingContext(int totalChunks, BufferedImage targetImage, ActorRef requester) {
            this.totalChunks = totalChunks;
            this.completedChunks = new HashSet<>();
            this.targetImage = targetImage;
            this.requester = requester;
        }

        public void markChunkComplete(int chunkId) {
            completedChunks.add(chunkId);
        }

        public boolean isAllComplete() {
            return completedChunks.size() == totalChunks;
        }
    }

    private Receive waitingForCompletion(ProcessingContext context) {
        return receiveBuilder()
                .match(ImageProcessingActor.ProcessChunkMessage.class, msg -> {
                    context.markChunkComplete(msg.getChunkId());

                    if(context.isAllComplete()){
                        context.getRequester()
                                .tell(new ImageProcessedMessage(context.getTargetImage()), getSelf());
                    }
                    getContext().unbecome();
        })
                .build();
    }
}
