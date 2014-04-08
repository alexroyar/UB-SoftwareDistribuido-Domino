/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import modelo.Ficha;
import modelo.Tirada;
import vista.DominoesServer;

/**
 *
 * @author alex
 */
public class ControladorServidor {

    // Atributos.
    
    private Socket socket;              // DataSocket.
    private ComUtils comUtils;          // Controla lectura/escritura.

    // Constructores.
    
    /**
     * Constructor de ControladorServidor.
     * @param socket DataSocket.
     * @param comUtils Controla lectura/escritura.
     * @see ControladorServidor.init()
     */
    public ControladorServidor(Socket socket, ComUtils comUtils) {
            this.socket = socket;
            this.comUtils = comUtils;
    }
    
    /**
     * Segunda parte del constructor, para parámetros conflictivos.
     * @throws java.io.IOException
     */
    public void init() throws IOException{
       this.socket.setSoTimeout(Rules.TIMEOUT_MENU);
    }
    
    // Métodos.
    
    /**
     * Cierra la conexión con el socket y cierra el hilo de ejecución limpiamente.
     * @throws java.io.IOException
     */
    public void cerrar_conexion() throws IOException {
        System.out.println("Final de la partida");
        this.socket.close();
        restar_contador();
    }
    
    /**
     * Método que avisa al Servidor de que tiene un cliente menos desconectado.
     */
    public synchronized void restar_contador(){
        DominoesServer.connected_clients--;
    }
    
    /**
     * Envia una ficha robada al cliente.
     * @param ficha Ficha robada.
     * @throws java.io.IOException
     */
    public void enviar_ficha_robada(Ficha ficha) throws IOException {            
        if (ficha.equals(Rules.NO_FICHA)){

        } else {

        }
        // Cabecera del mensaje.
        this.comUtils.write_byte(Rules.FICHA_ROBADA);
        // Números de la ficha.
        this.comUtils.write_char(ficha.get_numero_l());
        this.comUtils.write_char(ficha.get_numero_r());
    }
    
    /**
     * Se recibe una tirada del cliente.
     * @return Tirada del cliente. Null si no es válida. 
     * @throws java.io.InterruptedIOException 
     */
    public Tirada recibir_tirada() throws InterruptedIOException, SocketException, IOException {
        Tirada t = null;
        // Cambio del timeout. 
        this.socket.setSoTimeout(Rules.TIMEOUT_READ);

        // Números de la ficha.
        char num0 = this.comUtils.read_char() ;
        char num1 = this.comUtils.read_char() ;

        // Posición de la ficha.
        char c = comUtils.read_char();

        // Comprobamos que el formato de la ficha sea correcto. Si es NO_FICHA devolvemos
        // un null. 
        if (num0  == 'N' && num1  == 'T' && c == ' ') {
            t = null;
        } else {
            t = new Tirada(Rules.TIRADA_C_S, new Ficha(Character.getNumericValue(num0),Character.getNumericValue(num1)), c);
        }
        
        // Restauración del timeout.
        this.socket.setSoTimeout(Rules.TIMEOUT_MENU);

        return t;
    }
    

    /**
     * Se envía una tirada al cliente.
     * @param tirada Tirada a enviar.
     * @param sizeManoServer  Cantidad de fichas en la mano del server tras tirar
     *                          la última ficha.
     * @throws java.io.IOException
     */
    public void enviar_tirada(Tirada tirada, int sizeManoServer) throws IOException {
        // Cabecera del mensaje.
        this.comUtils.write_byte((byte)tirada.get_tipo());

        // Números de la ficha.
        this.comUtils.write_char(tirada.get_ficha().get_numero_l());
        this.comUtils.write_char(tirada.get_ficha().get_numero_r());

        // Posición de la ficha.
        this.comUtils.write_char(tirada.get_posicion());

        // Fichas restantes en la mano del server.
        this.comUtils.write_int32(sizeManoServer);
    }

    /**
     * Se envía una no tirada al cliente.
     * @param sizeManoServer  Cantidad de fichas en la mano del server tras tirar
     *                          la última ficha.
     * @throws java.io.IOException
     */
    public void enviar_no_tirada(int sizeManoServer) throws IOException {
        // Cabecera del mensaje.
        this.comUtils.write_byte(Rules.TIRADA_S_C);

        // Mensaje de no tidada.
        this.comUtils.write_char('N');
        this.comUtils.write_char(' ');
        this.comUtils.write_char('T');

        // Fichas restantes en la mano del server.
        this.comUtils.write_int32(sizeManoServer);
    }
    
    /**
     * Se recibe un error del cliente. Se cerrará la conexión más adelante.
     * @return Mensaje de error.
     * @throws java.io.InterruptedIOException
     * @throws java.io.IOException
     */
    public String recibir_error() throws InterruptedIOException, IOException {
        // Se cambia el tiemout. Luego no se restaurará porque se saldrá 
        // de la partida.
        this.socket.setSoTimeout(Rules.TIMEOUT_READ);
        String retorno= "";

        // Cabecera del error.
        int id_error = this.comUtils.read_byte();

        // Chars que corresponden a la longitud del mensaje
        // de error (unidades, decenas y centenas).
        char centenas = this.comUtils.read_char();
        char decenas = this.comUtils.read_char();
        char unidades = this.comUtils.read_char();

        // Conversión de la cadena de chars a número entero.
        int msg_size = Integer.getInteger("" + centenas + decenas + unidades);

        // Recuperación del mensaje de tamaño variable definido
        // por los parámetros anteriores.
        String msg = this.comUtils.read_string_variable(msg_size);

        if (msg_size >= Rules.STRMAXSIZE){
            msg = msg.substring(0, Rules.STRMAXSIZE-1);
            System.out.println("Mensaje de error incompleto, supera el máximo definido"
                    + "por el protocolo (" + Rules.STRMAXSIZE + ").");
        }
        
        // Dependiendo del tipo de error, el servidor actuará de una
        // u otra manera.
        switch (id_error) {
            case Rules.SYNTAX_ERROR:
                retorno += "SYNTAX_ERROR:\n\t";
                break;

            case Rules.ILLEGAL_ACTION:
                 retorno += "ILLEGAL_ACTION:\n\t";
                break;

            case Rules.NOT_ENOUGHT_RESOURCES:
                retorno += "NOT_ENOUGHT_RESOURCES:\n\t";
                break;

            case Rules.INTERNAL_ERROR:
                retorno += "INTERNAL_ERROR:\n\t";
                break;

            case Rules.UNKNOWN_ERROR:
                retorno += "UNKNOWN_ERROR:\n\t";
                break;

            default:
                retorno += "UNKNOWN_ERROR_UNDEFINED:\n\t";
                break;
        }
        retorno += msg;
        this.socket.setSoTimeout(Rules.TIMEOUT_MENU);
        return retorno;
    }

    /**
     * Se envía un error al servidor. Se cerrará la conexión más adelante.
     * @param tipo Cabecera del tipo de error.
     * @param mensaje Mensaje de error.
     * @throws java.io.IOException
     */
    public void enviar_error(byte tipo, String mensaje) throws IOException {
        // Cabecera de mensaje.
        this.comUtils.write_byte(Rules.ERROR);

        // Cabecera de error.
        this.comUtils.write_byte(tipo);

        // Calculamos la longitud del mensaje y la codificamos en
        // tres chars teniendo en mente que el máximo es 140.
        int size = mensaje.length();

        if (size > Rules.STRMAXSIZE) {
            // PETA.
        }

        // Si el mensaje pasa el filtro, lo codificamos como toca.
        String length_msg = String.valueOf(size);


        char centena, decena, unidad;
        if (size > 99) {
            centena = '1';
            decena = (char) length_msg.charAt(1);
            unidad = (char) length_msg.charAt(2);
        } else if (size < 99 && size > 9) {
            centena = '0';
            decena = (char) length_msg.charAt(0);
            unidad = (char) length_msg.charAt(1);
        } else {
            centena = '0';
            decena = '0';
            unidad = (char) length_msg.charAt(0);
        }

        // Longitud del mensaje.
        this.comUtils.write_char(centena);
        this.comUtils.write_char(decena);
        this.comUtils.write_char(unidad);

        // Mensaje de error.
        this.comUtils.write_string_variable(size, mensaje);
    }

    /**
     * Se envía mano inicial al cliente.
     * @param mano ArrayList<Ficha> con las Rules.FICHAS_INICIALES del cliente.
     * @param ficha Ficha o Rules.NO_FICHa que determina si comienza el servidor
     *               o el cliente, respectivamente.
     * @throws java.io.IOException
     */
    public void enviar_mano_inicial(ArrayList<Ficha> mano, Ficha ficha) throws IOException {
        // Cabecera de mensaje.
         this.comUtils.write_byte(Rules.INIT);
         // La mano inicial.
         for (Ficha f : mano) {
             this.comUtils.write_char(f.get_numero_l());
             this.comUtils.write_char(f.get_numero_r());
         }
         // Comprobamos si cliente empieza o no. En caso negativo, enviamos
         // dos chars 'N' y 'T' para indicárselo.
         if (ficha != null) {
             this.comUtils.write_char(ficha.get_numero_l());
             this.comUtils.write_char(ficha.get_numero_r());
         } else {
             this.comUtils.write_char('N');
             this.comUtils.write_char('T');
         }
    }

    /**
     * Se envía final de la partida al cliente, informándole de quién
     * gana y porqué. Caso de empate con puntuación del servidor.
     * @param winner Determina el ganador.
     * @param scoreServer Puntuación del servidor.
     * @throws java.io.IOException
     */
    public void enviar_fin_partida(byte winner, int scoreServer) throws IOException {
        // Cabecera de mensaje.
        this.comUtils.write_byte(Rules.GAME_FINISH);

        // Quién ha ganado.
        this.comUtils.write_byte(winner);
        this.comUtils.write_int32(scoreServer);
    }
    
    /**
     * Se envía final de la partida al cliente, informándole de quién
     * gana y porqué. Caso de no empate..
     * @param winner Determina el ganador.
     * @throws java.io.IOException
     */
    public void enviar_fin_partida(byte winner) throws IOException {
        // Cabecera de mensaje.
        this.comUtils.write_byte(Rules.GAME_FINISH);

        // Quién ha ganado.
        this.comUtils.write_byte(winner);
    }
    
    /**
     * Se recibe la cabecera de cualquier mensaje.
     * @return El valor de la cabecera.
     * @throws java.io.InterruptedIOException
     * @throws java.net.SocketException
     * @throws java.io.IOException
     * @see Rules.
     */
    public int recibir_header() throws InterruptedIOException, SocketException, IOException {
        int num;
        // Se establece el timeout básico para el menú.
        this.socket.setSoTimeout(Rules.TIMEOUT_MENU);
        num = this.comUtils.read_byte();
        return num;
    }
}
