package dal.graphic.ocr;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import dal.graphic.*;
import dal.graphic.general.SettingsController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartOCRController extends Controller {
    private Webcam webcam;
    private volatile boolean isWebcamOpen = false;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private volatile BufferedImage capturedImage; // Keep the captured image before cropping for ctrl-Z
    private volatile BufferedImage currentImage;  // Store the most recent image
    private volatile boolean isPreviewing = true;  // Flag to stop the preview feed
    private double startX, startY;  // Starting point of the rectangle
    private boolean isSelecting = false;  // Flag to check if selection is happening
    private GraphicsContext gc; // Graphic context for Selection Canvas

    @FXML
    private BorderPane root;

    @FXML
    private VBox mainVBox;

    @FXML
    private ImageView previewImageView;

    @FXML
    private Button captureButton;

    @FXML
    private Canvas previewCanvas;

    @FXML
    private HBox captureHBox;

    @FXML
    private HBox selectionHBox;

    @FXML
    private Button readTextButton;

    @FXML
    public void initialize() {
        // Find and open the selected camera.
        String requestedCamName = SettingsController.getCaptureDevice();
        List<Webcam> webcams = Webcam.getWebcams();
        this.webcam = webcams.stream()
                .filter(webcam -> webcam.getName().equals(requestedCamName))
                .findFirst()
                .orElse(null);

        if (webcam == null) {
            ErrorDisplayer.displayError("Camera \"" + requestedCamName + "\" not found. Please select another one in the settings.");
            Platform.runLater(() -> SceneManager.switchScene(SceneType.SETTINGS, (Stage) root.getScene().getWindow(), new int[]{(int)(root).getWidth(), (int)(root).getHeight()}));
        }

        // Initialize UI components
        captureButton.setDisable(true);

        // Initialize the canvas for drawing rectangles
        gc = previewCanvas.getGraphicsContext2D();

        // Ensure the Canvas resizes to match the ImageView after it's laid out
        previewImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            previewCanvas.setWidth(newBounds.getWidth());
            previewCanvas.setHeight(newBounds.getHeight());
        });

        // Open the selected camera
        openCameraInThread();

        // Start by showing the capture view.
        showCaptureView();

        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage != null) { // Prevent error when no camera with given name (so when app switches to settings before finishing loading)
                // Ensure the camera is closed when program exits by clicking X button.
                stage.setOnCloseRequest(event -> closeResources());
            }

            // Catch ctrl-Z.
            root.setOnKeyPressed(event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                    if (!isPreviewing) {
                        System.out.println("Undo triggered.");
                        if (currentImage == capturedImage) { // Undo capture
                            System.out.println("Undoing capture.");
                            resetCapture();
                        } else { // Undo crop instead.
                            System.out.println("Undoing selection.");
                            resetCrop();
                        }
                    }
                    event.consume(); // Prevents event from propagating further if needed
                }
            });
        });
    }

    private void openCameraInThread() {
        captureButton.setDisable(true);
        cameraExecutor.submit(() -> {
            webcam.setViewSize(WebcamResolution.VGA.getSize());

            webcam.open(); // Open without blocking timeout

            if (webcam.isOpen()) {
                isWebcamOpen = true;
                Platform.runLater(() -> captureButton.setDisable(false));
            }
        });

        // Check after 5 seconds if the camera is still not open
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for camera to be opened.");
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            Platform.runLater(() -> {
                if (!isWebcamOpen) {
                    // Ensure the scene is still open after the 5 seconds.
                    if (root.getScene().getWindow() != null) {
                        ErrorDisplayer.displayError("Couldn't open camera: " + webcam.getName() + ". Please select another one in the settings.");
                        mainMenu();
                    }
                } else {
                    captureButton.setDisable(false);
                }
            });
        }).start();
    }

    private void startPreview() {
        isPreviewing = true;

        // Continuously grab frames and update the preview
        cameraExecutor.submit(() -> {
            while (webcam.isOpen() && isPreviewing) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    currentImage = image;  // Store the most recent image

                    Image fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> previewImageView.setImage(fxImage));
                }

                try {
                    Thread.sleep(100); // Update the preview every 100 milliseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @FXML
    private void handleImageViewDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    private void handleImageViewDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            File file = dragboard.getFiles().getFirst(); // Get the first dropped file
            try {
                // Switch to the selection view
                showSelectionView();

                // Convert file to JavaFX Image and display it
                Image image = new Image(file.toURI().toString());

                // Update the ImageView and the current image
                currentImage = ImageIO.read(file);
                capturedImage = currentImage; // Failsafe for ctrl-Z
                previewImageView.setImage(image);

                System.out.println("Image successfully loaded!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        event.setDropCompleted(true);
        event.consume();
    }

    @FXML
    private void captureImage() {
        // Only capture if the webcam is open and the current image is available
        if (webcam != null && webcam.isOpen() && currentImage != null) {
            // Save the current image as capturedImage
            capturedImage = currentImage;
            // Display the captured image in the ImageView
            Platform.runLater(() -> {
                Image fxImage = SwingFXUtils.toFXImage(currentImage, null);
                previewImageView.setImage(fxImage);  // Show the captured image
            });

            showSelectionView();
        } else {
            ErrorDisplayer.displayError("Failed to capture image: no image available.");
        }
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        if (!isPreviewing) {
            // Start drawing a rectangle when the mouse is pressed
            startX = event.getX();
            startY = event.getY();
            isSelecting = true;
        }
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        if (!isPreviewing) {
            double endX = event.getX();
            double endY = event.getY();

            // Calculate x, y, width, and height correctly
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            // Redraw the rectangle
            gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }

    @FXML
    private void handleMouseReleased(MouseEvent event) {
        if (!isPreviewing && isSelecting) {
            System.out.println("Mouse released, computing cropped image...");
            isSelecting = false;  // Stop selecting

            double endX = event.getX();
            double endY = event.getY();

            // Ensure proper selection (handle negative width/height)
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            System.out.println("Selection: x=" + x + ", y=" + y + ", dx=" + width + ", dy=" + height);

            // Get the displayed image
            BufferedImage bufferedImage = currentImage;
            if (bufferedImage == null) return;

            // Get actual image dimensions
            double imageWidth = bufferedImage.getWidth();
            double imageHeight = bufferedImage.getHeight();

            // Get ImageView dimensions
            double viewWidth = previewImageView.getFitWidth();
            double viewHeight = previewImageView.getFitHeight();

            // Compute scaling factors
            double ratioX = viewWidth / imageWidth;
            double ratioY = viewHeight / imageHeight;
            double scale = Math.min(ratioX, ratioY);  // Maintain aspect ratio

            // Convert the canvas selection coordinates to image coordinates
            int imgX = (int) (x / scale);
            int imgY = (int) (y / scale);
            int imgWidth = (int) (width / scale);
            int imgHeight = (int) (height / scale);

            // Ensure the cropping stays within the bounds of the image
            imgX = Math.max(0, Math.min(imgX, (int) imageWidth - 1));
            imgY = Math.max(0, Math.min(imgY, (int) imageHeight - 1));
            imgWidth = Math.min(imgWidth, (int) imageWidth - imgX);
            imgHeight = Math.min(imgHeight, (int) imageHeight - imgY);

            if (imgWidth <= 0 || imgHeight <= 0) { // Avoid errors when selection is too small.
                // Clear the rectangle selection from the canvas
                gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

                System.out.println("Selection is too small.");
                return;
            }

            System.out.println("Cropping image with: imgX=" + imgX + ", imgY=" + imgY + ", imgWidth=" + imgWidth + ", imgHeight=" + imgHeight);

            // Crop the BufferedImage based on the calculated coordinates
            BufferedImage croppedImage = bufferedImage.getSubimage(imgX, imgY, imgWidth, imgHeight);

            // Convert back to a JavaFX Image and display it
            Image fxCroppedImage = SwingFXUtils.toFXImage(croppedImage, null);
            currentImage = croppedImage;
            previewImageView.setImage(fxCroppedImage);

            // Clear the rectangle selection from the canvas
            gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        }
    }

    @FXML
    private void readText() {
        // Close resources before switching scene.
        closeResources();

        // Switch scene and initialize new controller.
        SceneManager.switchScene(SceneType.OCR, (Stage) root.getScene().getWindow(), new int[]{(int)(root).getWidth(), (int)(root).getHeight()});
        OCRController newController = (OCRController) SceneManager.getCurrentController();
        newController.initText(currentImage);
    }

    @FXML
    private void resetCapture() {
        showCaptureView();
    }

    @FXML
    private void resetCrop() {
        System.out.println("Rolling back crop...");
        currentImage = capturedImage;
        // Update Image View graphically.
        Image fxImage = SwingFXUtils.toFXImage(currentImage, null);
        previewImageView.setImage(fxImage);
    }

    private void resetView() {
        mainVBox.getChildren().remove(captureHBox);
        mainVBox.getChildren().remove(selectionHBox);
    }

    private void showCaptureView() {
        resetView();

        mainVBox.getChildren().add(captureHBox);
        mainVBox.getChildren().remove(selectionHBox);

        startPreview();
    }

    private void showSelectionView() {
        resetView();

        mainVBox.getChildren().remove(captureHBox);
        mainVBox.getChildren().add(selectionHBox);
        isPreviewing = false;

        readTextButton.requestFocus();
    }

    private void closeResources() {
        if (SceneManager.getCurrentController() instanceof StartOCRController) {
            if (webcam != null) {
                webcam.close();
                isWebcamOpen = false;
            }
            cameraExecutor.shutdown();
        }
    }

    @Override
    @FXML
    protected void mainMenu() {
        super.mainMenu();
        closeResources();
    }

    @Override
    @FXML
    protected void settings() {
        super.settings();
        closeResources();
    }

    @Override
    @FXML
    protected void quit() {
        closeResources();
        super.quit();
    }
}
