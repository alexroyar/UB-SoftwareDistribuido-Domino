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
        //this.historico.add("· Iteración 1:");
    }
    
    public void añadir_accion(String accion){
        this.historico.add(accion);
        
    }
    @Override
    public String toString(){
        String retorno = "";
        int count=0;
        for (int i = 0; i < this.historico.size(); i++) {
            if(count==3){
                retorno+="\n\t\t"+this.historico.get(i);
                //if(this.historico.get(i).length()<22){retorno+="\t";}
                count=0;
            }
            else{
                retorno+="\t\t"+this.historico.get(i);
                //if(this.historico.get(i).length()<22){retorno+="\t";}
            }
            count++;
        }
        
        
//        for (String string :this.historico) {
//            retorno+=string+"\n";
//        }
        return retorno+"\n";
    }
}


// String print = "";
//        int count=0;
//        for (int i = 0; i < this.tablero.get_fichas().size(); i++) {
//            if(count==6){print += "\n\n\t-> "+this.tablero.get_fichas().get(i);count=0;}
//            else{
//            print += this.tablero.get_fichas().get(i);
//            }
//            count++;
//        }
//        System.out.println("\t"+print+"\n");