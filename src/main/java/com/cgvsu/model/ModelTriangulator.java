package com.cgvsu.model;

import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ModelTriangulator {
    public static class Triangle {
        public int v1, v2, v3;

        public Triangle(int v1, int v2, int v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        @Override
        public String toString() {
            return "Triangle{" +
                    "v1=" + v1 +
                    ", v2=" + v2 +
                    ", v3=" + v3 +
                    '}';
        }
    }

    private final List<Vector3f> vertices;
    private List<Polygon> polygons;


    public ModelTriangulator(List<Vector3f> vertices, List<Polygon> polygons) {
        this.vertices = vertices;
        this.polygons = polygons;
        triangulateAndCalculateNormals();
        System.out.println("ModelTriangulator created. Vertices size: " + vertices.size() + ", Polygons size: " + polygons.size());
    }


    // Метод для триангуляции и вычисления нормалей
    public void triangulateAndCalculateNormals() {
        System.out.println("Starting triangulation and normal calculation.");
        triangulatePolygons();
        calculateVertexNormals();
        System.out.println("Finished triangulation and normal calculation.");
    }

    // Триангуляция всех полигонов в модели (алгоритм уха)
    private void triangulatePolygons() {
        System.out.println("Starting polygon triangulation.");
        List<Polygon> triangulatedPolygons = new ArrayList<>();

        for (Polygon polygon : polygons) {
            System.out.println("Triangulating polygon with vertices: " + polygon.getVertexIndices());
            if (polygon.getVertexIndices().size() == 3) { // Если полигон уже треугольник, добавляем без изменений
                triangulatedPolygons.add(polygon);
                System.out.println("Polygon is already a triangle.");
                continue;
            }

            triangulatedPolygons.addAll(triangulatePolygon(polygon));
        }
        this.polygons = triangulatedPolygons;
        System.out.println("Finished polygon triangulation. Total triangles: " + triangulatedPolygons.size());
    }

    // Триангуляция одного полигона (алгоритм уха)
    private List<Polygon> triangulatePolygon(Polygon polygon) {
        List<Polygon> triangles = new ArrayList<>();
        List<Integer> vertexIndices = new ArrayList<>(polygon.getVertexIndices());
        List<Integer> textureVertexIndices = (polygon.getTextureVertexIndices().size() > 0) ? new ArrayList<>(polygon.getTextureVertexIndices()) : new ArrayList<>();
        List<Integer> normalIndices = (polygon.getNormalIndices().size() > 0) ? new ArrayList<>(polygon.getNormalIndices()) : new ArrayList<>();

        while (vertexIndices.size() > 3) {
            int earIndex = findEar(vertexIndices);
            int v1 = vertexIndices.get((earIndex - 1 + vertexIndices.size()) % vertexIndices.size());
            int v2 = vertexIndices.get(earIndex);
            int v3 = vertexIndices.get((earIndex + 1) % vertexIndices.size());

            Polygon triangle = new Polygon();
            triangle.setVertexIndices(new ArrayList<>(List.of(v1, v2, v3)));
            System.out.println("Created triangle with vertices: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3);




            if (!textureVertexIndices.isEmpty()) {
                int t1 = textureVertexIndices.get((earIndex - 1 + vertexIndices.size()) % textureVertexIndices.size());
                int t2 = textureVertexIndices.get(earIndex);
                int t3 = textureVertexIndices.get((earIndex + 1) % textureVertexIndices.size());
                triangle.setTextureVertexIndices(new ArrayList<>(List.of(t1, t2, t3)));
                System.out.println("Added texture coordinates: t1=" + t1 + ", t2=" + t2 + ", t3=" + t3);
            }
            if (!normalIndices.isEmpty()) {
                int n1 = normalIndices.get((earIndex - 1 + vertexIndices.size()) % normalIndices.size());
                int n2 = normalIndices.get(earIndex);
                int n3 = normalIndices.get((earIndex + 1) % normalIndices.size());
                triangle.setNormalIndices(new ArrayList<>(List.of(n1, n2, n3)));
                System.out.println("Added normal indices: n1=" + n1 + ", n2=" + n2 + ", n3=" + n3);
            }

            triangles.add(triangle);

            vertexIndices.remove(earIndex);
            if (!textureVertexIndices.isEmpty()) {
                textureVertexIndices.remove(earIndex);
            }
            if (!normalIndices.isEmpty()) {
                normalIndices.remove(earIndex);
            }
        }
        Polygon lastTriangle = new Polygon();
        lastTriangle.setVertexIndices(new ArrayList<>(vertexIndices));
        if (!textureVertexIndices.isEmpty()) {
            lastTriangle.setTextureVertexIndices(new ArrayList<>(textureVertexIndices));
        }
        if (!normalIndices.isEmpty()) {
            lastTriangle.setNormalIndices(new ArrayList<>(normalIndices));
        }
        triangles.add(lastTriangle);
        System.out.println("Last triangle vertices: " + lastTriangle.getVertexIndices());
        return triangles;
    }

    private int findEar(List<Integer> vertexIndices) {
        int earIndex = 0;
        for (int i = 0; i < vertexIndices.size(); i++) {
            int v1 = vertexIndices.get((i - 1 + vertexIndices.size()) % vertexIndices.size());
            int v2 = vertexIndices.get(i);
            int v3 = vertexIndices.get((i + 1) % vertexIndices.size());

            Vector3f vec1 = vertices.get(v1);
            Vector3f vec2 = vertices.get(v2);
            Vector3f vec3 = vertices.get(v3);

            Vector3f v12 = vec2.sub(vec1);
            Vector3f v13 = vec3.sub(vec1);
            Vector3f normal = v12.cross(v13);
            boolean isEar = true;
            for (int j = 0; j < vertexIndices.size(); j++) {
                if (j == i || j == (i - 1 + vertexIndices.size()) % vertexIndices.size() || j == (i + 1) % vertexIndices.size())
                    continue;

                Vector3f vTest = vertices.get(vertexIndices.get(j));
                Vector3f testVec1 = vec1.sub(vTest);
                if (normal.dot(testVec1) > 0) {
                    isEar = false;
                    break;
                }
            }
            if (isEar) {
                earIndex = i;
                System.out.println("Found ear at index: " + i);
                break;
            }
        }
        return earIndex;
    }

    private void calculateVertexNormals() {
        System.out.println("Starting vertex normal calculation.");
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() == 3) {
                calculateTriangleNormal(polygon);
            } else {
                // System.out.println("Полигон не треугольник");
            }
        }
        System.out.println("Finished vertex normal calculation.");
    }

    private void calculateTriangleNormal(Polygon polygon) {
        List<Integer> vertexIndices = polygon.getVertexIndices();
        int v1 = vertexIndices.get(0);
        int v2 = vertexIndices.get(1);
        int v3 = vertexIndices.get(2);

        Vector3f vec1 = vertices.get(v1);
        Vector3f vec2 = vertices.get(v2);
        Vector3f vec3 = vertices.get(v3);




        Vector3f v12 = vec2.sub(vec1);
        Vector3f v13 = vec3.sub(vec1);
        Vector3f normal = v12.cross(v13).normalizeV();
        List<Integer> normalIndices = new ArrayList<>();
        for (int i = 0; i < vertexIndices.size(); i++) {
            vertices.set(vertexIndices.get(i), vertices.get(vertexIndices.get(i)).add(normal));
            normalIndices.add(vertexIndices.get(i));
        }
        polygon.setNormalIndices(new ArrayList<>(normalIndices));
        System.out.println("Calculated normal for triangle: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ", normal=" + normal);
    }

    public Vector3f getNormal(Vector3f v1, Vector3f v2, Vector3f v3, float[] barycentric, List<Vector3f> vertices) {
        try {
            int index1 = (int) v1.getZ();
            int index2 = (int) v2.getZ();
            int index3 = (int) v3.getZ();

            if (index1 < 0 || index1 >= vertices.size() || index2 < 0 || index2 >= vertices.size() || index3 < 0 || index3 >= vertices.size()) {
                System.err.println("Error: Invalid vertex index in getNormal: index1=" + index1 + ", index2=" + index2 + ", index3=" + index3 +". Vertices size: " + vertices.size());
                return new Vector3f(0, 0, 0);
            }
            Vector3f normal1 = vertices.get(index1);
            Vector3f normal2 = vertices.get(index2);
            Vector3f normal3 = vertices.get(index3);

            Vector3f normal = new Vector3f(
                    normal1.getX() * barycentric[0] + normal2.getX() * barycentric[1] + normal3.getX() * barycentric[2],
                    normal1.getY() * barycentric[0] + normal2.getY() * barycentric[1] + normal3.getY() * barycentric[2],
                    normal1.getZ() * barycentric[0] + normal2.getZ() * barycentric[1] + normal3.getZ() * barycentric[2]).normalizeV();
            System.out.println("Get normal: " + normal + ", from indices: " + index1 + ", " + index2 + ", " + index3);
            return normal;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException in getNormal: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3);
            return new Vector3f(0, 0, 0);
        }
    }

    // Метод для получения треугольников (после триангуляции)
    public List<Triangle> getTriangles() {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() == 3)
                triangles.add(new Triangle(polygon.getVertexIndices().get(0), polygon.getVertexIndices().get(1), polygon.getVertexIndices().get(2)));
        }
        System.out.println("Returning " + triangles.size() + " triangles.");
        return triangles;
    }

    public List<Vector3f> getVertices() {
        List<Vector3f> normalizedVertices = new ArrayList<>();
        for (Vector3f vertex : vertices) {
            normalizedVertices.add(vertex.normalizeV());
        }
        System.out.println("Returning " + normalizedVertices.size() + " vertices.");
        return normalizedVertices;
    }
}
