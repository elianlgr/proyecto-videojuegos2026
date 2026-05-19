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
    // le baje la velocidad para que la caminata se vea natural
    private float speed = 150f; 
    private float size = 60f; 
    
    // variables de animacion 
    private float tiempoFrame = 0;
    // que tan rapido cambia de piecito
    private float velocidadAnimacion = 0.15f;
    // de la 0 a la 3 (los pasos)
    private int columnaActual = 0; 
    // 0=frente, 1=espaldas, 2=derecha, 3=izquierda
    private int filaActual = 0;    
    private boolean caminando = false;

    public Player(AssetManager assetManager, Node rootNode, float screenWidth, float screenHeight) {
        quad = new Quad(size, size);
        geom = new Geometry("PlayerNode", quad);
        
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        geom.setLocalTranslation(centerX, centerY, 5);

        // cargamos tu lamina de sprites
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/player.png"); 
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        // forzamos al "marco de carton" a empezar en el primer cuadrito (frente, quieto)
        actualizarFrame(0, 0);

        rootNode.attachChild(geom);
    }

    public void move(float dx, float dy, float tpf) {
        Vector3f pos = geom.getLocalTranslation();
        geom.setLocalTranslation(pos.x + (dx * speed * tpf), pos.y + (dy * speed * tpf), pos.z);
        
        // decidir la fila segun la direccion
        // -Y = camina hacia abajo (Frente)
        if (dy < 0) filaActual = 0;      
        // +Y = camina hacia arriba (Espaldas)
        else if (dy > 0) filaActual = 1;
        // +X = camina a la derecha
        else if (dx > 0) filaActual = 3; 
        // -X = camina a la izquierda
        else if (dx < 0) filaActual = 2; 
        
        // ejecutamos la animacion de los pies
        caminando = true;
        animar(tpf); 
    }
    
    // metodo para detenerlo si el usuario suelta las teclas
    public void stop() {
        caminando = false;
        // regresamos al "cuadro 0" de la fila actual para que se quede de pie quieto
        columnaActual = 0; 
        actualizarFrame(columnaActual, filaActual);
    }

    private void animar(float tpf) {
        if (!caminando) return;

        tiempoFrame += tpf;
        if (tiempoFrame >= velocidadAnimacion) {
            tiempoFrame = 0;
            // pasamos al siguiente cuadro de la caminata
            columnaActual++; 
            
            // si ya dimos los 4 pasos, volvemos a empezar el ciclo
            if (columnaActual > 3) {
                columnaActual = 0; 
            }
            actualizarFrame(columnaActual, filaActual);
        }
    }

    // qui "recortamos" la imagen en tiempo real
    private void actualizarFrame(int columna, int fila) {
        int filaInvertida = 3 - fila;
        
        // como es una grilla de 4x4, cada cuadro ocupa exactamente el 25% (0.25f)
        float tamanoFrame = 0.25f; 
        
        float xStart = columna * tamanoFrame;
        float xEnd = xStart + tamanoFrame;
        
        float yStart = filaInvertida * tamanoFrame;
        float yEnd = yStart + tamanoFrame;

        // le pasamos las coordenadas de nuestro "marco" al motor grafico
        float[] texCoords = new float[]{
            xStart, yStart,
            xEnd,   yStart,
            xEnd,   yEnd,
            xStart, yEnd
        };

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    // nos permite tener la figura para calcular colisiones 
    public Geometry getGeom() {
        return geom;
    }
    
    // nos dice si el jugador se esta moviendo o no 
    public boolean isCaminando() {
        return caminando;
    }
}