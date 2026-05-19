package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import java.util.ArrayList;

public class GestorEntorno {
    
    private AssetManager assetManager;
    private Node rootNode;
    private ArrayList<Arbusto> listaArbustos;

    // el constructor que recibe las herramientas desde tu Main
    public GestorEntorno(AssetManager assetManager, Node rootNode, ArrayList<Arbusto> listaArbustos) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.listaArbustos = listaArbustos;
    }

    // metodo para crear una cuadrícula ordenada pero con colores al azar
    public void crearCuadriculaAleatoria(float inicioX, float inicioY, int columnas, int filas) {
        // lista con tus 3 tipos de arbustos
        String[] texturas = {
            "Textures/arbustoAzul.png",
            "Textures/arbustoRojo.png",
            "Textures/arbustoVioleta.png"
        };

        // separacion entre cada arbusto (puedes ajustar estos números si los quieres más juntos o separados)
        float separacionX = 90f; 
        float separacionY = 70f; 

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                // calculamos la posicion exacta en la rejilla
                float posX = inicioX + (col * separacionX);
                float posY = inicioY + (fila * separacionY);

                // elegimos un color al azar de la lista
                int indiceTextura = (int) (Math.random() * texturas.length);

                // creamos el arbusto y lo metemos a la lista para que se mueva con el viento
                Arbusto nuevoArbusto = new Arbusto(assetManager, rootNode, posX, posY, texturas[indiceTextura]);
                listaArbustos.add(nuevoArbusto);
            }
        }
    }
}