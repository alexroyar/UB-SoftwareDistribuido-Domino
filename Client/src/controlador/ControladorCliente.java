/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controlador;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import modelo.Ficha;
import modelo.FinPartida;
import modelo.TiradaCliente;
import modelo.TiradaServidor;

/**
 *
 * @author alex
 */
public class ControladorCliente {
    
    private Socket socket;
    private ComUtils comUtils;
    private InetAddress servidor;
    private String ip_servidor;
    private int puerto;
            
    /**
     * Constructor de ControladorCliente.
     * @param ip_servidor Dirección IP del ServerSocket.
     * @param puerto Puerto.
     */
    public ControladorCliente(String ip_servidor, int puerto){
        this.ip_servidor = ip_servidor;
        this.puerto = puerto;
    }
    
    /**
     * Segunda parte del constructor, para parámetros conflictivos.
     */
    public void init(){
        try {
            this.servidor = InetAddress.getByName(ip_servidor);
            this.socket = new Socket(servidor, puerto);
            this.comUtils = new ComUtils(this.socket);
            this.comUtils.init();
            this.socket.setSoTimeout(Rules.TIMEOUT_READ);
        } catch (IOException e) {
        }
    }
    
    /**
     * Se cierra la conexión entre cliente y servidor.
     */
    public void cerrar_conexion(){
        try {
            this.socket.close();
        } catch (IOException ex) {
            System.out.println("Error en cerrar_conexion:\n\t");
            System.out.println(ex.getMessage());
        }
    }  

    /**
     * Se envía una solicitud de comenzar partida.
     */
    public void enviar_hello(){
        try {
            this.comUtils.write_byte(Rules.HELLO);
        } catch (IOException ex) {
            System.out.println("Error en enviar_hello:\n\t");
            System.out.println(ex.getMessage());
        }
    }
    
    /**
     * Se lee la cabecera del mensaje entrante.
     * @return Valor de la cabecera del mensaje.
     */
    public int recibir_header(){
        int num = -1;
        try {
            num = this.comUtils.read_byte();
        } catch (IOException ex) {
            System.out.println("Error en recibir_header:\n\t");
            System.out.println(ex.getMessage());
        } finally {
            return num;
        }
    }
    
    /**
     * Se recibe la tirada del servidor.
     * @return Una Tirada si la tirada es válida o un null en caso contrario.
     */
    public TiradaServidor recibir_tirada (){
        TiradaServidor t = null;
        try{
            char num0 = this.comUtils.read_char();
            char num1 = this.comUtils.read_char();
            char pos = this.comUtils.read_char();
            int size = this.comUtils.read_int32();
            // Comprobamos que el formato de la ficha sea correcto.
            if (num0 == 'N' && num1 == 'T' && pos == ' ') {
                return new TiradaServidor(Rules.TIRADA_S_C, Rules.NO_FICHA, pos, size);
            } else if (num0  == 'N'
                    || num1  == ' '
                    || pos == 'T') {
                System.out.println("ERROR de tirada del server");
                enviar_error(Rules.ILLEGAL_ACTION, "Formato no válido");
                // Algo mal hecho. Error.
            } else {
                // TiradaCliente buena.
                t = new TiradaServidor(Rules.TIRADA_C_S, new Ficha(num0,num1), pos, size);
            }
            
        } catch (IOException ex) {
            System.out.println("Error en recibir_tirada:\n\t");
            System.out.println(ex.getMessage());
        } finally {
            return t;
        }
        
        
    }
    
    /**
     * Se envía una tirada del cliente al servidor.
     * @param tirada Tirada del cliente.
     */
    public void enviar_tirada(TiradaCliente tirada){
        try {
            this.comUtils.write_byte((byte)tirada.get_tipo());
            this.comUtils.write_char(tirada.get_ficha().get_numero_l());
            this.comUtils.write_char(tirada.get_ficha().get_numero_r());
            this.comUtils.write_char(tirada.get_posicion());
        } catch (IOException ex) {
            System.out.println("Error en enviar_tirada:\n\t");
            System.out.println(ex.getMessage());
        }
    }
    
    /**
     * Se envía una no tirada del cliente al servidor.
     */
    public void enviar_no_tirada(){
        try {
//            Scanner in = new Scanner(System.in);
//            in.hasNextInt();
            this.comUtils.write_byte(Rules.TIRADA_C_S);
            this.comUtils.write_char('N');
            this.comUtils.write_char('T');
            this.comUtils.write_char(' ');
        } catch (IOException ex) {
            System.out.println("Error en enviar_no_tirada:\n\t");
            System.out.println(ex.getMessage());
        }
    }
    
    /**
     * Se recibe una ficha robada del servidor. 
     * @return Ficha robada.
     */
    public Ficha recibir_ficha_robada(){
        Ficha f = null;
        try{
            char num0 = this.comUtils.read_char();
            char num1 = this.comUtils.read_char();
            f = new Ficha(num0, num1);
            // Comprobar ficha.

        } catch (IOException ex) {
            System.out.println("Error en recibir_ficha_robada:\n\t");
            System.out.println(ex.getMessage());
        }finally {
            return f;
        }
        
        
    }
       
    /**
     * Se reciben las fichas iniciales de la mano del cliente y una ficha
     * que puede ser una ficha normal o una Rules.NO_Ficha dependiendo de 
     * si empieza el servidor o el cliente, respectivamente, a jugar.
     * @return ArrayList<Ficha>, mano del cliente.
     */
    public ArrayList<Ficha> recibir_init(){
        ArrayList<Ficha> fichas = new ArrayList();
        try{
            char num0, num1;
            for (int i = 0; i < Rules.FICHAS_INICIALES; i++) {
                num0 = this.comUtils.read_char();
                num1 = this.comUtils.read_char();
                fichas.add(new Ficha(num0,num1));
            }
            // Octava ficha indica quién tira.
            num0 = this.comUtils.read_char();
            num1 = this.comUtils.read_char(); 
            fichas.add(new Ficha(num0, num1));
            
            // Números de la ficha.
            
        } catch (IOException ex) {
            System.out.println("Error en recibir_init:\n\t");
            System.out.println(ex.getMessage());
        } finally { return fichas;
        }
    }
    
    /**
     * Se recibe el final de la partida.
     * @return FinDePartida, con el ganador.
     */
    public FinPartida recibir_fin_partida(){
        try {
            byte tipo = this.comUtils.read_byte();
            byte winner = this.comUtils.read_byte();
            if(winner==Rules.GANAN_TODOS){
                int puntos = this.comUtils.read_int32();
                return new FinPartida(tipo, winner,puntos);
            }
            return new FinPartida(tipo, winner);
            
        } catch (IOException ex) {
            System.out.println("Error en recibir_fin_partida:\n\t");
            System.out.println(ex.getMessage());
            this.cerrar_conexion();
            return null;
        }
        //return -1;
    }
    
    /**
     * Se envía un error determinando el tipo y el mensaje.
     * @param tipo Tipo de error.
     * @param mensaje Mensaje de error.
     * @see Rules.
     */
    public void enviar_error(byte tipo, String mensaje){
        int len = mensaje.length();
        if(len>139){
            mensaje = mensaje.substring(0, Rules.STRMAXSIZE-1);
        }
        switch (tipo){
            case Rules.SYNTAX_ERROR:
                break;
            case Rules.ILLEGAL_ACTION:
                break;
            case Rules.NOT_ENOUGHT_RESOURCES:
                break;
            case Rules.INTERNAL_ERROR:
                break;
            case Rules.UNKNOWN_ERROR:
                break;
            default:
                break;                
        }
    }
    
    /**
     * Se recibe un error del servidor.
     * @return Mensaje de error.
     */
    public String recibir_error() {
        try {
            // Cabecera del error.
            byte id_error = this.comUtils.read_byte();

            // Chars que corresponden a la longitud del mensaje
            // de error (unidades, decenas y centenas).
            char centenas = this.comUtils.read_char();
            char decenas = this.comUtils.read_char();
            char unidades = this.comUtils.read_char();
            
            // Conversión de la cadena de chars a número entero.
            int msg_size = Character.getNumericValue(centenas) * 100 
                    + Character.getNumericValue(decenas) * 10 
                    + Character.getNumericValue(unidades);
            
            // Recuperación del mensaje de tamaño variable definido
            // por los parámetros anteriores.
            String msg = this.comUtils.read_string_variable(msg_size);
            
            // Dependiendo del tipo de error, el servidor actuará de una
            // u otra manera.
            String retorno="";
            
            switch (id_error) {
                case Rules.SYNTAX_ERROR:
                    retorno= "SYNTAX_ERROR:\n\t"+msg;
                    break;

                case Rules.ILLEGAL_ACTION:
                    retorno= "ILLEGAL_ACTION:\n\t"+msg;
                    break;

                case Rules.NOT_ENOUGHT_RESOURCES:
                    retorno= "NOT_ENOUGHT_RESOURCES:\n\t"+msg;
                    break;

                case Rules.INTERNAL_ERROR:
                    retorno= "INTERNAL_ERROR:\n\t"+msg;
                    break;

                case Rules.UNKNOWN_ERROR:
                    retorno= "UNKNOWN_ERROR:\n\t"+msg;
                    break;
            }
            return retorno;

        } catch (IOException ex) {
            System.out.println("Error en recibir_errores:\n\t");
            System.out.println(ex.getMessage());
            return "";
        }
    }


}


