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

// Representa al jugador en una vista cenital, gestionando su posicion, fisicas basicas y animacion por sprites
public class Player {
    
    // Componentes visuales y fisicos del personaje
    private Geometry geom;
    private Quad quad;
    private float speed = 150f; 
    private float size = 60f; 
    
    // Variables encargadas de controlar los tiempos y estados de la animacion
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.15f;
    private int columnaActual = 0; 
    private int filaActual = 0;    
    private boolean caminando = false;
    
    // Dimensiones totales de la hoja de sprites
    private float columnasTotales = 9f; 
    private float filasTotales = 9f;
    
    // Dimensiones de la ventana para evitar que el jugador salga del area visible
    private float screenWidth;
    private float screenHeight;

    // Constructor que inicializa el modelo grafico y lo añade al escenario principal
    public Player(AssetManager assetManager, Node rootNode, float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        quad = new Quad(size, size);
        geom = new Geometry("PlayerNode", quad);
        
        // Calcula el centro de la pantalla para ubicar al personaje al inicio
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        geom.setLocalTranslation(centerX, centerY, 5);

        // Aplica el material y la textura principal con soporte para transparencias
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/player1.png"); 
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        // Asegura que el personaje siempre se renderice y permita ver objetos detras de el
        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        // Muestra el primer fotograma de la animacion
        actualizarFrame(0, 0);

        rootNode.attachChild(geom);
    }

    // Calcula la nueva posicion del jugador y gestiona los limites y cambios de direccion
    public void move(float dx, float dy, float tpf) {
        Vector3f pos = geom.getLocalTranslation();
        
        float nuevoX = pos.x + (dx * speed * tpf);
        float nuevoY = pos.y + (dy * speed * tpf);
        
        // Colisiones logicas para mantener al jugador dentro del marco de la pantalla
        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;
        
        if (nuevoY < 0) nuevoY = 0;
        if (nuevoY > screenHeight - size) nuevoY = screenHeight - size;

        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);
        
        // Cambia la fila del spritesheet para reflejar la direccion hacia la que camina
        if (dy < 0) filaActual = 0;      
        else if (dy > 0) filaActual = 1;
        else if (dx > 0) filaActual = 3; 
        else if (dx < 0) filaActual = 2; 
        
        // Activa la bandera de movimiento y llama al ciclo de animacion
        caminando = true;
        animar(tpf); 
    }
    
    // Detiene la caminata y restablece el sprite a su estado de reposo en la misma direccion
    public void stop() {
        caminando = false;
        columnaActual = 0; 
        actualizarFrame(columnaActual, filaActual);
    }

    // Avanza a traves de las columnas del spritesheet segun el tiempo transcurrido
    private void animar(float tpf) {
        if (!caminando) return;

        tiempoFrame += tpf;
        if (tiempoFrame >= velocidadAnimacion) {
            tiempoFrame = 0;
            columnaActual++; 
            
            // Reinicia el bucle de animacion al llegar al ultimo fotograma de la fila
            if (columnaActual > 3) {
                columnaActual = 0; 
            }
            actualizarFrame(columnaActual, filaActual);
        }
    }

    // Modifica las coordenadas UV del Quad para mostrar una seccion especifica de la textura
    private void actualizarFrame(int columna, int fila) {
        // Invierte la coordenada Y ya que OpenGL lee las texturas desde la esquina inferior izquierda
        int filaInvertida = 3 - fila;
        
        // Define el tamano proporcional de cada fotograma respecto al total de la imagen
        float tamanoFrame = 0.25f; 
        
        // Calcula los limites horizontales y verticales del fotograma a mostrar
        float xStart = columna * tamanoFrame;
        float xEnd = xStart + tamanoFrame;
        
        float yStart = filaInvertida * tamanoFrame;
        float yEnd = yStart + tamanoFrame;

        // Establece los 4 puntos de textura para mapearlos al modelo cuadrado
        float[] texCoords = new float[]{
            xStart, yStart,
            xEnd,   yStart,
            xEnd,   yEnd,
            xStart, yEnd
        };

        // Aplica el nuevo mapeo borrando el bufer de coordenadas anterior
        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    // Devuelve el nodo geometrico para integrarlo en interacciones de colision con otros objetos
    public Geometry getGeom() {
        return geom;
    }
    
    // Permite verificar desde clases externas si el personaje esta en movimiento
    public boolean isCaminando() {
        return caminando;
    }
    
    // Devuelve al jugador a las coordenadas iniciales en el centro del escenario
    public void resetPosicion() {
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        geom.setLocalTranslation(centerX, centerY, 5);
        stop();
    }
}