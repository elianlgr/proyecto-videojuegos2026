package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioData; //esto es para agregar musica de fondo o sonidos:)


// Clase principal que maneja el ciclo de vida del juego y las interacciones
public class Main extends SimpleApplication implements ActionListener {

    // Entidades principales del jugador y enemigos
    private Player player;
    private PlayerPlataforma playerLateral;
    private ArrayList<Arbusto> listaArbustos = new ArrayList<>();
    private EnemigoCPU enemigo;
    
    // Gestores de logica de los sistemas del juego
    private GestorInteracciones interacciones;
    private GestorGUI gestorGUI;
    private GestorEscenarios gestorEscenarios; 
    private GestorCombate gestorCombate;
    
    // Controles de movimiento basico y salto
    private boolean up, down, left, right, saltar;
    
    // Variables para el control de la carga y transicion de pantallas
    private boolean enTransicion = false;
    private boolean enEscenario2 = false;
    private float tiempoDeCarga = 0f;
    
    // Variables para manejar la logica de derrota
    private boolean enGameOver = false;
    private float tiempoGameOver = 0f;
    
    // Variables para manejar la logica de victoria
    private boolean enVictoria = false;
    private float tiempoVictoria = 0f;
    
    // Variables para la musica
    private AudioNode musicaEscenario1;
    private AudioNode musicaEscenario2;
    private AudioNode sonidoEspada1;
    
    


    // Metodo de entrada principal que arranca la aplicacion jMonkeyEngine
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    // Metodo llamado al iniciar el juego para configurar todo el entorno
    @Override
    public void simpleInitApp() {
        // Desactiva la camara libre y habilita el cursor
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
        cam.setParallelProjection(true);
    
        // Obtiene las dimensiones de la pantalla desde la camara
        float width = cam.getWidth();
        float height = cam.getHeight();
    
        // Configura el area de renderizado y posiciona la camara
        cam.setFrustum(-1000, 1000, 0, width, height, 0);
        cam.setLocation(new Vector3f(0, 0, 10));

        // Instancia los diferentes modelos del jugador y el enemigo
        player = new Player(assetManager, rootNode, width, height);
        playerLateral = new PlayerPlataforma(assetManager, rootNode, width);
        enemigo = new EnemigoCPU(assetManager, rootNode, width);
    
        // Crea el entorno grafico colocando elementos en el escenario
        GestorEntorno entorno = new GestorEntorno(assetManager, rootNode, listaArbustos);
        entorno.crearCuadriculaAleatoria(100, 100, 4, 2);
        entorno.crearCuadriculaAleatoria(850, 700, 4, 2);
        entorno.crearCuadriculaAleatoria(1350, 320, 5, 3);
        entorno.crearCuadriculaAleatoria(200, 800, 3, 3);
    
        // Inicializa la interfaz grafica y las barras de salud
        gestorGUI = new GestorGUI(assetManager, guiNode, cam, guiFont);
        gestorGUI.iniciarBarraVida(); 
        gestorGUI.iniciarBarraVidaEnemigoConTextura(); 

        // Enlaza la logica de combate entre entidades
        gestorCombate = new GestorCombate(playerLateral, enemigo, gestorGUI);

        // Configura y carga el primer nivel o escenario
        gestorEscenarios = new GestorEscenarios(assetManager, rootNode, player, playerLateral, listaArbustos, gestorGUI);
        gestorEscenarios.cargarFondos(width, height);
        gestorEscenarios.iniciarEscenario1(); 

        // Prepara el gestor que escucha cuando el jugador toca elementos del entorno
        interacciones = new GestorInteracciones(player, listaArbustos);

        // Configura controles y oculta los contadores de rendimiento
        initKeys();
        setDisplayStatView(false);
        setDisplayFps(false);
        
        //La musica bonita para el ambiente bien chidori 
        musicaEscenario1 = new AudioNode(assetManager,
        "Audio/Longing for AIR.wav",
        AudioData.DataType.Stream);
       
        
       

        musicaEscenario1.setLooping(true);
        musicaEscenario1.setPositional(false);
        musicaEscenario1.setVolume(0.4f); // volumen medio-bajo
        musicaEscenario1.play();
        
        musicaEscenario2= new AudioNode(assetManager, 
        "Audio/The Lobotomy.wav",
        AudioData.DataType.Stream);
        
        musicaEscenario2.setLooping(true);
        musicaEscenario2.setPositional(false);
        musicaEscenario2.setVolume(0.5f);

        musicaEscenario1.play();
        
        
        // Sonidos de la espada 
        sonidoEspada1= new AudioNode(assetManager,
        "Audio/espada.wav",
        AudioData.DataType.Buffer);
        
        sonidoEspada1.setPositional(false);
        sonidoEspada1.setVolume(0.8f);
       
    }

    // Registra los atajos del teclado y del raton
    private void initKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("TestDano", new KeyTrigger(KeyInput.KEY_T));
        
        inputManager.addMapping("Ataque", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Up", "Down", "Left", "Right", "Jump", "TestDano", "Ataque");
    }

    // Captura y gestiona las acciones realizadas por el jugador
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        // Bloquea los controles si la partida acabo
        if (enGameOver) return;
        
        // Actualiza las direcciones de movimiento si la tecla respectiva esta pulsada
        if (name.equals("Up")) up = isPressed;
        if (name.equals("Down")) down = isPressed;
        if (name.equals("Left")) left = isPressed;
        if (name.equals("Right")) right = isPressed;
        if (name.equals("Jump")) saltar = isPressed; 
        
        // Prueba de disminucion de vida manual mediante el teclado
        if (name.equals("TestDano") && !isPressed) {
            if (enEscenario2) {
                playerLateral.recibirDano(1);
                gestorGUI.actualizarBarraVida(playerLateral.getVidaActual());
            }
        }

        // Acciona el ataque principal
        if (name.equals("Ataque") && isPressed) {
            if (enEscenario2) {
                playerLateral.atacar();
                sonidoEspada1.playInstance();
                        
                gestorCombate.verificarAtaqueJugador(true);
            }
        }
    }

    // Bucle principal que actualiza toda la logica cada fotograma
    @Override
    public void simpleUpdate(float tpf) {
        
        // Maneja el tiempo de espera despues de perder y reinicia la partida
        if (enGameOver) {
            tiempoGameOver += tpf;
            if (tiempoGameOver >= 3.0f) {
                enGameOver = false;
                enEscenario2 = false;
                tiempoGameOver = 0f;
                
                // Reinicia las posiciones y desactiva elementos hostiles
                player.resetPosicion();
                playerLateral.resetVidaYPosicion();
                enemigo.desactivar();
                
                // Limpia la pantalla de derrota y restaura la vida
                gestorGUI.ocultarGameOver();
                gestorGUI.actualizarBarraVida(5); 
                gestorGUI.actualizarBarraVidaEnemigo(5);
                
                // Vuelve a colocar al jugador en el primer nivel
                gestorEscenarios.iniciarEscenario1();
                up = down = left = right = saltar = false; 
            }
            return; 
        }
        
        // Maneja el tiempo de espera al ganar para reiniciar el ciclo de juego
        if (enVictoria) {
            tiempoVictoria += tpf;
            if (tiempoVictoria >= 3.0f) {
                enVictoria = false;
                enEscenario2 = false;
                tiempoVictoria = 0f;
            
                gestorGUI.ocultarYouWin();
            
                // Restaura los datos principales
                player.resetPosicion();
                playerLateral.resetVidaYPosicion();
                enemigo.destruir(rootNode);
                
                // Genera un nuevo enemigo para la proxima partida
                enemigo = new EnemigoCPU(assetManager, rootNode, cam.getWidth());
                gestorGUI.actualizarBarraVidaEnemigo(5);
                gestorCombate = new GestorCombate(playerLateral, enemigo, gestorGUI);
                
                // Devuelve al jugador a la zona inicial
                gestorEscenarios.iniciarEscenario1();
                up = down = left = right = saltar = false; 
            }
            return; 
        }

        // Pantalla de carga simulada entre escenarios
        if (enTransicion) {
            tiempoDeCarga += tpf; 
            if (tiempoDeCarga >= 3.0f) { 
                enTransicion = false;
                enEscenario2 = true;
                musicaEscenario1.stop(); //esto es para que la musica se calle  
                
                // Activa el segundo escenario de formato plataforma
                musicaEscenario2.play(); // y de paso se pone la musica que sigue
                gestorEscenarios.cambiarAEscenario2();
                enemigo.activar();
                gestorGUI.ocultarPantallaCarga(); 
            }
            return; 
        }

        // Logica especifica cuando el jugador esta en el entorno de pelea
        if (enEscenario2) {
            float direccionX = 0;
            if (left) direccionX = -1;
            if (right) direccionX = 1;
            
            // Actualiza la gravedad y velocidad horizontal del personaje
            playerLateral.actualizarFisicas(direccionX, saltar, tpf);
            
            // Actualiza el comportamiento del enemigo mandandole los datos del jugador y la UI
            enemigo.actualizarInteligencia(playerLateral, gestorGUI, tpf);
            
            // Verifica si el jugador murio
            if (playerLateral.getVidaActual() <= 0) {
                enGameOver = true;
                tiempoGameOver = 0f;
                gestorGUI.mostrarGameOver();
            }
        
            // Verifica si el jugador derroto a su oponente
            if (enemigo.getVidaActual() <= 0 && !enVictoria) {
                enVictoria = true;
                tiempoVictoria = 0f;
                gestorGUI.mostrarYouWin();
                System.out.println("Victoria detectada");
            }
            
        } else {
            // Logica correspondiente al primer escenario exploratorio superior
            float dx = 0, dy = 0;
            if (up) dy = 1;
            if (down) dy = -1;
            if (left) dx = -1;
            if (right) dx = 1;

            // Mueve el sprite o detiene las animaciones dependiendo de los inputs
            if (dx != 0 || dy != 0) {
                player.move(dx, dy, tpf);
            } else {
                player.stop(); 
            }
            
            // Crea un efecto de animacion ambiental en la decoracion
            for (Arbusto arbusto : listaArbustos){
                arbusto.mecerConViento(tpf);
            }

            // Comprueba colisiones con la decoracion para lanzar la transicion de nivel
            if (interacciones.vigilar(tpf)) {
                enTransicion = true;
                tiempoDeCarga = 0f; 
                gestorGUI.mostrarPantallaCarga();
            }
        }
    }
}