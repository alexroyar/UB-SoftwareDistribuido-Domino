package vista;

import controlador.ComUtils;
import controlador.ControladorServidor;
import controlador.IllegalActionException;
import controlador.Rules;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.Ficha;
import modelo.Historico;
import modelo.Tablero;
import modelo.Tirada;

/**
 *
 * @author arodriya8.alumnes
 */
public class PartidaDominoServidor implements Runnable  {
    // Atributos.
    
    private ControladorServidor controlador; // Controlador de lectura/escritura.
    private Socket socket;                   // DataSocket.
    private ComUtils comUtils;               // Clase encargada de las comunicaciones.
    private Tablero tablero;                 // Tablero de la partida.
    private ArrayList<Ficha> resto;          // Fichas disponibles para robar.
    private ArrayList<Ficha> mano_servidor;  // Mano real del servidor.
    private Historico historico;             // Histórico de acciones.
    private int mano_cliente;                // Cálculo del tamaño de la mano del cliente.
    private int iteracion = 1;               // Turno de la partida.
    private int id_thread;                   // Identificador del thread.
    private boolean fin_partida     = false;
    private boolean pasa_servidor   = false;
    private boolean pasa_cliente    = false;
    
    // Constructores.

    /**
     * Constructor de PartidaDominoServidor.
     * @param id_thread Número de thread que le corresponde como identificador.
     * @param socket    Un DataSocket.
     */
    public PartidaDominoServidor(int id_thread,Socket socket) {
            this.id_thread = id_thread;
            this.socket = socket;
            this.resto = new ArrayList<>();
            this.pasa_cliente = false;
            this.pasa_servidor = false;
    }
    
    /**
     * Segunda parte del constructor, para parámetros conflictivos.
     */
    private void init(){
        this.comUtils = new ComUtils(this.socket);
        this.comUtils.init();
        this.controlador = new ControladorServidor(this.socket, this.comUtils);
    }

    
    /**
     * Método que se llamará cuando el hilo comience su ejecución. Pone en marcha
     * la partida.
     * @see PartidaDominoServer.jugar()
     */
    @Override
    public void run(){
        System.out.println("Comienza el thread " + this.id_thread + ".");
        init();
        jugar();
        System.out.println("Fin del thread " + this.id_thread + ".");
        Thread.currentThread().interrupt();
//        return;
    }
    
       
    /**
     * Se generan las fichas iniciales del Dominó.
     * @see Rules.
     */
    private void crear_fichas() {
        for (int i = Rules.MIN_NUM_TILE; i <= Rules.MAX_NUM_TILE; i++) {
            for (int j = i + 1; j <= Rules.MAX_NUM_TILE; j++) {
                this.resto.add(new Ficha(i, j));
            }
            this.resto.add(new Ficha(i, i));
        }
    }

    /**
     * Núcleo de la partida en el que se llevan a cabo las respuestas al cliente
     * dependiendo de los mensajes que se reciban.
     */
    private void jugar() {
        try {
            // Bucle 'infinito' de la partida. Se saldrá vía excepciones o tras terminar
            // legamente el juego.
            while (!this.fin_partida) {
                // Lectura constante de la cabecera del mensaje que el cliente enviará
                // al servidor. 
                byte header = (byte) this.controlador.recibir_header();
                
                switch (header) {
                    // Solicitud de comienzo de partida.
                    case Rules.HELLO:
                        // Se configuran varios aspectos de la partida.
                        configurar_partida();
                        
                        // Se establece quién comienza a jugar primero, repartiendo
                        // la mano inicial a ambos bandos.
                        Ficha f;
                        ArrayList<Ficha> mano_cliente_temporal = repartir_mano_inicial();
                        Ficha turno = primer_turno(mano_cliente_temporal);
                        if (turno != null) {
                            this.historico.add_accion("\tEmpieza servidor.");
                            this.tablero.add_left(turno);
                        } else {
                            this.historico.add_accion("\tEmpieza cliente.");
                        }
                        
                        this.controlador.enviar_mano_inicial(mano_cliente_temporal, turno);
                        this.mano_cliente = Rules.FICHAS_INICIALES;
                        imprimir_estado();
                        break;

                    // Se recibe una tirada del cliente.
                    case Rules.TIRADA_C_S:
                        Tirada tirada = this.controlador.recibir_tirada();
                       
                        // Dependiendo de la tirada, el cliente estará jugando o 
                        // solicitando una ficha robada. Siempre que se le pueda dar
                        // una ficha robada, así se hará.
                        if (tirada == null) {
                            // Si se puede robar...
                            if (!this.resto.isEmpty()) {
                                // ... se le enviará la ficha al cliente.
                                Ficha ficha_robada = robar_ficha_cliente();
                                this.controlador.enviar_ficha_robada(ficha_robada);
                                this.historico.add_accion("\tCliente roba la ficha "+ficha_robada+".");
                                this.mano_cliente+=1;
                              
                            // Si no se puede robar, el servidor pasa del tema y 
                            // juega con naturalidad. Queda como si el cliente 'pasara' turno.
                            } else {
                                this.historico.add_accion("\tCliente no puede tirar ni robar. Resto vacío.");
                                this.pasa_cliente = true;
                                
                                // El servidor juega, calculando su movimiento. Si no puede jugar, tendrá que robar
                                // hasta que o bien pueda tirar o bien no queden fichas en el resto.
                                Tirada tirada_servidor = tirada_servidor();
                                if (tirada_servidor == null){
                                    this.historico.add_accion("\tServidor no puede tirar ni robar. Resto vacío");
                                    this.historico.add_accion("\tCliente y servidor más colgados que la mojama.");
                                    this.pasa_servidor = true;
                                    this.pasa_cliente = true;
                                    this.fin_partida = true;
                                    this.controlador.enviar_fin_partida(Rules.GANAN_TODOS, sumar_puntos(this.mano_servidor));
                                } else {
                                    this.pasa_servidor = false;
                                    this.tablero.insertar_tirada(tirada_servidor);
                                    this.historico.add_accion("\tServidor  tira la ficha "+tirada_servidor.get_ficha());
                                    this.controlador.enviar_tirada(tirada_servidor, this.mano_servidor.size());
                                    
                                    // Si el servidor se queda sin fichas, ha ganado.
                                    if (this.mano_servidor.isEmpty()){
                                        this.historico.add_accion("\tServidor se queda sin fichas. Gana la partida");
                                        this.fin_partida = true;
                                        this.controlador.enviar_fin_partida(Rules.GANA_SERVIDOR);   
                                    }
                                }
                            }
                            
                        // El cliente ha tirado una ficha.
                        } else {
                            if (es_tirada_ilegal(tirada)){ 
                                throw new IllegalActionException("La ficha " + tirada.get_ficha().toString() +
                                    " es una ficha ilegal");
                            }
                            
                            // Si el lanzamiento de la ficha es incorrecto, se le notifica al cliente vía
                            // un mensaje de error y se cierra la conexión.
                            if (!this.tablero.insertar_tirada(tirada)){
                                this.historico.add_accion("\tCliente tira ficha erronea " + tirada.get_ficha() + " " + tirada.get_posicion());
                                this.controlador.enviar_error(Rules.ILLEGAL_ACTION, "No puedes tirar esa ficha en el tablero... >:(");
                            
                            // Si el lanzamiento es correcto, se sigue con la partida.
                            } else {
                                this.historico.add_accion("\tCliente tira la ficha " +tirada.get_ficha() + " " + tirada.get_posicion());
                                this.mano_cliente -= 1;
                                this.pasa_cliente = false;
                                
                                // Si el cliente se queda sin fichas, ha ganado.
                                if (this.mano_cliente < 1){
                                    this.historico.add_accion("\tCliente ya no tiene fichas que tirar. Gana la partida.");
                                    this.fin_partida = true;
                                    this.controlador.enviar_fin_partida(Rules.GANA_CLIENTE, sumar_puntos(this.mano_servidor));
                                
                                // Si no ha ganado aún, el servidor se prepara para responder.    
                                } else {
                                    Tirada tirada_servidor = tirada_servidor();
                                    // Si no puede tirar, tiene que pasar. 
                                    if (tirada_servidor == null) {
                                        this.historico.add_accion("\tServidor pasa ya que no puede tirar.");
                                        this.pasa_servidor = true;
                                        this.controlador.enviar_no_tirada(this.mano_servidor.size());
                                    } else {
                                        this.historico.add_accion("\tServidor tira la ficha " + tirada_servidor.get_ficha() + " " + tirada.get_posicion() + " .");
                                        this.tablero.insertar_tirada(tirada_servidor);
                                        this.pasa_servidor = false;
                                        this.controlador.enviar_tirada(tirada_servidor, this.mano_servidor.size()); 
                                        
                                        // Si el servidor se queda sin fichas, gana la partida.
                                        if (this.mano_servidor.isEmpty()){
                                            this.historico.add_accion("\tServidor gana la partida al vaciar su mano.");
                                            this.fin_partida = true;
                                            this.controlador.enviar_fin_partida(Rules.GANA_SERVIDOR);
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Se comprueba que cliente y servidor hayan pasado o no
                        // a la vez su turno, lo que sería el final de la partida.
                        if (this.pasa_cliente && this.pasa_servidor){
                            this.controlador.enviar_fin_partida(Rules.GANAN_TODOS, sumar_puntos(this.mano_servidor));
                        }
                        imprimir_estado();
                        break;

                    // El cliente ha enviado un error.
                    case Rules.ERROR:
                        System.out.println(this.controlador.recibir_error());
                        this.fin_partida = true;
                        break;
                        
                    // Llegan mensajes no identificados en el protocolo.
                    default:
                        System.out.println("Llegan mensajes no identificados.");
                        System.out.println("Cabecera: " + header);
                        this.fin_partida = true;
                        break;
                }
            }            
            
        // Excepción de timeout.
        } catch (InterruptedIOException timeout_exception){
            System.out.println("Error de timeout.");
            try {
                this.controlador.enviar_error(Rules.INTERNAL_ERROR, "Error de timeout.");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar un error de timeout.");
                System.out.println(ex.toString());
            }
            System.out.println(timeout_exception.toString());
            
            
        // Excepción de socket.
        } catch (SocketException socket_exception){
            System.out.println("Error de socket.");
            try {
                this.controlador.enviar_error(Rules.INTERNAL_ERROR, "Error de socket.");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar un error de socket.");
                System.out.println(ex.toString());
            }
            System.out.println(socket_exception.toString());
            
            
        // Excepción de protocolo.
        } catch (ProtocolException protocol_exception){
            System.out.println("Error de protocolo.");
            try {
                this.controlador.enviar_error(Rules.SYNTAX_ERROR, "Error de sintaxi/protocolo.");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar un error de sintaxi/protocolo.");
                System.out.println(ex.toString());
            }
            System.out.println(protocol_exception.toString());
            
            
        // Excepción de entrada/salida del socket.
        } catch (IOException io_exception) {
            System.out.println("Error de comunicación IO del socket.");
            try {
                this.controlador.enviar_error(Rules.INTERNAL_ERROR, "Error de IO del socket.");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar un error de IO de Socket.");
                System.out.println(ex.toString());
            }
            System.out.println(io_exception.toString());
            
            
        // Excepción de jugada ilegal.
        } catch (IllegalActionException illegal_action){
            System.out.println("Error de IllegalActionException");
            try {
                this.controlador.enviar_error(Rules.ILLEGAL_ACTION, "Ficha/posición ilegal");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar de una illegal action.");
                System.out.println(ex.toString());
            }
            System.out.println(illegal_action.toString());
            
        
        // Excepción indefinida.
        } catch (Exception e){
            System.out.println("Error indefinido.");
            try {
                this.controlador.enviar_error(Rules.UNKNOWN_ERROR, "Error indefinido en el protocolo.");
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de notificar de un error indefinido.");
                System.out.println(ex.toString());
            }
            System.out.println(e.toString());
        
        // Finalmente, cerramos conexión. SIEMPRE.
        } finally {
            try {
                this.controlador.cerrar_conexion();
            } catch (IOException ex) {
                System.out.println("Problemas a la hora de cerrar la conexión.");  
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Imprime el estado de una partida.
     */
    public void imprimir_estado(){
        this.iteracion+=1;
        System.out.println("-------Estado de partida ID "+this.id_thread+"-----------");
        System.out.println("- Cantida fichas servidor: "+this.mano_servidor.size());
        System.out.println("- Cantida fichas cliente: "+this.mano_cliente);
        System.out.println("- Cantida fichas tablero: "+this.tablero.get_fichas().size());
        System.out.println("- Cantida fichas resto: "+this.resto.size());
        System.out.println("- Fichas en tablero: "+this.tablero.toString());
        System.out.println("- Fichas servidor: "+this.mano_servidor.toString());
        System.out.println("- Histórico:");
        System.out.println(this.historico.toString());
        System.out.println("\n");
        //System.out.println("--------------------------------------");
        this.historico.add_accion("· Iteración "+this.iteracion+":");
    }
   
    /**
     * Se inicializan valores de una partida tales como el tablero, las fichas
     * o el histórico de aciones.
     * @see PartidaDominoServidor.repartir_mano_inicial()
     */
    private void configurar_partida() {
        // Inicializamos el tablero.
        this.historico=new Historico();
        this.tablero = new Tablero();
        // Generamos las fichas y las mezclamos.
        this.resto = new ArrayList<>();
        crear_fichas();
        Collections.shuffle(resto);
        // Damos una mano al server.
        this.mano_servidor = repartir_mano_inicial();
        //Rules.imprimir_mano(mano_servidor, "Imprimo mano servidor:");
    }
    
    /**
     * Sumatorio de puntos de una mano de fichas.
     * @param fichas ArraList<Ficha>, una mano.
     * @return Sumatorio de puntos.
     */
    private int sumar_puntos(ArrayList<Ficha> fichas){
        int puntos = 0;
        for (Ficha ficha : fichas) {
            puntos+=ficha.get_valor();
        }
        return puntos;
    }
    
    /**
     * Se reparte la mano inicial, creando un ArrayList<Ficha> del tamaño
     * definido en Rules.
     * @return ArrayList<Ficha> del tamaño Rules.FICHAS_INICIALES.
     */
    public ArrayList<Ficha> repartir_mano_inicial() {
        ArrayList<Ficha> mano = new ArrayList();
        for (int i = 0; i < Rules.FICHAS_INICIALES; i++) {
            mano.add(this.resto.remove(i));

        }
        return mano;
    }

    /**
     * Se roba una ficha del resto para el cliente.
     * @return Una Ficha si hay fichas en el resto, un null si no hay más fichas
     *          disponibles.
     */
    public Ficha robar_ficha_cliente() {
        if (!this.resto.isEmpty()) return this.resto.remove(0);
        else                        return null;
        
    }

    /**
     * Se roba una ficha del resto para el servidor, montando directamente
     * la jugada que se llevará a cabo.
     * @return Una Tirada si hay fichas en el resto y la tirada encaja en el tablero, 
     *          un null en caso contrario.
     * @see Tablero.encaja(Ficha f)
     */
    public Tirada robar_ficha_servidor() {
        if (!this.resto.isEmpty()) {
            Ficha ficha_robada= this.resto.remove(0);
            Tirada tirada = this.tablero.encaja(ficha_robada);
            if(tirada == null){
                this.mano_servidor.add(ficha_robada);
                return robar_ficha_servidor();
            }else{
                return tirada;
            }
        } else {
            return null;
        }
    }
    
    /**
     * El servidor calcula su tirada.
     * @return Una Tirada válida si se puede realizar o un null en caso contrario.
     * @see PartidaDominoServidor.puedo_tirar()
     * @see PartidaDominoServidor.robar_ficha_servidor()
     */
    private Tirada tirada_servidor() {
        // Generamos la tirada del servidor.
        Tirada tirada_servidor = puedo_tirar();
        if (tirada_servidor == null) {
            tirada_servidor = robar_ficha_servidor();
            if (tirada_servidor == null) {
                    System.out.println("Ya no puedo tirar ni robar :(");
                    this.pasa_servidor=true;
                    return tirada_servidor;
                    
            } else {
                this.historico.add_accion("\tServidor roba "+tirada_servidor.get_ficha());
                return tirada_servidor;
            }
        }
        return tirada_servidor;
    }

    /**
     * Se comprueba que el servidor pueda realizar alguna jugada con su 
     * mano actual.
     * @return Una Tirada válida si puede tirar, un null en caso contrario.
     * @see Tablero.encaja(Ficha)
     */
    // Devuelve la tirada que hará el servidor. Si no puede, devuelve null.
    public Tirada puedo_tirar() {
        Tirada tirada;
        for (Ficha ficha_mano : mano_servidor) {
            tirada = tablero.encaja(ficha_mano);
            if (tirada != null) {
                this.mano_servidor.remove(ficha_mano);
                return tirada;
            }
        }
        return null;
    }
    
    /**
     * Se calcula quién tira primero en el inicio de la partida, comparando
     * las manos de cliente y servidor.
     * @param mano_cliente ArrayList<Ficha> con la mano del cliente.
     * @return Una Ficha normal si comienza el servidor, un null si
     *          comienza el cliente.
     * @see PartidaDominoServidor.doble_mas_alto(ArrayList<Ficha> mano)
     * @see PartidaDominoServidor.ficha_mas_alta(ArrayList<Ficha> mano)
     */
    private Ficha primer_turno(ArrayList<Ficha> mano_cliente) {
        int doble_mas_alto_server = doble_mas_alto(this.mano_servidor);
        int doble_mas_alto_cliente = doble_mas_alto(mano_cliente);
        Ficha ficha_mas_alta_server;
        Ficha ficha_mas_alta_cliente;

        // Ninguno de los dos tiene ningún doble. Comparamos las fichas 
        // más altas de cada uno.
        if (doble_mas_alto_cliente == -1 && doble_mas_alto_server == -1) {
            ficha_mas_alta_cliente = ficha_mas_alta(mano_cliente);
            ficha_mas_alta_server = ficha_mas_alta(this.mano_servidor);

            // Empieza cliente.
            if (ficha_mas_alta_cliente.get_valor()> ficha_mas_alta_server.get_valor()){
                return null;
              
            // Empieza servidor.
            } else if (ficha_mas_alta_cliente.get_valor() < ficha_mas_alta_server.get_valor()) {
                this.mano_servidor.remove(ficha_mas_alta_server);
                return ficha_mas_alta_server;
            
            // Se produce empate. Usamos el criterio de la ficha más alta.
            } else {
                // Empieza cliente.
                if (ficha_mas_alta_cliente.get_valor_maximo()> ficha_mas_alta_server.get_valor_maximo()) {
                    return null;
                    
                // Empieza servidor.
                } else if (ficha_mas_alta_cliente.get_valor_maximo() < ficha_mas_alta_server.get_valor_maximo()) {
                    this.mano_servidor.remove(ficha_mas_alta_server);
                    return ficha_mas_alta_server;
                
                // Empate que deriva de un error. No se tendría que llegar aquí nunca.
                } else {
                    // Super error.
                }
            }
            
        // Empieza cliente.
        } else if (doble_mas_alto_cliente > doble_mas_alto_server) {
            return null;
            
        // Empieza servidor.
        } else if (doble_mas_alto_cliente < doble_mas_alto_server) {
            return this.mano_servidor.remove(this.mano_servidor.indexOf(
                    new Ficha(doble_mas_alto_server, doble_mas_alto_server)));
        }

        // Empate que deriva de un error. No se tendría que llegar aquí nunca.
        return null;
    }

    /**
     * Se busca el doble más alto de un ArrayList<Ficha>, que será lo que 
     * decidirá a priori quién empieza la partida.
     * @param mano ArrayList<Ficha>, la mano del cliente.
     * @return El número de la ficha doble más alta o -1 en caso de que no 
     *          haya fichas dobles. 
     */
    private int doble_mas_alto(ArrayList<Ficha> mano) {
        int max = -1;

        for (int j = Rules.MAX_NUM_TILE; j <= Rules.MAX_NUM_TILE; j++) {
            for (Ficha ficha_mano : mano) {
                if (ficha_mano.equals(new Ficha(j, j))) {
                    if (j > max) {
                        max = j;
                    }
                }
            }
        }

        return max;
    }

    /**
     * Se busca la ficha de mayor valor de un ArrayList<Ficha>. De una mano,
     * vaya. Si las manos no tienen dobles será el criterio usado para decidir
     * quién comienza la partida.
     * @param mano ArrayList<Ficha>, la mano del cliente.
     * @return La Ficha más alta del ArrayList pasado por parámetros.
     */
    private Ficha ficha_mas_alta(ArrayList<Ficha> mano) {
        int sum = 0;
        int max = -1;
        Ficha aux = null;
        for (Ficha ficha_mano : mano) {
            sum = ficha_mano.get_valor();
            if (sum > max) {
                max = sum;
                aux = ficha_mano;
            }
        }
        return aux;
    }
    
    /**
     * Comprueba que una ficha determina no esté repetida en el resto,
     * mano del servidor y fichas en juego.
     * @param f Ficha a analizar.
     * @return True si está repetida, false en caso contrario.
     */
    public boolean ficha_repetida(Ficha f){
        return this.tablero.get_fichas().contains(f) ||
               this.mano_servidor.contains(f) ||
               this.resto.contains(f);
    }

    /**
     * Comprueba que una ficha sea ilegal.
     * @param f Ficha a analizar.
     * @return True si es una ficha ilegal, false en caso contrario. 
     */
    public boolean ficha_ilegal(Ficha f) {
        return (f.get_numero_l() > Rules.MAX_NUM_TILE) || 
               (f.get_numero_l() < Rules.MIN_NUM_TILE) ||
               (f.get_numero_r() > Rules.MAX_NUM_TILE) || 
               (f.get_numero_r() < Rules.MIN_NUM_TILE);
    }
    
    /**
     * Comprueba que una tirada es legal.
     * @param t Tirada a analizar.
     * @return True si es ilegal, false si es legal.
     */
    public boolean es_tirada_ilegal (Tirada t){
        return ( (t.get_posicion() != Rules.LEFT) && 
                 (t.get_posicion() != Rules.RIGHT) ) ||
               ficha_ilegal(t.get_ficha()) ||
               ficha_repetida(t.get_ficha());
    }
}
