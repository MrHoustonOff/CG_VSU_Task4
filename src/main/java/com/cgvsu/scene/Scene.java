package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.model.Model;

import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.cgvsu.render_engine.RenderEngine.render;

public class Scene {

  //  private List<Model> models;
 //   private List<Model> selectedModels;
    private int lastWidth, lastHeight;
    private float[] zBuffer;
    private int[] colorBuffer;
 //   private PolygonIndexData[][] polygonZBuffer; ///PolygonIndexData - поменять на какой то другой класс который существует
  //  private PolygonIndexData chosenPolygonIndexData;
    private boolean enableLightMoving = false;

    private HashMap<Model, ModelSceneOptions> models;
    private Model activeModel;
    private List<Camera> cameras;
    private int activeCameraIndex;

    private final Camera DEFAULT_CAMERA = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

//    public Scene(){
//        models = new ArrayList<>();
//        selectedModels = new ArrayList<>();
//    }

    public Scene() {
        this.models = new HashMap<>();
        this.cameras = new ArrayList<>();
        this.cameras.add(DEFAULT_CAMERA);
        this.activeCameraIndex = 0;
    }

    public void addModel(Model model) {
        models.put(model, new ModelSceneOptions());
    }

    public void deleteModel(Model model) {
        models.remove(model);
    }

//    public void setChosenFace(int x, int y){
//        chosenPolygonIndexData = polygonZBuffer[y][x];
//    }

//    public PolygonIndexData getChosenFace(){
//        return chosenPolygonIndexData;
//    }
//    public List<Model> getModels(){
//        return models;
//    }
//    public List<Model> getSelectedModels(){
//        return selectedModels;
//    }
//    public void addModels(Model model){
//        models.add(model);
//    }

    public void setActiveModel(Model model) {
        activeModel = model;
    }

    public Model getActiveModel() {
        return activeModel;
    }

    public Set<Model> getModels() {
        return models.keySet();
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
    }

    public void deleteCamera(int index) {
        cameras.remove(index);
        if (index == activeCameraIndex) {
            activeCameraIndex = 0;
        }
    }

    public void setActiveCamera(int index) {
        activeCameraIndex = index;
    }

    public Camera getActiveCamera() {
        return cameras.get(activeCameraIndex);
    }

    public ModelSceneOptions getModelOptions(Model model) {
        return models.get(model);
    }

    public void addModelSceneOptions(Model model, ModelSceneOptions options) {
        models.put(model, options);
    }

    public Model getModelAt(Vector3f point) {
        for (Model model : models.keySet()) {
            for (Vector3f vertex : model.getVertices()) {
                if (vertex.equals(point)) {
                    return model;
                }
            }
        }
        return null;
    }

//    public boolean isEnableLightMoving() {
//        return enableLightMoving;
//    }
//
//    public void setEnableLightMoving(boolean enableLightMoving) {
//        this.enableLightMoving = enableLightMoving;
//    }

    public void renderScene(GraphicsContext graphicsContext, Camera camera/*, LightingManager lightingManager*/, double width, double height) {

        int w = (int) width;
        int h = (int) height;

        if (zBuffer == null || lastWidth != w || lastHeight != h) {
            zBuffer = new float[w * h];
            colorBuffer = new int[w * h];

            lastWidth = w;
            lastHeight = h;

      //      polygonZBuffer = new PolygonIndexData[h][w];

        }
        Arrays.fill(zBuffer, Float.MAX_VALUE);
        Arrays.fill(colorBuffer, 0);

//
//        for (int i = 0; i < h; i++) {
//            for (int j = 0; j < w; j++) {
//                polygonZBuffer[i][j] = null;
//            }
//        }

//        RenderContext renderContext = new RenderContext(w, h, zBuffer, colorBuffer, polygonZBuffer, chosenPolygonIndexData);
//
//
//        //рендеринг моделек
//        for (Model model : models) {
//            render(camera, model, renderContext, model.getRenderParameters(), lightingManager);
//
//        }
//
//        //рендер источников освещения
//        List<Light> lights = lightingManager.getActiveLights();
//        for (Light light : lights) {
//            if (light.isDrawable())
//                render(camera, light.getSphere(), renderContext, light.getRenderParameters(), lightingManager);
//
//        }


        WritableImage writableImage = new WritableImage(w, h);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), colorBuffer, 0, w);
        graphicsContext.drawImage(writableImage, 0, 0);

    }

}
