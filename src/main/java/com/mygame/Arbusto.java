package com.mygame;

import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

// Clase que representa un elemento decorativo de la vegetacion con animacion de balanceo
public class Arbusto {
    
    // Nodo principal que agrupa la geometria para permitir rotaciones desde un punto de pivote especifico
    private Node nodoArbusto;
    
    // Acumulador de tiempo para calcular el ciclo de la animacion del viento
    private float tiempo = 0;
    
    // Multiplicador que define la velocidad del ciclo de balanceo
    private float velocidadViento = 2.0f;
    
    // Amplitud maxima del doblez provocado por el viento expresada en radianes
    private float fuerzaViento = 0.08f;   

    // Inicializa el objeto, configura su geometria y lo ubica en las coordenadas indicadas dentro de la escena
    public Arbusto(AssetManager assetManager, Node rootNode, float x, float y, String texturePath) {
        
        // Crea un nodo contenedor para manipular transformaciones complejas como la rotacion desde la base
        nodoArbusto = new Node("NodoArbusto");

        // Define las dimensiones fisicas que tendra la imagen bidimensional en el mundo del juego
        float width = 100f;
        float height = 80f;
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry("ArbustoGeo", quad);

        // Desplaza la geometria graficamente hacia la izquierda para que el eje de rotacion quede en el centro de la base
        geom.setLocalTranslation(-width / 2, 0, 0);

        // Genera el material base sin sombreado y aplica la textura recibida por parametro
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture(texturePath);
        mat.setTexture("ColorMap", tex);
        
        // Configura el renderizado para que reconozca el canal alfa y dibuje correctamente las transparencias
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);
        
        // Configura los parametros de visibilidad continua y los asigna a la cola de dibujado correspondiente
        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent); 
        geom.setCullHint(CullHint.Never);
        nodoArbusto.setCullHint(CullHint.Never);

        // Vincula la imagen ya texturizada al nodo contenedor principal
        nodoArbusto.attachChild(geom);
        
        // Posiciona el arbusto en el espacio asignando una profundidad intermedia para organizar las capas visuales
        nodoArbusto.setLocalTranslation(x, y, 2);
        
        // Integra el nodo finalizado al arbol de la escena para que se muestre en pantalla
        rootNode.attachChild(nodoArbusto);
    }

    // Modifica dinamicamente la rotacion del nodo basandose en el tiempo para crear una ilusion de movimiento organico
    public void mecerConViento(float tpf) {
        // Avanza el tiempo virtual de la animacion de forma proporcional a la velocidad definida
        tiempo += tpf * velocidadViento;
        
        // Genera un movimiento oscilatorio suave en ambas direcciones utilizando una funcion senoidal
        float angulo = FastMath.sin(tiempo) * fuerzaViento;
        
        // Convierte el angulo calculado a una rotacion aplicable unicamente sobre el eje de profundidad Z
        Quaternion rotacion = new Quaternion();
        rotacion.fromAngleAxis(angulo, Vector3f.UNIT_Z);
        nodoArbusto.setLocalRotation(rotacion);
    }
    
    // Proporciona acceso al nodo estructural para permitir la evaluacion de distancias y colisiones
    public Node getNodo() {
        return nodoArbusto;
    }
}