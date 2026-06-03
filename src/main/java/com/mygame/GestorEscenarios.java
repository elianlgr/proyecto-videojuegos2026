    package com.mygame;

    import com.jme3.asset.AssetManager;
    import com.jme3.material.Material;
    import com.jme3.scene.Geometry;
    import com.jme3.scene.Node;
    import com.jme3.scene.shape.Quad;
    import com.jme3.scene.Spatial.CullHint;
    import com.jme3.texture.Texture;
    import java.util.ArrayList;

    // Clase que administra la carga y transicion entre los diferentes mapas o niveles del juego
    public class GestorEscenarios {

        // Referencias principales al motor para cargar recursos y anadir objetos a la escena
        private AssetManager assetManager;
        private Node rootNode;

        // Geometrias bidimensionales que renderizan las imagenes de fondo de cada nivel
        private Geometry mapGeo1;
        private Geometry mapGeo2;

        // Referencias a las entidades interactivas y gestores necesarios para sincronizar los cambios de estado
        private Player playerTopDown;
        private PlayerPlataforma playerLateral;
        private ArrayList<Arbusto> listaArbustos;
        private GestorGUI gestorGUI;

        // Constructor que recibe y almacena todas las instancias que seran afectadas por los cambios de mapa
        public GestorEscenarios(AssetManager assetManager, Node rootNode, Player p1, PlayerPlataforma p2, ArrayList<Arbusto> arbustos, GestorGUI gui) {
            this.assetManager = assetManager;
            this.rootNode = rootNode;
            this.playerTopDown = p1;
            this.playerLateral = p2;
            this.listaArbustos = arbustos;
            this.gestorGUI = gui; 
        }

        // Genera las superficies y aplica las texturas de fondo dejandolas listas en la jerarquia principal
        public void cargarFondos(float width, float height) {
            // Crea un rectangulo base con las dimensiones totales de la pantalla
            Quad mapQuad = new Quad(width, height); 

            // Configura la geometria y textura del primer nivel de exploracion
            mapGeo1 = new Geometry("MapaMundo1", mapQuad);
            mapGeo1.setLocalTranslation(0, 0, -1); 
            Material matMap1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture mapTexture1 = assetManager.loadTexture("Textures/escenario1.png");
            matMap1.setTexture("ColorMap", mapTexture1);
            mapGeo1.setMaterial(matMap1);
            rootNode.attachChild(mapGeo1);

            // Configura la geometria y textura del segundo nivel orientado al combate de plataformas
            mapGeo2 = new Geometry("MapaMundo2", mapQuad);
            mapGeo2.setLocalTranslation(0, 0, -1); 
            Material matMap2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            Texture mapTexture2 = assetManager.loadTexture("Textures/escenario2.jpg");
            matMap2.setTexture("ColorMap", mapTexture2);
            mapGeo2.setMaterial(matMap2);
            rootNode.attachChild(mapGeo2);
        }

        // Establece la configuracion inicial al iniciar el juego mostrando solo el entorno de vista superior
        public void iniciarEscenario1() {
            // Hace visible el fondo del mundo abierto y oculta el entorno de batalla
            mapGeo1.setCullHint(CullHint.Never);             
            mapGeo2.setCullHint(CullHint.Always);            

            // Muestra al jugador con perspectiva aerea y desactiva el modelo de plataformas
            playerTopDown.getGeom().setCullHint(CullHint.Never); 
            playerLateral.desactivar();                      

            // Asegura que toda la vegetacion interactiva sea visible
            for (Arbusto arbusto : listaArbustos) {
                arbusto.getNodo().setCullHint(CullHint.Never);
            }

            // Oculta la interfaz de salud ya que no hay combates en esta zona
            if (gestorGUI != null) {
                gestorGUI.ocultarBarraVida();
            }
        }

        // Realiza el cambio visual y logico ocultando la zona de exploracion y revelando el entorno de batalla
        public void cambiarAEscenario2() {
            // Vuelve invisibles el mapa, el jugador y los elementos interactivos del primer escenario
            mapGeo1.setCullHint(CullHint.Always);
            playerTopDown.getGeom().setCullHint(CullHint.Always);
            for (Arbusto arbusto : listaArbustos) {
                arbusto.getNodo().setCullHint(CullHint.Always);
            }

            // Muestra el fondo del segundo escenario y el personaje con fisicas de salto
            mapGeo2.setCullHint(CullHint.Never);
            playerLateral.activar();

            // Activa los elementos de la interfaz de usuario dedicados a la pelea
            gestorGUI.mostrarBarraVida(); 

            // Confirmacion por consola del exito de la transicion
            System.out.println("Transicion al Escenario 2 gestionada exitosamente.");
        }
    }