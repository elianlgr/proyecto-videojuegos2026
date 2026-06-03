package com.mygame;

// Clase encargada de evaluar las distancias y aplicar la logica de combate entre entidades
public class GestorCombate {
    
    // Referencias a los combatientes y al sistema que dibuja la informacion en pantalla
    private PlayerPlataforma player;
    private EnemigoCPU enemigo;
    private GestorGUI gui;

    // Constructor que establece los enlaces necesarios para interactuar con la vida de los personajes
    public GestorCombate(PlayerPlataforma player, EnemigoCPU enemigo, GestorGUI gui) {
        this.player = player;
        this.enemigo = enemigo;
        this.gui = gui;
    }

    // Evalua si el ataque emitido por el jugador logra impactar en el oponente
    public void verificarAtaqueJugador(boolean atacando) {
        // Detiene la ejecucion si no se ha registrado ningun intento de ataque
        if (!atacando) return;
        
        // Previene que se sigan procesando calculos sobre un enemigo que ya no tiene salud
        if (enemigo.getVidaActual() <= 0) return;

        // Mide la separacion horizontal absoluta entre el modelo del jugador y el del enemigo
        float dist = Math.abs(player.getGeom().getLocalTranslation().x - enemigo.getGeom().getLocalTranslation().x);
        
        // Verifica si la distancia es lo suficientemente corta como para considerarse un golpe exitoso
        if (dist < 200f) {
            // Resta puntos de salud al oponente y actualiza el estado de su barra en la interfaz
            enemigo.recibirDano(1, this.gui);
        }
    }
}