/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import controlador.Rules;
import java.util.ArrayList;


/**
 *
 * @author alex
 */
public class Tablero {
    
    // Atributos.
    private ArrayList<Ficha> fichas;    // Fichas en juego.

    // Constructores.
    
    /**
     * Constructor de Tablero.
     */
    public Tablero() {
        this.fichas = new ArrayList<>();
    }
    
    // Métodos.

    /**
     * Comprueba si una ficha encaja en el tablero de alguna manera.
     * @param ficha Ficha a encajar.
     * @return La tirada a realizar si encaja o un null en caso contrario.
     */
    public Tirada encaja(Ficha ficha){
        //Si la ficha la tengo que girar pongo un 1
        //Si no pongo un 0
        if(!this.fichas.isEmpty()){
            if (get_extremo_izquierdo() == ficha.get_numero_l()){
                return new Tirada(Rules.TIRADA_S_C, ficha.flip(), Rules.LEFT);
            } else if (get_extremo_izquierdo() == ficha.get_numero_r()){
                return new Tirada(Rules.TIRADA_S_C, ficha, Rules.LEFT);
            } else if (get_extremo_derecho() == ficha.get_numero_l()){
                return new Tirada(Rules.TIRADA_S_C, ficha, Rules.RIGHT);
            } else if (get_extremo_derecho() == ficha.get_numero_r()){
                return new Tirada(Rules.TIRADA_S_C, ficha.flip(), Rules.RIGHT);
            } else{
                return null;
            }
        } else {
            return new Tirada(Rules.TIRADA_S_C,ficha,Rules.RIGHT);
        }
    }
        
    /**
     * Versión propia de toString.
     * @return ToString.
     */
    @Override
    public String toString(){
        return this.fichas.toString();
    }
   
    
    /**
     * Inserta una ficha en el tablero.
     * @param tirada Tirada a insertar.
     * @return True si se ha insertado correctamente, false en caso contrario.
     * @see Tablero.add_right(Ficha f)
     * @see Tablero.add_left(Ficha f)
     */
    public boolean insertar_tirada(Tirada tirada){
        if(tirada.get_posicion()==Rules.LEFT){
            return add_left(tirada.get_ficha());
        }else{
            return add_right(tirada.get_ficha());
        }
    }

    /**
     * Getter de las fichas del tablero.
     * @return ArrayList<Ficha> con las fichas en juego.
     */
    public ArrayList<Ficha> get_fichas() {
        return this.fichas;
    }
    
    /**
     * Añade una ficha al final del ArrayList, que sería el extremo derecho.
     * @param f Ficha a insertar.
     * @return True si se ha insertado correctamente, false en caso contrario.
     */
    public boolean add_right(Ficha f) {
        if (get_extremo_derecho() == f.get_numero_l() || this.fichas.isEmpty()){
            return this.fichas.add(f);
        } else {
            return false;
        }
    }

    /**
     * Añade una ficha al inicio del ArrayList, que sería el extremo izquierdo.
     * @param f Ficha a insertar.
     * @return True si se ha insertado correctamente, false en caso contrario.
     */
    public boolean add_left(Ficha f) {
        if (get_extremo_izquierdo() == f.get_numero_r() || this.fichas.isEmpty()){
            this.fichas.add(0,f);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Devuelve el número que hace de extremo izquierdo en el tablero. En otras
     * palabras, el número izquierdo de la ficha situada a la izquierda del tablero.
     * @return Número del extremo izquierdo. -1 si el tablero está vacío.
     */
    public int get_extremo_izquierdo(){
        if (!this.fichas.isEmpty()){
            return this.fichas.get(0).get_numero_l();
        }
        return -1;
        
    }
    
    /**
     * Devuelve el número que hace de extremo derecho en el tablero. En otras
     * palabras, el número derecho de la ficha situada a la derecha del tablero.
     * @return Número del extremo derecho. -1 si el tablero está vacío.
     */
    public int get_extremo_derecho(){
        if (!this.fichas.isEmpty()){
            return this.fichas.get(fichas.size()-1).get_numero_r();
        }
        return -1;
    }
}
