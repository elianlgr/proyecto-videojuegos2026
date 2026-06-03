package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;

public class GestorGUI {
    
    private AssetManager assetManager;
    private Node guiNode;
    private Camera cam;
    private BitmapFont guiFont;
    
    // Variables para la interfaz
    private Geometry fondoNegro;
    private BitmapText textoCarga;
    private Geometry barraVidaGeo;
    private Quad quadBarra;
    
    // NUEVO: Variable para la pantalla de Game Over
    private Geometry gameOverGeo;

    public GestorGUI(AssetManager assetManager, Node guiNode, Camera cam, BitmapFont guiFont) {
        this.assetManager = assetManager;
        this.guiNode = guiNode;
        this.cam = cam;
        this.guiFont = guiFont;
    }

    public void mostrarPantallaCarga() {
        if (fondoNegro == null) {
            Quad fondoQuad = new Quad(cam.getWidth(), cam.getHeight());
            fondoNegro = new Geometry("FondoNegro", fondoQuad);
            Material matNegro = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            matNegro.setColor("Color", ColorRGBA.Black);
            fondoNegro.setMaterial(matNegro);
            
            textoCarga = new BitmapText(guiFont, false);
            textoCarga.setSize(guiFont.getCharSet().getRenderedSize() * 2);
            textoCarga.setColor(ColorRGBA.White);
            textoCarga.setText("Cargando...");
            
            float x = (cam.getWidth() - textoCarga.getLineWidth()) / 2;
            float y = (cam.getHeight() + textoCarga.getLineHeight()) / 2;
            textoCarga.setLocalTranslation(x, y, 0);
        }
        guiNode.attachChild(fondoNegro);
        guiNode.attachChild(textoCarga);
    }
    
    public void ocultarPantallaCarga() {
        if (fondoNegro != null && guiNode.hasChild(fondoNegro)) {
            guiNode.detachChild(fondoNegro);
            guiNode.detachChild(textoCarga);
        }
    }

    // --- NUEVOS METODOS GAME OVER ---
    public void mostrarGameOver() {
        if (gameOverGeo == null) {
            Quad quad = new Quad(cam.getWidth(), cam.getHeight());
            gameOverGeo = new Geometry("GameOverPantalla", quad);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture tex = assetManager.loadTexture("Textures/GameOver.png");
            mat.setTexture("ColorMap", tex);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            gameOverGeo.setMaterial(mat);
        }
        guiNode.attachChild(gameOverGeo);
    }

    public void ocultarGameOver() {
        if (gameOverGeo != null && guiNode.hasChild(gameOverGeo)) {
            guiNode.detachChild(gameOverGeo);
        }
    }

    // --- METODOS DE BARRA DE VIDA ---
    public void iniciarBarraVida() {
        float width = 180f;
        float height = 45f;
        
        quadBarra = new Quad(width, height);
        barraVidaGeo = new Geometry("BarraVida", quadBarra);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/barra_vida.png");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaGeo.setMaterial(mat);
        
        barraVidaGeo.setLocalTranslation(20, cam.getHeight() - height - 20, 0);
        barraVidaGeo.setCullHint(CullHint.Always);
        guiNode.attachChild(barraVidaGeo);
        actualizarBarraVida(5); 
    }

    public void mostrarBarraVida() {
        barraVidaGeo.setCullHint(CullHint.Never);
    }
    
    public void ocultarBarraVida() {
        if (barraVidaGeo != null) {
            barraVidaGeo.setCullHint(CullHint.Always);
        }
    }

    public void actualizarBarraVida(int vidaActual) {
        if (vidaActual < 0) vidaActual = 0;
        if (vidaActual > 5) vidaActual = 5;

        float tamanoFrameY = 1.0f / 6.0f; 
        float yStart = vidaActual * tamanoFrameY;
        float yEnd = yStart + tamanoFrameY;

        float[] texCoords = new float[]{
            0, yStart, 
            1, yStart, 
            1, yEnd,   
            0, yEnd    
        };

        quadBarra.clearBuffer(VertexBuffer.Type.TexCoord);
        quadBarra.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
}