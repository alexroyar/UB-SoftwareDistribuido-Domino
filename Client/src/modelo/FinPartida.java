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
public class FinPartida {
    private byte tipo;
    private byte ganador;
    private int puntos;
    
    public FinPartida(byte tipo, byte ganador){
        this.tipo=tipo;
        this.ganador=ganador;
    }
    public FinPartida(byte tipo, byte ganador, int puntos){
        this.tipo=tipo;
        this.ganador=ganador;
        this.puntos=puntos;
    }
    
    public byte get_tipo(){
        return this.tipo;
    }
    public byte get_ganador(){
        return this.ganador;
    }
    public int get_puntos(){
        return this.puntos;
    }
}
