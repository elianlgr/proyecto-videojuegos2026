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

public class Arbusto {
    private Node nodoArbusto;
    private float tiempo = 0;
    // que tan rapido se mece
    private float velocidadViento = 2.0f;
    // que tanto se dobla (en radianes)
    private float fuerzaViento = 0.08f;   

    public Arbusto(AssetManager assetManager, Node rootNode, float x, float y, String texturePath) {
        // usamos un Node para poder agrupar y rotar la imagen desde la base, no desde una esquina
        nodoArbusto = new Node("NodoArbusto");

        // asumimos un tamaño para el arbusto en pantalla
        float width = 100f;
        float height = 80f;
        Quad quad = new Quad(width, height);
        Geometry geom = new Geometry("ArbustoGeo", quad);

        // movemos la geometria un poco a la izquierda para que el "centro de rotacion" quede en la base
        geom.setLocalTranslation(-width / 2, 0, 0);

        // cargamos la textura y material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture(texturePath);
        mat.setTexture("ColorMap", tex);
        
        // activa la transparencia del archivo PNG
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setCullHint(CullHint.Never);
        geom.setQueueBucket(Bucket.Transparent); 
        geom.setCullHint(CullHint.Never);
        nodoArbusto.setCullHint(CullHint.Never);

        // armamos el arbusto
        nodoArbusto.attachChild(geom);
        
        // Z = 2 para que esté por delante del mapa (Z=-1) pero detras del jugador (Z=5)
        nodoArbusto.setLocalTranslation(x, y, 2);
        
        // lo agregamos al escenario principal
        rootNode.attachChild(nodoArbusto);
    }

    // este metodo lo llamaremos constantemente desde el Main para simular el viento
    public void mecerConViento(float tpf) {
        tiempo += tpf * velocidadViento;
        
        // calculamos el angulo usando la curva del Seno
        float angulo = FastMath.sin(tiempo) * fuerzaViento;
        
        // aplicamos la rotacion solo en el eje Z (para que gire en 2D)
        Quaternion rotacion = new Quaternion();
        rotacion.fromAngleAxis(angulo, Vector3f.UNIT_Z);
        nodoArbusto.setLocalRotation(rotacion);
    }
    
    // nos permite tener el nodo para calcular colisiones 
    public Node getNodo() {
        return nodoArbusto;
    }
}