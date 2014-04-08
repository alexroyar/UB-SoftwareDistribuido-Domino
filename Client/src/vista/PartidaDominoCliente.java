/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista;

import controlador.ControladorCliente;
import controlador.Rules;
import java.util.ArrayList;
import java.util.Scanner;
import modelo.Ficha;
import modelo.FinPartida;
import modelo.Historico;
import modelo.Tablero;
import modelo.TiradaCliente;
import modelo.TiradaServidor;

/**
 *
 * @author alex
 */
public class PartidaDominoCliente {

    // Atributos.
    private Tablero tablero;                // Tablero con las fichas en juego.
    private ArrayList<Ficha> mano_cliente;  // Mano del cliente.                // 
    private ControladorCliente controlador; // Controlador de lectura/escritura.
    private String ip_servidor;             // IP del DataSocket.
    private int puerto,manoS, iteracion;    // Puerto del DataSocket, tamaño de la mano del servidor y turno de la partida.
    private Scanner input;                  // Scanner para la entrada de teclado.
    private Historico historico;            // Histórico de acciones.
    private boolean fin_partida = false;
    private boolean pidoFicha = false;
    private boolean jugando = false;   
    
    // Constructores.
    /**
     * Constructor de PartidaDominoCliente.
     * @param ip_servidor Dirección IP.
     * @param puerto  Puerto.
     */
    public PartidaDominoCliente(String ip_servidor, int puerto) {
        this.input = new Scanner(System.in);
        this.tablero = new Tablero();
        this.ip_servidor = ip_servidor;
        this.puerto = puerto;
        this.fin_partida = false;
        this.iteracion = 1;
        this.controlador = new ControladorCliente(this.ip_servidor, this.puerto);
        this.controlador.init();
    }

    /**
     * Inicia la partida.
     */
    public void init(){
        jugar_partida();
    }
    
    /**
     * Método en el que se lleva a cabo toda la partida.
     */
    private void jugar_partida() {
        try {
            // El cliente comienza solicitando el inicio de partida al servidor.
            this.controlador.enviar_hello();
            
            // Bucle 'infinito' de la partida. Se saldrá vía excepciones o tras terminar
            // legamente el juego.
            while (!this.fin_partida) {
                // Se lee la cabecera del mensaje entrante y se determina qué hacer.
                byte header = (byte) this.controlador.recibir_header();              
                switch (header) {
                    case Rules.INIT:
                        if(!jugando){
                            this.jugando=true;
                            // Se configura la partida y se recibe la mano inicial.
                            configurar_partida();
                            this.mano_cliente = this.controlador.recibir_init();

                            // Dependiendo del valor de la última ficha de la mano,
                            // comenzará jugando el cliente o el servidor.
                            Ficha ficha_inicial = this.mano_cliente.remove(7);

                            if (ficha_inicial.equals(Rules.NO_FICHA)){
                                this.historico.añadir_accion("Empieza cliente.");
                                this.manoS = 7;
                            } else {
                                this.historico.añadir_accion("Empieza servidor.");
                                this.historico.añadir_accion("Servidor tira "+ficha_inicial);
                                this.tablero.add_right(ficha_inicial);
                                this.manoS = 6;
                            }

                            play();
                        }
                        break;
                        
                    // El servidor ha enviado una tirada.
                    case Rules.TIRADA_S_C:
                        // Se recibe una tirada. Si la tirada es correcta, se inserta en el 
                        // tablero. Si se recibe una NO_FICHA en la tirada se da por entendido
                        // que el servidor pasa su turno.
                        TiradaServidor tirada_servidor = this.controlador.recibir_tirada();
                        if (!tirada_servidor.get_ficha().equals(Rules.NO_FICHA)){
                            this.historico.añadir_accion("Servidor tira "+tirada_servidor.get_ficha()+".");
                            if(!es_tirada_ilegal(tirada_servidor)){
                                recibir_tirada(tirada_servidor);
                                if (this.manoS > 0){
                                    play();
                                }
                            }else{
                                this.historico.añadir_accion("Servidor ILLEGAL_ACTION.");
                                this.historico.añadir_accion("Error ficha "+tirada_servidor.get_ficha());
                                this.controlador.enviar_error(Rules.ILLEGAL_ACTION, "Esta ficha ya está en tablero o mano del cliente");
                            }
                        }
                        break;
                
                    // La partida ha llegado a su fin.
                    case Rules.GAME_FINISH:
                        FinPartida fin = this.controlador.recibir_fin_partida();
                        
                        // Dependiendo del tipo de final de partida, se muestra
                        // diferente información al cliente.
                        switch (fin.get_tipo()){
                            case Rules.GANA_CLIENTE:
                                this.historico.añadir_accion("Cliente gana la partida.");
                                imprimir_fin_partida(fin);
                                break;
                            case Rules.GANA_SERVIDOR:
                                imprimir_fin_partida(fin);
                                this.historico.añadir_accion("Servidor gana la partida.");
                                break;
                            case Rules.GANAN_TODOS:
                                imprimir_fin_partida(fin);
                                this.historico.añadir_accion("Empate.");
                                break;
                        }
                        this.fin_partida = true;
                        break;
                        
                    //El servidor ha enviado un error.
                    case Rules.ERROR:
                        //System.out.println(this.controlador.recibir_error());
                        imprimir_fin_partida(Rules.ERROR,this.controlador.recibir_error());
                        this.fin_partida=true;
                        break;


                    // El cliente ha pedido una ficha al servidor, y el servidor
                    // se la envía si hay disponibles.
                    case Rules.FICHA_ROBADA:
                        Ficha fichaRobada = this.controlador.recibir_ficha_robada();
                        if (fichaRobada != null){
                            this.historico.añadir_accion("Cliente roba "+fichaRobada+".");
                            System.out.println(fichaRobada);
                            this.mano_cliente.add(fichaRobada);
                        } else {
                            //ERROR
                            System.out.println("ficha robada es null");
                        }
                        
                        play();
                        break;
                        
                    // Mensaje no identificable.
                    default:
                        this.controlador.enviar_error(Rules.SYNTAX_ERROR,"No sabemos que narices envias.");
                        imprimir_fin_partida(Rules.SYNTAX_ERROR, "No sabemos que narices envía el servidor.");
                        this.fin_partida=true;     
                }
            }
            this.controlador.cerrar_conexion();
            
            
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.toString());
            System.out.println(e.getMessage());
        }
        

    }
    
 /**
     * Comprueba que una ficha determina no esté repetida en el resto,
     * mano del servidor y fichas en juego.
     * @param f Ficha a analizar.
     * @return True si está repetida, false en caso contrario.
     */
    public boolean ficha_repetida(Ficha f){
        return this.tablero.get_fichas().contains(f) ||
               this.mano_cliente.contains(f);
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
    public boolean es_tirada_ilegal (TiradaServidor t){
        return ((t.get_posicion() != Rules.LEFT) && 
                (t.get_posicion() != Rules.RIGHT) ) ||
               ficha_ilegal(t.get_ficha()) ||
               ficha_repetida(t.get_ficha());
    }    
    
    
    /**
     * Se inicializan valores de una partida tales como el tablero, las fichas
     * o el histórico de aciones.
     */
    private void configurar_partida() {
        // Inicializamos el tablero.
        this.historico=new Historico();
        this.tablero = new Tablero();
    }
    
    /**
     * Se recibe la tirada del servidor y se inserta en el tablero. 
     * @param t TiradaServidor a insertar.
     */
    private void recibir_tirada(TiradaServidor t){
        this.tablero.insertar_tirada_servidor(t);
        this.manoS = t.get_mano_server_size();
        
    }
    
    /**
     * El cliente decide qué ficha quiere tirar y dónde, generando así
     * una tiradaCliente.
     * @return TiradaCliente válida.
     */
    public TiradaCliente seleccionar_ficha() {
        TiradaCliente t = null;
        imprimir_estado();
        if(puedo_tirar()){
            System.out.println("\nSelecciona que ficha quieres tirar: ");
            int ficha = input.nextInt();
            while(ficha<1 || ficha > this.mano_cliente.size()){System.out.println("\nSelecciona que ficha quieres tirar: ");ficha = input.nextInt();}
            System.out.println("En que posición?");
            System.out.println("1) Izquierda");
            System.out.println("2) Derecha");
            
            int posicion = this.input.nextInt();
            
            while((posicion<1 || posicion >2)){System.out.println("En que posición?");posicion = input.nextInt();}
            char pos='L';
            if(posicion<2){pos = 'L';}else{pos = 'R';}
            t = new TiradaCliente(Rules.TIRADA_C_S,this.mano_cliente.remove(ficha-1),pos);
        } else{
            System.out.println("Lamentablemente no puedes tirar, haz enter para pasar/robar :(");
            this.input.next();
        }
        return t;
    }
    
    /**
     * 
     * @return 
     */
    private int fichas_resto(){
        return Rules.TILES-this.tablero.get_fichas().size()-this.manoS-this.mano_cliente.size();
    }
    
    private void imprimir_fin_partida(byte error, String msg){
        if(jugando){imprimir_estado();}
        System.out.println("--------------------------ERROR--------------------------");
        System.out.println(msg);
        System.out.println("---------------------------------------------------------");
    }
    private void imprimir_fin_partida(FinPartida fin_partida){
        imprimir_estado();
        System.out.println("--------------------FIN DE LA PARTIDA--------------------");
        System.out.println("--                                                     --");
        switch(fin_partida.get_tipo()){
            case Rules.GANA_SERVIDOR:
                System.out.println("--              No sabes jugar al dómino!              --");
                System.out.println("--                   Gana Servidor                     --");             
                break;
            case Rules.GANA_CLIENTE:
                System.out.println("--                 Mis felicitaciones!                 --");
                System.out.println("--                     Has Ganado                      --");          
                break;
            case Rules.GANAN_TODOS:
                System.out.println("--                 CLIENTE Y SERVIDOR                  --");
                System.out.println("--             mas colgados que la mojama              --");
                System.out.println("--                 CLIENTE Y SERVIDOR                  --");
                System.out.println("Servidor termina con "+fin_partida.get_puntos()+".");
                System.out.println("Cliente termina con "+sumar_puntos(this.mano_cliente)+".");
                break;
        }
        System.out.println("--                                                     --");
        System.out.println("---------------------------------------------------------");
    }
    
    private void imprimir_estado(){
        this.iteracion+=1;
        System.out.println("----------Estado de partida-----------");
        System.out.println("- Cantidad fichas servidor: "+manoS);
        System.out.println("- Cantidad fichas cliente: "+this.mano_cliente.size());
        System.out.println("- Cantidad fichas tablero: "+this.tablero.get_fichas().size());
        System.out.println("- Cantidad fichas resto: "+fichas_resto());
        System.out.println("- Histórico:\n");
        System.out.println(this.historico);
        System.out.println("- Fichas en tablero:\n");//this.tablero.toString()
        pintar_tablero();
        System.out.println("- Fichas cliente:\n");
        for (int i = 0; i < this.mano_cliente.size(); i++) {
            if((i+1)%4!=0){
                System.out.print(""+(i+1)+") "+this.mano_cliente.get(i)+"\t");
            }else{
                System.out.println(""+(i+1)+") "+this.mano_cliente.get(i)+"\t");
            }
            if(i+1==this.mano_cliente.size()){System.out.println("\n");}
        }
        System.out.println("---------------------------------------");
        //this.historico.añadir_accion("· Iteración "+this.iteracion+":");
    }
    private int sumar_puntos(ArrayList<Ficha> fichas){
        int puntos = 0;
        for (Ficha ficha : fichas) {
            puntos+=ficha.get_valor();
        }
        return puntos;
    }
    public void pintar_tablero(){
        String print = "";
        int count=0;
        for (int i = 0; i < this.tablero.get_fichas().size(); i++) {
            if(count==6){print += "\n\n\t-> "+this.tablero.get_fichas().get(i);count=0;}
            else{
            print += this.tablero.get_fichas().get(i);
            }
            count++;
        }
        System.out.println("\t"+print+"\n");
    }

    public void play(){
        TiradaCliente tirada = seleccionar_ficha();
        if(tirada == null){
            this.historico.añadir_accion("Cliente solicita robar.");
            this.controlador.enviar_no_tirada();
        } else {
            TiradaCliente tirada_ok = this.tablero.preparar_tirada(tirada);
            if (tirada_ok == null) {
                this.historico.añadir_accion("Cliente ficha ko."+tirada.get_ficha());
                //System.out.println("Las trampas y menitras hacen llorar al niño Jesús\n\tselecciona una ficha válida...");
                this.mano_cliente.add(tirada.get_ficha());
                play();
            } else{
                this.historico.añadir_accion("Cliente tira "+tirada_ok.get_ficha()+" "+tirada_ok.get_posicion()+".");
                this.tablero.insertar_tirada_cliente(tirada_ok);
                this.controlador.enviar_tirada(tirada_ok);
            }
        }
        
    }
      
    public boolean puedo_tirar(){
        // Lo primero de todo, comprobar si es tablero está vacío.
        if (this.tablero.get_fichas().isEmpty()) {
            return true;
        }        
        // Si no está vacío, mirar si existe algún movimiento posible.
        for (Ficha ficha : this.mano_cliente){
            if(this.tablero.encaja(ficha)!=null){
                //System.out.println("Tienes movimientos posibles");
                return true;
            }
        }
        //System.out.println("No tienes movimientos posibles");
        // Si no hay ningún movimiento posible, la respuesta es no.
        return false;
    }


}
