package controllers;

import com.app.IImageService;
import com.app.model.ImageResponse;
import play.mvc.Controller;


import com.encentral.scaffold.commons.ApiUtils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Tag(name = "Image Processing", description = "Operations for image color inversion using Akka concurrency")
public class ImageController extends HomeController {

    private final IImageService imageService;

    @Inject
    public ImageController(IImageService imageService) {
        this.imageService = imageService;
    }

    @Operation(
            summary = "Upload and invert image colors",
            description = "Upload an image and invert its colors using Akka actors for parallel processing. " +
                    "Color transformation: Red->Green, Green->Blue, Blue->Red"
    )
    public CompletionStage<Result> uploadImage() {
        Http.Request request = request();
        Http.MultipartFormData<File> body = request.body().asMultipartFormData();

        if (body == null) {
            return CompletableFuture.completedFuture(
                    badRequest(Json.toJson(new ApiResponse(false, "No multipart form data found")))
            );
        }

        Http.MultipartFormData.FilePart<File> filePart = body.getFile("image");

        if (filePart == null) {
            return CompletableFuture.completedFuture(
                    badRequest(Json.toJson(new ApiResponse(false, "No file uploaded with key 'image'")))
            );
        }

        File imageFile = filePart.getFile();
        String originalFilename = filePart.getFilename();

        return imageService.uploadAndInvertImage(imageFile, originalFilename)
                .thenApply(filename -> {
                    ImageResponse response = new ImageResponse(
                            "Image inverted successfully using Akka actors",
                            filename,
                            "/api/image/" + filename
                    );
                    return ok(Json.toJson(response));
                })
                .exceptionally(ex -> {
                    play.Logger.error("Failed to process image", ex);
                    return internalServerError(
                            Json.toJson(new ApiResponse(false, "Failed to process image: " + ex.getMessage()))
                    );
                });
    }

    @Operation(
            summary = "Get inverted image",
            description = "Retrieve a previously inverted image by its filename"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image retrieved successfully",
                    content = @Content(mediaType = "image/png")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Image not found"
            )
    })
    public Result getImage(
            @Parameter(description = "Filename of the inverted image") String filename
    ) {
        Optional<File> imageFile = imageService.getInvertedImage(filename);

        if (imageFile.isPresent()) {
            try {
                return ok(new FileInputStream(imageFile.get()))
                        .as("image/png")
                        .withHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            } catch (Exception e) {
                play.Logger.error("Failed to read image file", e);
                return internalServerError(Json.toJson(new ApiResponse(false, "Failed to read image file")));
            }
        } else {
            return notFound(Json.toJson(new ApiResponse(false, "Image not found")));
        }
    }
}
