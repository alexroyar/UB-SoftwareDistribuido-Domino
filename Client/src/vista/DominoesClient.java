/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

/**
 *
 * @author fer
 */
public class DominoesClient {
    // Atributos.
    private String  nomMaquina; // IP.
    private int     numPort;    // Puerto.
        
    // Constructores.
    
    /**
     * Constructor de DominoesClient a partir de los argumentos de entrada.
     * @param args Argumentos de llamada.
     */
    public DominoesClient(String[] args){
        if (args.length != 2) {
          System.out.println("Uso: java Client <maquina_servidor> <puerto>");
          System.exit(1);
        }
        
        this.nomMaquina = args[0];
        this.numPort    = Integer.parseInt(args[1]);
    }
    
    
    /**
     * Segunda parte del constructor, para parámetros conflictivos.
     */
    public void init(){
        PartidaDominoCliente game = new PartidaDominoCliente(this.nomMaquina, this.numPort );
        game.init();
    }
    
    // Main.
    
    /**
     * Main.
     * @param args Argumentos de llamada.
     */
    public static void main(String[] args) {
        DominoesClient game = new DominoesClient(args);
        game.init();
    }
}
