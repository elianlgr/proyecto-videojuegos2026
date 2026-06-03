package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import java.util.ArrayList;

// Clase responsable de la generacion y distribucion de los elementos decorativos del mapa
public class GestorEntorno {
    
    // Dependencias del motor grafico para cargar recursos y anadirlos a la escena
    private AssetManager assetManager;
    private Node rootNode;
    
    // Referencia a la lista global donde se almacenara la vegetacion creada
    private ArrayList<Arbusto> listaArbustos;

    // Inicializa el gestor vinculando las herramientas principales del juego y la lista de objetos
    public GestorEntorno(AssetManager assetManager, Node rootNode, ArrayList<Arbusto> listaArbustos) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.listaArbustos = listaArbustos;
    }

    // Genera un bloque rectangular de arbustos posicionados matematicamente pero con variacion visual
    public void crearCuadriculaAleatoria(float inicioX, float inicioY, int columnas, int filas) {
        
        // Rutas de los recursos graficos disponibles para la vegetacion
        String[] texturas = {
            "Textures/arbustoAzul.png",
            "Textures/arbustoRojo.png",
            "Textures/arbustoVioleta.png"
        };

        // Distancia en unidades que habra entre cada elemento generado
        float separacionX = 90f; 
        float separacionY = 70f; 

        // Recorre las dimensiones indicadas para poblar la cuadricula iteracion por iteracion
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                
                // Calcula las coordenadas bidimensionales de la celda actual
                float posX = inicioX + (col * separacionX);
                float posY = inicioY + (fila * separacionY);

                // Selecciona un numero aleatorio para escoger un color distinto para cada instancia
                int indiceTextura = (int) (Math.random() * texturas.length);

                // Instancia el nuevo objeto grafico en la escena y lo registra en la lista global de interacciones
                Arbusto nuevoArbusto = new Arbusto(assetManager, rootNode, posX, posY, texturas[indiceTextura]);
                listaArbustos.add(nuevoArbusto);
            }
        }
    }
}