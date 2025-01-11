package com.cgvsu.model;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import java.util.ArrayList;

/**
 * Класс ModelEditingTools предоставляет набор инструментов для редактирования 3D-модели.
 * Это сервисный класс, который реализует операции над данными модели,
 * не изменяя внутреннюю логику самой модели.
 *
 * Сюда же стоит добавить удаление полигонов и другую шнягу
 */

public class ModelEditingTools {
    public static void deformModelFromRawData(ArrayList<Vector3f> trList, Model model) {
        Matrix4f transformationMatrix = GraphicConveyor.scaleRotateTranslate(trList.get(0), trList.get(1), trList.get(2));
        model.addTransformation(transformationMatrix);
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();

        for (Vector3f vertex : model.getOriginalVertices()) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(transformationMatrix, vertex);
            transformedVertices.add(transformedVertex);
        }

        model.setVertices(transformedVertices);
        model.setNormals(com.cgvsu.model.CalculateNormals.calculateNormals(model));
    }
    public static void deformModelFromTransformationMatrix(Matrix4f transformationMatrix, Model model) {
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();

        for (Vector3f vertex : model.getOriginalVertices()) {
            Vector3f transformedVertex = GraphicConveyor.multiplyMatrix4ByVector3(transformationMatrix, vertex);
            transformedVertices.add(transformedVertex);
        }

        model.setVertices(transformedVertices);
        model.setNormals(com.cgvsu.model.CalculateNormals.calculateNormals(model));
    }
}
