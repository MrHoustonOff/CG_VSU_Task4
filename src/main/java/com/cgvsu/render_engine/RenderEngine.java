package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.scene.Scene;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Set<Model> models,
            final int width,
            final int height) {

        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0));
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(projectionMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(modelMatrix);

        for (Model model : models) {
            final int nPolygons = model.getPolygons().size();
            for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
                final int nVerticesInPolygon = model.getPolygons().get(polygonInd).getVertexIndices().size();

                ArrayList<Point2f> resultPoints = new ArrayList<>();
                for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    Vector3f vertex = model.getVertices().get(model.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                    Vector3f vertexVecmath = new Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                    Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                    resultPoints.add(resultPoint);
                }

                for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                    graphicsContext.strokeLine(
                            resultPoints.get(vertexInPolygonInd - 1).getX(),
                            resultPoints.get(vertexInPolygonInd - 1).getY(),
                            resultPoints.get(vertexInPolygonInd).getX(),
                            resultPoints.get(vertexInPolygonInd).getY());
                }

                if (nVerticesInPolygon > 0) {
                    graphicsContext.strokeLine(
                            resultPoints.get(nVerticesInPolygon - 1).getX(),
                            resultPoints.get(nVerticesInPolygon - 1).getY(),
                            resultPoints.get(0).getX(),
                            resultPoints.get(0).getY());
                }
            }
        }
    }
}
