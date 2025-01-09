package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.CalculateNormals;
import com.cgvsu.model.Model;
import com.cgvsu.objWriter.FileDialogHandler;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import com.cgvsu.scene.Scene;

public class GuiController {

    private static final float TRANSLATION = 0.5F;
    @FXML
    private Label XLabel;
    @FXML
    private Label XTranslationLabel;
    @FXML
    private Label YTranslationLabel;
    @FXML
    private Label ZTranslationLabel;
    @FXML
    private Label XScaleLabel;
    @FXML
    private Label YScaleLabel;
    @FXML
    private Label ZScaleLabel;
    @FXML
    private Label XRotationLabel;
    @FXML
    private Label YRotationLabel;
    @FXML
    private Label ZRotationLabel;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox transformationBox;

    @FXML
    private TextField translationX, translationY, translationZ;

    @FXML
    private TextField scaleX, scaleY, scaleZ;

    @FXML
    private TextField rotationX, rotationY, rotationZ;

    @FXML
    private Button applyButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private CheckBox saveDeformationCheckBox;

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private CheckBox useLightingCheckBox;

    @FXML
    private ComboBox<Model> modelComboBox;

    @FXML
    private Label selectModelLabel;

    @FXML
    private Label translationLabel;

    @FXML
    private Label scaleLabel;

    @FXML
    private Label rotationLabel;

    private Scene scene;

    private Timeline timeline;

    private boolean isLeftButtonPressed = false;
    private double lastMouseX, lastMouseY;
    private boolean isAltPressed = false;
    private boolean isFPressed = false;
    private boolean isDarkTheme = false;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        scene = new Scene();

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            scene.getActiveCamera().setAspectRatio((float) (width / height));

            RenderEngine.render(canvas.getGraphicsContext2D(), scene.getActiveCamera(), scene.getModels(), (int) width, (int) height);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        applyButton.setOnAction(event -> applyTransformation());
        saveButton.setOnAction(event -> saveModel());
        deleteButton.setOnAction(event -> deleteModel());

        resetTransformationFields();
        canvas.setOnMouseClicked(event -> canvas.requestFocus());
        canvas.requestFocus();

        modelComboBox.setOnAction(event -> setActiveModel());
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleOnScroll);

        canvas.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.F) {
                scene.getActiveCamera().cameraReset();
            }
        });

        updateModelComboBox();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = createFileChooser("Model (*.obj)", "*.obj", "Load Model");
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);
            model.setName(file.getName());
            model.setOriginalVertices(model.getVertices());
            scene.addModel(model);
            updateModelComboBox();
            modelComboBox.getSelectionModel().select(model);
        } catch (IOException exception) {
            System.err.println("Error reading file: " + exception.getMessage());
        }
    }

    private FileChooser createFileChooser(String description, String extension, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        fileChooser.setTitle(title);
        File initialDirectory = new File("3DModels");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        return fileChooser;
    }

    @FXML
    private void applyTransformation() {
        Model activeModel = scene.getActiveModel();
        if (activeModel == null) {
            System.err.println("Error: No active model selected. Please select a model.");
            return;
        }
        try {
            float tX = -1 * Float.parseFloat(translationX.getText());
            float tY = Float.parseFloat(translationY.getText());
            float tZ = Float.parseFloat(translationZ.getText());

            float sX = Float.parseFloat(scaleX.getText());
            float sY = Float.parseFloat(scaleY.getText());
            float sZ = Float.parseFloat(scaleZ.getText());

            float rX = Float.parseFloat(rotationX.getText());
            float rY = Float.parseFloat(rotationY.getText());
            float rZ = Float.parseFloat(rotationZ.getText());

            float[] transformations = {tX, tY, tZ, sX, sY, sZ, rX, rY, rZ};

            System.out.println("Transformations applied: ");
            for (float value : transformations) {
                System.out.print(value + " ");
            }
            System.out.println();

            ArrayList<Vector3f> transformationList = new ArrayList<>();
            transformationList.add(new Vector3f(rX, rY, rZ));
            transformationList.add(new Vector3f(sX, sY, sZ));
            transformationList.add(new Vector3f(tX, tY, tZ));

            recalculateNormals(transformationList, activeModel);

            resetTransformationFields();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input in transformation fields.");
        }
    }

    private void recalculateNormals(ArrayList<Vector3f> trList, Model model) {
        Matrix4f transformationMatrix = GraphicConveyor.scaleRotateTranslate(trList.get(0), trList.get(1), trList.get(2));
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();

        for (Vector3f vertex : model.getOriginalVertices()) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(transformationMatrix, vertex);
            transformedVertices.add(transformedVertex);
        }

        model.setVertices(transformedVertices);
        model.setNormals(CalculateNormals.calculateNormals(model));
    }

    private void resetTransformationFields() {
        translationX.setText("0");
        translationY.setText("0");
        translationZ.setText("0");

        scaleX.setText("1");
        scaleY.setText("1");
        scaleZ.setText("1");

        rotationX.setText("0");
        rotationY.setText("0");
        rotationZ.setText("0");
    }

    @FXML
    private void saveModel() {
        Model activeModel = scene.getActiveModel();
        if (activeModel == null) {
            System.err.println("Error: No active model selected. Please select a model.");
            return;
        }

        boolean saveDeformation = saveDeformationCheckBox.isSelected();
        boolean useTexture = useTextureCheckBox.isSelected();
        boolean useLighting = useLightingCheckBox.isSelected();

        FileDialogHandler.saveModel(activeModel, saveDeformation);
        System.out.println("Model saved. Save deformation: " + saveDeformation + ", Use Texture: " + useTexture + ", Use Lighting: " + useLighting);
    }

    @FXML
    private void deleteModel() {
        Model selectedModel = modelComboBox.getValue();
        if (selectedModel != null) {
            scene.deleteModel(selectedModel);
            updateModelComboBox();
        }
    }

    private void rotateCamera(double deltaX, double deltaY) {
        float sensitivity = 0.5f;
        float azimuth = scene.getActiveCamera().getAzimuth();
        float elevation = scene.getActiveCamera().getElevation();

        azimuth += (float) (deltaX * sensitivity);
        elevation += (float) (deltaY * sensitivity);

        elevation = Math.max(-89, Math.min(89, elevation));
        azimuth = azimuth % 360;
        if (azimuth < 0) azimuth += 360;

        scene.getActiveCamera().setAzimuth(azimuth);
        scene.getActiveCamera().setElevation(elevation);

        scene.getActiveCamera().updatePosition();
    }

    private void panCamera(double deltaX, double deltaY) {
        float panSensitivity = 0.05f;
        Vector3f direction = scene.getActiveCamera().getTarget().sub(scene.getActiveCamera().getPosition());
        direction.normalize();

        Vector3f right = direction.cross(new Vector3f(0, 1, 0));
        right.normalize();
        right.multiply((float) deltaX * panSensitivity);

        Vector3f up = new Vector3f(0, 1, 0);
        up.multiply((float) deltaY * panSensitivity);
        scene.getActiveCamera().setTarget(scene.getActiveCamera().getTarget().add(right).add(up));
        scene.getActiveCamera().updatePosition();
    }

    private void handleOnScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        float zoomSensitivity = 0.1f;

        float distance = scene.getActiveCamera().getDistance();
        distance -= (float) (delta * zoomSensitivity);
        distance = Math.max(10.0f, distance);
        scene.getActiveCamera().setDistance(distance);
        scene.getActiveCamera().updatePosition();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            isLeftButtonPressed = false;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        isAltPressed = event.isAltDown();

        if (event.isPrimaryButtonDown()) {
            isLeftButtonPressed = true;
        }
        if (event.isMiddleButtonDown()) {
            boolean isMiddleButtonPressed = true;
        }

        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;

        if (isAltPressed) {
            if (isLeftButtonPressed) {
                rotateCamera(deltaX, deltaY);
            }
            boolean isMiddleButtonPressed = false;
            if (isMiddleButtonPressed) {
                panCamera(deltaX, deltaY);
            }
        }

        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        scene.getActiveCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    private void setActiveModel() {
        Model selectedModel = modelComboBox.getValue();
        if (selectedModel != null) {
            scene.setActiveModel(selectedModel);
        }
    }

    private void updateModelComboBox() {
        modelComboBox.getItems().clear();
        modelComboBox.getItems().addAll(scene.getModels());
    }

    private void handleMouseClicked(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            double x = event.getX();
            double y = event.getY();
            Vector3f point = new Vector3f((float) x, (float) y, 0);
            Model model = scene.getModelAt(point);
            if (model != null) {
                scene.setActiveModel(model);
                modelComboBox.getSelectionModel().select(model);
            }
        }
    }

    @FXML
    private void toggleDarkTheme(ActionEvent event) {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            anchorPane.setStyle("-fx-background-color: #444444;");
            transformationBox.setStyle("-fx-background-color: #444444;");
            selectModelLabel.setStyle("-fx-text-fill: white;");
            translationLabel.setStyle("-fx-text-fill: white;");
            scaleLabel.setStyle("-fx-text-fill: white;");
            rotationLabel.setStyle("-fx-text-fill: white;");
            applyButton.setStyle("-fx-text-fill: black;");
            deleteButton.setStyle("-fx-text-fill: black;");
            saveDeformationCheckBox.setStyle("-fx-text-fill: white;");
            useTextureCheckBox.setStyle("-fx-text-fill: white;");
            useLightingCheckBox.setStyle("-fx-text-fill: white;");
            saveButton.setStyle("-fx-text-fill: black;");
            modelComboBox.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            XTranslationLabel.setStyle("-fx-text-fill: white;");
            YTranslationLabel.setStyle("-fx-text-fill: white;");
            ZTranslationLabel.setStyle("-fx-text-fill: white;");
            XRotationLabel.setStyle("-fx-text-fill: white;");
            YRotationLabel.setStyle("-fx-text-fill: white;");
            ZRotationLabel.setStyle("-fx-text-fill: white;");
            XScaleLabel.setStyle("-fx-text-fill: white;");
            YScaleLabel.setStyle("-fx-text-fill: white;");
            ZScaleLabel.setStyle("-fx-text-fill: white;");
        } else {
            anchorPane.setStyle("-fx-background-color: #ffffff;");
            transformationBox.setStyle("-fx-background-color: #ffffff;");
            selectModelLabel.setStyle("-fx-text-fill: black;");
            translationLabel.setStyle("-fx-text-fill: black;");
            scaleLabel.setStyle("-fx-text-fill: black;");
            rotationLabel.setStyle("-fx-text-fill: black;");
            applyButton.setStyle("-fx-text-fill: black;");
            deleteButton.setStyle("-fx-text-fill: black;");
            saveDeformationCheckBox.setStyle("-fx-text-fill: black;");
            useTextureCheckBox.setStyle("-fx-text-fill: black;");
            useLightingCheckBox.setStyle("-fx-text-fill: black;");
            saveButton.setStyle("-fx-text-fill: black;");
            modelComboBox.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            translationZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            scaleZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationX.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationY.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            rotationZ.setStyle("-fx-text-fill: black; -fx-background-color: #ffffff;");
            XTranslationLabel.setStyle("-fx-text-fill: black;");
            YTranslationLabel.setStyle("-fx-text-fill: black;");
            ZTranslationLabel.setStyle("-fx-text-fill: black;");
            XRotationLabel.setStyle("-fx-text-fill: black;");
            YRotationLabel.setStyle("-fx-text-fill: black;");
            ZRotationLabel.setStyle("-fx-text-fill: black;");
            XScaleLabel.setStyle("-fx-text-fill: black;");
            YScaleLabel.setStyle("-fx-text-fill: black;");
            ZScaleLabel.setStyle("-fx-text-fill: black;");
        }
    }
}
