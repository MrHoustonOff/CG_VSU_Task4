package com.cgvsu.render_engine;

import com.cgvsu.math.*;
import com.cgvsu.model.*;

import com.cgvsu.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.cgvsu.model.BoundingBox.getBoundingBox;
import static com.cgvsu.render_engine.GraphicConveyor.*;
import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

public class RenderEngine {

    private static Matrix4f modelViewProjectionMatrix;
    private static Texture texture = null;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model model,
            final int width,
            final int height,
            RenderParameters renderParameters
            ) {
        //  System.out.println("render: " );

        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0));
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        modelViewProjectionMatrix = new Matrix4f(projectionMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);
        if(model != null) {
            texture = model.getTexture();
        }
        // Цвет заливки
        Color fillColor = Color.RED;
        // System.out.println("models: " + models.size());

        if(model == null){
            return;
        }

        final int nPolygons = model.getPolygons().size();
        // System.out.println("nPolygons: " + nPolygons);

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = model.getPolygons().get(polygonInd).getVertexIndices().size();
            //    System.out.println("nVerticesInPolygon: " + nVerticesInPolygon);

            ArrayList<Point2f> screenPoints = new ArrayList<>();
            ArrayList<Float> zValues = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = model.getVertices().get(model.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath);
                Point2f screenPoint = vertexToPoint(transformedVertex, width, height);
                screenPoints.add(screenPoint);
                zValues.add(transformedVertex.getZ());
            }


            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        screenPoints.get(vertexInPolygonInd - 1).getX(),
                        screenPoints.get(vertexInPolygonInd - 1).getY(),
                        screenPoints.get(vertexInPolygonInd).getX(),
                        screenPoints.get(vertexInPolygonInd).getY());
            }

            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        screenPoints.get(nVerticesInPolygon - 1).getX(),
                        screenPoints.get(nVerticesInPolygon - 1).getY(),
                        screenPoints.get(0).getX(),
                        screenPoints.get(0).getY());
            }
        }
        if(!renderParameters.getEmptyParams()){
            if(renderParameters.isTexturePolygon()){
                renderPolygonTexture(graphicsContext, model, width, height);
            }
            if(renderParameters.isColorPolygon()){
                renderPolygonColor(graphicsContext, model, width, height);
            }
            if (renderParameters.isAllColorPolygon()) {
                renderPolygonAllColor(graphicsContext, model, width, height);
            }
        }
    }

    private static void renderPolygonAllColor(
            GraphicsContext graphicsContext,
            Model mesh,
            int width,
            int height){
     //   System.out.println("renderPolygonAllColor = "+ mesh.getPolygons());
        float[][] zBuffer = initializeZBuffer(width, height);

        for (Polygon polygon : mesh.getPolygons()) {
            if (polygon.getVertexIndices().size() == 4) {
                Point2f[] screenPoints = new Point2f[polygon.getVertexIndices().size()];
                float[] zValues = new float[polygon.getVertexIndices().size()];

                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                    Vector3f transformedVertex = transformVertex(mesh, polygon, i, modelViewProjectionMatrix);
                    screenPoints[i] = vertexToPoint(transformedVertex, width, height);
                    zValues[i] = transformedVertex.getZ();
                }
                rasterizeTriangle(screenPoints, zValues, zBuffer, graphicsContext);
            }
        }

    }
    private static void renderPolygonColor(
            GraphicsContext graphicsContext,
            Model mesh,
            int width,
            int height

    ) {
        float[][] zBuffer = initializeZBuffer(width, height);
   //     System.out.println("renderPolygonColor");
        for (Polygon polygon : mesh.getPolygons()) {
            if (polygon.getVertexIndices().size() == 3) {
                Point2f[] screenPoints = new Point2f[polygon.getVertexIndices().size()];
                float[] zValues = new float[polygon.getVertexIndices().size()];

                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                    Vector3f transformedVertex = transformVertex(mesh, polygon, i, modelViewProjectionMatrix);
                    screenPoints[i] = vertexToPoint(transformedVertex, width, height);
                    zValues[i] = transformedVertex.getZ();
                }
                rasterizeTriangleColor(screenPoints, zValues, zBuffer, graphicsContext);
            }
        }
    }

    private static void renderPolygonTexture(
            GraphicsContext graphicsContext,
            Model mesh,
            int width,
            int height
    ) {
       // System.out.println("renderPolygonTexture = "+ mesh.getPolygons());

        float[][] zBuffer = initializeZBuffer(width, height);
//        for (Polygon polygon : mesh.getPolygons()) {
//            if (polygon.getVertexIndices().size() == 4) {
//                Point2f[] screenPoints = new Point2f[polygon.getVertexIndices().size()];
//                float[] zValues = new float[polygon.getVertexIndices().size()];
//
//                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
//                    Vector3f transformedVertex = transformVertex(mesh, polygon, i, modelViewProjectionMatrix);
//                    System.out.println("polygon.getTextureVertexIndices() = "+ polygon.getTextureVertexIndices().get(i));
//
////                    float u = polygon.getTextureVertexIndices().get(i); // Получаем координату u
////                    float v = polygon.getTextureVertexIndices().get(i); // Получаем координату v
//                    // Получаем текстурные координаты для вершины
//                    TextureCoordinate texCoord = polygon.getTextureCoordinates().get(i); // Предполагается, что этот метод возвращает список текстурных координат
//                    float u = texCoord.getU(); // Получаем координату u
//                    float v = texCoord.getV(); // Получаем координату v
//
//
//                    screenPoints[i] = vertexToPoint1(transformedVertex, width, height, u, v);
//                    zValues[i] = transformedVertex.getZ();
//                }
//                rasterizeTexture(screenPoints, zValues, zBuffer, graphicsContext, mesh);
//            }
//        }


        for (Polygon polygon : mesh.getPolygons()) {
            if (polygon.getVertexIndices().size() == 4) {
                Point2f[] screenPoints = new Point2f[polygon.getVertexIndices().size()];
                float[] zValues = new float[polygon.getVertexIndices().size()];

                ArrayList<Vector2f> texCoords = polygon.getTextureCoordinates(); // Получаем текстурные координаты
           //     System.out.println("texCoords = "+ texCoords);
            //    System.out.println("polygon.getVertexIndices().size() = "+ polygon.getVertexIndices().size());
                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                    Vector3f transformedVertex = transformVertex(mesh, polygon, i, modelViewProjectionMatrix);
                    float u = texCoords.get(i).getX(); // Получаем координату u
                    float v = texCoords.get(i).getY(); // Получаем координату v

                    screenPoints[i] = vertexToPoint1(transformedVertex, width, height, u, v);

                    zValues[i] = transformedVertex.getZ();
                }
                rasterizeTexture(screenPoints, zValues, zBuffer, graphicsContext, mesh);
            }
        }




//        float[][] zBuffer = initializeZBuffer(width, height);
//        for (Polygon polygon : mesh.getPolygons()) {
//            if (polygon.getVertexIndices().size() == 4) {
//                Point2f[] screenPoints = new Point2f[polygon.getVertexIndices().size()];
//                float[] zValues = new float[polygon.getVertexIndices().size()];
//
//                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
//                    Vector3f transformedVertex = transformVertex(mesh, polygon, i, modelViewProjectionMatrix);
//                    screenPoints[i] = vertexToPoint(transformedVertex, width, height);
//                    zValues[i] = transformedVertex.getZ();
//                }
//                rasterizeTexture(screenPoints, zValues, zBuffer, graphicsContext, mesh);
//            }
//        }

    }
    private static void rasterizeTexture(Point2f[] points, float[] zValues, float[][] zBuffer, GraphicsContext graphicsContext, Model model) {
        //Image texture = new Image(":/images/123.jpg");
        Image texture = new Image(Objects.requireNonNull(RenderEngine.class.getResourceAsStream("/images/123.jpg")));


        // Определяем границы четырехугольника
            int minX = (int) Math.max(0, Math.floor(Math.min(
                    points[0].getX(),
                    Math.min(points[1].getX(), Math.min(points[2].getX(), points[3].getX()))
            )));
            int maxX = (int) Math.min(zBuffer.length - 1, Math.ceil(Math.max(
                    points[0].getX(),
                    Math.max(points[1].getX(), Math.max(points[2].getX(), points[3].getX()))
            )));
            int minY = (int) Math.max(0, Math.floor(Math.min(
                    points[0].getY(),
                    Math.min(points[1].getY(), Math.min(points[2].getY(), points[3].getY()))
            )));
            int maxY = (int) Math.min(zBuffer[0].length - 1, Math.ceil(Math.max(
                    points[0].getY(),
                    Math.max(points[1].getY(), Math.max(points[2].getY(), points[3].getY()))
            )));

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    // Вычисляем барицентрические координаты для каждого из треугольников
                    float[] barycentricCoords1 = calculateBarycentricCoordinates(new Point2f[]{points[0], points[1], points[2]}, x, y);
                    float[] barycentricCoords2 = calculateBarycentricCoordinates(new Point2f[]{points[0], points[2], points[3]}, x, y);

                    // Проверяем, находится ли точка внутри первого или второго треугольника
                    boolean insideTriangle1 = isPointInside(barycentricCoords1[0], barycentricCoords1[1], barycentricCoords1[2]);
                    boolean insideTriangle2 = isPointInside(barycentricCoords2[0], barycentricCoords2[1], barycentricCoords2[2]);

                    if (insideTriangle1 || insideTriangle2) {
                        // Вычисляем глубину
                        float depth = insideTriangle1 ? interpolateDepth1(barycentricCoords1, zValues) : interpolateDepth1(barycentricCoords2, zValues);

                        // Проверяем, нужно ли обновить zBuffer
                        if (depth < zBuffer[x][y]) {
                            zBuffer[x][y] = depth;

                            // Интерполяция текстурных координат
                            float u, v;
                            if (insideTriangle1) {
                                u = barycentricCoords1[0] * points[0].getU() +
                                        barycentricCoords1[1] * points[1].getU() +
                                        barycentricCoords1[2] * points[2].getU();
                                v = barycentricCoords1[0] * points[0].getV() +
                                        barycentricCoords1[1] * points[1].getV() +
                                        barycentricCoords1[2] * points[2].getV();

                            } else {
                                u = barycentricCoords2[0] * points[0].getU() +
                                        barycentricCoords2[1] * points[2].getU() +
                                        barycentricCoords2[2] * points[3].getU();
                                v = barycentricCoords2[0] * points[0].getV() +
                                        barycentricCoords2[1] * points[2].getV() +
                                        barycentricCoords2[2] * points[3].getV();
                            }
                          //  System.out.println("textureColor u = "+ u +" v = "+v);

                            // Получаем цвет из текстуры
                            Color textureColor = getColor1(texture, u, v);
                            //model.loadTexture("/images/123.jpg");
                           // Texture texture1 = new Texture("/images/123.jpg");
                           // Color textureColor = texture1.getColor(u, v);

                            //  System.out.println("textureColor = "+ textureColor);

                            graphicsContext.getPixelWriter().setColor(x, y, textureColor);
                        }
                    }
                }
            }
    }

    public static Color getColor1(Image texture, float u, float v) {
        u = clamp1(u, 0.0f, 1.0f);
        v = clamp1(v, 0.0f, 1.0f);

        int x = (int) Math.min((int) (u * (texture.getWidth() - 1)), texture.getWidth() - 1);
        int y = (int) Math.min((int) ((1 - v) * (texture.getHeight() - 1)), texture.getHeight() - 1); // Инвертируем V

        PixelReader pixelReader = texture.getPixelReader();
        return pixelReader.getColor(x, y);
    }
    private static float clamp1(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

   // private float clamp(float value, float min, float max) {
     //   return Math.max(min, Math.min(max, value));
   // }

    private static Color getTextureColor(Image texture, float u, float v) {
        // Приводим текстурные координаты в диапазон [0, 1]
        u = u % 1.0f;
        v = v % 1.0f;

        // Приводим координаты к размеру текстуры
        int x = (int) (u * (texture.getWidth() - 1)); // Вычитаем 1, чтобы избежать выхода за пределы
        int y = (int) (v * (texture.getHeight() - 1)); // Вычитаем 1, чтобы избежать выхода за пределы

        // Убедимся, что координаты находятся в пределах текстуры
        x = Math.max(0, Math.min(x, (int) texture.getWidth() - 1));
        y = Math.max(0, Math.min(y, (int) texture.getHeight() - 1));

        // Получаем цвет из текстуры
        PixelReader pixelReader = texture.getPixelReader();
        Color color = pixelReader.getColor(x, y);

        // Проверяем, что цвет не белый (для отладки)
        if (color.equals(Color.WHITE)) {
            System.out.println("Получен белый цвет на координатах: (" + x + ", " + y + ")");
        }

        return color;


//        // Приводим текстурные координаты в диапазон [0, 1]
//        u = u % 1.0f;
//        v = v % 1.0f;
//
//        // Приводим координаты к размеру текстуры
//        int x = (int) (u * texture.getWidth());
//        int y = (int) (v * texture.getHeight());
//
//        // Убедимся, что координаты находятся в пределах текстуры
//        x = Math.max(0, Math.min(x, (int) texture.getWidth() - 1));
//        y = Math.max(0, Math.min(y, (int) texture.getHeight() - 1));
//
//        // Получаем цвет из текстуры
//        PixelReader pixelReader = texture.getPixelReader();
//        return pixelReader.getColor(x, y);
    }

    private static void rasterizeTriangle(Point2f[] points, float[] zValues, float[][] zBuffer, GraphicsContext graphicsContext) {
        Color colorA = Color.RED;
        int minX = (int) Math.max(0, Math.floor(Math.min(
                points[0].getX(),
                Math.min(points[1].getX(), Math.min(points[2].getX(), points[3].getX()))
        )));
        int maxX = (int) Math.min(zBuffer.length - 1, Math.ceil(Math.max(
                points[0].getX(),
                Math.max(points[1].getX(), Math.max(points[2].getX(), points[3].getX()))
        )));
        int minY = (int) Math.max(0, Math.floor(Math.min(
                points[0].getY(),
                Math.min(points[1].getY(), Math.min(points[2].getY(), points[3].getY()))
        )));
        int maxY = (int) Math.min(zBuffer[0].length - 1, Math.ceil(Math.max(
                points[0].getY(),
                Math.max(points[1].getY(), Math.max(points[2].getY(), points[3].getY()))
        )));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {

                // Вычисляем барицентрические координаты для каждого из треугольников
                float[] barycentricCoords1 = calculateBarycentricCoordinates(new Point2f[]{points[0], points[1], points[2]}, x, y);
                float[] barycentricCoords2 = calculateBarycentricCoordinates(new Point2f[]{points[0], points[2], points[3]}, x, y);

                // Проверяем, находится ли точка внутри первого или второго треугольника
                boolean insideTriangle1 = isPointInside(barycentricCoords1[0], barycentricCoords1[1], barycentricCoords1[2]);
                boolean insideTriangle2 = isPointInside(barycentricCoords2[0], barycentricCoords2[1], barycentricCoords2[2]);

                if (insideTriangle1 || insideTriangle2) {
                    // Вычисляем глубину (если нужно)
                    float depth = interpolateDepth1(barycentricCoords1, zValues);
                    // Проверяем, нужно ли обновить zBuffer
                    if (depth < zBuffer[x][y]) {
                        zBuffer[x][y] = depth;
                        graphicsContext.getPixelWriter().setColor(x, y, colorA);
                    }
                }
            }
        }
    }

    private static void rasterizeTriangleColor(Point2f[] points, float[] zValues, float[][] zBuffer, GraphicsContext graphicsContext) {
        Color colorA = Color.GREEN;
        Color colorB = Color.BLACK;
        Color colorC = Color.YELLOW;

        int minX = (int) Math.max(0, Math.floor(Math.min(points[0].getX(), Math.min(points[1].getX(), points[2].getX()))));
        int maxX = (int) Math.min(zBuffer.length - 1, Math.ceil(Math.max(points[0].getX(), Math.max(points[1].getX(), points[2].getX()))));
        int minY = (int) Math.max(0, Math.floor(Math.min(points[0].getY(), Math.min(points[1].getY(), points[2].getY()))));
        int maxY = (int) Math.min(zBuffer[0].length - 1, Math.ceil(Math.max(points[0].getY(), Math.max(points[1].getY(), points[2].getY()))));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                float[] barycentricCoords = calculateBarycentricCoordinates(points, x, y);
                float lambda0 = barycentricCoords[0];
                float lambda1 = barycentricCoords[1];
                float lambda2 = barycentricCoords[2];

                if (isPointInsideTriangle(lambda0, lambda1, lambda2)) {
                    float depth = interpolateDepth(lambda0, lambda1, lambda2, zValues);

                    if (depth < zBuffer[x][y]) {
                        zBuffer[x][y] = depth;
                        Color color = interpolateColor(lambda0, lambda1, lambda2, colorA, colorB, colorC);
                        graphicsContext.getPixelWriter().setColor(x, y, color);
                    }
                }
            }
        }
    }
    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static Color interpolateColor(float lambda0, float lambda1, float lambda2, Color colorA, Color colorB, Color colorC) {
        double red = lambda0 * colorA.getRed() + lambda1 * colorB.getRed() + lambda2 * colorC.getRed();
        double green = lambda0 * colorA.getGreen() + lambda1 * colorB.getGreen() + lambda2 * colorC.getGreen();
        double blue = lambda0 * colorA.getBlue() + lambda1 * colorB.getBlue() + lambda2 * colorC.getBlue();
        return new Color(clamp(red), clamp(green), clamp(blue), 1.0);
    }

    private static boolean isPointInsideTriangle(float lambda0, float lambda1, float lambda2) {
        return lambda0 >= 0 && lambda1 >= 0 && lambda2 >= 0;
    }
    private static boolean isPointInside(float lambda0, float lambda1, float lambda2) {
        return lambda0 >= 0 && lambda1 >= 0 && lambda2 >= 0;
    }
    private static float interpolateDepth(float lambda0, float lambda1, float lambda2, float[] zValues) {
        return lambda0 * zValues[0] + lambda1 * zValues[1] + lambda2 * zValues[2];
    }

    // Метод для интерполяции глубины (пример)
    private static float interpolateDepth1(float[] barycentricCoords, float[] zValues) {
        return barycentricCoords[0] * zValues[0] + barycentricCoords[1] * zValues[1] + barycentricCoords[2] * zValues[2];
    }

    private static float[] calculateBarycentricCoordinates(Point2f[] points, int x, int y) {
        float denominator = ((points[1].getY() - points[2].getY()) * (points[0].getX() - points[2].getX()) +
                (points[2].getX() - points[1].getX()) * (points[0].getY() - points[2].getY()));

        float lambda0 = ((points[1].getY() - points[2].getY()) * (x - points[2].getX()) +
                (points[2].getX() - points[1].getX()) * (y - points[2].getY())) / denominator;

        float lambda1 = ((points[2].getY() - points[0].getY()) * (x - points[2].getX()) +
                (points[0].getX() - points[2].getX()) * (y - points[2].getY())) / denominator;

        float lambda2 = 1.0f - lambda0 - lambda1;

        return new float[]{lambda0, lambda1, lambda2};
    }
//    private static float[] calculateBarycentricCoordinates2(Point2f[] points, int x, int y) {
//        float denominator = ((points[1].getY() - points[2].getY()) * (points[0].getX() - points[2].getX()) +
//                (points[2].getX() - points[1].getX()) * (points[0].getY() - points[2].getY()));
//
//        float lambda0 = ((points[1].getY() - points[2].getY()) * (x - points[2].getX()) +
//                (points[2].getX() - points[1].getX()) * (y - points[2].getY())) / denominator;
//
//        float lambda1 = ((points[2].getY() - points[0].getY()) * (x - points[2].getX()) +
//                (points[0].getX() - points[2].getX()) * (y - points[2].getY())) / denominator;
//
//        float lambda2 = 1.0f - lambda0 - lambda1;
//        float lambda3 =
//
//        return new float[]{lambda0, lambda1, lambda2, lambda3};
//    }
    private static Vector3f transformVertex(Model mesh, Polygon polygon, int vertexIndex, Matrix4f transformationMatrix) {
        Vector3f vertex = mesh.getVertices().get(polygon.getVertexIndices().get(vertexIndex));
        Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());
        return multiplyMatrix4ByVector3(transformationMatrix, vertexVecmath);
    }

    private static float[][] initializeZBuffer(int width, int height) {
        float[][] zBuffer = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                zBuffer[x][y] = Float.MAX_VALUE;
            }
        }
        return zBuffer;
    }

    public static void renderColor(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Set<Model> models,
            final int width,
            final int height,
            boolean paintOver) {
        //  System.out.println("render: " );

        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0));
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);

        // Цвет заливки
        Color fillColor = Color.RED;
        // System.out.println("models: " + models.size());

        for (Model model : models) {
            final int nPolygons = model.getPolygons().size();
            // System.out.println("nPolygons: " + nPolygons);

            for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
                final int nVerticesInPolygon = model.getPolygons().get(polygonInd).getVertexIndices().size();
                //    System.out.println("nVerticesInPolygon: " + nVerticesInPolygon);

                ArrayList<Point2f> screenPoints = new ArrayList<>();
                ArrayList<Float> zValues = new ArrayList<>();
                for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    Vector3f vertex = model.getVertices().get(model.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                    Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                    Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath);
                    Point2f screenPoint = vertexToPoint(transformedVertex, width, height);
                    screenPoints.add(screenPoint);
                    zValues.add(transformedVertex.getZ());
                }


                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    graphicsContext.strokeLine(
                            screenPoints.get(vertexInPolygonInd - 1).getX(),
                            screenPoints.get(vertexInPolygonInd - 1).getY(),
                            screenPoints.get(vertexInPolygonInd).getX(),
                            screenPoints.get(vertexInPolygonInd).getY());
                }

                if (nVerticesInPolygon > 0) {
                    graphicsContext.strokeLine(
                            screenPoints.get(nVerticesInPolygon - 1).getX(),
                            screenPoints.get(nVerticesInPolygon - 1).getY(),
                            screenPoints.get(0).getX(),
                            screenPoints.get(0).getY());
                }
            }
            float[][] zBuffer = initializeZBuffer(width, height);
            //  Matrix4f modelViewProjectionMatrix = calculateModelViewProjectionMatrix(camera);
            for (Polygon polygon : model.getPolygons()) {

                if(polygon.getVertexIndices().size()==3) {
                    //renderPolygonColor(graphicsContext, polygon, modelViewProjectionMatrix, width, height, zBuffer, model);////////////////////////
                }

            }
        }

    }

//    public static void renderTriangle(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Set<Model> models,
//            final int width,
//            final int height,
//            boolean paintOver) {
//        System.out.println("renderTriangle: ");
//
//        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0));
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix);
//        modelViewProjectionMatrix.multiply(viewMatrix);
//        modelViewProjectionMatrix.multiply(modelMatrix);
//
//        for (Model model : models) {
//            ModelColor m_color = new ModelColor(model);
//
//        }
//
//
//
//        // rasterizePolygon(graphicsContext, vertices, zBuffer, fillColor, BoundingBox.getWidth(boundingBox), BoundingBox.getHeight(boundingBox));
//
//
//    }

}



//public class  RenderEngine {
//
//    public static void render(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Set<Model> models,
//            final int width,
//
//            final int height) {
//
//        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0));
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix);
//        modelViewProjectionMatrix.multiply(viewMatrix);
//        modelViewProjectionMatrix.multiply(modelMatrix);
//
//        for (Model model : models) {
//            final int nPolygons = model.getPolygons().size();
//            for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
//                final int nVerticesInPolygon = model.getPolygons().get(polygonInd).getVertexIndices().size();
//
//                ArrayList<Point2f> resultPoints = new ArrayList<>();
//                for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                    Vector3f vertex = model.getVertices().get(model.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
//
//                    Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());
//
//                    Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
//                    resultPoints.add(resultPoint);
//                }
//
//                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                    graphicsContext.strokeLine(
//                            resultPoints.get(vertexInPolygonInd - 1).getX(),
//                            resultPoints.get(vertexInPolygonInd - 1).getY(),
//                            resultPoints.get(vertexInPolygonInd).getX(),
//                            resultPoints.get(vertexInPolygonInd).getY());
//                }
//
//                if (nVerticesInPolygon > 0) {
//                    graphicsContext.strokeLine(
//                            resultPoints.get(nVerticesInPolygon - 1).getX(),
//                            resultPoints.get(nVerticesInPolygon - 1).getY(),
//                            resultPoints.get(0).getX(),
//                            resultPoints.get(0).getY());
//                }
//            }
//        }
//    }
//}
