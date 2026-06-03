package com.mygame;

import java.util.ArrayList;

// Clase encargada de supervisar la relacion de cercania entre el jugador y los elementos del entorno
public class GestorInteracciones {
    
    // Referencias a los objetos del juego que seran evaluados
    private Player player;
    private ArrayList<Arbusto> listaArbustos;
    
    // Contadores para medir cuanto tiempo pasa el jugador interactuando
    private float tiempoQuieto = 0;
    private float tiempoMoviendo = 0;

    // Inicializa el gestor guardando la referencia del personaje y la lista de la vegetacion
    public GestorInteracciones(Player player, ArrayList<Arbusto> listaArbustos) {
        this.player = player;
        this.listaArbustos = listaArbustos;
    }

    // Metodo principal que evalua colisiones y tiempos de interaccion fotograma a fotograma
    public boolean vigilar(float tpf) {
        // Variable temporal para saber si se detecto una interseccion en este ciclo
        boolean tocandoArbusto = false;

        // Extrae las coordenadas actuales del jugador en los ejes X e Y
        float pX = player.getGeom().getLocalTranslation().x;
        float pY = player.getGeom().getLocalTranslation().y;

        // Recorre cada elemento de la lista para calcular su distancia respecto al personaje
        for (Arbusto arbusto : listaArbustos) {
            // Obtiene las coordenadas individuales del objeto actual
            float aX = arbusto.getNodo().getLocalTranslation().x;
            float aY = arbusto.getNodo().getLocalTranslation().y;

            // Calcula la separacion absoluta en ambos ejes
            float distanciaX = Math.abs(pX - aX);
            float distanciaY = Math.abs(pY - aY);

            // Define una caja de colision de 50 unidades, si la distancia es menor, hay contacto
            if (distanciaX < 50f && distanciaY < 50f) {
                tocandoArbusto = true;
                // Rompe el ciclo ya que basta con tocar un solo elemento para proceder
                break;
            }
        }

        // Logica de acumulacion de tiempo dependiendo del estado de movimiento del jugador
        if (tocandoArbusto) {
            if (player.isCaminando()) {
                // Acumula tiempo de movimiento y resetea el contador de inactividad
                tiempoMoviendo += tpf;
                tiempoQuieto = 0; 
                
                // Si camina continuamente por 4 segundos, dispara el evento retornando verdadero
                if (tiempoMoviendo >= 4.0f) {
                    tiempoMoviendo = 0; 
                    return true; 
                }
            } else {
                // Acumula tiempo estatico y resetea el contador de movimiento
                tiempoQuieto += tpf;
                tiempoMoviendo = 0; 
                
                // Si permanece inactivo por 2 segundos, dispara el evento retornando verdadero
                if (tiempoQuieto >= 2.0f) {
                    tiempoQuieto = 0;
                    return true; 
                }
            }
        } else {
            // Si no hay contacto, se reinician todos los contadores de tiempo
            tiempoQuieto = 0;
            tiempoMoviendo = 0;
        }
        
        // Retorna falso si aun no se ha cumplido ninguna condicion de tiempo
        return false; 
    }
}