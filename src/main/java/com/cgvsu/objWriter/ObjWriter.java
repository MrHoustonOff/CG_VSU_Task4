package com.cgvsu.objWriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

/**
 * Class for writing a 3D model in OBJ format.
 *
 * <p>This class provides methods to convert model data (vertices, texture coordinates, normals, and polygons)
 * into OBJ format strings and write them to a file. It is assumed that the model writing is called from the GUI
 * through the {@link #write(Model, boolean, boolean, boolean)} method.</p>
 */
public class ObjWriter {
    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    /**
     * Writes the model to a file.
     *
     * <p>This method is the entry point and is used to write a 3D model to an OBJ format file.
     * It automatically creates the necessary directories and file if they do not exist.</p>
     *
     * @param model    The model object containing vertices, texture coordinates, normals, and polygons.
     * @param saveDeformation Whether to save the deformation of the model.
     * @param useTexture Whether to use texture coordinates.
     * @param useLighting Whether to use lighting (normals).
     * @return A string representation of the OBJ file content.
     */
    public static String write(Model model, boolean saveDeformation, boolean useTexture, boolean useLighting) {
        Objects.requireNonNull(model, "model must not be null");

        StringBuilder stringBuilder = new StringBuilder();

        // Write vertices
        List<Vector3f> vertices = model.getVertices();
        for (Vector3f vector : vertices) {
            stringBuilder.append(vertexToString(vector)).append("\n");
        }

        // Write texture coordinates if enabled
        if (useTexture) {
            List<Vector2f> textureVertices = model.getTextureVertices();
            for (Vector2f vector : textureVertices) {
                stringBuilder.append(textureVertexToString(vector)).append("\n");
            }
        }

        // Write normals if enabled
        if (useLighting) {
            List<Vector3f> normals = model.getNormals();
            for (Vector3f vector : normals) {
                stringBuilder.append(normalToString(vector)).append("\n");
            }
        }

        // Write faces
        List<Polygon> polygons = model.getPolygons();
        for (Polygon polygon : polygons) {
            stringBuilder.append(polygonToString(polygon, useTexture, useLighting)).append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Converts a vertex to an OBJ format string.
     *
     * @param vector The vector representing the vertex.
     * @return The OBJ format string (e.g., "v 1.0 2.0 3.0").
     */
    public static String vertexToString(Vector3f vector) {
        return OBJ_VERTEX_TOKEN + " " + vector.getX() + " " + vector.getY() + " " + vector.getZ();
    }

    /**
     * Converts a texture coordinate to an OBJ format string.
     *
     * @param vector The vector representing the texture coordinate.
     * @return The OBJ format string (e.g., "vt 0.5 0.5").
     */
    public static String textureVertexToString(Vector2f vector) {
        return OBJ_TEXTURE_TOKEN + " " + vector.getX() + " " + vector.getY();
    }

    /**
     * Converts a normal to an OBJ format string.
     *
     * @param vector The vector representing the normal.
     * @return The OBJ format string (e.g., "vn 0.0 1.0 0.0").
     */
    public static String normalToString(Vector3f vector) {
        return OBJ_NORMAL_TOKEN + " " + vector.getX() + " " + vector.getY() + " " + vector.getZ();
    }

    /**
     * Converts a polygon to an OBJ format string.
     *
     * @param polygon The polygon with vertex, texture coordinate, and normal indices.
     * @param useTexture Whether to include texture coordinates.
     * @param useLighting Whether to include normals.
     * @return The OBJ format string (e.g., "f 1/1/1 2/2/2 3/3/3").
     */
    public static String polygonToString(Polygon polygon, boolean useTexture, boolean useLighting) {
        StringBuilder stringBuilder = new StringBuilder(OBJ_FACE_TOKEN);
        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();
        boolean hasTextures = textureVertexIndices.size() == vertexIndices.size();
        boolean hasNormals = normalIndices.size() == vertexIndices.size();
        for (int i = 0; i < vertexIndices.size(); i++) {
            stringBuilder.append(" ")
                    .append(vertexIndices.get(i) + 1);
            if (hasNormals && useLighting) {
                stringBuilder.append("/")
                        .append(normalIndices.get(i) + 1);
            }
            if (hasTextures && useTexture) {
                stringBuilder.append("/")
                        .append(textureVertexIndices.get(i) + 1);
            }
        }
        return stringBuilder.toString();
    }
}