package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class Main extends SimpleApplication implements ActionListener {

    private Player player;
    private boolean up, down, left, right;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
        cam.setParallelProjection(true);
        
        float width = cam.getWidth();
        float height = cam.getHeight();
        
        cam.setFrustum(-1000, 1000, 0, width, height, 0);
        cam.setLocation(new Vector3f(0, 0, 10));

        // configuramos para que el mapa se extienda por toda la pantalla
        Quad mapQuad = new Quad(width, height); 
        Geometry mapGeo = new Geometry("MapaMundo", mapQuad);
        mapGeo.setLocalTranslation(0, 0, -1); 

        Material matMap = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture mapTexture = assetManager.loadTexture("Textures/escenario1.png");
        matMap.setTexture("ColorMap", mapTexture);
        mapGeo.setMaterial(matMap);
        rootNode.attachChild(mapGeo);

        // se inicia el jugador (Player)
        // le pasamos el ancho y alto para que el mismo calcule su centro
        player = new Player(assetManager, rootNode, width, height);

        // controles del juego
        initKeys();
        
        // limpiar pantalla de datos tecnicos
        setDisplayStatView(false);
        setDisplayFps(false);
    }

    private void initKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "Up", "Down", "Left", "Right");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Up")) up = isPressed;
        if (name.equals("Down")) down = isPressed;
        if (name.equals("Left")) left = isPressed;
        if (name.equals("Right")) right = isPressed;
    }

    @Override
    public void simpleUpdate(float tpf) {
        float dx = 0, dy = 0;
        if (up) dy = 1;
        if (down) dy = -1;
        if (left) dx = -1;
        if (right) dx = 1;

        if (dx != 0 || dy != 0) {
            player.move(dx, dy, tpf);
        }
    }
}