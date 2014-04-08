/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

import controlador.ComUtils;
import controlador.RechazarConexion;
import controlador.Rules;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author fer
 */
public class DominoesServer {
    
    // Atributos.
    
    private ServerSocket serverSocket;          // ServerSocket.
    private Socket socket;                      // DataSocket.
    private ComUtils comUtils;                  // Clase encargada de lectura/escritura.
    private int puerto;                         // Puerto en el que se conectará el DataSocket.
    private int id_threads;                     // Identificador de threads hijos.
    public static int connected_clients;        // Clientes conectados.

    // Constructores.
    
    /**
     * Constructor de DominoesServer a partir de los argumentos de entrada.
     * @param args Argumentos de llamada.
     */
    public DominoesServer(int puerto){
        connected_clients = 0;
        this.puerto = puerto;
        this.id_threads = 1; 
    }
    
    // Métodos.
    
    /**
     * El servidor espera a recibir solicitudes de partida
     * de un cliente para comenzar partidas. También crea hilos de partidas.
     * Bucle 'infinito'.
     */
    private void waiting(){
        try {
          this.serverSocket = new ServerSocket(this.puerto);
          System.out.println("ServerSocket preparado en el puerto " + this.puerto);
    
          while (true) {
            System.out.println("Servidor: Esperando conexión de un cliente...");
            
            this.socket = serverSocket.accept();
            // Mirar si podemos o no empezar una partida (demasiados clientes conectados).
            if (connected_clients < Rules.MAX_CLIENTS){

                connected_clients++;
                System.out.println("Servidor: Conexión aceptada. Cliente ID: " + this.id_threads + ".");
                System.out.println("Clientes conectados: " + connected_clients);
                
                // Comienza la partida. Cada partida es un thread diferente.
                PartidaDominoServidor game = new PartidaDominoServidor(this.id_threads, this.socket);
                new Thread(game).start();
                System.out.println("Se ha creado la partida con ID " + id_threads + ".");
                this.id_threads++;           
            } else {
                new RechazarConexion(this.socket);
            }
            
          }
        } catch (IOException ex) {
          System.out.println("CEls errors han de ser tractats correctament pel vostre programa");
            System.out.println(ex.toString());
          System.exit(1);
        } // fi del catch
        
    }
    
        
    /**
     * Main.
     * @param args Argumentos de llamada.
     */
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Uso: java Server <port>");
            System.exit(1);
        }
        
        DominoesServer partida = new DominoesServer(Integer.parseInt(args[0]));
        partida.waiting();
    }

}
