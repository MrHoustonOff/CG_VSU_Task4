package com.cgvsu.scene;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Scene {
    private HashMap<Model, ModelSceneOptions> models;
    private Model activeModel;

    private List<Camera> cameras;
    private int activeCameraIndex;

    private final Camera DEFAULT_CAMERA = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

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
}