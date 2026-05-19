package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

public class GestorGUI {
    
    private AssetManager assetManager;
    private Node guiNode;
    private Camera cam;
    private BitmapFont guiFont;

    // El constructor recibe todas las herramientas de dibujo desde el Main
    public GestorGUI(AssetManager assetManager, Node guiNode, Camera cam, BitmapFont guiFont) {
        this.assetManager = assetManager;
        this.guiNode = guiNode;
        this.cam = cam;
        this.guiFont = guiFont;
    }

    // El método que antes tenías en el Main, ahora vive aquí
    public void mostrarPantallaCarga() {
        // 1. Creamos un fondo negro del tamaño de la pantalla
        Quad fondoQuad = new Quad(cam.getWidth(), cam.getHeight());
        Geometry fondoNegro = new Geometry("FondoNegro", fondoQuad);
        Material matNegro = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matNegro.setColor("Color", ColorRGBA.Black);
        fondoNegro.setMaterial(matNegro);
        
        // Lo pegamos al guiNode para que tape TODO el juego
        guiNode.attachChild(fondoNegro);

        // 2. Creamos el texto de "Cargando..."
        BitmapText textoCarga = new BitmapText(guiFont, false);
        textoCarga.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        textoCarga.setColor(ColorRGBA.White);
        textoCarga.setText("Cargando...");
        
        // Matemáticas para centrar el texto
        float x = (cam.getWidth() - textoCarga.getLineWidth()) / 2;
        float y = (cam.getHeight() + textoCarga.getLineHeight()) / 2;
        textoCarga.setLocalTranslation(x, y, 0);
        
        guiNode.attachChild(textoCarga);
        
        System.out.println("Pantalla de carga activada.");
    }
}