package com.cgvsu.model;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.RenderParameters;
import javafx.stage.FileChooser;

import java.awt.*;
import java.util.*;

import static java.awt.Color.RGBtoHSB;

public class Model {

    private ArrayList<Vector3f> originalVertices = new ArrayList<>();
    //TODO ПРИ ДАЛЬНЕЙШЕМ РАСШИРЕНИИ КОДА ПРОШУ ОБРАТИТЬ ВНИМАНИЕ!
    // Модель дублирует всю инфу в ОРИДЖИНАЛ (начальную) и ТЕКУЩУЮ. Поэтому создайте такие же поля для координат текстур, НОРМАЛЕЙ и тп
    //TODO таким макаром мы сохраняем начальный вид модели!!!
    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    private ArrayList<Polygon> originalPolygons = new ArrayList<Polygon>(); // оригинальные полигоны
    private ArrayList<Polygon> triangulatePolygons = new ArrayList<Polygon>(); // полигон с триангуляцией
    private ArrayList<Polygon> colorsPolygons = new ArrayList<Polygon>();
    private ArrayList<Polygon> allColorPolygons = new ArrayList<Polygon>();
    private ArrayList<Polygon> texturePolygons = new ArrayList<Polygon>();
    private RenderParameters renderParameters = new RenderParameters();
    private String name = "Model";
    private Texture texture;
    private ArrayList<Matrix4f> transformations = new ArrayList<>(
            Arrays.asList(
                    new Matrix4f(
                            1, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1
                    ),
                    new Matrix4f(
                            1, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, 1, 0,  // Пример T_z = 5
                            0, 0, 0, 1
                    )
            )
    );


    public Model() {
        // Инициализируем массив пустыми элементами для избежания IndexOutOfBoundsException
        transformations.add(null);
        transformations.add(null);
    }

    // Геттер для получения элемента по индексу
    public Matrix4f getTransformation(int index) {
        return transformations.get(index);
    }

    // Сеттер для добавления элемента в 1 индекс с автоматическим сдвигом
    public void addTransformation(Matrix4f matrix) {
        if (transformations.size() < 2) {
            transformations.add(matrix);
        } else {
            transformations.set(0, transformations.get(1)); // Смещаем текущий элемент 1 в 0
            transformations.set(1, matrix); // Устанавливаем новый элемент в 1
        }
    }

    // Геттеры и сеттеры для vertices
    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
    }

    // Геттеры и сеттеры для textureVertices
    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    // Геттеры и сеттеры для normals
    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public void setNormals(ArrayList<Vector3f> normals) {
        this.normals = normals;
    }

//    public static void setModelTriangulator(ArrayList<Vector3f> normals, ArrayList<Polygon> polygons) {
//        System.out.println("ModelTriangulator");
//        polygons = ModelTriangulator.triangulateModel(polygons);
//    }

    public RenderParameters getRenderParameters() {
        return renderParameters;
    }
    public void setRenderParameters(RenderParameters renderParameters) {
        this.renderParameters = renderParameters;
    }

    // Геттеры и сеттеры для polygons
    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public ArrayList<Vector3f> getOriginalVertices() {
        return originalVertices;
    }

    public void setOriginalVertices(ArrayList<Vector3f> originalVertices) {
        this.originalVertices = originalVertices;
    }

//***********************************
    public ArrayList<Polygon> getTriangulatePolygons() {
        return triangulatePolygons;
    }

    public void setTriangulatePolygons(ArrayList<Polygon> triangulatePolygons) {
        this.triangulatePolygons = triangulatePolygons;
    }

    public ArrayList<Polygon> getOriginalPolygons() {
        return originalPolygons;
    }

    public void setOriginalPolygons(ArrayList<Polygon> originalPolygons) {
        this.originalPolygons = originalPolygons;
    }

    public ArrayList<Polygon> getColorsPolygons() {
        return colorsPolygons;
    }

    public void setColorsPolygons(ArrayList<Polygon> colorsPolygons) {
        this.colorsPolygons = colorsPolygons;
    }
    public ArrayList<Polygon> getAllColorPolygons() {
        return allColorPolygons;
    }

    public void setAllColorPolygons(ArrayList<Polygon> allColorPolygons) {
        this.allColorPolygons = allColorPolygons;
    }
    public ArrayList<Polygon> getTexturePolygons() {
        return texturePolygons;
    }

    public void setTexturePolygons(ArrayList<Polygon> NewColorPolygons) {
        this.texturePolygons = NewColorPolygons;
    }


    //Переопределяем метод toString дабы CUMbox выводил имена моделей как надо.
    @Override
    public String toString() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName(String name) {
        return name;

    }
    public void loadTexture(String texturePath) {
        this.texture = new Texture(texturePath);
    }
    public Texture getTexture() {
        return texture;
    }
    public void clearTexture () {
        this.texture = null;
    }


}