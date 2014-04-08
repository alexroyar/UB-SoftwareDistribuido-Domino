/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

/**
 *
 * @author alex
 */
public class TiradaServidor {
    // Atributos.
    private int tipo;           // Tipo de tirada.
    private int manoServerSize;           // Mano del server.
    private char posicion;      // Posición de la tirada.
    private Ficha ficha;        // Ficha de la tirada.
    
    /**
     * Una tiradaServidor consta de su tipo, ficha, posición en la que va y
     * mano del server.
     * @param tipo 
     * @param ficha
     * @param posicion
     * @see Rules
     * @see Ficha
     */
    public TiradaServidor(byte tipo, Ficha ficha, char posicion, int size) {
        this.tipo = tipo;
        if (size != -1) this.manoServerSize = size;
        this.ficha=ficha;
        this.posicion=posicion;
    }
    
    // Métodos.
    /**
     * Getter de ficha.
     * @return Ficha.
     */
    public Ficha get_ficha(){
        return this.ficha;
    }
    
    /** Getter de la posición de la ficha.
     * @return Posición.
     */
    public char get_posicion(){
        return this.posicion;
    }
    
    /** Getter del tipo de la ficha.
     * @return Posición.
     */
    public int get_tipo(){
        return this.tipo;
    }
    
    /** Getter de la mano del server.
     * @return Fichas restantes en la mano del server.
     */
    public int get_mano_server_size(){
        return this.manoServerSize;
    }
    
    /**
     * Versión propia de toString.
     * @return ToString.
     */
    @Override
    public String toString(){
        return this.ficha + "pos: " + this.posicion + "manoServer: " + this.manoServerSize;
    }
      
}
