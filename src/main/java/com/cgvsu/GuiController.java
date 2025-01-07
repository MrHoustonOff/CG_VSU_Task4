package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.CalculateNormals;
import com.cgvsu.model.ModelTriangulator;
import com.cgvsu.objWriter.FileDialogHandler;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.scene.Scene;

public class GuiController {

    private static final float TRANSLATION = 0.5F;

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
    private CheckBox saveDeformationCheckBox;

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private CheckBox useLightingCheckBox;

    @FXML
    private ComboBox<Model> modelComboBox;

    private Scene scene;

    private Timeline timeline;

    private boolean isLeftButtonPressed = false;
    //private boolean isRightButtonPressed = false;
    private boolean isMiddleButtonPressed = false;
    private double lastMouseX, lastMouseY;
    private boolean isAltPressed = false;
    private boolean isFPressed = false;

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

            if (scene.getActiveModel() != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), scene.getActiveCamera(), scene.getActiveModel(), (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        applyButton.setOnAction(event -> applyTransformation());
        saveButton.setOnAction(event -> saveModel());

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
                scene.getActiveCamera().cameraReset(); // быстренько запихнул метод сброса камеры.
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
            model.setOriginalVertices(model.getVertices());
            scene.addModel(model);
            updateModelComboBox();
            modelComboBox.getSelectionModel().select(model);
        } catch (IOException exception) {
            System.err.println("Error reading file: " + exception.getMessage());
        }
    }

    @FXML
    private void addModel() {
        FileChooser fileChooser = createFileChooser("Model (*.obj)", "*.obj", "Add Model");
        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);
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
        System.out.println("8888888");
        model.setModelTriangulator(model.getNormals(), model.getPolygons());



        //model.setPolygon() = ModelTriangulator.triangulateModel(model.polygon);

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

        FileDialogHandler.saveModel(activeModel, saveDeformation, useTexture, useLighting);
        System.out.println("Model saved. Save deformation: " + saveDeformation + ", Use Texture: " + useTexture + ", Use Lighting: " + useLighting);
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
        float zoomSensitivity = 0.01f;

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
//        if (!event.isSecondaryButtonDown()) {
//            isRightButtonPressed = false;
//        }
    }

    private void handleMousePressed(MouseEvent event) {
        isAltPressed = event.isAltDown();

        if (event.isPrimaryButtonDown()) {
            isLeftButtonPressed = true;
        }
//        if (event.isSecondaryButtonDown()) {
//            isRightButtonPressed = true;
//        }
        if (event.isMiddleButtonDown()) {
            isMiddleButtonPressed = true;
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
}