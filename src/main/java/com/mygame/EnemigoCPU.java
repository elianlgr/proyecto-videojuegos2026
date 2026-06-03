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

// Clase que representa a un enemigo controlado por la computadora con inteligencia artificial basica
public class EnemigoCPU {
    
    // Componentes visuales y de renderizado del enemigo
    private Geometry geom;
    private Quad quad;
    private Material mat;
    
    // Variables fisicas para el movimiento, tamano y control de caida
    private float speed = 120f;
    private float size = 300f;
    private float velocidadY = 0f;
    private final float GRAVEDAD = -700f;
    private float sueloY = 85f;
    private boolean enElSuelo = false;
    private float screenWidth;
    
    // Atributos de inteligencia artificial, vida y logica de combate
    private int vidaActual = 5;
    private boolean atacando = false;
    private boolean yaHizoDano = false; 
    private float tiempoCooldown = 0f;
    
    // Distancia minima requerida para ejecutar un ataque cuerpo a cuerpo
    private final float DISTANCIA_ATAQUE = 280f;

    // Recursos y contadores para la gestion de las animaciones por hojas de sprites
    private Texture texIdle, texWalk, texAttack;
    private int totalFrames = 10;
    private int columnaActual = 0;
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.1f;
    private String estadoActual = ""; 
    private boolean mirandoIzquierda = false;

    // Inicializa el modelo del enemigo, carga sus texturas y lo posiciona oculto en el escenario
    public EnemigoCPU(AssetManager assetManager, Node rootNode, float screenWidth) {
        this.screenWidth = screenWidth;
        quad = new Quad(size, size); 
        geom = new Geometry("EnemigoCPU", quad);
        
        texIdle = assetManager.loadTexture("Textures/Idle.png");
        texWalk = assetManager.loadTexture("Textures/Walk.png");
        texAttack = assetManager.loadTexture("Textures/Attack.png");
        configurarTextura(texIdle);
        configurarTextura(texWalk);
        configurarTextura(texAttack);

        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texIdle);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Always);
        geom.setQueueBucket(Bucket.Transparent);
        geom.setLocalTranslation(screenWidth - 300, sueloY, 6);
        rootNode.attachChild(geom);
    }

    // Configura el filtrado de las texturas para mantener un estilo pixel art nitido y sin difuminados
    private void configurarTextura(Texture tex) {
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        tex.setWrap(Texture.WrapMode.EdgeClamp);
    }

    // Evalua la distancia respecto al jugador para decidir si debe moverse hacia el o atacar
    public void actualizarInteligencia(PlayerPlataforma jugador, GestorGUI gui, float tpf) {
        if (vidaActual <= 0) return;

        // Calcula los centros exactos de ambos modelos para una medicion de distancia mas precisa
        float miCentroX = geom.getLocalTranslation().x + (size / 2);
        float jugadorCentroX = jugador.getGeom().getLocalTranslation().x + 225f; 
        
        float distancia = jugadorCentroX - miCentroX;
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

        actualizarFisicas(direccionX, tpf, jugador, gui);
    }

    // Inicia la secuencia ofensiva y establece un tiempo de espera antes del siguiente ataque permitido
    private void atacar() {
        atacando = true;
        yaHizoDano = false; 
        cambiarEstado("ATTACK", 5);
        tiempoCooldown = 0.6f;
    }

    // Aplica el movimiento horizontal, la caida por gravedad, gestiona colisiones y controla los fotogramas
    private void actualizarFisicas(float direccionX, float tpf, PlayerPlataforma jugador, GestorGUI gui) {
        if (direccionX < 0) mirandoIzquierda = true;
        else if (direccionX > 0) mirandoIzquierda = false;

        Vector3f pos = geom.getLocalTranslation();
        float nuevoX = pos.x + (direccionX * speed * tpf);

        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;

        if (!enElSuelo) velocidadY += GRAVEDAD * tpf;
        float nuevoY = pos.y + (velocidadY * tpf);
        
        if (nuevoY <= sueloY) { 
            nuevoY = sueloY; 
            velocidadY = 0; 
            enElSuelo = true; 
        } else { 
            enElSuelo = false; 
        }

        geom.setLocalTranslation(Math.round(nuevoX), Math.round(nuevoY), pos.z);

        if (atacando) {
            if (columnaActual >= totalFrames - 1) atacando = false;
            
            // Detecta el momento exacto del golpe en la animacion para aplicar dano al jugador
            if (columnaActual == 2 && !yaHizoDano) { 
                float miCentroX = geom.getLocalTranslation().x + (size / 2);
                float jugadorCentroX = jugador.getGeom().getLocalTranslation().x + 225f;
                float dist = Math.abs(miCentroX - jugadorCentroX);
                
                // Aplica la reduccion de vida si el jugador se encuentra dentro del rango del impacto
                if (dist <= 280f) {
                    jugador.recibirDano(1); 
                    if (gui != null) {
                        gui.actualizarBarraVida(jugador.getVidaActual());
                    }
                    yaHizoDano = true;
                }
            }
        } else {
            // Selecciona la animacion de movimiento o inactividad si no esta atacando
            if (direccionX != 0) cambiarEstado("WALK", 12); 
            else cambiarEstado("IDLE", 10); 
        }

        // Avanza la animacion al siguiente fotograma segun el tiempo transcurrido
        tiempoFrame += tpf;
        if (tiempoFrame >= velocidadAnimacion) {
            tiempoFrame = 0;
            columnaActual = (columnaActual + 1) % totalFrames;
            actualizarUV(columnaActual, totalFrames, mirandoIzquierda);
        }
    }

    // Modifica la textura activa y ajusta la cantidad de fotogramas segun la accion que se va a realizar
    private void cambiarEstado(String nuevoEstado, int frames) {
        if (this.estadoActual.equals(nuevoEstado)) return;
        this.estadoActual = nuevoEstado;
        this.totalFrames = frames;
        this.columnaActual = 0;
        switch (nuevoEstado) {
            case "IDLE":   mat.setTexture("ColorMap", texIdle);   break;
            case "WALK":   mat.setTexture("ColorMap", texWalk);   break;
            case "ATTACK": mat.setTexture("ColorMap", texAttack); break;
        }
    }

    // Recorta la textura original para mostrar solo el fotograma actual y lo voltea segun la direccion
    private void actualizarUV(int frame, int total, boolean mirandoIzquierda) {
        float tamanoFrameX = 1.0f / (float)total;
        float xStart = (float)frame * tamanoFrameX;
        float xEnd = (float)(frame + 1) * tamanoFrameX;
        float padding = 0.005f;
        xStart += padding; xEnd -= padding;

        float[] texCoords;
        if (mirandoIzquierda) {
            texCoords = new float[]{ xEnd, 0f, xStart, 0f, xStart, 1f, xEnd, 1f };
        } else {
            texCoords = new float[]{ xStart, 0f, xEnd, 0f, xEnd, 1f, xStart, 1f };
        }
        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    // Reduce la vida del enemigo al recibir un golpe y solicita la actualizacion de la interfaz grafica
    public void recibirDano(int cantidad, GestorGUI gui) {
        if (vidaActual <= 0) return;
        this.vidaActual -= cantidad;
        
        if (gui != null) {
            gui.actualizarBarraVidaEnemigo(vidaActual);
        }
    
        if (vidaActual <= 0) {
            vidaActual = 0;
        }
    }
    
    // Metodos de utilidad para consultar el estado actual, mostrar, ocultar o eliminar al enemigo del mapa
    public int getVidaActual() { return vidaActual; }
    public void activar() { geom.setCullHint(CullHint.Never); }
    public void desactivar() { geom.setCullHint(CullHint.Always); }
    public Geometry getGeom() { return geom; }
    public void destruir(Node rootNode) { if (geom != null) rootNode.detachChild(geom); }
}