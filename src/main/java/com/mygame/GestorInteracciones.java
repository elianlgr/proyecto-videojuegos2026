package com.mygame;

import java.util.ArrayList;

public class GestorInteracciones {
    
    private Player player;
    private ArrayList<Arbusto> listaArbustos;
    
    private float tiempoQuieto = 0;
    private float tiempoMoviendo = 0;

    // el constructor recibe a los "actores" que va a vigilar
    public GestorInteracciones(Player player, ArrayList<Arbusto> listaArbustos) {
        this.player = player;
        this.listaArbustos = listaArbustos;
    }

    // este es el metodo que llamaremos desde el main
    public boolean vigilar(float tpf) {
        boolean tocandoArbusto = false;

        float pX = player.getGeom().getLocalTranslation().x;
        float pY = player.getGeom().getLocalTranslation().y;

        for (Arbusto arbusto : listaArbustos) {
            float aX = arbusto.getNodo().getLocalTranslation().x;
            float aY = arbusto.getNodo().getLocalTranslation().y;

            float distanciaX = Math.abs(pX - aX);
            float distanciaY = Math.abs(pY - aY);

            if (distanciaX < 50f && distanciaY < 50f) {
                tocandoArbusto = true;
                break;
            }
        }

        if (tocandoArbusto) {
            if (player.isCaminando()) {
                tiempoMoviendo += tpf;
                tiempoQuieto = 0; 
                
                if (tiempoMoviendo >= 4.0f) {
                    tiempoMoviendo = 0; 
                    return true; 
                }
            } else {
                tiempoQuieto += tpf;
                tiempoMoviendo = 0; 
                
                if (tiempoQuieto >= 2.0f) {
                    tiempoQuieto = 0;
                    return true; 
                }
            }
        } else {
            tiempoQuieto = 0;
            tiempoMoviendo = 0;
        }
        
        return false; 
    }
}