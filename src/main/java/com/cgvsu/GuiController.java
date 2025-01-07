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
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML //честно я хз зачем он нужен... но создал
    private VBox transformationBox;

    @FXML //текстовые филды для перемещения
    private TextField translationX, translationY, translationZ;

    @FXML //текстовы филды для масштабы
    private TextField scaleX, scaleY, scaleZ;

    @FXML //текстовые филды для ротейшена
    private TextField rotationX, rotationY, rotationZ;

    @FXML //кнопка для принятия изменений переданных в филды выше
    private Button applyButton;

    @FXML //кнопка для сейва
    private Button saveButton;

    @FXML //чекбокс  ака сохранять изменения модели или нет. Должен быть рядом с сейв баттаном
    private CheckBox saveDeformationCheckBox;

    private Model mesh = null;

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        applyButton.setOnAction(event -> applyTransformation());
        saveButton.setOnAction(event -> saveModel());

        //TODO Все эти 3 метода важны.
        //обнуляем поля чтобы не было ошибок... наверное это костыль.
        resetTransformationFields();
        // Устанавливаем фокус на Canvas при клике. как у крутых людей.
        canvas.setOnMouseClicked(event -> canvas.requestFocus());
        //ну и сразу бахаем фокус на канвас.
        canvas.requestFocus();
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
            mesh = ObjReader.read(fileContent);
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }

    //TODO мега важный метод который я используб для передачи инфы о деформации модели
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

            // Трансформы применяются здесь
            System.out.println("Transformations applied: ");
            for (float value : transformations) {
                System.out.print(value + " ");
            }
            System.out.println();

            // Сбрасываем значения полей до значений по умолчанию
            resetTransformationFields();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input in transformation fields.");
        }
    }

    //Todo просто метод который обнуляет текстфилды после принятия изменений
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

    //TODO метод котороый я используя для сохранения деформаций.
    @FXML
    private void saveModel() {
        boolean saveDeformation = saveDeformationCheckBox.isSelected();

        FileDialogHandler.saveModel(mesh);
        // save сюды
        System.out.println("Model saved. Save deformation: " + saveDeformation);
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        System.out.println(TRANSLATION);
        System.out.println(camera.getPosition());

        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        System.out.println(camera.getPosition());
        System.out.println(TRANSLATION);

        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        System.out.println(camera.getPosition());
        System.out.println(TRANSLATION);
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}
