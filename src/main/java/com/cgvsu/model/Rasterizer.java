package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

//todo: Неактивен надо вызвать где то или сделать галочку переключения в интерфейсе

public class Rasterizer {

    private final int width;  // Ширина изображения
    private final int height; // Высота изображения
    private final BufferedImage image; // Изображение, на котором рисуем
    private final ModelTriangulator modelTriangulator; // Объект для триангуляции и вычисления нормалей
    private final Camera camera; // Объект камеры
    private float[][] zBuffer; // Z-буфер для отсечения невидимых пикселей

    /**
     * Конструктор класса Rasterizer.
     *
     * @param width             Ширина изображения.
     * @param height            Высота изображения.
     * @param modelTriangulator Объект для триангуляции и вычисления нормалей.
     * @param camera            Объект камеры.
     */
    public Rasterizer(int width, int height, ModelTriangulator modelTriangulator, Camera camera) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.modelTriangulator = modelTriangulator;
        this.camera = camera;
        this.zBuffer = new float[width][height]; // Инициализация Z-буфера
    }

    /**
     * Метод для очистки Z-буфера.
     * Заполняет Z-буфер отрицательной бесконечностью, чтобы новые пиксели гарантированно перекрывали старые.
     */
    private void clearZBuffer() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                zBuffer[i][j] = Float.NEGATIVE_INFINITY;
            }
        }
    }

    /**
     * Метод для растеризации модели.
     *
     * @param vertices      Список вершин модели.
     * @param triangles     Список треугольников модели.
     * @param color         Цвет заполнения полигонов.
     * @param lightDirection Направление света для расчета освещения.
     * @id865676420 (@return) Изображение с отрисованной моделью.
     */
    public BufferedImage rasterize(List<Vector3f> vertices, List<ModelTriangulator.Triangle> triangles, Color color, Vector3f lightDirection) {
        System.out.println("Rasterizing started. Triangles count: " + triangles.size());
        clearZBuffer(); // Очищаем Z-буфер перед началом рендеринга
        int triangleCount = 0;
        for (ModelTriangulator.Triangle triangle : triangles) {
            if (triangleCount > 100) {
                System.out.println("Triangle count exceeded 100, stopping rasterization.");
                break;
            }
            rasterizeTriangle(triangle, vertices, color, lightDirection); // Растеризуем каждый треугольник
            triangleCount++;
        }
        System.out.println("Rasterizing finished.");
        return image; // Возвращаем отрендеренное изображение
    }

    /**
     * Метод для растеризации одного треугольника.
     *
     * @param triangle      Треугольник для растеризации.
     * @param vertices      Список вершин модели.
     * @param color         Цвет заполнения полигона.
     * @param lightDirection Направление света для расчета освещения.
     */
    private void rasterizeTriangle(ModelTriangulator.Triangle triangle, List<Vector3f> vertices, Color color, Vector3f lightDirection) {
        int v1 = triangle.v1; // Индекс первой вершины
        int v2 = triangle.v2; // Индекс второй вершины
        int v3 = triangle.v3; // Индекс третьей вершины

        // Проверка на корректность индексов вершин
        if (v1 < 0 || v1 >= vertices.size() || v2 < 0 || v2 >= vertices.size() || v3 < 0 || v3 >= vertices.size()) {
            System.err.println("Error: Invalid vertex index in triangle: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ". Vertices size: " + vertices.size());
            return;
        }

        // Получаем вершины из списка
        Vector3f vert1 = vertices.get(v1);
        Vector3f vert2 = vertices.get(v2);
        Vector3f vert3 = vertices.get(v3);


        System.out.println("Rasterizing triangle: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ", z values: v1.z=" + vert1.getZ() + ", v2.z=" + vert2.getZ() + ", v3.z=" + vert3.getZ());

        // Проецируем вершины на 2D плоскость
        float[] projected = projectTriangle(vert1, vert2, vert3);

        // Если проекция не удалась, выводим ошибку и выходим из метода
        if (projected == null) {
            System.err.println("Error: NaN in projected coordinates.");
            return;
        }

        // Вычисляем ограничивающий прямоугольник для треугольника
        float minX = Math.min(projected[0], Math.min(projected[2], projected[4]));
        float maxX = Math.max(projected[0], Math.max(projected[2], projected[4]));
        float minY = Math.min(projected[1], Math.min(projected[3], projected[5]));
        float maxY = Math.max(projected[1], Math.max(projected[3], projected[5]));

        // Проходим по всем пикселям в ограничивающем прямоугольнике
        for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
            for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
                // Проверяем, находится ли пиксель в пределах изображения
                if (x < 0 || x >= width || y < 0 || y >= height) {
                    continue;
                }

                // Вычисляем барицентрические координаты для текущего пикселя
                float[] barycentric = barycentricCoordinates(x, y, projected);

                // Проверяем, находится ли пиксель внутри треугольника
                if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) {
                    continue;
                }

                // Вычисляем z-координату для текущего пикселя
                float z = vert1.getZ() * barycentric[0] + vert2.getZ() * barycentric[1] + vert3.getZ() * barycentric[2];

                // Если z-координата больше, чем текущее значение в Z-буфере, то отрисовываем пиксель
                if (z > zBuffer[x][y]) {
                    zBuffer[x][y] = z; // Обновляем значение Z-буфера

                    // Получаем нормаль для текущего пикселя
                    Vector3f normal = modelTriangulator.getNormal(
                            new Vector3f(0,0,v1),
                            new Vector3f(0,0,v2),
                            new Vector3f(0,0,v3),
                            barycentric, vertices);
                    // Если нормаль существует
                    if (normal.length() != 0) {
                        float diffuseIntensity = Math.max(0, normal.dot(lightDirection)); // Рассчитываем интенсивность освещения
                        int colorValue = getColor(diffuseIntensity, color); // Получаем цвет пикселя
                        image.setRGB(x, y, colorValue); // Отрисовываем пиксель на изображении
                    }
                }
            }
        }
        System.out.println("Rasterizing triangle finished: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3);
    }

    /**
     * Метод для проецирования 3D координат треугольника в 2D
     *
     * @param v1 Первая вершина треугольника
     * @param v2 Вторая вершина треугольника
     * @param v3 Третья вершина треугольника
     * @id865676420 (@return) Массив из 6 float чисел (x1, y1, x2, y2, x3, y3) координат
     */
    private float[] projectTriangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        float fov = (float) Math.toRadians(camera.getFov()); // Получаем угол обзора камеры и переводим в радианы
        float aspectRatio = (float) width / height; // Вычисляем соотношение сторон экрана
        float scale = 1 / (float) Math.tan(fov / 2); // Вычисляем масштабный коэффициент
        float[] projected = new float[6]; // Массив для хранения 2D координат

        float z1 = v1.getZ(); // z-координата первой вершины
        float z2 = v2.getZ(); // z-координата второй вершины
        float z3 = v3.getZ(); // z-координата третьей вершины

        // Проверяем, чтобы z-координаты вершин были положительными, т.к.деление на ноль или отрицательное число приведет к ошибке
        if (z1 <= 0 || z2 <= 0 || z3 <= 0) {
            System.err.println("Error: z values are <= 0 in projectTriangle: z1=" + z1 + ", z2=" + z2 + ", z3=" + z3);
            return null; // Если условие не выполняется, то возвращаем null
        }

        // Вычисляем 2D координаты
        projected[0] = (v1.getX() * scale * aspectRatio) / z1 * (width / 2) + (width / 2);
        projected[1] = (v1.getY() * scale) / z1 * (height / 2) + (height / 2);
        projected[2] = (v2.getX() * scale * aspectRatio) / z2 * (width / 2) + (width / 2);
        projected[3] = (v2.getY() * scale) / z2 * (height / 2) + (height / 2);
        projected[4] = (v3.getX() * scale * aspectRatio) / z3 * (width / 2) + (width / 2);
        projected[5] = (v3.getY() * scale) / z3 * (height / 2) + (height / 2);
        return projected; // Возвращаем массив с 2D координатами
    }

    /**
     * Метод для вычисления барицентрических координат
     *
     * @param x         Координата x пикселя
     * @param y         Координата y пикселя
     * @param projected Массив с 2D координатами вершин треугольника
     * @id865676420 (@return) Массив барицентрических координат (w1, w2, w3)
     */
    private float[] barycentricCoordinates(float x, float y, float[] projected) {
        float x1 = projected[0]; // x-координата первой вершины
        float y1 = projected[1]; // y-координата первой вершины
        float x2 = projected[2]; // x-координата второй вершины
        float y2 = projected[3]; // y-координата второй вершины
        float x3 = projected[4]; // x-координата третьей вершины
        float y3 = projected[5]; // y-координата третьей вершины
        float area = 0.5f * ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3)); // Вычисляем площадь треугольника
        float w1 = 0.5f * ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / area; // Вычисляем барицентрическую координату w1
        float w2 = 0.5f * ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / area; // Вычисляем барицентрическую координату w2
        float w3 = 1 - w1 - w2; // Вычисляем барицентрическую координату w3
        return new float[]{w1, w2, w3}; // Возвращаем массив с барицентрическими координатами
    }

    /**
     * Метод для получения цвета пикселя.
     *
     * @param diffuseIntensity Интенсивность освещения
     * @param color           Цвет заполнения полигона
     * @id865676420 (@return) цвет пикселя в формате int
     */
    private int getColor(float diffuseIntensity, Color color) {
        int r = (int) (color.getRed() * diffuseIntensity); // Вычисляем красный канал цвета
        int g = (int) (color.getGreen() * diffuseIntensity); // Вычисляем зеленый канал цвета
        int b = (int) (color.getBlue() * diffuseIntensity); // Вычисляем синий канал цвета
        r = Math.min(255, Math.max(0, r)); // Ограничиваем красный канал значением от 0 до 255
        g = Math.min(255, Math.max(0, g)); // Ограничиваем зеленый канал значением от 0 до 255
        b = Math.min(255, Math.max(0, b)); // Ограничиваем синий канал значением от 0 до 255
        return new Color(r, g, b).getRGB(); // Возвращаем цвет в формате int
    }
}