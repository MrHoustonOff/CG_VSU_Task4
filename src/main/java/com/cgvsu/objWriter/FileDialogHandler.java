package com.cgvsu.objWriter;

import com.cgvsu.model.Model;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDialogHandler {

    public static void saveModel(Model model, boolean saveDeformation, boolean useTexture, boolean useLighting) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog(new Stage());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = ObjWriter.write(model, saveDeformation, useTexture, useLighting);
            Files.writeString(fileName, fileContent);
        } catch (IOException exception) {
            System.err.println("Error writing file: " + exception.getMessage());
        }
    }
}