package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;

public class Main extends SimpleApplication implements ActionListener {

    private Player player;
    private PlayerPlataforma playerLateral;
    private ArrayList<Arbusto> listaArbustos = new ArrayList<>();
    private EnemigoCPU enemigo;
    
    // Gestores
    private GestorInteracciones interacciones;
    private GestorGUI gestorGUI;
    private GestorEscenarios gestorEscenarios; 
    
    // Variables de control y logica
    private boolean up, down, left, right, saltar;
    private boolean enTransicion = false;
    private boolean enEscenario2 = false;
    private float tiempoDeCarga = 0f;
    
    // NUEVO: Variables para controlar el Game Over
    private boolean enGameOver = false;
    private float tiempoGameOver = 0f;

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

        player = new Player(assetManager, rootNode, width, height);
        playerLateral = new PlayerPlataforma(assetManager, rootNode, width);
        enemigo = new EnemigoCPU(assetManager, rootNode, width);
        
        GestorEntorno entorno = new GestorEntorno(assetManager, rootNode, listaArbustos);
        entorno.crearCuadriculaAleatoria(100, 100, 4, 2);
        entorno.crearCuadriculaAleatoria(850, 700, 4, 2);
        entorno.crearCuadriculaAleatoria(1350, 320, 5, 3);
        entorno.crearCuadriculaAleatoria(200, 800, 3, 3);
        
        gestorGUI = new GestorGUI(assetManager, guiNode, cam, guiFont);
        gestorGUI.iniciarBarraVida(); 
        
        gestorEscenarios = new GestorEscenarios(assetManager, rootNode, player, playerLateral, listaArbustos, gestorGUI);
        gestorEscenarios.cargarFondos(width, height);
        gestorEscenarios.iniciarEscenario1(); 
        
        interacciones = new GestorInteracciones(player, listaArbustos);
        
        initKeys();
        setDisplayStatView(false);
        setDisplayFps(false);
    }

    private void initKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("TestDano", new KeyTrigger(KeyInput.KEY_T));
        
        inputManager.addListener(this, "Up", "Down", "Left", "Right", "Jump", "TestDano");
        
        inputManager.addMapping("Ataque", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Up", "Down", "Left", "Right", "Jump", "TestDano", "Ataque");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (enGameOver) return;
        
        if (name.equals("Up")) up = isPressed;
        if (name.equals("Down")) down = isPressed;
        if (name.equals("Left")) left = isPressed;
        if (name.equals("Right")) right = isPressed;
        if (name.equals("Jump")) saltar = isPressed; 
        
        if (name.equals("TestDano") && !isPressed) {
            if (enEscenario2) {
                playerLateral.recibirDano(1);
                gestorGUI.actualizarBarraVida(playerLateral.getVidaActual());
            }
        }

        if (name.equals("Ataque") && isPressed) {
            if (enEscenario2) {
                playerLateral.atacar();
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        
        if (enGameOver) {
            tiempoGameOver += tpf;
            
            if (tiempoGameOver >= 3.0f) {
                enGameOver = false;
                enEscenario2 = false;
                tiempoGameOver = 0f;
                
                player.resetPosicion();
                playerLateral.resetVidaYPosicion();
                enemigo.desactivar();
                
                gestorGUI.ocultarGameOver();
                gestorGUI.actualizarBarraVida(5); 
                
                gestorEscenarios.iniciarEscenario1();
                
                up = down = left = right = saltar = false; 
            }
            return; 
        }

        if (enTransicion) {
            tiempoDeCarga += tpf; 
            if (tiempoDeCarga >= 3.0f) { 
                enTransicion = false;
                enEscenario2 = true;
                gestorEscenarios.cambiarAEscenario2();
                enemigo.activar();
                gestorGUI.ocultarPantallaCarga(); 
            }
            return; 
        }

        if (enEscenario2) {
            float direccionX = 0;
            if (left) direccionX = -1;
            if (right) direccionX = 1;
            
            playerLateral.actualizarFisicas(direccionX, saltar, tpf);
            enemigo.actualizarInteligencia(playerLateral, tpf);
            
            if (playerLateral.getVidaActual() <= 0) {
                enGameOver = true;
                tiempoGameOver = 0f;
                gestorGUI.mostrarGameOver();
            }
            
        } else {
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
                tiempoDeCarga = 0f; 
                gestorGUI.mostrarPantallaCarga();
            }
        }
    }
}