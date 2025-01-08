package com.cgvsu.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Polygon {

    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureVertexIndices;
    private ArrayList<Integer> normalIndices;
    private Color color;
    private float x;
    private float y;

    public Polygon() {
        vertexIndices = new ArrayList<Integer>();
        textureVertexIndices = new ArrayList<Integer>();
        normalIndices = new ArrayList<Integer>();
        this.x = 0;
        this.y = 0;
    }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public float getPositionX() {
        return x;
    }
    public float getPositionY() {
        return y;
    }
    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        assert vertexIndices.size() >= 3;
        this.vertexIndices = vertexIndices;
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        assert textureVertexIndices.size() >= 3;
        this.textureVertexIndices = textureVertexIndices;
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        assert normalIndices.size() >= 3;
        this.normalIndices = normalIndices;
    }

    public void setColor(Color color_new) {
        this.color = color_new;
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public ArrayList<Integer> getTextureVertexIndices() {
        return textureVertexIndices;
    }

    public ArrayList<Integer> getNormalIndices() {
        return normalIndices;
    }
}
