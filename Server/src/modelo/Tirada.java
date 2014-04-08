package modelo;

/**
 *
 * @author alex
 */
public class Tirada {
    
    // Atributos.
    
    private int tipo;           // Tipo de tirada.
    private char posicion;      // Posición de la tirada.
    private Ficha ficha;        // Ficha de la tirada.
    
    // Constructores.
    
    /**
     * Una tirada consta de su tipo, ficha y posición en la que va.
     * @param tipo 
     * @param ficha
     * @param posicion
     * @see Rules
     * @see Ficha
     */
    public Tirada(byte tipo, Ficha ficha, char posicion) {
        this.tipo = tipo;
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
    
    /** Getter de la posición de la fica.
     * @return Posición.
     */
    public char get_posicion(){
        return this.posicion;
    }
    
    /** Getter del tipo de la fica.
     * @return Posición.
     */
    public int get_tipo(){
        return this.tipo;
    }
    /**
     * Versión propia de toString.
     * @return ToString.
     */
    @Override
    public String toString(){
        return ficha + "pos: "+posicion;
    }
}
