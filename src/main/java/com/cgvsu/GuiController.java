package com.cgvsu;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.CalculateNormals;
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
import java.util.ArrayList;
import java.util.List;

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
            //todo: АНАЛОГИЧНЫМ ОБРАЗОМ РАСФОСОВЫВАЕМ ВСЕ ОРИГИНАЛЬНЫЕ ШТУЧКИ!!!
            mesh.setOriginalVertices(mesh.getVertices());
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }

    //TODO метод применения трансформов РЕАЛИЗОВАН ДЛЯ ОДНОГО МЕША!!!! Ваня как только сделаешь сцену - не забудь переделать этот метод под сцену!!!
    @FXML
    private void applyTransformation() {
        //проверка на пустой меш
        if (mesh == null) {
            System.err.println("Ошибка: передана пустая модель. Пожалуйста, передайте корректную модель. ВАНЯ НЕ ЗАБУДЬ!!! Файл FileDialo как-то там строка 23!!!!");
            // TODO: ВАНЯ СДЕЛАЙ БЛЯТЬ ОБРАБОТКУ ЭТОЙ ОШИБКИ КРАСИВО ОКОШКОМ
            return;
        }
        try {
            // Получаем значения из полей для трансформации
            float tX = Float.parseFloat(translationX.getText());
            float tY = Float.parseFloat(translationY.getText());
            float tZ = Float.parseFloat(translationZ.getText());

            float sX = Float.parseFloat(scaleX.getText());
            float sY = Float.parseFloat(scaleY.getText());
            float sZ = Float.parseFloat(scaleZ.getText());

            float rX = Float.parseFloat(rotationX.getText());
            float rY = Float.parseFloat(rotationY.getText());
            float rZ = Float.parseFloat(rotationZ.getText());

            // Создаем массив с трансформациями для дебага. Потом можно удалить все до матрицы преобразований.
            float[] transformations = {tX, tY, tZ, sX, sY, sZ, rX, rY, rZ};

            // Выводим примененные трансформации
            System.out.println("Transformations applied: ");
            for (float value : transformations) {
                System.out.print(value + " ");
            }
            System.out.println();

            // Создаем матрицу преобразования
            //Порядок R -> S -> T
            ArrayList<Vector3f> transformationList = new ArrayList<>();
            transformationList.add(new Vector3f(rX, rY, rZ));  // Параметры вращения
            transformationList.add(new Vector3f(sX, sY, sZ));  // Параметры масштабирования
            transformationList.add(new Vector3f(tX, tY, tZ));  // Параметры перемещения

            //TODO ВОТ ТУТ МЫ ПЕРЕДАЕМ ЛИШЬ ОДНУ МОДЕЛЬ!!!
            recalculationOfNormals(transformationList, mesh);

            // Сбрасываем значения полей до значений по умолчанию
            resetTransformationFields();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input in transformation fields.");
        }
    }

    private void recalculationOfNormals(ArrayList<Vector3f> trList, Model model) {
        //TODO вся логика реализованна для одной модели. После реализации сцены ДОБАВЬ цикл который будет перебирать ПЕРЕДАННЫЙ МАСИВ МОДЕЛЕЙ и выполнять для них все эти дейсвтия.
        Matrix4f transformationMatrix = GraphicConveyor.scaleRotateTranslate(trList.get(0), trList.get(1), trList.get(2));
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();

        for (Vector3f vertex : model.getVertices()) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(transformationMatrix, vertex);
            transformedVertices.add(transformedVertex);
        }

        model.setVertices(transformedVertices);
        model.setNormals(CalculateNormals.calculateNormals(model));
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
        if (mesh == null) {
            System.err.println("Ошибка: передана пустая модель. Пожалуйста, передайте корректную модель. ВАНЯ НЕ ЗАБУДЬ!!! Файл FileDialo как-то там строка 23!!!!");
            // TODO: ВАНЯ СДЕЛАЙ БЛЯТЬ ОБРАБОТКУ ЭТОЙ ОШИБКИ КРАСИВО ОКОШКОМ
            return;
        }

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
