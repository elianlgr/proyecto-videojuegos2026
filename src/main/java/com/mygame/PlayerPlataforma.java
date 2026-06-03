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

// Representa al personaje del jugador en el escenario de plataformas con vista lateral
// Gestiona sus fisicas de salto, animaciones complejas, ataques y estados de salud
public class PlayerPlataforma {
    
    // Componentes graficos y parametros de dimensiones y velocidad base
    private Geometry geom;
    private Quad quad;
    private float speed = 200f; 
    private float size = 450f; 
    
    // Variables para el control de la gravedad, fuerza de saltos y colision con el piso
    private float velocidadY = 0f;          
    private final float GRAVEDAD = -700f;
    private final float FUERZA_SALTO = 350f; 
    private float sueloY = -75f;          
    private boolean enElSuelo = false;       
    private float screenWidth;
    
    // Atributos de vitalidad del personaje
    private int vidaMaxima = 5;
    private int vidaActual = 5;

    // Dimensiones de la textura maestra que contiene la grilla de sprites
    private float columnasTotales = 10f; 
    private float filasTotales = 8f;

    // Control de tiempos, orientacion y rastreo de la animacion
    private float tiempoFrame = 0;
    private float velocidadAnimacion = 0.06f; 
    private int columnaActual = 0; 
    private int filaActual = 0; 
    private boolean mirandoIzquierda = false;
    private boolean anteriorMirandoIzquierda = false;
    
    // Identificador para sincronizar la logica del personaje con lo que se dibuja
    private int estadoActual = 0;
    
    // Indicador del estado ofensivo
    private boolean atacando = false;
    private boolean recibiendoGolpe = false;
    private float tiempoGolpe = 0f;
    private Material mat;

    // Inicializa la forma geometrica, carga la textura principal y configura su transparencia
    public PlayerPlataforma(AssetManager assetManager, Node rootNode, float screenWidth) {
        this.screenWidth = screenWidth;
        quad = new Quad(size, size);
        geom = new Geometry("PlayerPlataformaNode", quad);
        geom.setLocalTranslation(100, sueloY, 6); 

        
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/PlayerPlataforma.png"); 
        mat.setTexture("ColorMap", tex);
        mat.setColor("Color", com.jme3.math.ColorRGBA.White);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);

        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent);
        
        actualizarFrame(0, 0, false);
        desactivar();
        rootNode.attachChild(geom);
    }

    // Procesa las entradas de movimiento, aplica gravedad, actualiza la posicion y calcula la animacion
    public void actualizarFisicas(float direccionX, boolean quiereSaltar, float tpf) {

        if (recibiendoGolpe) {
    tiempoGolpe -= tpf;

    if (tiempoGolpe <= 0) {
        recibiendoGolpe = false;
        mat.setColor("Color", com.jme3.math.ColorRGBA.White);
    }
        }
        
        // Detiene el movimiento horizontal si el personaje esta ejecutando un ataque
        if (atacando) {
            direccionX = 0; 
        }

        // Determina la direccion a la que mira el modelo para voltearlo graficamente si es necesario
        anteriorMirandoIzquierda = mirandoIzquierda;
        if (direccionX < 0) mirandoIzquierda = true;
        else if (direccionX > 0) mirandoIzquierda = false;

        Vector3f pos = geom.getLocalTranslation();
        float nuevoX = pos.x + (direccionX * speed * tpf);

        // Limita el desplazamiento a los bordes de la pantalla visible
        if (nuevoX < 0) nuevoX = 0;
        if (nuevoX > screenWidth - size) nuevoX = screenWidth - size;

        // Aplica caida libre si el personaje no se encuentra parado sobre el suelo
        if (!enElSuelo) velocidadY += GRAVEDAD * tpf; 

        // Aplica un impulso vertical hacia arriba si se recibe el comando y las condiciones lo permiten
        if (quiereSaltar && enElSuelo && !atacando) {
            velocidadY = FUERZA_SALTO; 
            enElSuelo = false;         
        }

        float nuevoY = pos.y + (velocidadY * tpf);
        
        // Fija al personaje en la posicion base al colisionar con el nivel del piso
        if (nuevoY <= sueloY) {
            nuevoY = sueloY;
            velocidadY = 0;
            enElSuelo = true;
        }

        geom.setLocalTranslation(nuevoX, nuevoY, pos.z);

        // Maquina de estados que define que fila de animacion debe reproducirse segun la prioridad
        int nuevoEstado = 0;
        
        if (atacando) {
            nuevoEstado = filaActual; 
        } else if (!enElSuelo) {
            nuevoEstado = 4; 
        } else if (direccionX != 0) {
            nuevoEstado = 2; 
        } else {
            nuevoEstado = 0; 
        }

        boolean cambioEstado = (nuevoEstado != estadoActual);
        boolean cambioDireccion = (mirandoIzquierda != anteriorMirandoIzquierda);
        
        // Revisa si ocurrio una transicion de accion o giro para reiniciar el ciclo visual
        if (cambioEstado || cambioDireccion) {
            estadoActual = nuevoEstado;
            filaActual = nuevoEstado;
            
            if (cambioEstado) {
                columnaActual = 0;
                tiempoFrame = 0;
            }
            actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
        }

        // Controla la duracion de cada dibujo, acelerando los tiempos durante los ataques
        tiempoFrame += tpf;
        float limiteTiempo = atacando ? 0.08f : velocidadAnimacion;

        if (tiempoFrame >= limiteTiempo) {
            tiempoFrame = 0;
            columnaActual++;
            
            // Obtiene cuantos fotogramas componen la animacion actual para saber cuando terminar
            int limiteColumnas = obtenerMaximoColumnas(filaActual);
            
            if (columnaActual > limiteColumnas) {
                if (atacando) {
                    atacando = false;
                    estadoActual = -1; 
                } else if (filaActual == 4) {
                    // Congela el sprite en el ultimo cuadro si se encuentra cayendo
                    columnaActual = limiteColumnas; 
                } else {
                    // Reinicia la secuencia al inicio para movimientos continuos como correr
                    columnaActual = 0; 
                }
                actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
            } else {
                actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
            }
        }
    }

    // Devuelve la cantidad de dibujos exactos que posee cada secuencia particular en la hoja de texturas
    private int obtenerMaximoColumnas(int fila) {
        switch(fila) {
            case 0: return 0; 
            case 1: return 9; 
            case 2: return 9; 
            case 3: return 7; 
            case 4: return 4; 
            case 5: return 2; 
            case 6: return 2; 
            case 7: return 3; 
            default: return 0;
        }
    }

    // Inicia la accion ofensiva fijando el estado especifico de la espada
    public void atacar() {
        if (!atacando && enElSuelo) {
            atacando = true;
            columnaActual = 0; 
            tiempoFrame = 0;
            
            filaActual = 6; 
            
            estadoActual = filaActual; 
            
            actualizarFrame(columnaActual, filaActual, mirandoIzquierda);
        }
    }

    // Calcula y aplica las coordenadas UV al material para mostrar solo el cuadro activo
    private void actualizarFrame(int columna, int fila, boolean izquierda) {
        int filaInvertida = (int)(filasTotales - 1) - fila; 
        
        float tamanoFrameX = 1.0f / columnasTotales; 
        float tamanoFrameY = 1.0f / filasTotales; 
        
        float xStart = columna * tamanoFrameX;
        float xEnd = xStart + tamanoFrameX;
        
        float yStart = filaInvertida * tamanoFrameY;
        float yEnd = yStart + tamanoFrameY;

        float[] texCoords;

        // Configura el mapeo de forma normal o en espejo horizontal dependiendo hacia donde este mirando
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
    
    // Metodos de utilidad para la gestion de reduccion de salud
    public void recibirDano(int cantidad) {
    vidaActual -= cantidad;

    if (vidaActual < 0) {
        vidaActual = 0;
    }

    recibiendoGolpe = true;
    tiempoGolpe = 0.15f;

    mat.setColor("Color", com.jme3.math.ColorRGBA.Red);
    }  // se modificó para de esta forma darle más realismo al golpe
    
    // Retorna los puntos de salud restantes del personaje
    public int getVidaActual() { 
        return vidaActual; 
    }
    
    // Restaura las estadisticas y posiciona de nuevo al modelo para nuevas partidas
    public void resetVidaYPosicion() { 
        vidaActual = vidaMaxima; 
        velocidadY = 0f; 
        enElSuelo = false; 
        geom.setLocalTranslation(100, sueloY, 6); 
    }
    
    // Muestra el objeto geometrico en pantalla
    public void activar() { 
        geom.setCullHint(CullHint.Never); 
    }
    
    // Oculta el objeto geometrico
    public void desactivar() { 
        geom.setCullHint(CullHint.Always); 
    }
    
    // Proporciona acceso al nodo principal para gestion externa de jerarquias o colisiones
    public Geometry getGeom() { 
        return geom; 
    }
}