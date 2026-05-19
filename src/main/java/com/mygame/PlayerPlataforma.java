package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture;
import com.jme3.renderer.queue.RenderQueue.Bucket;

public class PlayerPlataforma {
    private Geometry geom;
    private Quad quad;
    // un poco mas rapido para el estilo plataforma
    private float speed = 200f; 
    private float size = 60f; 
    
    // velocidad vertical actual (subiendo o cayendo)
    private float velocidadY = 0f;          
    // fuerza constante que nos jala hacia abajo
    private final float GRAVEDAD = -700f;
    // el impulso inicial hacia arriba
    private final float FUERZA_SALTO = 350f; 
    // linea del suelo temporal (mientras hacemos bloques)
    private float sueloY = 100f;             
    // ¿esta pisando firme?
    private boolean enElSuelo = false;       

    public PlayerPlataforma(AssetManager assetManager, Node rootNode) {
        quad = new Quad(size, size);
        geom = new Geometry("PlayerPlataformaNode", quad);
        
        // lo colocamos abajo a la izquierda para empezar el nivel lateral
        // z=6 para que se dibuje por encima de todo
        geom.setLocalTranslation(100, sueloY, 6); 

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        // nota: por ahora usare tu player.png actual para que compile sin errores.
        // cuando dibujes tu hoja de sprites de perfil, solo cambias esto a "Textures/player_lateral.png"
        Texture tex = assetManager.loadTexture("Textures/player.png"); 
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        // al principio del juego este personaje estara escondido
        desactivar();

        rootNode.attachChild(geom);
    }

    // el motor de las fisicas: este metodo calculara la gravedad en tiempo real
    public void actualizarFisicas(float direccionX, boolean quiereSaltar, float tpf) {
        Vector3f pos = geom.getLocalTranslation();

        // 1. calcular movimiento horizontal (solo izquierda -1, derecha 1, o quieto 0)
        float nuevoX = pos.x + (direccionX * speed * tpf);

        // 2. aplicar gravedad (si no esta en el suelo, acumula velocidad de caida)
        if (!enElSuelo) {
            velocidadY += GRAVEDAD * tpf; 
        }

        // 3. logica del salto
        if (quiereSaltar && enElSuelo) {
            // asignamos el impulso vertical directo
            velocidadY = FUERZA_SALTO; 
            // en el aire inmediatamente
            enElSuelo = false;         
        }

        // 4. calcular movimiento vertical
        float nuevoY = pos.y + (velocidadY * tpf);

        // 5. colision temporal con el suelo
        // si el calculo dice que va a bajar mas alla de nuestra linea de suelo, lo frenamos
        if (nuevoY <= sueloY) {
            nuevoY = sueloY;
            velocidadY = 0;
            enElSuelo = true;
        }

        // aplicamos la posicion final calculada en este frame
        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);
    }

    // metodos para encender y apagar al personaje cuando cambiemos de escenario
    public void activar() {
        geom.setCullHint(CullHint.Never);
    }

    public void desactivar() {
        geom.setCullHint(CullHint.Always);
    }
    
    public Geometry getGeom() {
        return geom;
    }
}