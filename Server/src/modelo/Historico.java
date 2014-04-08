/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class Historico {
    ArrayList<String> historico;
    public Historico(){
        this.historico=new ArrayList<>();
        this.historico.add("· Iteración 1:");
    }
    
    public void add_accion(String accion){
        this.historico.add(accion);
        
    }
    @Override
    public String toString(){
        String retorno = "";
        for (String string :this.historico) {
            retorno+=string+"\n";
        }
        return retorno;
    }
}
