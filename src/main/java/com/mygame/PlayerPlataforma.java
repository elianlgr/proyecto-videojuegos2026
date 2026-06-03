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

public class PlayerPlataforma {
    private Geometry geom;
    private Quad quad;
    private float speed = 200f; 
    private float size = 450f; 
    
    // fisicas
    private float velocidadY = 0f;          
    private final float GRAVEDAD = -700f;
    private final float FUERZA_SALTO = 350f; 
    private float sueloY = -75f;          
    private boolean enElSuelo = false;       
    private float screenWidth;
    
    // vida
    private int vidaMaxima = 5;
    private int vidaActual = 5;

    // --- VARIABLES DE LA CUADRICULA (SPRITE SHEET) ---
    private float columnasTotales = 10f; 
    private float filasTotales = 8f;

    // variables de animacion
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.06f; // CORREGIDO: Velocidad rapida para que no se vea estatico
    private int columnaActual = 0; 
    private int filaActual = 0; 
    private boolean mirandoIzquierda = false;
    private boolean anteriorMirandoIzquierda = false;
    
    // Maquina de Estados Visuales
    private int estadoActual = 0;
    
    // variable de combate
    private boolean atacando = false;

    public PlayerPlataforma(AssetManager assetManager, Node rootNode, float screenWidth) {
        this.screenWidth = screenWidth;
        quad = new Quad(size, size);
        geom = new Geometry("PlayerPlataformaNode", quad);
        geom.setLocalTranslation(100, sueloY, 6); 

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/PlayerPlataforma.png"); 
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        actualizarFrame(0, 0, false);
        desactivar();
        rootNode.attachChild(geom);
    }

    public void actualizarFisicas(float direccionX, boolean quiereSaltar, float tpf) {

        if (atacando) {
            direccionX = 0; 
        }

        anteriorMirandoIzquierda = mirandoIzquierda;
        if (direccionX < 0) mirandoIzquierda = true;
        else if (direccionX > 0) mirandoIzquierda = false;

        Vector3f pos = geom.getLocalTranslation();
        float nuevoX = pos.x + (direccionX * speed * tpf);

        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;

        if (!enElSuelo) velocidadY += GRAVEDAD * tpf; 

        if (quiereSaltar && enElSuelo && !atacando) {
            velocidadY = FUERZA_SALTO; 
            enElSuelo = false;         
        }

        float nuevoY = pos.y + (velocidadY * tpf);
        if (nuevoY <= sueloY) {
            nuevoY = sueloY;
            velocidadY = 0;
            enElSuelo = true;
        }

        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);

        // --- MAQUINA DE ESTADOS VISUALES ---
        int nuevoEstado = 0;
        
        if (atacando) {
            nuevoEstado = filaActual; 
        } else if (!enElSuelo) {
            nuevoEstado = 4; // Salto
        } else if (direccionX != 0) {
            nuevoEstado = 2; // CORREGIDO: Fila 2 para correr, ya no desliza
        } else {
            nuevoEstado = 0; // Quieto
        }

        boolean cambioEstado = (nuevoEstado != estadoActual);
        boolean cambioDireccion = (mirandoIzquierda != anteriorMirandoIzquierda);
        
        if (cambioEstado || cambioDireccion) {
            estadoActual = nuevoEstado;
            filaActual = nuevoEstado;
            
            if (cambioEstado) {
                columnaActual = 0;
                tiempoFrame = 0;
            }
            actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
        }

        // --- GESTOR DE FOTOGRAMAS ---
        tiempoFrame += tpf;
        float limiteTiempo = atacando ? 0.08f : velocidadAnimacion;

        if (tiempoFrame >= limiteTiempo) {
            tiempoFrame = 0;
            columnaActual++;
            
            int limiteColumnas = obtenerMaximoColumnas(filaActual);
            
            if (columnaActual > limiteColumnas) {
                if (atacando) {
                    atacando = false;
                    estadoActual = -1; 
                } else if (filaActual == 4) {
                    columnaActual = limiteColumnas; // Congela el dibujo SOLO si está en el salto
                } else {
                    columnaActual = 0; // Reinicia a 0 cualquier otra accion (como correr), haciendo el bucle
                }
                actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
            } else {
                actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
            }
        }
    }

    // CORREGIDO: Limites exactos basados en la cantidad real de dibujos en tu PNG
    private int obtenerMaximoColumnas(int fila) {
        switch(fila) {
            case 0: return 0; // Quieto (1 dibujo)
            case 1: return 9; // Caminar (10 dibujos)
            case 2: return 9; // Correr (10 dibujos)
            case 3: return 7; // Dash (8 dibujos)
            case 4: return 4; // Salto/Caida (5 dibujos)
            case 5: return 2; // Ataque 1 (3 dibujos)
            case 6: return 2; // Ataque 2 (3 dibujos)
            case 7: return 3; // Ataque 3 (4 dibujos)
            default: return 0;
        }
    }

    public void atacar() {
        if (!atacando && enElSuelo) {
            atacando = true;
            columnaActual = 0; 
            tiempoFrame = 0;
            
            filaActual = 6; // El golpe especifico de espada
            
            estadoActual = filaActual; 
            
            actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
        }
    }

    private void actualizarFrame(int columna, int fila, boolean izquierda) {
        int filaInvertida = (int)(filasTotales - 1) - fila; 
        
        float tamanoFrameX = 1.0f / columnasTotales; 
        float tamanoFrameY = 1.0f / filasTotales; 
        
        float xStart = columna * tamanoFrameX;
        float xEnd = xStart + tamanoFrameX;
        
        float yStart = filaInvertida * tamanoFrameY;
        float yEnd = yStart + tamanoFrameY;

        float[] texCoords;

        if (izquierda) {
            texCoords = new float[]{
                xEnd,   yStart,
                xStart, yStart,
                xStart, yEnd,
                xEnd,   yEnd
            };
        } else {
            texCoords = new float[]{
                xStart, yStart,
                xEnd,   yStart,
                xEnd,   yEnd,
                xStart, yEnd
            };
        }

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    public void recibirDano(int cantidad) { vidaActual -= cantidad; if (vidaActual < 0) vidaActual = 0; }
    public int getVidaActual() { return vidaActual; }
    public void resetVidaYPosicion() { vidaActual = vidaMaxima; velocidadY = 0f; enElSuelo = false; geom.setLocalTranslation(100, sueloY, 6); }
    public void activar() { geom.setCullHint(CullHint.Never); }
    public void desactivar() { geom.setCullHint(CullHint.Always); }
    public Geometry getGeom() { return geom; }
}