package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.renderer.queue.RenderQueue.Bucket;

public class Player {
    private Geometry geom;
    private Quad quad;
    private float speed = 150f; 
    private float size = 60f; 
    
    // variables de animacion 
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.15f;
    private int columnaActual = 0; 
    private int filaActual = 0;    
    private boolean caminando = false;
    
    private float columnasTotales = 9f; 
    private float filasTotales = 9f;
    
    // limites de pantalla
    private float screenWidth;
    private float screenHeight;

    public Player(AssetManager assetManager, Node rootNode, float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        quad = new Quad(size, size);
        geom = new Geometry("PlayerNode", quad);
        
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        geom.setLocalTranslation(centerX, centerY, 5);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/player1.png"); 
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        actualizarFrame(0, 0);

        rootNode.attachChild(geom);
    }

    public void move(float dx, float dy, float tpf) {
        Vector3f pos = geom.getLocalTranslation();
        
        float nuevoX = pos.x + (dx * speed * tpf);
        float nuevoY = pos.y + (dy * speed * tpf);
        
        // limites de pantalla (muro invisible)
        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;
        
        if (nuevoY < 0) nuevoY = 0;
        if (nuevoY > screenHeight - size) nuevoY = screenHeight - size;

        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);
        
        // decidir la fila segun la direccion
        if (dy < 0) filaActual = 0;      
        else if (dy > 0) filaActual = 1;
        else if (dx > 0) filaActual = 3; 
        else if (dx < 0) filaActual = 2; 
        
        caminando = true;
        animar(tpf); 
    }
    
    public void stop() {
        caminando = false;
        columnaActual = 0; 
        actualizarFrame(columnaActual, filaActual);
    }

    private void animar(float tpf) {
        if (!caminando) return;

        tiempoFrame += tpf;
        if (tiempoFrame >= velocidadAnimacion) {
            tiempoFrame = 0;
            columnaActual++; 
            
            if (columnaActual > 3) {
                columnaActual = 0; 
            }
            actualizarFrame(columnaActual, filaActual);
        }
    }

    private void actualizarFrame(int columna, int fila) {
        int filaInvertida = 3 - fila;
        float tamanoFrame = 0.25f; 
        
        float xStart = columna * tamanoFrame;
        float xEnd = xStart + tamanoFrame;
        
        float yStart = filaInvertida * tamanoFrame;
        float yEnd = yStart + tamanoFrame;

        float[] texCoords = new float[]{
            xStart, yStart,
            xEnd,   yStart,
            xEnd,   yEnd,
            xStart, yEnd
        };

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    public Geometry getGeom() {
        return geom;
    }
    
    public boolean isCaminando() {
        return caminando;
    }
    
    public void resetPosicion() {
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        geom.setLocalTranslation(centerX, centerY, 5);
        stop();
    }
}