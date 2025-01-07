package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.objWriter.FileDialogHandler;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
    private ComboBox<Model> modelComboBox;

    private Scene scene;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        scene = new Scene();

        Timeline timeline = new Timeline();
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
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);
            scene.addModel(model);
            modelComboBox.getItems().add(model);
            modelComboBox.getSelectionModel().select(model);
        } catch (IOException exception) {
            System.err.println("Error reading file: " + exception.getMessage());
        }
    }

    @FXML
    private void applyTransformation() {
        try {
            float tX = Float.parseFloat(translationX.getText());
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

            resetTransformationFields();
        } catch (NumberFormatException e) {
            System.err.println("Invalid input in transformation fields.");
        }
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
        boolean saveDeformation = saveDeformationCheckBox.isSelected();
        FileDialogHandler.saveModel(scene.getActiveModel());
        System.out.println("Model saved. Save deformation: " + saveDeformation);
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
}