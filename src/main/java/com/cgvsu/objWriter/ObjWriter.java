package com.cgvsu.objWriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Класс ObjWriter предназначен для записи 3D-модели в формате OBJ.
 *
 * <p>Этот класс предоставляет методы для преобразования данных модели (вершин, текстурных координат, нормалей и полигонов)
 * в строки формата OBJ и записи их в файл. Предполагается, что запись модели вызывается из GUI
 * через метод {@link #write(Model, String)}.</p>
 */
public class ObjWriter {
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    /**
     * Метод записи модели в файл.
     *
     * <p>Этот метод является точкой входа и используется для записи 3D-модели в файл формата OBJ.
     * Он автоматически создает необходимые директории и файл, если они отсутствуют.</p>
     *
     * @param model    Объект модели, содержащий вершины, текстурные координаты, нормали и полигоны.
     * @param filename Имя выходного файла (включая путь).
     */
    public static void write(Model model, String filename) {
        File file = new File(filename);
        if (!createDir(file.getParentFile()))
            return;
        if (!createFile(file))
            return;
        try (PrintWriter writer = new PrintWriter(file)) {
            model.getVertices().forEach(v -> writer.println(vertexToString(v)));
            model.getTextureVertices().forEach(v -> writer.println(textureVertexToString(v)));
            model.getNormals().forEach(v -> writer.println(normalToString(v)));
            model.getPolygons().forEach(v -> writer.println(polygonToString(v)));
        } catch (IOException e) {
            System.out.println("Error while writing file");
        }
    }

    /**
     * Создает директорию, если она отсутствует.
     *
     * @param directory Директория, которую нужно создать.
     * @return {@code true}, если директория была успешно создана или уже существует; {@code false} в случае ошибки.
     */
    private static boolean createDir(File directory) {
        if (directory != null && !directory.exists() && !directory.mkdirs()) {
            System.out.println("Couldn't create dir: " + directory);
            return false;
        }
        return true;
    }

    /**
     * Создает файл, если он отсутствует.
     *
     * @param file Файл, который нужно создать.
     * @return {@code true}, если файл был успешно создан или уже существует; {@code false} в случае ошибки.
     */
    private static boolean createFile(File file) {
        try {
            if (!file.createNewFile())
                System.out.println("Warning: " + file.getName() + " already exists");
        } catch (IOException e) {
            System.out.println("Error while creating the file");
            return false;
        }
        return true;
    }

    /**
     * Преобразует вершину в строку формата OBJ.
     *
     * @param vector Вектор, представляющий вершину.
     * @return Строка формата OBJ (например, "v 1.0 2.0 3.0").
     */
    public static String vertexToString(Vector3f vector) {
        return OBJ_VERTEX_TOKEN + " " + vector.getX() + " " + vector.getY() + " " + vector.getZ();
    }

    /**
     * Преобразует текстурную координату в строку формата OBJ.
     *
     * @param vector Вектор, представляющий текстурную координату.
     * @return Строка формата OBJ (например, "vt 0.5 0.5").
     */
    public static String textureVertexToString(Vector2f vector) {
        return OBJ_TEXTURE_TOKEN + " " + vector.getX() + " " + vector.getY();
    }

    /**
     * Преобразует нормаль в строку формата OBJ.
     *
     * @param vector Вектор, представляющий нормаль.
     * @return Строка формата OBJ (например, "vn 0.0 1.0 0.0").
     */
    public static String normalToString(Vector3f vector) {
        return OBJ_NORMAL_TOKEN + " " + vector.getX() + " " + vector.getY() + " " + vector.getZ();
    }

    /**
     * Преобразует полигон в строку формата OBJ.
     *
     * @param polygon Полигон с индексами вершин, текстурных координат и нормалей.
     * @return Строка формата OBJ (например, "f 1/1/1 2/2/2 3/3/3").
     */
    public static String polygonToString(Polygon polygon) {
        StringBuilder stringBuilder = new StringBuilder(OBJ_FACE_TOKEN);
        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();
        boolean hasTextures = textureVertexIndices.size() == vertexIndices.size();
        boolean hasNormals = normalIndices.size() == vertexIndices.size();
        for (int i = 0; i < vertexIndices.size(); i++) {
            stringBuilder.append(" ")
                    .append(getFormattedIndex(vertexIndices, i));
            if (hasNormals) {
                stringBuilder.append("/");
                if (hasTextures) {
                    stringBuilder.append(getFormattedIndex(textureVertexIndices, i))
                            .append("/")
                            .append(getFormattedIndex(normalIndices, i));
                } else {
                    stringBuilder.append("/")
                            .append(getFormattedIndex(normalIndices, i));
                }
            } else {
                if (hasTextures) {
                    stringBuilder.append("/")
                            .append(getFormattedIndex(textureVertexIndices, i));
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Форматирует индекс (начинает отсчет с 1, как это принято в формате OBJ).
     *
     * @param indices Список индексов.
     * @param index   Текущий индекс в списке.
     * @return Индекс, увеличенный на 1.
     */
    private static int getFormattedIndex(List<Integer> indices, int index) {
        return indices.get(index) + 1;
    }
}
