package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture;
import java.util.ArrayList;

public class GestorEscenarios {
    
    private AssetManager assetManager;
    private Node rootNode;
    
    // Geometrias que sostendran las imagenes de tus fondos
    private Geometry mapGeo1;
    private Geometry mapGeo2;
    
    // Referencias a los actores y gestores externos
    private Player playerTopDown;
    private PlayerPlataforma playerLateral;
    private ArrayList<Arbusto> listaArbustos;
    private GestorGUI gestorGUI;

    // Constructor actualizado
    public GestorEscenarios(AssetManager assetManager, Node rootNode, Player p1, PlayerPlataforma p2, ArrayList<Arbusto> arbustos, GestorGUI gui) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.playerTopDown = p1;
        this.playerLateral = p2;
        this.listaArbustos = arbustos;
        this.gestorGUI = gui; 
    }

    // Metodo para crear y cargar los fondos en memoria
    public void cargarFondos(float width, float height) {
        Quad mapQuad = new Quad(width, height); 
        
        // --- FONDO ESCENARIO 1 (Tipo Pokemon) ---
        mapGeo1 = new Geometry("MapaMundo1", mapQuad);
        mapGeo1.setLocalTranslation(0, 0, -1); 
        Material matMap1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture mapTexture1 = assetManager.loadTexture("Textures/escenario1.png");
        matMap1.setTexture("ColorMap", mapTexture1);
        mapGeo1.setMaterial(matMap1);
        rootNode.attachChild(mapGeo1);
        
        // --- FONDO ESCENARIO 2 (Tipo Hollow Knight) ---
        mapGeo2 = new Geometry("MapaMundo2", mapQuad);
        mapGeo2.setLocalTranslation(0, 0, -1); 
        Material matMap2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture mapTexture2 = assetManager.loadTexture("Textures/escenario2.jpg");
        matMap2.setTexture("ColorMap", mapTexture2);
        mapGeo2.setMaterial(matMap2);
        rootNode.attachChild(mapGeo2);
    }

    // Configuracion inicial al arrancar el juego
    public void iniciarEscenario1() {
        mapGeo1.setCullHint(CullHint.Never);             
        mapGeo2.setCullHint(CullHint.Always);            
        playerTopDown.getGeom().setCullHint(CullHint.Never); 
        playerLateral.desactivar();                      
        
        for (Arbusto arbusto : listaArbustos) {
            arbusto.getNodo().setCullHint(CullHint.Never);
        }
        if (gestorGUI != null) {
            gestorGUI.ocultarBarraVida();
        }
    }

    // Ejecuta la transicion visual completa al campo de batalla
    public void cambiarAEscenario2() {
        // 1. Ocultar todo lo del Nivel 1 
        mapGeo1.setCullHint(CullHint.Always);
        playerTopDown.getGeom().setCullHint(CullHint.Always);
        for (Arbusto arbusto : listaArbustos) {
            arbusto.getNodo().setCullHint(CullHint.Always);
        }
        
        // 2. Mostrar Nivel 2 
        mapGeo2.setCullHint(CullHint.Never);
        playerLateral.activar();
        
        // 3. Encender la interfaz de combate
        gestorGUI.mostrarBarraVida(); 
        
        System.out.println("Transicion al Escenario 2 gestionada exitosamente.");
    }
}