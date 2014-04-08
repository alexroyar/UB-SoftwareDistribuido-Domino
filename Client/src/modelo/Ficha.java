/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.util.Arrays;

/**
 *
 * @author fer
 */
public class Ficha {
    
    // Atributos.
    private int[] nums; // Números de la ficha.
    
    // Constructores.
    
    /**
     * Constructor de ficha, versión int.
     * @param a Primer número.
     * @param b Segundo número.
     */
    public Ficha(int a, int b) {
        this.nums = new int[]{a, b};
    }
    
    /**
     * Constructor de ficha, versión char.
     * @param a Primer número.
     * @param b Segundo número.
     */
    public Ficha(char a, char b) {
        this.nums = new int[]{Character.getNumericValue(a),Character.getNumericValue(b)};
    }
    
    /**
     * Devuelve los números de una ficha.
     * @return Int[] con los números de la ficha.
     */
    public int[] get_numeros(){
        return this.nums;
    }
    
    /**
     * Devuelve el primer número (izquierda) de la ficha.
     * @return Primer número.
     */
    public int get_numero_l(){
        return this.nums[0];
    }
    
    /**
     * Devuelve el segundo número (derecha) de la ficha.
     * @return Segundo número.
     */
    public int get_numero_r(){
        return this.nums[1];
    }
    
    /**
     * Devuelve la suma de los números de una ficha.
     * @return Sumatorio de números de una ficha.
     */
    public int get_valor(){
        return this.get_numero_l() + this.get_numero_r();
    }
    
    /**
     * Devuelve el número más alto de una ficha
     * @return Número más alto de la ficha.
     */
    public int get_valor_maximo(){
        if (this.get_numero_l() >= this.get_numero_r())  
            return this.get_numero_l();
        else
            return this.get_numero_r();
    }
    
    /**
     * Da la vuelta a la ficha (intercambiando sus números).
     * @return Ficha dada la vuelta.
     */
    public Ficha flip(){
        return new Ficha(this.get_numero_r(), this.get_numero_l());
    }

    /**
     * Versión propia de toString.
     * @return ToString.
     */
    @Override
    public String toString() {
        return "[" + this.get_numero_l() + "|" + this.get_numero_r() + "]";
    }
    
    /**
     * Hashcode teniendo en cuenta los números de la ficha.
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.hashCode(this.nums);
        return hash;
    }

    /**
     * Método equals para comparar fichas.
     * @param obj Ficha a comparar.
     * @return True si son iguales, false en caso contrario.
     * @see Ficha.hashCode()
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final Ficha other = (Ficha) obj;
        if (!Arrays.equals(this.nums, other.nums)) {
            return false;
        }
        return true;
    }
    
}
