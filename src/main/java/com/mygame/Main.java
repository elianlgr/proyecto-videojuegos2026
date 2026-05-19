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
import java.util.ArrayList;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

public class Main extends SimpleApplication implements ActionListener {

    private Player player;
    private boolean up, down, left, right;
    private boolean enTransicion = false;
    private ArrayList<Arbusto> listaArbustos = new ArrayList<>();
    private GestorInteracciones interacciones;
    private GestorGUI gestorGUI;
    private PlayerPlataforma playerLateral;
    private boolean saltar = false;
    private boolean enEscenario2 = false;
    

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
        
        GestorEntorno entorno = new GestorEntorno(assetManager, rootNode, listaArbustos);
        entorno.crearCuadriculaAleatoria(100, 100, 4, 2);
        entorno.crearCuadriculaAleatoria(850, 700, 4, 2);
        entorno.crearCuadriculaAleatoria(1350, 320, 5, 3);
        entorno.crearCuadriculaAleatoria(200, 800, 3, 3);
        
        // se inicia el jugador (Player)
        // le pasamos el ancho y alto para que el mismo calcule su centro
        player = new Player(assetManager, rootNode, width, height);
        //creamos al jugador del escenario 2
        playerLateral = new PlayerPlataforma(assetManager, rootNode);
        
        // controles del juego
        initKeys();
        
        // limpiar pantalla de datos tecnicos
        setDisplayStatView(false);
        setDisplayFps(false);
        
        interacciones = new GestorInteracciones(player, listaArbustos);
        gestorGUI = new GestorGUI(assetManager, guiNode, cam, guiFont);
    }

    private void initKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "Up", "Down", "Left", "Right");
        
        // teclas para playerplataforma
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Up", "Down", "Left", "Right", "Jump");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Up")) up = isPressed;
        if (name.equals("Down")) down = isPressed;
        if (name.equals("Left")) left = isPressed;
        if (name.equals("Right")) right = isPressed;
        if (name.equals("Jump")) { saltar = isPressed; }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (enTransicion) {
            return; 
        }

        // en que escenario estamos jugando actualmente?
        if (enEscenario2) {
            // logica del escenario 2
            
            float direccionX = 0;
            if (left) direccionX = -1;
            if (right) direccionX = 1;
            // nota: ignoramos 'up' y 'down' porque aqui solo caminamos a los lados
            
            // le pasamos las teclas a nuestro simulador de fisicas
            playerLateral.actualizarFisicas(direccionX, saltar, tpf);
            
        } else {
            // logica del escenario 1 (top-down / pokemon)
            
            float dx = 0, dy = 0;
            if (up) dy = 1;
            if (down) dy = -1;
            if (left) dx = -1;
            if (right) dx = 1;

            if (dx != 0 || dy != 0) {
                player.move(dx, dy, tpf);
            } else {
                player.stop(); 
            }
            
            for (Arbusto arbusto : listaArbustos){
                arbusto.mecerConViento(tpf);
            }

            if (interacciones.vigilar(tpf)) {
                enTransicion = true;
                gestorGUI.mostrarPantallaCarga();
            }
        }
    }
}