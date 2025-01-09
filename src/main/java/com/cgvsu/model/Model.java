package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.*;

public class Model {

    private ArrayList<Vector3f> originalVertices = new ArrayList<>();
    //TODO ПРИ ДАЛЬНЕЙШЕМ РАСШИРЕНИИ КОДА ПРОШУ ОБРАТИТЬ ВНИМАНИЕ! Модель дублирует всю инфу в ОРИДЖИНАЛ (начальную) и ТЕКУЩУЮ. Поэтому создайте такие же поля для координат текстур, НОРМАЛЕЙ и тп
    //TODO таким макаром мы сохраняем начальный вид модели!!!
    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    private String name = "Kiska";

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
}