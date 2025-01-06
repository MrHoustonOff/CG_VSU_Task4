package com.cgvsu.math.tests.Model;

import com.cgvsu.model.CalculateNormals;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.cgvsu.math.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Тесты для проверки корректности расчета нормалей методом `CalculateNormals`.
 */
class ModelNormalCalculatingTest {

    @Test
    void calculateNormalsForCube() {
        // Создаем модель
        Model model = new Model();

        // Добавляем вершины куба
        model.getVertices().add(new Vector3f(-1.0f, -1.0f, 1.0f)); // Вершина 0
        model.getVertices().add(new Vector3f(-1.0f, 1.0f, 1.0f));  // Вершина 1
        model.getVertices().add(new Vector3f(-1.0f, -1.0f, -1.0f)); // Вершина 2
        model.getVertices().add(new Vector3f(-1.0f, 1.0f, -1.0f));  // Вершина 3
        model.getVertices().add(new Vector3f(1.0f, -1.0f, 1.0f));  // Вершина 4
        model.getVertices().add(new Vector3f(1.0f, 1.0f, 1.0f));   // Вершина 5
        model.getVertices().add(new Vector3f(1.0f, -1.0f, -1.0f));  // Вершина 6
        model.getVertices().add(new Vector3f(1.0f, 1.0f, -1.0f));   // Вершина 7

        // Создаем грани куба (полигоны)
        ArrayList<Integer> face1 = new ArrayList<>(Arrays.asList(0, 1, 3, 2));
        ArrayList<Integer> face2 = new ArrayList<>(Arrays.asList(2, 3, 7, 6));
        ArrayList<Integer> face3 = new ArrayList<>(Arrays.asList(6, 7, 5, 4));
        ArrayList<Integer> face4 = new ArrayList<>(Arrays.asList(4, 5, 1, 0));
        ArrayList<Integer> face5 = new ArrayList<>(Arrays.asList(2, 6, 4, 0));
        ArrayList<Integer> face6 = new ArrayList<>(Arrays.asList(7, 3, 1, 5));

        // Добавляем полигоны в модель
        model.getPolygons().add(createPolygon(face1));
        model.getPolygons().add(createPolygon(face2));
        model.getPolygons().add(createPolygon(face3));
        model.getPolygons().add(createPolygon(face4));
        model.getPolygons().add(createPolygon(face5));
        model.getPolygons().add(createPolygon(face6));

        // Вызываем метод для расчета нормалей
        ArrayList<Vector3f> calculatedNormals = CalculateNormals.calculateNormals(model);

        // Корень из 3 для нормализации
        float sqrt3 = (float) Math.sqrt(3);

        // Ожидаемые нормали для каждой вершины
        ArrayList<Vector3f> expectedNormals = new ArrayList<>(Arrays.asList(
                new Vector3f(-sqrt3 / 3, -sqrt3 / 3, sqrt3 / 3),  // Нормаль для вершины 0
                new Vector3f(-sqrt3 / 3, sqrt3 / 3, sqrt3 / 3),   // Нормаль для вершины 1
                new Vector3f(-sqrt3 / 3, -sqrt3 / 3, -sqrt3 / 3), // Нормаль для вершины 2
                new Vector3f(-sqrt3 / 3, sqrt3 / 3, -sqrt3 / 3),  // Нормаль для вершины 3
                new Vector3f(sqrt3 / 3, -sqrt3 / 3, sqrt3 / 3),   // Нормаль для вершины 4
                new Vector3f(sqrt3 / 3, sqrt3 / 3, sqrt3 / 3),    // Нормаль для вершины 5
                new Vector3f(sqrt3 / 3, -sqrt3 / 3, -sqrt3 / 3),  // Нормаль для вершины 6
                new Vector3f(sqrt3 / 3, sqrt3 / 3, -sqrt3 / 3)    // Нормаль для вершины 7
        ));

        // Проверяем, совпадают ли рассчитанные нормали с ожидаемыми
        for (int i = 0; i < calculatedNormals.size(); i++) {
            Vector3f calculated = calculatedNormals.get(i);
            Vector3f expected = expectedNormals.get(i);

            assertEquals(expected.getX(), calculated.getX(), 0.0001, "Ошибка в X нормали вершины " + i);
            assertEquals(expected.getY(), calculated.getY(), 0.0001, "Ошибка в Y нормали вершины " + i);
            assertEquals(expected.getZ(), calculated.getZ(), 0.0001, "Ошибка в Z нормали вершины " + i);
        }
    }

    /**
     * Утилитарный метод для создания полигона с заданными индексами вершин.
     *
     * @param vertexIndices список индексов вершин полигона
     * @return Полигон с заданными индексами вершин
     */
    private Polygon createPolygon(ArrayList<Integer> vertexIndices) {
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(vertexIndices);
        return polygon;
    }
}
