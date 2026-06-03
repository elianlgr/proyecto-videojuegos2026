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

public class EnemigoCPU {
    private Geometry geom;
    private Quad quad;
    private Material mat;
    private float speed = 120f;
    private float size = 450f;
    
    // Físicas
    private float velocidadY = 0f;
    private final float GRAVEDAD = -700f;
    private float sueloY = -75f;
    private boolean enElSuelo = false;
    private float screenWidth;
    
    // IA y combate
    private int vidaActual = 5;
    private boolean atacando = false;
    private float tiempoCooldown = 0f;
    private final float DISTANCIA_ATAQUE = 200f;

    // Animación y Texturas
    private Texture texIdle, texWalk, texAttack;
    private int totalFrames = 10;
    private int columnaActual = 0;
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.1f;
    private String estadoActual = "IDLE";

    public EnemigoCPU(AssetManager assetManager, Node rootNode, float screenWidth) {
        this.screenWidth = screenWidth;
        quad = new Quad(100f, 150f);
        quad = new Quad(size, size);
        geom = new Geometry("EnemigoCPU", quad);
        geom.setLocalTranslation(screenWidth - 300, sueloY, 6);

        // Carga de texturas individuales
        texIdle = assetManager.loadTexture("Textures/Idle.png");
        texWalk = assetManager.loadTexture("Textures/Walk.png");
        texAttack = assetManager.loadTexture("Textures/Attack.png");
        configurarFiltros(texIdle);
        configurarFiltros(texWalk);
        configurarFiltros(texAttack);

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texIdle);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Always);
        geom.setQueueBucket(Bucket.Transparent);
        rootNode.attachChild(geom);
    }

    private void configurarFiltros(Texture tex) {
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
    }

    // --- MÉTODOS QUE EL MAIN ESTÁ BUSCANDO ---
    public void activar() { geom.setCullHint(CullHint.Never); }
    public void desactivar() { geom.setCullHint(CullHint.Always); }

    public void actualizarInteligencia(PlayerPlataforma jugador, float tpf) {
        if (vidaActual <= 0) return;

        float miX = geom.getLocalTranslation().x;
        float jugadorX = jugador.getGeom().getLocalTranslation().x;
        float distancia = jugadorX - miX;
        float distanciaAbsoluta = Math.abs(distancia);

        float direccionX = 0;
        if (tiempoCooldown > 0) tiempoCooldown -= tpf;

        if (atacando) {
            direccionX = 0;
        } else if (distanciaAbsoluta <= DISTANCIA_ATAQUE && tiempoCooldown <= 0) {
            atacar();
        } else if (distanciaAbsoluta > DISTANCIA_ATAQUE) {
            direccionX = (distancia > 0) ? 1 : -1;
        }

        actualizarFisicas(direccionX, tpf);
    }

    private void atacar() {
        atacando = true;
        cambiarEstado("ATTACK");
        tiempoCooldown = 1.5f;
    }

    private void actualizarFisicas(float direccionX, float tpf) {
        Vector3f pos = geom.getLocalTranslation();
        float nuevoX = pos.x + (direccionX * speed * tpf);

        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;

        if (!enElSuelo) velocidadY += GRAVEDAD * tpf;
        float nuevoY = pos.y + (velocidadY * tpf);
        if (nuevoY <= sueloY) { nuevoY = sueloY; velocidadY = 0; enElSuelo = true; }

        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);

        // Selección de animación
        if (atacando) {
            if (columnaActual >= totalFrames - 1) atacando = false; // Termina ataque
        } else {
            cambiarEstado(direccionX != 0 ? "WALK" : "IDLE");
        }

        // Animación (Timer)
        tiempoFrame += tpf;
        if (tiempoFrame >= velocidadAnimacion) {
            tiempoFrame = 0;
            columnaActual = (columnaActual + 1) % totalFrames;
            actualizarUV(columnaActual, totalFrames);
        }
    }

    private void cambiarEstado(String nuevoEstado) {
        if (this.estadoActual.equals(nuevoEstado)) return;
        this.estadoActual = nuevoEstado;
        this.columnaActual = 0;
        switch (nuevoEstado) {
            case "IDLE":   mat.setTexture("ColorMap", texIdle);   totalFrames = 10; break;
            case "WALK":   mat.setTexture("ColorMap", texWalk);   totalFrames = 10; break;
            case "ATTACK": mat.setTexture("ColorMap", texAttack); totalFrames = 5;  break;
        }
    }

    private void actualizarUV(int frame, int total) {
        float tamanoFrameX = 1.0f / total;
        // Ajustamos ligeramente las coordenadas para no tocar los bordes del frame
        float offset = 0.005f; 
        float xStart = (frame * tamanoFrameX) + offset;
        float xEnd = ((frame + 1) * tamanoFrameX) - offset;

        // Mantenemos la orientación correcta (0 abajo, 1 arriba)
        float[] texCoords = new float[]{
            xStart, 0,  
            xEnd, 0,    
            xEnd, 1,    
            xStart, 1   
        };

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
}