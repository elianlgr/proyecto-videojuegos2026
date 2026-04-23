package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;

public class Player {
    private Geometry geom;
    private float speed = 400f; 
    private float size = 60f; 

    public Player(AssetManager assetManager, Node rootNode, float screenWidth, float screenHeight) {
        Quad quad = new Quad(size, size);
        geom = new Geometry("PlayerNode", quad);
        
        // calculo del centro exacto
        float centerX = (screenWidth / 2) - (size / 2);
        float centerY = (screenHeight / 2) - (size / 2);
        
        // Z = 5 para estar muy por delante del fondo
        geom.setLocalTranslation(centerX, centerY, 5);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red); 
        geom.setMaterial(mat);

        // forzamos la visibilidad
        geom.setCullHint(CullHint.Never);
        
        rootNode.attachChild(geom);
    }

    public void move(float dx, float dy, float tpf) {
        Vector3f pos = geom.getLocalTranslation();
        geom.setLocalTranslation(pos.x + (dx * speed * tpf), pos.y + (dy * speed * tpf), pos.z);
    }
}