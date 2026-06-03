package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;

// Clase encargada de administrar y dibujar todos los elementos de la interfaz de usuario en pantalla
public class GestorGUI {
    
    // Referencias a las herramientas del motor grafico y la escena de interfaz
    private AssetManager assetManager;
    private Node guiNode;
    private Camera cam;
    private BitmapFont guiFont;
    private Picture youWinPic;
    
    // Elementos visuales para la pantalla de carga
    private Geometry fondoNegro;
    private BitmapText textoCarga;
    
    // Elementos graficos que componen la barra de salud del jugador
    private Geometry barraVidaGeo;
    private Quad quadBarra;
    
    // Elementos graficos que componen la barra de salud del oponente
    private Geometry barraVidaEnemigoGeo;
    private Quad quadBarraEnemigo;
    
    // Elemento grafico para la pantalla de derrota
    private Geometry gameOverGeo;

    // Constructor que enlaza las herramientas necesarias para construir elementos graficos en 2D
    public GestorGUI(AssetManager assetManager, Node guiNode, Camera cam, BitmapFont guiFont) {
        this.assetManager = assetManager;
        this.guiNode = guiNode;
        this.cam = cam;
        this.guiFont = guiFont;
    }

    // Crea y muestra una pantalla negra con texto de espera durante las transiciones de nivel
    public void mostrarPantallaCarga() {
        if (fondoNegro == null) {
            Quad fondoQuad = new Quad(cam.getWidth(), cam.getHeight());
            fondoNegro = new Geometry("FondoNegro", fondoQuad);
            Material matNegro = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            matNegro.setColor("Color", ColorRGBA.Black);
            fondoNegro.setMaterial(matNegro);
            
            textoCarga = new BitmapText(guiFont, false);
            textoCarga.setSize(guiFont.getCharSet().getRenderedSize() * 2);
            textoCarga.setColor(ColorRGBA.White);
            textoCarga.setText("Cargando...");
            
            float x = (cam.getWidth() - textoCarga.getLineWidth()) / 2;
            float y = (cam.getHeight() + textoCarga.getLineHeight()) / 2;
            textoCarga.setLocalTranslation(x, y, 0);
        }
        guiNode.attachChild(fondoNegro);
        guiNode.attachChild(textoCarga);
    }
    
    // Retira la pantalla de carga del nodo principal de la interfaz
    public void ocultarPantallaCarga() {
        if (fondoNegro != null && guiNode.hasChild(fondoNegro)) {
            guiNode.detachChild(fondoNegro);
            guiNode.detachChild(textoCarga);
        }
    }

    // Construye y proyecta la imagen de derrota ocupando toda la pantalla
    public void mostrarGameOver() {
        if (gameOverGeo == null) {
            Quad quad = new Quad(cam.getWidth(), cam.getHeight());
            gameOverGeo = new Geometry("GameOverPantalla", quad);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture tex = assetManager.loadTexture("Textures/GameOver.png");
            mat.setTexture("ColorMap", tex);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            gameOverGeo.setMaterial(mat);
        }
        guiNode.attachChild(gameOverGeo);
    }

    // Elimina la pantalla de derrota del area visible
    public void ocultarGameOver() {
        if (gameOverGeo != null && guiNode.hasChild(gameOverGeo)) {
            guiNode.detachChild(gameOverGeo);
        }
    }

    // Configura la geometria, textura y posicion inicial de la barra de salud del jugador
    public void iniciarBarraVida() {
        float width = 180f;
        float height = 45f;
        
        quadBarra = new Quad(width, height);
        barraVidaGeo = new Geometry("BarraVida", quadBarra);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/barra_vida.png");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaGeo.setMaterial(mat);
        
        // Coloca la barra de salud en la esquina superior izquierda
        barraVidaGeo.setLocalTranslation(20, cam.getHeight() - height - 20, 0);
        barraVidaGeo.setCullHint(CullHint.Always);
        guiNode.attachChild(barraVidaGeo);
        actualizarBarraVida(5); 
    }

    // Configura la barra de salud del enemigo y prepara la imagen oculta de victoria
    public void iniciarBarraVidaEnemigoConTextura() {
        float width = 180f;
        float height = 45f;
        
        quadBarraEnemigo = new Quad(width, height);
        barraVidaEnemigoGeo = new Geometry("BarraVidaEnemigo", quadBarraEnemigo);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/barra_vida.png");
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        barraVidaEnemigoGeo.setMaterial(mat);
        
        // Coloca la barra enemiga en la esquina superior derecha
        barraVidaEnemigoGeo.setLocalTranslation(cam.getWidth() - width - 20, cam.getHeight() - height - 20, 0);
        barraVidaEnemigoGeo.setCullHint(CullHint.Always);
        guiNode.attachChild(barraVidaEnemigoGeo);
        actualizarBarraVidaEnemigo(5); 

        // Crea y posiciona en el centro la imagen de fin del juego por victoria
        float anchoPantalla = cam.getWidth();
        float altoPantalla = cam.getHeight();
        youWinPic = new Picture("YouWin");
        youWinPic.setImage(assetManager, "Textures/YouWin.png", true);
        youWinPic.setWidth(800);
        youWinPic.setHeight(400);
        youWinPic.setLocalTranslation((anchoPantalla - 800) / 2, (altoPantalla - 400) / 2, 0);
        youWinPic.setCullHint(CullHint.Always);
        guiNode.attachChild(youWinPic);
    }

    // Hace visibles las barras de salud de ambos personajes durante los combates
    public void mostrarBarraVida() {
        if (barraVidaGeo != null) barraVidaGeo.setCullHint(CullHint.Never);
        if (barraVidaEnemigoGeo != null) barraVidaEnemigoGeo.setCullHint(CullHint.Never);
    }
    
    // Esconde las barras de salud cuando el jugador esta explorando el mapa normal
    public void ocultarBarraVida() {
        if (barraVidaGeo != null) barraVidaGeo.setCullHint(CullHint.Always);
        if (barraVidaEnemigoGeo != null) barraVidaEnemigoGeo.setCullHint(CullHint.Always);
    }

    // Modifica las coordenadas de la textura de la barra del jugador para reflejar el nivel de daño
    public void actualizarBarraVida(int vidaActual) {
        if (vidaActual < 0) vidaActual = 0;
        if (vidaActual > 5) vidaActual = 5;

        float tamanoFrameY = 1.0f / 6.0f; 
        float yStart = vidaActual * tamanoFrameY;
        float yEnd = yStart + tamanoFrameY;

        float[] texCoords = new float[]{ 0, yStart, 1, yStart, 1, yEnd, 0, yEnd };

        quadBarra.clearBuffer(VertexBuffer.Type.TexCoord);
        quadBarra.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    // Modifica las coordenadas de la textura de la barra del enemigo simulando su desgaste
    public void actualizarBarraVidaEnemigo(int vidaActual) {
        if (vidaActual < 0) vidaActual = 0;
        if (vidaActual > 5) vidaActual = 5;

        float tamanoFrameY = 1.0f / 6.0f; 
        float yStart = vidaActual * tamanoFrameY;
        float yEnd = yStart + tamanoFrameY;

        float[] texCoords = new float[]{ 0, yStart, 1, yStart, 1, yEnd, 0, yEnd };

        quadBarraEnemigo.clearBuffer(VertexBuffer.Type.TexCoord);
        quadBarraEnemigo.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }
    
    // Muestra en pantalla el cartel indicando que el jugador ha ganado el combate
    public void mostrarYouWin() {
        if (youWinPic != null) youWinPic.setCullHint(CullHint.Never);
    }

    // Oculta el cartel de victoria para limpiar la pantalla de cara a una nueva partida
    public void ocultarYouWin() {
        if (youWinPic != null) youWinPic.setCullHint(CullHint.Always);
    }
}